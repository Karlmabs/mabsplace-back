package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.mappers.TransactionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TransactionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.WalletRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final TransactionMapper mapper;
  private final WalletRepository walletRepository;
  private final CurrencyRepository currencyRepository;

  public TransactionService(TransactionRepository transactionRepository, TransactionMapper mapper, WalletRepository walletRepository, CurrencyRepository currencyRepository) {
    this.transactionRepository = transactionRepository;
    this.mapper = mapper;
    this.walletRepository = walletRepository;
    this.currencyRepository = currencyRepository;
  }


  public Transaction createTransaction(TransactionRequestDto transaction) throws ResourceNotFoundException {
    Transaction newTransaction = mapper.toEntity(transaction);
    newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
    newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
    newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
    return transactionRepository.save(newTransaction);
  }

  public Transaction getTransactionById(Long id) throws ResourceNotFoundException{
    return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
  }

  public List<Transaction> getAllTransactions() {
    return transactionRepository.findAll();
  }

  public Transaction updateTransaction(Long id, TransactionRequestDto updatedTransaction) throws ResourceNotFoundException{
    Transaction target = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    Transaction updated = mapper.partialUpdate(updatedTransaction, target);
    updated.setSenderWallet(walletRepository.findById(updatedTransaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", updatedTransaction.getSenderWalletId())));
    updated.setReceiverWallet(walletRepository.findById(updatedTransaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", updatedTransaction.getReceiverWalletId())));
    updated.setCurrency(currencyRepository.findById(updatedTransaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", updatedTransaction.getCurrencyId())));
    return transactionRepository.save(updated);
  }

  public void deleteTransaction(Long id) {
    transactionRepository.deleteById(id);
  }
}
