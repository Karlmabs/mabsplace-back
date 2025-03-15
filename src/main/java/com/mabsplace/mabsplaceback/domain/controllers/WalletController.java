package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.mappers.WalletMapper;
import com.mabsplace.mabsplaceback.domain.services.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

  private final WalletService walletService;
  private final WalletMapper mapper;
  private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

  public WalletController(WalletService walletService, WalletMapper mapper) {
    this.walletService = walletService;
    this.mapper = mapper;
  }

  @PostMapping
  public ResponseEntity<WalletResponseDto> createWallet(@RequestBody WalletRequestDto walletRequestDto) {
    logger.info("Creating wallet with request: {}", walletRequestDto);
    Wallet createdWallet = walletService.createWallet(walletRequestDto);
    logger.info("Created wallet: {}", mapper.toDto(createdWallet));
    return new ResponseEntity<>(mapper.toDto(createdWallet), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<WalletResponseDto> getWalletById(@PathVariable Long id) {
    logger.info("Fetching wallet with ID: {}", id);
    Wallet wallet = walletService.getWalletById(id);
    logger.info("Fetched wallet: {}", wallet);
    return ResponseEntity.ok(mapper.toDto(wallet));
  }

  @GetMapping
  public ResponseEntity<List<WalletResponseDto>> getAllWallets() {
    logger.info("Fetching all wallets");
    List<Wallet> wallets = walletService.getAllWallets();
    logger.info("Fetched {} wallets", wallets.size());
    return new ResponseEntity<>(mapper.toDtoList(wallets), HttpStatus.OK);
  }

  @PatchMapping("/{id}/credit/{amount}")
  public ResponseEntity<WalletResponseDto> creditWallet(@PathVariable Long id, @PathVariable Double amount) {
    logger.info("Crediting wallet ID: {} with amount: {}", id, amount);
    Wallet updatedWallet = walletService.credit(id, BigDecimal.valueOf(amount));
    if (updatedWallet != null) {
      logger.info("Wallet credited successfully: {}", updatedWallet);
      return new ResponseEntity<>(mapper.toDto(updatedWallet), HttpStatus.OK);
    }
    logger.warn("Wallet not found for credit operation with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PatchMapping("/{id}/debit/{amount}")
  public ResponseEntity<WalletResponseDto> debitWallet(@PathVariable Long id, @PathVariable Double amount) {
    logger.info("Debiting wallet ID: {} with amount: {}", id, amount);
    Wallet updated = walletService.debit(id, BigDecimal.valueOf(amount));
    if (updated != null) {
      logger.info("Debited wallet successfully: {}", updated);
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    logger.warn("Wallet not found for debit operation with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PutMapping("/{id}")
  public ResponseEntity<WalletResponseDto> updateWallet(@PathVariable Long id, @RequestBody WalletRequestDto updatedWallet) {
    logger.info("Updating wallet with ID: {}, Request: {}", id, updatedWallet);
    Wallet updated = walletService.updateWallet(id, updatedWallet);
    if (updated != null) {
      logger.info("Updated wallet successfully: {}", updated);
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    logger.warn("Wallet not found with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
    logger.info("Deleting wallet with ID: {}", id);
    walletService.deleteWallet(id);
    logger.info("Deleted wallet successfully with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
