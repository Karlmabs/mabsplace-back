package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PaymentRequest;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.transaction.TransactionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import com.mabsplace.mabsplaceback.domain.mappers.TransactionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TransactionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
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
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final CurrencyRepository currencyRepository;
    private final UserRepository userRepository;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CoolPayService coolPayService;

    @Value("${mabsplace.app.privateKey}")
    private String privateKey;

    public TransactionService(TransactionRepository transactionRepository, TransactionMapper mapper, WalletRepository walletRepository, WalletService walletService, CurrencyRepository currencyRepository, UserRepository userRepository, CoolPayService coolPayService) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.currencyRepository = currencyRepository;
        this.userRepository = userRepository;
        this.coolPayService = coolPayService;
    }

    // implement method to change a transaction status
    public Transaction changeTransactionStatus(Long id, TransactionStatus transactionStatus) throws ResourceNotFoundException {
        logger.info("Attempting to change transaction status. Transaction ID: {}, New Status: {}", id, transactionStatus);

        Transaction target = transactionRepository.findById(id).orElseThrow(() -> {
            logger.error("Transaction not found with ID: {}", id);
            return new ResourceNotFoundException("Transaction", "id", id);
        });
        target.setTransactionStatus(transactionStatus);

        // if the transaction status is completed and the transaction type is topup, credit the receiver wallet and if the transaction status is completed and the transaction type is withdrawal, debit the sender wallet
        if (target.getTransactionStatus().equals(TransactionStatus.COMPLETED)) {
            if (target.getTransactionType().equals(TransactionType.TOPUP)) {
                walletService.credit(target.getReceiverWallet().getId(), target.getAmount());
            } else if (target.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
                walletService.debit(target.getSenderWallet().getId(), target.getAmount());
            } else if (target.getTransactionType().equals(TransactionType.TRANSFER)) {
                walletService.debit(target.getSenderWallet().getId(), target.getAmount());
                walletService.credit(target.getReceiverWallet().getId(), target.getAmount());
            }
            logger.info("Transaction actions completed for Transaction ID: {}", id);
        }
        Transaction updatedTransaction = transactionRepository.save(target);
        logger.info("Transaction status updated successfully. Transaction ID: {}", id);
        return updatedTransaction;
    }

    public Object topUpWallet(TransactionRequestDto transaction) throws ResourceNotFoundException {
        logger.info("Top-up wallet requested with data: {}", transaction);

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
        logger.info("Top-up wallet saved successfully. Transaction ID: {}", save.getId());

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
    }

    public TransactionResponseDto topUpWalletMobile(TransactionRequestDto transaction) throws ResourceNotFoundException {
        logger.info("Top-up wallet requested with data: {}", transaction);

        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.TOPUP);
        newTransaction.setTransactionDate(new Date());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setTransactionRef(UUID.randomUUID().toString());

        Transaction save = transactionRepository.save(newTransaction);
        logger.info("Top-up wallet saved successfully. Transaction ID: {}", save.getId());

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

        executorService.submit(() -> coolPayService.makePayment(build));

        return mapper.toDto(save);
    }

    // implement to transfer money from one wallet to another
    public TransactionResponseDto transferMoney(TransactionRequestDto transaction) throws ResourceNotFoundException {
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction .getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.TRANSFER);
        newTransaction.setTransactionDate(new Date());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        Transaction save = transactionRepository.save(newTransaction);

        changeTransactionStatus(save.getId(), TransactionStatus.COMPLETED);

        return mapper.toDto(save);
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

        if (transaction.getTransactionStatus().equals(TransactionStatus.PENDING)) {
            if (status.equals("SUCCESS") && transaction.getTransactionType().equals(TransactionType.TOPUP)) {
                walletService.credit(transaction.getReceiverWallet().getId(), transaction.getAmount());
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
            } else {
                transaction.setTransactionStatus(TransactionStatus.CANCELLED);
            }
            logger.info("Transaction status updated to {}", transaction.getTransactionStatus());
            return transactionRepository.save(transaction);
        } else {
            logger.info("Transaction status is not PENDING, no update performed.");
            return transaction;
        }
    }

    // Runs every hour
    @Scheduled(fixedRate = 3600000)
    public void checkAndCancelPendingTransactions() {
        logger.info("Scheduled task initiated: Checking and cancelling pending transactions older than one hour.");
        // Calculate the time one hour ago
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600 * 1000);

        // Fetch transactions that are PENDING and were created more than one hour ago
        List<Transaction> transactions = transactionRepository.findByTransactionStatusAndTransactionDateBefore(TransactionStatus.PENDING, oneHourAgo);

        logger.info("Found {} pending transactions to cancel.", transactions.size());

        // Update each transaction's status to CANCELLED
        transactions.forEach(transaction -> {
            try {
                changeTransactionStatus(transaction.getId(), TransactionStatus.CANCELLED);
                logger.info("Transaction ID {} cancelled successfully.", transaction.getId());
            } catch (ResourceNotFoundException e) {
                logger.error("Error cancelling transaction ID {}: {}", transaction.getId(), e.getMessage());
            }
        });
        logger.info("Scheduled task completed: Pending transaction cancellations processed.");
    }

    public Object withdrawFromWallet(TransactionRequestDto transaction) throws ResourceNotFoundException {
        logger.info("Processing withdrawal request: {}", transaction);

        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(walletRepository.findById(transaction.getSenderWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getSenderWalletId())));
        newTransaction.setReceiverWallet(walletRepository.findById(transaction.getReceiverWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", transaction.getReceiverWalletId())));
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        newTransaction.setTransactionDate(new Date());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);

        Transaction savedTransaction = transactionRepository.save(newTransaction);
        logger.info("Withdrawal transaction created successfully. Transaction ID: {}", savedTransaction.getId());

        return savedTransaction;
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

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByReceiverWalletUserId(userId);
    }

    // Implement method to transfer money from one user to another
    public TransactionResponseDto transferMoneyToUser(TransactionRequestDto transaction, Long senderId, Long receiverId) throws ResourceNotFoundException {
        logger.info("Processing user-to-user transfer: Amount: {}, Sender ID: {}, Receiver ID: {}", 
                transaction.getAmount(), senderId, receiverId);
        
        // Get sender and receiver users
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", receiverId));
        
        // Get their wallets
        Wallet senderWallet = sender.getWallet();
        Wallet receiverWallet = receiver.getWallet();
        
        // Set wallet IDs in transaction
        transaction.setSenderWalletId(senderWallet.getId());
        transaction.setReceiverWalletId(receiverWallet.getId());
        
        // Check if sender has sufficient balance
        if (!walletService.checkBalance(senderWallet.getBalance(), transaction.getAmount())) {
            logger.error("Insufficient funds in sender wallet ID: {}", senderWallet.getId());
            throw new RuntimeException("Insufficient funds");
        }
        
        // Create and process the transaction
        Transaction newTransaction = mapper.toEntity(transaction);
        newTransaction.setSenderWallet(senderWallet);
        newTransaction.setReceiverWallet(receiverWallet);
        newTransaction.setCurrency(currencyRepository.findById(transaction.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", transaction.getCurrencyId())));
        newTransaction.setTransactionType(TransactionType.TRANSFER);
        newTransaction.setTransactionDate(new Date());
        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setTransactionRef(UUID.randomUUID().toString());
        newTransaction.setSenderName(sender.getFirstname() + " " + sender.getLastname());
        newTransaction.setSenderPhoneNumber(sender.getPhonenumber());
        
        Transaction savedTransaction = transactionRepository.save(newTransaction);
        logger.info("Transfer transaction created: {}", savedTransaction.getId());
        
        // Process the transfer immediately
        changeTransactionStatus(savedTransaction.getId(), TransactionStatus.COMPLETED);
        logger.info("Transfer completed successfully");
        
        return mapper.toDto(savedTransaction);
    }
}
