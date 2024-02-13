package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.mappers.WalletMapper;
import com.mabsplace.mabsplaceback.domain.services.WalletService;
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

  public WalletController(WalletService walletService, WalletMapper mapper) {
    this.walletService = walletService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<WalletResponseDto> createWallet(@RequestBody WalletRequestDto walletRequestDto) {
    Wallet createdWallet = walletService.createWallet(walletRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdWallet), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<WalletResponseDto> getWalletById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(walletService.getWalletById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<WalletResponseDto>> getAllWallets() {
    List<Wallet> Wallets = walletService.getAllWallets();
    return new ResponseEntity<>(mapper.toDtoList(Wallets), HttpStatus.OK);
  }

  @PatchMapping("/{id}/credit/{amount}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<WalletResponseDto> creditWallet(@PathVariable Long id, @PathVariable Double amount) {
    Wallet updated = walletService.credit(id, BigDecimal.valueOf(amount));
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PatchMapping("/{id}/debit/{amount}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<WalletResponseDto> debitWallet(@PathVariable Long id, @PathVariable Double amount) {
    Wallet updated = walletService.debit(id, BigDecimal.valueOf(amount));
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<WalletResponseDto> updateWallet(@PathVariable Long id, @RequestBody WalletRequestDto updatedWallet) {
    Wallet updated = walletService.updateWallet(id, updatedWallet);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
    walletService.deleteWallet(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }


}
