package com.mytech.api.controllers.wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.wallet.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final ModelMapper modelMapper;

    public WalletController(WalletService walletService, WalletRepository walletRepository, ModelMapper modelMapper) {
        this.walletService = walletService;
        this.modelMapper = modelMapper;
        this.walletRepository = walletRepository;
    }

    @PostMapping
    public ResponseEntity<?> createWallet(@RequestBody @Valid WalletDTO walletDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            System.out.println(errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        
        Wallet wallet = modelMapper.map(walletDTO, Wallet.class);
        Wallet savedWallet = walletService.saveWallet(wallet);
        WalletDTO savedWalletDTO = modelMapper.map(savedWallet, WalletDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWalletDTO);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<?> getWalletById(@PathVariable int walletId) {
        Wallet wallet = walletService.getWalletById(walletId);
        if (wallet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found with id: " + walletId);
        }
        
        WalletDTO walletDTO = modelMapper.map(wallet, WalletDTO.class);
        return ResponseEntity.ok(walletDTO);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getWalletsByUserId(@PathVariable int userId) {
        List<Wallet> wallets = walletService.getWalletsByUserId(userId);
        if (wallets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        
        List<WalletDTO> walletDTOs = wallets.stream()
                .map(w -> modelMapper.map(w, WalletDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(walletDTOs);
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<?> updateWallet(@PathVariable int walletId, @RequestBody @Valid WalletDTO walletDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        
        Wallet existingWallet = walletService.getWalletById(walletId);
        if (existingWallet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found with id: " + walletId);
        }
        
        // Map fields from the WalletDTO to the existing Wallet entity
        modelMapper.map(walletDTO, existingWallet);

        // The walletId is not part of the DTO, so we explicitly set it again
        existingWallet.setWalletId(walletId);

        // Save the updated wallet using the wallet service
        Wallet updatedWallet = walletService.saveWallet(existingWallet);
        WalletDTO updatedWalletDTO = modelMapper.map(updatedWallet, WalletDTO.class);
        return ResponseEntity.ok(updatedWalletDTO);
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<?> deleteWallet(@PathVariable int walletId) {
        Wallet wallet = walletService.getWalletById(walletId);
        if (wallet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found with id: " + walletId);
        }
        
        walletService.deleteWallet(walletId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @GetMapping("/total-balance/{userId}")
    public ResponseEntity<BigDecimal> getTotalBalance(@PathVariable int userId) {
        BigDecimal totalBalance = walletRepository.getTotalBalanceForUser(userId);
        return ResponseEntity.ok().body(totalBalance);
    }
}