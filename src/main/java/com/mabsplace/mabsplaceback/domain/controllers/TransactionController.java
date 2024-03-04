package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import com.mabsplace.mabsplaceback.domain.mappers.TransactionMapper;
import com.mabsplace.mabsplaceback.domain.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper mapper;

    public TransactionController(TransactionService transactionService, TransactionMapper mapper) {
        this.transactionService = transactionService;
        this.mapper = mapper;
    }

    @PostMapping("/top-up")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> topUpWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        Transaction createdTransaction = transactionService.topUpWallet(transactionRequestDto);
        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    // implement api to change the status of a transaction
    @PutMapping("/{id}/status/{transactionStatus}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> changeTransactionStatus(@PathVariable ("id") Long id, @PathVariable ("transactionStatus") String transactionStatus) {
        Transaction createdTransaction = transactionService.changeTransactionStatus(id, TransactionStatus.valueOf(transactionStatus));
        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> withdrawFromWallet(@RequestBody TransactionRequestDto transactionRequestDto) {
        Transaction createdTransaction = transactionService.withdrawFromWallet(transactionRequestDto);
        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        Transaction createdTransaction = transactionService.createTransaction(transactionRequestDto);
        return new ResponseEntity<>(mapper.toDto(createdTransaction), HttpStatus.CREATED);
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
}
