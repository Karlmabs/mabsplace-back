package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.repositories.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

  private final WalletRepository walletRepository;

  public WalletService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public Wallet createWallet(Wallet wallet) {
    return walletRepository.save(wallet);
  }

  public Wallet getWallet(Long id) {
    return walletRepository.findById(id).orElse(null);
  }

  public Wallet updateWallet(Wallet wallet) {
    return walletRepository.save(wallet);
  }

  public void deleteWallet(Long id) {
    walletRepository.deleteById(id);
  }

  public Wallet getWalletByUserId(Long userId) {
    return walletRepository.findByUserId(userId);
  }


}
