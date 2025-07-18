package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.wallet.WalletRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.mappers.WalletMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.repositories.WalletRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class WalletService {

  private final WalletRepository walletRepository;
  private final WalletMapper mapper;
  private final UserRepository userRepository;
  private final CurrencyRepository currencyRepository;

  private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

  public WalletService(WalletRepository walletRepository, WalletMapper mapper, UserRepository userRepository, CurrencyRepository currencyRepository) {
    this.walletRepository = walletRepository;
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.currencyRepository = currencyRepository;
  }

  public Wallet createWallet(WalletRequestDto wallet) throws ResourceNotFoundException {
    logger.info("Creating wallet with data: {}", wallet);
    Wallet newWallet = mapper.toEntity(wallet);
    newWallet.setUser(userRepository.findById(wallet.getUserId())
        .orElseThrow(() -> {
            logger.error("User not found with ID: {}", wallet.getUserId());
            return new ResourceNotFoundException("User", "id", wallet.getUserId());
        }));
    newWallet.setCurrency(currencyRepository.findById(wallet.getCurrencyId())
        .orElseThrow(() -> {
            logger.error("Currency not found with ID: {}", wallet.getCurrencyId());
            return new ResourceNotFoundException("Currency", "id", wallet.getCurrencyId());
        }));
    Wallet savedWallet = walletRepository.save(newWallet);
    logger.info("Wallet created successfully with ID: {}", savedWallet.getId());
    return savedWallet;
  }

  public void deleteWallet(Long id) {
    logger.info("Deleting wallet with ID: {}", id);
    walletRepository.deleteById(id);
    logger.info("Wallet deleted successfully with ID: {}", id);
  }

  public Wallet getWalletByUserId(Long userId) {
    logger.info("Fetching wallet for user ID: {}", userId);
    Wallet wallet = walletRepository.findByUserId(userId);
    if (wallet == null) {
        logger.warn("No wallet found for user ID: {}", userId);
    } else {
        logger.info("Fetched wallet successfully for user ID: {}", userId);
    }
    return wallet;
  }

  public Wallet getWalletById(Long id) {
    logger.info("Fetching wallet by ID: {}", id);
    Wallet wallet = walletRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("Wallet not found with ID: {}", id);
            return new ResourceNotFoundException("Wallet", "id", id);
        });
    logger.info("Wallet retrieved successfully: {}", wallet);
    return wallet;
  }

  public List<Wallet> getAllWallets() {
    logger.info("Fetching all wallets");
    List<Wallet> wallets = walletRepository.findAll();
    logger.info("Retrieved {} wallets", wallets.size());
    return wallets;
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
    logger.info("Checking balance for user with id: {}", userId);
    Wallet wallet = walletRepository.findByUserId(userId);
    logger.info("User found: {}", wallet.getUser().getUsername());
    logger.info("User balance: {}", wallet.getBalance());
    logger.info("Amount to be deducted: {}", amount);
    return wallet.getBalance().compareTo(amount) >= 0;
  }

  public boolean checkBalance(BigDecimal userBalance, BigDecimal amount) {
    return userBalance.compareTo(amount) >= 0;
  }

  public Wallet debit(Long id, BigDecimal amount) {
    logger.info("Debiting wallet ID: {} with amount: {}", id, amount);
    Wallet wallet = walletRepository.findById(id).orElseThrow(() -> {
        logger.error("Wallet not found with ID: {}", id);
        return new ResourceNotFoundException("Wallet", "id", id);
    });
    wallet.setBalance(wallet.getBalance().subtract(amount));
    Wallet updatedWallet = walletRepository.save(wallet);
    logger.info("Wallet debited successfully. New balance: {}", wallet.getBalance());
    return updatedWallet;
  }

  public Wallet credit(Long id, BigDecimal amount) {
    logger.info("Crediting wallet ID: {} with amount: {}", id, amount);
    Wallet wallet = walletRepository.findById(id).orElseThrow(() -> {
        logger.error("Wallet not found with ID: {}", id);
        return new ResourceNotFoundException("Wallet", "id", id);
    });
    wallet.setBalance(wallet.getBalance().add(amount));
    Wallet updatedWallet = walletRepository.save(wallet);
    logger.info("Wallet credited successfully: {}", updatedWallet);
    return updatedWallet;
  }

  public void rewardReferrer(User referrer, BigDecimal referralReward) {
    logger.info("Rewarding referrer user ID: {} with amount: {}", referrer.getId(), referralReward);
    Wallet wallet = walletRepository.findByUserId(referrer.getId());
    if (wallet == null) {
        logger.error("Wallet not found for referrer user ID: {}", referrer.getId());
        throw new ResourceNotFoundException("Wallet", "userId", referrer.getId());
    }
    wallet.setBalance(wallet.getBalance().add(referralReward));
    walletRepository.save(wallet);
    logger.info("Reward added successfully to referrer user ID: {}", referrer.getId());
  }

}
