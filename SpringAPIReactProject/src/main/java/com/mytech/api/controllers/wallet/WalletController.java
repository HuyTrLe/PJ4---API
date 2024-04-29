package com.mytech.api.controllers.wallet;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mytech.api.auth.repositories.UserRepository;
import com.mytech.api.auth.services.MyUserDetails;
import com.mytech.api.models.wallet.Wallet;
import com.mytech.api.models.wallet.WalletDTO;
import com.mytech.api.repositories.wallet.WalletRepository;
import com.mytech.api.services.wallet.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    WalletService walletService;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("#walletDTO.userId == authentication.principal.id")
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

    @PutMapping("/update/{walletId}")
    @PreAuthorize("#walletDTO.userId == authentication.principal.id")
    public ResponseEntity<?> updateWallet(@PathVariable int walletId, @RequestBody @Valid WalletDTO walletDTO,
            BindingResult result) {
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
        modelMapper.map(walletDTO, existingWallet);
        existingWallet.setWalletId(walletId);
        Wallet updatedWallet = walletService.saveWallet(existingWallet);
        WalletDTO updatedWalletDTO = modelMapper.map(updatedWallet, WalletDTO.class);
        return ResponseEntity.ok(updatedWalletDTO);
    }

    @DeleteMapping("/delete/{walletId}")
    public ResponseEntity<String> deleteCategory(@PathVariable int walletId, Authentication authentication) {
        Wallet wallet = walletService.getWalletById(walletId);
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if (!wallet.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this transaction.");
        }
        walletService.deleteWallet(walletId);

        return ResponseEntity.ok("Wallets deleted successfully");
    }

}