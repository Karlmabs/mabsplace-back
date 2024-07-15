package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PaymentRequest;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import com.mabsplace.mabsplaceback.domain.mappers.TransactionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TransactionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.WalletRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final CurrencyRepository currencyRepository;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CoolPayService coolPayService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Value("${mabsplace.app.privateKey}")
    private String privateKey;

    public TransactionService(TransactionRepository transactionRepository, TransactionMapper mapper, WalletRepository walletRepository, WalletService walletService, CurrencyRepository currencyRepository, CoolPayService coolPayService) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.currencyRepository = currencyRepository;
        this.coolPayService = coolPayService;
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

    public Object topUpWallet(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.TOPUP);
        newTransaction.setTransactionDate(new Date());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setTransactionRef(UUID.randomUUID().toString());

        Transaction save = transactionRepository.save(newTransaction);

        User user = save.getReceiverWallet().getUser();

        PaymentRequest build = PaymentRequest.builder()
                .transaction_amount(save.getAmount().doubleValue())
                .transaction_currency("XAF")
                .transaction_reason(transaction.getReason().isEmpty() ? "Payment for order" : transaction.getReason())
                .app_transaction_ref(save.getTransactionRef())
                .customer_name(user.getFirstname() + " " + user.getLastname())
                .customer_email(user.getEmail())
                .customer_phone_number(transaction.getSenderPhoneNumber())
                .build();

        return coolPayService.generatePaymentLink(build);

//        executorService.submit(() -> coolPayService.makePayment(build));

//        return save;
    }

    public String calculateMD5Signature(Map<String, Object> data) {
        logger.info("Calculating MD5 signature for data {}", data);
        try {
            // Construct the string as per the given concatenation rule
            String dataString = String.valueOf(data.get("transaction_ref"))
                    + data.get("transaction_type")
                    + BigDecimal.valueOf(Double.parseDouble(data.get("transaction_amount").toString())).toPlainString()
                    + data.get("transaction_currency")
                    + data.get("transaction_operator")
                    + privateKey;

            logger.info("Data string for signature calculation: {}", dataString);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(dataString.getBytes());

            logger.info("MD5 hash for data string: {}", hashInBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }

            logger.info("MD5 signature for data: {}", sb);

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 cryptographic algorithm is not available.", e);
            throw new RuntimeException("MD5 cryptographic algorithm is not available.", e);
        }
    }

    public Transaction updateTransactionStatus(String transactionRef, String status) {
        logger.info("Updating payment status for transaction ref {}", transactionRef);

        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef).orElseThrow(() -> new RuntimeException("Payment not found."));

        logger.info("Transaction status is {}", status);

        if (status.equals("SUCCESS") && transaction.getTransactionType().equals(TransactionType.TOPUP)) {
            walletService.credit(transaction.getReceiverWallet().getId(), transaction.getAmount());
            transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        } else
            transaction.setTransactionStatus(TransactionStatus.CANCELLED);

        logger.info("Transaction status updated to {}", transaction.getTransactionStatus());

        return transactionRepository.save(transaction);
    }

    // Runs every hour
    @Scheduled(fixedRate = 3600000)
    public void checkAndCancelPendingTransactions() {
        // Calculate the time one hour ago
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600 * 1000);

        // Fetch transactions that are PENDING and were created more than one hour ago
        List<Transaction> transactions = transactionRepository.findByTransactionStatusAndTransactionDateBefore(TransactionStatus.PENDING, oneHourAgo);

        // Update each transaction's status to CANCELLED
        transactions.forEach(transaction -> {
            try {
                changeTransactionStatus(transaction.getId(), TransactionStatus.CANCELLED);
            } catch (ResourceNotFoundException e) {
                // Log the error or handle it as per your application's requirements
                logger.error("Error cancelling transaction with id {}", transaction.getId(), e);
            }
        });
    }

    public Object withdrawFromWallet(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        newTransaction.setTransactionDate(new Date());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        return transactionRepository.save(newTransaction);
    }

    public Object createTransaction(TransactionRequestDto transaction) throws ResourceNotFoundException {
        if (transaction.getTransactionType() == TransactionType.TOPUP) {
            return topUpWallet(transaction);
        } else if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            return withdrawFromWallet(transaction);
        } else {
            throw new IllegalArgumentException("Unsupported transaction type: " + transaction.getTransactionType());
        }
    }

    /*public Transaction createTransaction(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        return transactionRepository.save(newTransaction);
    }*/

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
