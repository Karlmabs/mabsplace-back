package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.mappers.WalletMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.repositories.WalletRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

  private final WalletRepository walletRepository;
  private final WalletMapper mapper;
  private final UserRepository userRepository;
  private final CurrencyRepository currencyRepository;

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

  public WalletService(WalletRepository walletRepository, WalletMapper mapper, UserRepository userRepository, CurrencyRepository currencyRepository) {
    this.walletRepository = walletRepository;
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.currencyRepository = currencyRepository;
  }

  public Wallet createWallet(WalletRequestDto wallet) throws ResourceNotFoundException{
    Wallet newWallet = mapper.toEntity(wallet);
    newWallet.setUser(userRepository.findById(wallet.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", wallet.getUserId())));
    newWallet.setCurrency(currencyRepository.findById(wallet.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", wallet.getCurrencyId())));
    return walletRepository.save(newWallet);
  }

  public void deleteWallet(Long id) {
    walletRepository.deleteById(id);
  }

  public Wallet getWalletByUserId(Long userId) {
    return walletRepository.findByUserId(userId);
  }


  public Wallet getWalletById(Long id) {
    return walletRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", id));
  }

  public List<Wallet> getAllWallets() {
    return walletRepository.findAll();
  }

  public Wallet updateWallet(Long id, WalletRequestDto updatedWallet) throws ResourceNotFoundException{
    Wallet target = walletRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", id));
    Wallet updated = mapper.partialUpdate(updatedWallet, target);
    updated.setUser(userRepository.findById(updatedWallet.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", updatedWallet.getUserId())));
    updated.setCurrency(currencyRepository.findById(updatedWallet.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", updatedWallet.getCurrencyId())));
    return walletRepository.save(updated);
  }

  // check if a user has enough balance in his wallet
  public boolean checkBalance(Long userId, BigDecimal amount) {
    logger.info("Checking balance for user with id: " + userId);
    Wallet wallet = walletRepository.findByUserId(userId);
    logger.info("User found: " + wallet.getUser().getUsername());
    logger.info("User balance: " + wallet.getBalance());
    logger.info("Amount to be deducted: " + amount);
    return wallet.getBalance().compareTo(amount) >= 0;
  }

  public Wallet debit(Long id, BigDecimal amount) {
    Wallet wallet = walletRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", id));
    wallet.setBalance(wallet.getBalance().subtract(amount));
    return walletRepository.save(wallet);
  }

  public Wallet credit(Long id, BigDecimal amount) {
    Wallet wallet = walletRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", id));
    wallet.setBalance(wallet.getBalance().add(amount));
    return walletRepository.save(wallet);
  }
}
