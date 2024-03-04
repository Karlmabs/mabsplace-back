package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import com.mabsplace.mabsplaceback.domain.mappers.TransactionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TransactionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.WalletRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final CurrencyRepository currencyRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionMapper mapper, WalletRepository walletRepository, WalletService walletService, CurrencyRepository currencyRepository) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.currencyRepository = currencyRepository;
    }

    // implement method to change a transaction status
    public Transaction changeTransactionStatus(Long id, TransactionStatus transactionStatus) throws ResourceNotFoundException {
        Transaction target = transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        target.setTransactionStatus(transactionStatus);
        // if the transaction status is completed and the transaction type is topup, credit the receiver wallet and if the transaction status is completed and the transaction type is withdrawal, debit the sender wallet
        if (target.getTransactionStatus().equals(TransactionStatus.COMPLETED)) {
            if (target.getTransactionType().equals(TransactionType.TOPUP)) {
                walletService.credit(target.getReceiverWallet().getId(), target.getAmount());
            } else if (target.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
                walletService.debit(target.getSenderWallet().getId(), target.getAmount());
            }
        }
        return transactionRepository.save(target);
    }

    public Transaction topUpWallet(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.TOPUP);
        newTransaction.setTransactionDate(new Date());
        return transactionRepository.save(newTransaction);
    }

    public Transaction withdrawFromWallet(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        newTransaction.setTransactionDate(new Date());
        return transactionRepository.save(newTransaction);
    }


    public Transaction createTransaction(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        return transactionRepository.save(newTransaction);
    }

    public Transaction getTransactionById(Long id) throws ResourceNotFoundException {
        return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction updateTransaction(Long id, TransactionRequestDto updatedTransaction) throws ResourceNotFoundException {
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
