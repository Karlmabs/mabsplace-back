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
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> topUpWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        Object createdTransaction = transactionService.topUpWallet(transactionRequestDto);
//        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PostMapping("/top-up-mobile")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> topUpWalletMobile(@RequestBody TransactionRequestDto transactionRequestDto) {
        TransactionResponseDto createdTransaction = transactionService.topUpWalletMobile(transactionRequestDto);
//        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PostMapping("/transaction-callback")
    public ResponseEntity<String> handlePaymentCallback(HttpServletRequest request, @RequestBody Map<String, Object> callbackData) {
        logger.info("Handling payment callback with data: {}", callbackData);

        /*if (!request.getRemoteAddr().equals(MY_COOLPAY_IP)) {
            logger.error("Received callback from unknown IP: {}", request.getRemoteAddr());
            return ResponseEntity.badRequest().body("KO");
        }*/

        logger.info("Received callback from CoolPay");
        /*String expectedSignature = callbackData.get("signature").toString();
        String calculatedSignature = paymentService.calculateMD5Signature(callbackData);

        if (!expectedSignature.equals(calculatedSignature)) {
            logger.error("Received callback with invalid signature");
            return ResponseEntity.badRequest().body("KO");
        }*/

        logger.info("Updating payment status for transaction ref: {}", callbackData.get("app_transaction_ref"));
        transactionService.updateTransactionStatus((String) callbackData.get("app_transaction_ref"), (String) callbackData.get("transaction_status"));

        logger.info("Payment status updated successfully");

        return ResponseEntity.ok("OK");
    }

    // implement api to change the status of a transaction
    @PutMapping("/{transactionRef}/status/{transactionStatus}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> changeTransactionStatus(@PathVariable("transactionRef") String transactionRef, @PathVariable("transactionStatus") String transactionStatus) {
        Transaction createdTransaction = transactionService.updateTransactionStatus(transactionRef, transactionStatus);
        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> withdrawFromWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        Object createdTransaction = transactionService.withdrawFromWallet(transactionRequestDto);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
//        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> createTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        Object createdTransaction = transactionService.createTransaction(transactionRequestDto);
//        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(transactionService.getTransactionById(id)));
    }

    @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        List<Transaction> Transactions = transactionService.getAllTransactions();
        return new ResponseEntity<>(mapper.toDtoList(Transactions), HttpStatus.OK);
    }

    @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> updateTransaction(@PathVariable Long id, @RequestBody TransactionRequestDto updatedTransaction) {
        Transaction updated = transactionService.updateTransaction(id, updatedTransaction);
        if (updated != null) {
            return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // get all transactions involving a user
    @GetMapping("/user/{userId}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByUserId(@PathVariable Long userId) {
        List<Transaction> Transactions = transactionService.getTransactionsByUserId(userId);
        return new ResponseEntity<>(mapper.toDtoList(Transactions), HttpStatus.OK);
    }
}
