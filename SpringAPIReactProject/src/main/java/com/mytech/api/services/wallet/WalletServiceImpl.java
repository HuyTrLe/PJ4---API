package com.mytech.api.services.wallet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.models.category.Category;
import com.mytech.api.models.expense.Expense;
import com.mytech.api.models.income.Income;
import com.mytech.api.models.saving_goals.SavingGoal;
import com.mytech.api.models.transaction.Transaction;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;
import com.mytech.api.repositories.categories.CategoryRepository;
import com.mytech.api.repositories.expense.ExpenseRepository;
import com.mytech.api.repositories.income.IncomeRepository;
import com.mytech.api.repositories.saving_goals.Saving_goalsRepository;
import com.mytech.api.repositories.transaction.TransactionRepository;
import com.mytech.api.repositories.wallet.WalletRepository;

@Service
public class WalletServiceImpl implements WalletService {

	private final WalletRepository walletRepository;
	private final TransactionRepository transactionRepository;
	private final CategoryRepository categoryRepository;
	private final IncomeRepository incomeRepository;
	private final ExpenseRepository expenseRepository;
	private final Saving_goalsRepository saving_goalsRepository;
	private final ModelMapper modelMapper;

	public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository,
			TransactionRepository transactionRepository, CategoryRepository categoryRepository,
			IncomeRepository incomeRepository, ExpenseRepository expenseRepository,
			Saving_goalsRepository saving_goalsRepository, ModelMapper modelMapper) {
		this.walletRepository = walletRepository;
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.incomeRepository = incomeRepository;
		this.expenseRepository = expenseRepository;
		this.saving_goalsRepository = saving_goalsRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public WalletDTO createWallet(WalletDTO walletDTO) {
		Wallet wallet = modelMapper.map(walletDTO, Wallet.class);

		if (walletRepository.existsByWalletName(wallet.getWalletName())) {
			throw new IllegalArgumentException("Wallet name already exists");
		}

		if (wallet.getWalletType() == 3 && wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet Goals cannot have a negative balance");
		}

		if (wallet.getCurrency().equals("USD") && wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet USD cannot have a negative balance");
		}

		String currency = wallet.getCurrency();
		if (!isValidCurrency(currency)) {
			throw new IllegalArgumentException("Invalid currency");
		}

		if (currency.equals("USD") && walletRepository.existsByCurrency(currency)) {
			throw new IllegalArgumentException("Only one wallet allowed per currency (USD)");
		}

		BigDecimal newBalance = wallet.getBalance().add(wallet.getBalance());
		wallet.setBalance(newBalance);
		walletRepository.save(wallet);
		Transaction transaction = new Transaction();
		transaction.setWallet(wallet);
		transaction.setTransactionDate(LocalDate.now());
		transaction.setAmount(wallet.getBalance().abs());
		transaction.setUser(wallet.getUser());

		if (currency.equals("USD")) {
			Category incomeCategory = categoryRepository.findByName("Incoming Transfer")
					.stream().findFirst().orElse(null);
			if (incomeCategory != null) {
				transaction.setCategory(incomeCategory);
				transaction = transactionRepository.save(transaction);

				// Tạo thu nhập
				Income income = new Income();
				income.setAmount(wallet.getBalance().abs());
				income.setIncomeDate(LocalDate.now());
				income.setUser(wallet.getUser());
				income.setTransaction(transaction);
				income.setWallet(wallet);
				income.setCategory(incomeCategory);
				incomeRepository.save(income);
			}
		} else if (currency.equals("VND")) {

			if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
				List<Category> incomeCategories = categoryRepository.findByName("Incoming Transfer");
				if (!incomeCategories.isEmpty()) {
					Category incomeCategory = incomeCategories.get(0);
					transaction.setCategory(incomeCategory);
					transaction = transactionRepository.save(transaction);
					Income income = new Income();
					income.setAmount(wallet.getBalance().abs());
					income.setIncomeDate(LocalDate.now());
					income.setUser(wallet.getUser());
					income.setTransaction(transaction);
					income.setWallet(wallet);
					income.setCategory(incomeCategory);
					incomeRepository.save(income);
				}
			} else {
				List<Category> expenseCategories = categoryRepository.findByName("Outgoing Transfer");
				if (!expenseCategories.isEmpty()) {
					Category expenseCategory = expenseCategories.get(0);
					transaction.setCategory(expenseCategory);
					transaction = transactionRepository.save(transaction);
					Expense expense = new Expense();
					expense.setAmount(wallet.getBalance().abs());
					expense.setExpenseDate(LocalDate.now());
					expense.setUser(wallet.getUser());
					expense.setTransaction(transaction);
					expense.setWallet(wallet);
					expense.setCategory(expenseCategory);
					expenseRepository.save(expense);
					expenseRepository.save(expense);
				}

			}
		}

		Wallet createdWallet = walletRepository.save(wallet);
		return modelMapper.map(createdWallet, WalletDTO.class);
	}

	@Override
	public WalletDTO updateWallet(int walletId, WalletDTO walletDTO) {
		Wallet existingWallet = walletRepository.findById(walletId)
				.orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));

		if (walletDTO == null) {
			return modelMapper.map(existingWallet, WalletDTO.class);
		}

		if (existingWallet.getWalletType() == 3 && walletDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet Goals cannot have a negative balance");
		}

		if ("USD".equals(existingWallet.getCurrency()) && walletDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Wallet USD cannot have a negative balance");
		}

		if (!existingWallet.getWalletName().equals(walletDTO.getWalletName()) &&
				walletRepository.existsByWalletNameAndWalletIdNot(walletDTO.getWalletName(), walletId)) {
			throw new IllegalArgumentException("Wallet name already exists");
		}

		BigDecimal oldBalance = existingWallet.getBalance();
		existingWallet.setWalletName(walletDTO.getWalletName());
		existingWallet.setBalance(walletDTO.getBalance());
		existingWallet = walletRepository.save(existingWallet);
		BigDecimal balanceDifference = existingWallet.getBalance().subtract(oldBalance);
		if (balanceDifference.compareTo(BigDecimal.ZERO) != 0) {
			if (existingWallet.getWalletType() == 3 && !existingWallet.getSavingGoals().isEmpty()) {
				SavingGoal selectedSavingGoal = existingWallet.getSavingGoals().get(0);
				selectedSavingGoal.setCurrentAmount(selectedSavingGoal.getCurrentAmount().add(balanceDifference));
				saving_goalsRepository.save(selectedSavingGoal);
			}
			Transaction adjustmentTransaction = new Transaction();
			adjustmentTransaction.setWallet(existingWallet);
			adjustmentTransaction.setTransactionDate(LocalDate.now());
			adjustmentTransaction.setAmount(balanceDifference.abs());
			adjustmentTransaction.setUser(existingWallet.getUser());
			Category category = null;
			if (balanceDifference.compareTo(BigDecimal.ZERO) > 0) {
				category = categoryRepository.findByName("Incoming Transfer")
						.stream().findFirst().orElse(null);
			} else {
				category = categoryRepository.findByName("Outgoing Transfer")
						.stream().findFirst().orElse(null);
			}
			if (category != null) {
				adjustmentTransaction.setCategory(category);
				transactionRepository.save(adjustmentTransaction);
			}
		}

		return modelMapper.map(existingWallet, WalletDTO.class);
	}

	private boolean isValidCurrency(String currency) {
		return currency.equals("VND") || currency.equals("USD");
	}

	@Override
	public void transferUSDToVND(int sourceWalletId, int destinationWalletId, BigDecimal amount) {
		Wallet sourceWallet = walletRepository.findById(sourceWalletId)
				.orElseThrow(() -> new RuntimeException("Source wallet not found with id: " + sourceWalletId));

		Wallet destinationWallet = walletRepository.findById(destinationWalletId)
				.orElseThrow(
						() -> new RuntimeException("Destination wallet not found with id: " + destinationWalletId));

		// Kiểm tra nếu ví nguồn là USD và ví đích là VND
		if (!sourceWallet.getCurrency().equals("USD") || !destinationWallet.getCurrency().equals("VND")) {
			throw new IllegalArgumentException("Transfer is only allowed from USD wallet to VND wallet");
		}

		// Tải tỷ giá hối đoái từ API
		BigDecimal exchangeRate = getUSDToVNDExchangeRate();

		// Tính toán số tiền tương ứng trong VND
		BigDecimal amountInVND = amount.multiply(exchangeRate);

		// Kiểm tra số dư của ví nguồn đủ để thực hiện giao dịch không
		if (sourceWallet.getBalance().compareTo(amount) < 0) {
			throw new IllegalArgumentException("Insufficient funds in source wallet");
		}

		// Tạo giao dịch chuyển tiền đi từ ví nguồn
		Transaction outgoingTransaction = new Transaction();
		outgoingTransaction.setTransactionDate(LocalDate.now());
		outgoingTransaction.setAmount(amount);
		outgoingTransaction.setWallet(sourceWallet);
		outgoingTransaction.setCategory(categoryRepository.findByName("Outgoing Transfer")
				.stream().findFirst().orElse(null));
		outgoingTransaction.setUser(sourceWallet.getUser());
		outgoingTransaction.setNotes("Transfer Money");
		transactionRepository.save(outgoingTransaction);

		// Tạo giao dịch chuyển tiền đến ví đích
		Transaction incomingTransaction = new Transaction();
		incomingTransaction.setTransactionDate(LocalDate.now());
		incomingTransaction.setAmount(amountInVND);
		incomingTransaction.setWallet(destinationWallet);
		incomingTransaction.setCategory(categoryRepository.findByName("Incoming Transfer")
				.stream().findFirst().orElse(null));
		incomingTransaction.setUser(destinationWallet.getUser());
		incomingTransaction.setNotes("Transfer Money");
		transactionRepository.save(incomingTransaction);

		// Cập nhật số dư cho cả hai ví
		BigDecimal newSourceBalance = sourceWallet.getBalance().subtract(amount);
		sourceWallet.setBalance(newSourceBalance);
		walletRepository.save(sourceWallet);

		BigDecimal newDestinationBalance = destinationWallet.getBalance().add(amountInVND);
		destinationWallet.setBalance(newDestinationBalance);
		walletRepository.save(destinationWallet);
	}

	private BigDecimal getUSDToVNDExchangeRate() {
		try {
			// Tạo URL cho API
			URL url = new URL("https://portal.vietcombank.com.vn/Usercontrols/TVPortal.TyGia/pXML.aspx?b=10");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// Đọc dữ liệu trả về từ API
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// Tìm vị trí của dấu "<?xml" đầu tiên trong dữ liệu
			int startIndex = response.indexOf("<?xml");

			// Loại bỏ bất kỳ nội dung trước dấu "<?xml"
			String xmlData = response.substring(startIndex);

			// Phân tích dữ liệu XML để lấy tỷ giá
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlData));
			Document document = builder.parse(is);

			// Xử lý dữ liệu XML và trả về tỷ giá
			NodeList exrateList = document.getElementsByTagName("Exrate");
			for (int i = 0; i < exrateList.getLength(); i++) {
				Node exrateNode = exrateList.item(i);
				if (exrateNode.getNodeType() == Node.ELEMENT_NODE) {
					Element exrateElement = (Element) exrateNode;
					if (exrateElement.getAttribute("CurrencyCode").equals("USD")) {
						String transferRate = exrateElement.getAttribute("Transfer").replace(",", "");
						return new BigDecimal(transferRate);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Failed to get USD to VND exchange rate");
	}

	@Override
	public Wallet getWalletById(int walletId) {
		return walletRepository.findById(walletId).orElse(null);
	}

	@Override
	public List<Wallet> getWalletsByUserId(int userId) {
		return walletRepository.findByUserId(userId);
	}

	@Override
	public void deleteWallet(int walletId) {
		walletRepository.deleteById(walletId);
	}

}
