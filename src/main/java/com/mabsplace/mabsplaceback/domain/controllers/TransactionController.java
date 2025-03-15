package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import com.mabsplace.mabsplaceback.domain.mappers.TransactionMapper;
import com.mabsplace.mabsplaceback.domain.services.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(TransactionService transactionService, TransactionMapper mapper) {
        this.transactionService = transactionService;
        this.mapper = mapper;
    }

    @PostMapping("/top-up")
    public ResponseEntity<Object> topUpWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        logger.info("Top-up wallet requested with data: {}", transactionRequestDto);
        Object createdTransaction = transactionService.topUpWallet(transactionRequestDto);
        logger.info("Wallet top-up successful: {}", createdTransaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PostMapping("/top-up-mobile")
    public ResponseEntity<TransactionResponseDto> topUpWalletMobile(@RequestBody TransactionRequestDto transactionRequestDto) {
        logger.info("Mobile wallet top-up requested: {}", transactionRequestDto);
        TransactionResponseDto createdTransaction = transactionService.topUpWalletMobile(transactionRequestDto);
        logger.info("Mobile wallet top-up successful: {}", createdTransaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transferToWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        logger.info("Transfer to wallet requested: {}", transactionRequestDto);
        TransactionResponseDto createdTransaction = transactionService.transferMoney(transactionRequestDto);
        logger.info("Wallet transfer successful: {}", createdTransaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.OK);
    }

    @PostMapping("/transaction-callback")
    public ResponseEntity<String> handlePaymentCallback(HttpServletRequest request, @RequestBody Map<String, Object> callbackData) {
        logger.info("Handling payment callback with data: {}", callbackData);

        logger.info("Received callback from CoolPay");

        logger.info("Updating payment status for transaction ref: {}", callbackData.get("app_transaction_ref"));
        transactionService.updateTransactionStatus((String) callbackData.get("app_transaction_ref"), (String) callbackData.get("transaction_status"));

        logger.info("Payment status updated successfully");

        return ResponseEntity.ok("OK");
    }

    @PutMapping("/{transactionRef}/status/{transactionStatus}")
    public ResponseEntity<TransactionResponseDto> changeTransactionStatus(@PathVariable("transactionRef") String transactionRef, @PathVariable("transactionStatus") String transactionStatus) {
        logger.info("Changing transaction status for ref: {} to {}", transactionRef, transactionStatus);
        Transaction createdTransaction = transactionService.updateTransactionStatus(transactionRef, transactionStatus);
        logger.info("Transaction status updated: {}", createdTransaction);
        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawFromWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        logger.info("Withdraw from wallet requested: {}", transactionRequestDto);
        Object createdTransaction = transactionService.withdrawFromWallet(transactionRequestDto);
        logger.info("Wallet withdrawal successful: {}", createdTransaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Object> createTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        logger.info("Creating transaction: {}", transactionRequestDto);
        Object createdTransaction = transactionService.createTransaction(transactionRequestDto);
        logger.info("Transaction created: {}", createdTransaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        logger.info("Fetching transaction by ID: {}", id);
        Transaction transaction = transactionService.getTransactionById(id);
        logger.info("Fetched transaction: {}", transaction);
        return ResponseEntity.ok(mapper.toDto(transaction));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        logger.info("Fetching all transactions");
        List<Transaction> transactions = transactionService.getAllTransactions();
        logger.info("Fetched {} transactions", transactions.size());
        return new ResponseEntity<>(mapper.toDtoList(transactions), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> updateTransaction(@PathVariable Long id, @RequestBody TransactionRequestDto updatedTransaction) {
        logger.info("Updating transaction with ID: {}, Request: {}", id, updatedTransaction);
        Transaction updated = transactionService.updateTransaction(id, updatedTransaction);
        if (updated != null) {
            logger.info("Transaction updated successfully: {}", updated);
            return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
        }
        logger.warn("Transaction not found with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        logger.info("Deleting transaction with ID: {}", id);
        transactionService.deleteTransaction(id);
        logger.info("Transaction deleted successfully with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByUserId(@PathVariable Long userId) {
        logger.info("Fetching transactions for user ID: {}", userId);
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        logger.info("Fetched {} transactions for user ID: {}", transactions.size(), userId);
        return new ResponseEntity<>(mapper.toDtoList(transactions), HttpStatus.OK);
    }
}
