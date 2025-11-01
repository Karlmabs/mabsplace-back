package com.mabsplace.mabsplaceback.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabsplace.mabsplaceback.domain.dtos.contributor.*;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PayoutRequest;
import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.repositories.*;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContributorPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(ContributorPaymentService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ContributorPaymentConfigRepository configRepository;

    @Autowired
    private ContributorPaymentRepository paymentRepository;

    @Autowired
    private GlobalPaymentSettingsRepository settingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CoolPayService coolPayService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    // ==================== CRUD Operations ====================

    @Transactional
    public ContributorPaymentConfig createConfig(CreateContributorConfigRequest request) {
        logger.info("Creating contributor payment config for user ID: {}", request.getUserId());

        // Check if config already exists for this user
        if (configRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Contributor payment config already exists for user ID: " + request.getUserId());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new RuntimeException("Currency not found with ID: " + request.getCurrencyId()));

        ContributorPaymentConfig config = new ContributorPaymentConfig();
        config.setUser(user);
        config.setAmountType(ContributorPaymentConfig.AmountType.valueOf(request.getAmountType()));
        config.setFixedAmount(request.getFixedAmount());
        config.setPercentageValue(request.getPercentageValue());
        config.setMinProfitThreshold(request.getMinProfitThreshold());
        config.setUseGlobalThreshold(request.getUseGlobalThreshold());
        config.setAlwaysPay(request.getAlwaysPay());
        config.setCurrency(currency);
        config.setPhoneNumber(request.getPhoneNumber());
        config.setIsActive(request.getIsActive());

        validateConfig(config);

        ContributorPaymentConfig saved = configRepository.save(config);
        logger.info("Successfully created contributor payment config with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public ContributorPaymentConfig updateConfig(Long configId, UpdateContributorConfigRequest request) {
        logger.info("Updating contributor payment config with ID: {}", configId);

        ContributorPaymentConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Contributor payment config not found with ID: " + configId));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new RuntimeException("Currency not found with ID: " + request.getCurrencyId()));

        config.setAmountType(ContributorPaymentConfig.AmountType.valueOf(request.getAmountType()));
        config.setFixedAmount(request.getFixedAmount());
        config.setPercentageValue(request.getPercentageValue());
        config.setMinProfitThreshold(request.getMinProfitThreshold());
        config.setUseGlobalThreshold(request.getUseGlobalThreshold());
        config.setAlwaysPay(request.getAlwaysPay());
        config.setCurrency(currency);
        config.setPhoneNumber(request.getPhoneNumber());
        config.setIsActive(request.getIsActive());

        validateConfig(config);

        ContributorPaymentConfig updated = configRepository.save(config);
        logger.info("Successfully updated contributor payment config with ID: {}", updated.getId());
        return updated;
    }

    public void deleteConfig(Long configId) {
        logger.info("Deleting contributor payment config with ID: {}", configId);
        if (!configRepository.existsById(configId)) {
            throw new RuntimeException("Contributor payment config not found with ID: " + configId);
        }
        configRepository.deleteById(configId);
        logger.info("Successfully deleted contributor payment config with ID: {}", configId);
    }

    public List<ContributorPaymentConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    public ContributorPaymentConfig getConfigById(Long configId) {
        return configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Contributor payment config not found with ID: " + configId));
    }

    // ==================== Global Settings ====================

    @Transactional
    public GlobalPaymentSettings updateGlobalSettings(UpdateGlobalSettingsRequest request) {
        logger.info("Updating global payment settings");

        GlobalPaymentSettings settings = settingsRepository.findFirstByOrderByIdAsc()
                .orElse(new GlobalPaymentSettings());

        settings.setMinProfitThreshold(request.getMinProfitThreshold());
        settings.setPaymentDayOfMonth(request.getPaymentDayOfMonth());
        settings.setIsEnabled(request.getIsEnabled());

        GlobalPaymentSettings saved = settingsRepository.save(settings);
        logger.info("Successfully updated global payment settings");
        return saved;
    }

    public GlobalPaymentSettings getGlobalSettings() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .orElse(new GlobalPaymentSettings());
    }

    // ==================== Payment Processing ====================

    @Transactional
    public ContributorPayment processPayment(Long configId) {
        logger.info("Processing payment for config ID: {}", configId);

        ContributorPaymentConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Contributor payment config not found with ID: " + configId));

        if (!config.getIsActive()) {
            throw new RuntimeException("Contributor payment config is not active");
        }

        String currentPeriod = getCurrentPaymentPeriod();
        BigDecimal netProfit = getCurrentMonthNetProfit();

        // Check if already paid this period
        if (paymentRepository.existsByConfigIdAndPaymentPeriod(configId, currentPeriod)) {
            throw new RuntimeException("Payment already processed for this contributor in period: " + currentPeriod);
        }

        // Check if eligible to be paid
        if (!isEligibleForPayment(config, netProfit)) {
            throw new RuntimeException("Contributor is not eligible for payment (profit threshold not met)");
        }

        // Calculate payment amount
        BigDecimal amount = calculatePaymentAmount(config, netProfit);

        // Create payment record
        ContributorPayment payment = new ContributorPayment();
        payment.setConfig(config);
        payment.setUser(config.getUser());
        payment.setAmountPaid(amount);
        payment.setPaymentPeriod(currentPeriod);
        payment.setNetProfitAtTime(netProfit);
        payment.setPaymentStatus(ContributorPayment.PaymentStatus.PROCESSING);

        ContributorPayment savedPayment = paymentRepository.save(payment);

        // Process payout via CoolPay
        try {
            String transactionRef = generateTransactionRef();
            PayoutRequest payoutRequest = buildPayoutRequest(config, amount, transactionRef, currentPeriod);

            Object coolpayResponse = coolPayService.processPayout(payoutRequest);
            String responseJson = objectMapper.writeValueAsString(coolpayResponse);

            // Update payment with success
            savedPayment.setCoolpayTransactionRef(transactionRef);
            savedPayment.setCoolpayResponse(responseJson);
            savedPayment.setPaymentStatus(ContributorPayment.PaymentStatus.COMPLETED);
            savedPayment.setProcessedAt(LocalDateTime.now());

            paymentRepository.save(savedPayment);

            // Send email notification
            sendPaymentNotificationEmail(config.getUser(), amount, currentPeriod, transactionRef);

            logger.info("Successfully processed payment for config ID: {} with amount: {}", configId, amount);
            return savedPayment;

        } catch (Exception e) {
            logger.error("Error processing payout for config ID: {}", configId, e);

            // Update payment with failure
            savedPayment.setPaymentStatus(ContributorPayment.PaymentStatus.FAILED);
            savedPayment.setFailureReason(e.getMessage());
            paymentRepository.save(savedPayment);

            throw new RuntimeException("Failed to process payout: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ContributorPayment retryFailedPayment(Long paymentId) {
        logger.info("Retrying failed payment with ID: {}", paymentId);

        ContributorPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        if (payment.getPaymentStatus() != ContributorPayment.PaymentStatus.FAILED) {
            throw new RuntimeException("Payment is not in FAILED status");
        }

        ContributorPaymentConfig config = payment.getConfig();

        // Update status to processing
        payment.setPaymentStatus(ContributorPayment.PaymentStatus.PROCESSING);
        payment.setFailureReason(null);
        paymentRepository.save(payment);

        // Retry payout via CoolPay
        try {
            String transactionRef = generateTransactionRef();
            PayoutRequest payoutRequest = buildPayoutRequest(config, payment.getAmountPaid(), transactionRef, payment.getPaymentPeriod());

            Object coolpayResponse = coolPayService.processPayout(payoutRequest);
            String responseJson = objectMapper.writeValueAsString(coolpayResponse);

            // Update payment with success
            payment.setCoolpayTransactionRef(transactionRef);
            payment.setCoolpayResponse(responseJson);
            payment.setPaymentStatus(ContributorPayment.PaymentStatus.COMPLETED);
            payment.setProcessedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            // Send email notification
            sendPaymentNotificationEmail(config.getUser(), payment.getAmountPaid(), payment.getPaymentPeriod(), transactionRef);

            logger.info("Successfully retried payment with ID: {}", paymentId);
            return payment;

        } catch (Exception e) {
            logger.error("Error retrying payment with ID: {}", paymentId, e);

            // Update payment with failure
            payment.setPaymentStatus(ContributorPayment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);

            throw new RuntimeException("Failed to retry payment: " + e.getMessage(), e);
        }
    }

    // ==================== Scheduled Task ====================

    @Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2 AM
    @Transactional
    public void processAutomaticPayments() {
        logger.info("Starting automatic contributor payments scheduled task");

        GlobalPaymentSettings settings = getGlobalSettings();

        if (!settings.getIsEnabled()) {
            logger.info("Automatic payments are disabled. Skipping.");
            return;
        }

        int currentDayOfMonth = LocalDateTime.now().getDayOfMonth();
        if (currentDayOfMonth != settings.getPaymentDayOfMonth()) {
            logger.info("Not the scheduled payment day. Today: {}, Scheduled: {}. Skipping.", currentDayOfMonth, settings.getPaymentDayOfMonth());
            return;
        }

        String currentPeriod = getCurrentPaymentPeriod();

        // Check if we've already run this month
        if (settings.getLastPaymentRun() != null) {
            String lastRunPeriod = settings.getLastPaymentRun().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (lastRunPeriod.equals(currentPeriod)) {
                logger.info("Payments already processed this month. Last run: {}. Skipping.", lastRunPeriod);
                return;
            }
        }

        logger.info("Processing automatic payments for period: {}", currentPeriod);

        List<ContributorPaymentConfig> activeConfigs = configRepository.findByIsActiveTrue();
        BigDecimal netProfit = getCurrentMonthNetProfit();

        logger.info("Found {} active contributor configs. Current net profit: {}", activeConfigs.size(), netProfit);

        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        for (ContributorPaymentConfig config : activeConfigs) {
            try {
                // Check if already paid this period
                if (paymentRepository.existsByConfigIdAndPaymentPeriod(config.getId(), currentPeriod)) {
                    logger.info("Skipping config ID {} - already paid this period", config.getId());
                    skippedCount++;
                    continue;
                }

                // Check eligibility
                if (!isEligibleForPayment(config, netProfit)) {
                    logger.info("Skipping config ID {} - not eligible (profit threshold not met)", config.getId());
                    skippedCount++;
                    continue;
                }

                // Process payment
                processPayment(config.getId());
                successCount++;

            } catch (Exception e) {
                logger.error("Error processing automatic payment for config ID: {}", config.getId(), e);
                failedCount++;
            }
        }

        // Update last payment run
        settings.setLastPaymentRun(LocalDateTime.now());
        settingsRepository.save(settings);

        logger.info("Automatic payments completed. Success: {}, Failed: {}, Skipped: {}", successCount, failedCount, skippedCount);
    }

    // ==================== Payment History ====================

    public List<ContributorPayment> getPaymentHistory() {
        return paymentRepository.findAll();
    }

    public List<ContributorPayment> getPaymentHistoryByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<ContributorPayment> getPaymentHistoryByPeriod(String period) {
        return paymentRepository.findByPaymentPeriod(period);
    }

    public List<ContributorPayment> getFailedPayments() {
        return paymentRepository.findByPaymentStatusOrderByCreatedAtDesc(ContributorPayment.PaymentStatus.FAILED);
    }

    // ==================== Payment Preview ====================

    public PaymentPreviewResponse previewNextPayments() {
        logger.info("Generating payment preview");

        String currentPeriod = getCurrentPaymentPeriod();
        BigDecimal netProfit = getCurrentMonthNetProfit();
        List<ContributorPaymentConfig> activeConfigs = configRepository.findByIsActiveTrue();

        List<PaymentPreviewResponse.EligiblePayment> eligiblePayments = new ArrayList<>();
        BigDecimal totalPayout = BigDecimal.ZERO;

        for (ContributorPaymentConfig config : activeConfigs) {
            // Skip if already paid this period
            if (paymentRepository.existsByConfigIdAndPaymentPeriod(config.getId(), currentPeriod)) {
                continue;
            }

            // Check eligibility
            if (!isEligibleForPayment(config, netProfit)) {
                continue;
            }

            BigDecimal amount = calculatePaymentAmount(config, netProfit);
            totalPayout = totalPayout.add(amount);

            String reason = config.getAmountType() == ContributorPaymentConfig.AmountType.FIXED
                    ? "Fixed amount"
                    : config.getPercentageValue() + "% of net profit";

            PaymentPreviewResponse.EligiblePayment eligiblePayment = new PaymentPreviewResponse.EligiblePayment(
                    config.getId(),
                    config.getUser().getId(),
                    config.getUser().getUsername(),
                    config.getUser().getEmail(),
                    config.getPhoneNumber(),
                    config.getAmountType().toString(),
                    amount,
                    reason
            );

            eligiblePayments.add(eligiblePayment);
        }

        return new PaymentPreviewResponse(
                netProfit,
                currentPeriod,
                eligiblePayments,
                totalPayout,
                eligiblePayments.size()
        );
    }

    // ==================== Helper Methods ====================

    private void validateConfig(ContributorPaymentConfig config) {
        if (config.getAmountType() == ContributorPaymentConfig.AmountType.FIXED) {
            if (config.getFixedAmount() == null || config.getFixedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Fixed amount must be greater than zero");
            }
        } else if (config.getAmountType() == ContributorPaymentConfig.AmountType.PERCENTAGE) {
            if (config.getPercentageValue() == null || config.getPercentageValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Percentage value must be greater than zero");
            }
            if (config.getPercentageValue().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("Percentage value cannot exceed 100");
            }
        }

        // Validate threshold logic
        if (!config.getAlwaysPay() && !config.getUseGlobalThreshold()) {
            if (config.getMinProfitThreshold() == null) {
                throw new RuntimeException("Min profit threshold is required when not using global threshold and not set to always pay");
            }
        }
    }

    private boolean isEligibleForPayment(ContributorPaymentConfig config, BigDecimal netProfit) {
        // Always pay if configured
        if (config.getAlwaysPay()) {
            return true;
        }

        // Check threshold
        BigDecimal threshold;
        if (config.getUseGlobalThreshold()) {
            GlobalPaymentSettings settings = getGlobalSettings();
            threshold = settings.getMinProfitThreshold();
            if (threshold == null) {
                threshold = BigDecimal.ZERO;
            }
        } else {
            threshold = config.getMinProfitThreshold();
        }

        return netProfit.compareTo(threshold) >= 0;
    }

    private BigDecimal calculatePaymentAmount(ContributorPaymentConfig config, BigDecimal netProfit) {
        if (config.getAmountType() == ContributorPaymentConfig.AmountType.FIXED) {
            return config.getFixedAmount();
        } else {
            // Calculate percentage of net profit
            return netProfit.multiply(config.getPercentageValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal getCurrentMonthNetProfit() {
        Double monthlyRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM payments " +
                "WHERE MONTH(payment_date) = MONTH(CURRENT_DATE) " +
                "AND YEAR(payment_date) = YEAR(CURRENT_DATE) " +
                "AND status = 'PAID'",
                Double.class
        );

        Double monthlyExpenses = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
                "WHERE MONTH(expense_date) = MONTH(CURRENT_DATE) " +
                "AND YEAR(expense_date) = YEAR(CURRENT_DATE)",
                Double.class
        );

        double netProfit = (monthlyRevenue != null ? monthlyRevenue : 0) - (monthlyExpenses != null ? monthlyExpenses : 0);
        return BigDecimal.valueOf(netProfit);
    }

    private String getCurrentPaymentPeriod() {
        return YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private String generateTransactionRef() {
        return "CONTRIB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PayoutRequest buildPayoutRequest(ContributorPaymentConfig config, BigDecimal amount, String transactionRef, String period) {
        PayoutRequest request = new PayoutRequest();
        request.setTransaction_amount(amount.doubleValue());
        request.setTransaction_currency(config.getCurrency().getName());
        request.setTransaction_reason("Contributor Payment - " + period);
        request.setTransaction_operator("Orange Money"); // Default, can be made configurable
        request.setApp_transaction_ref(transactionRef);
        request.setCustomer_phone_number(config.getPhoneNumber());
        request.setCustomer_name(config.getUser().getFirstname() + " " + config.getUser().getLastname());
        request.setCustomer_email(config.getUser().getEmail());
        request.setCustomer_lang("en"); // Default, can be made configurable

        return request;
    }

    private void sendPaymentNotificationEmail(User user, BigDecimal amount, String period, String transactionRef) {
        try {
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(user.getEmail())
                    .subject("Contributor Payment Processed - " + period)
                    .headerText("Contributor Payment Processed")
                    .body(String.format(
                            "<p>Dear %s,</p>" +
                            "<p>Your contributor payment has been processed successfully.</p>" +
                            "<div class='highlight-box'>" +
                            "<strong>Payment Details:</strong><br>" +
                            "Period: %s<br>" +
                            "Amount: %s<br>" +
                            "Transaction Reference: %s" +
                            "</div>" +
                            "<p>Thank you for your contribution to MabsPlace!</p>",
                            user.getFirstname(),
                            period,
                            amount.toString(),
                            transactionRef
                    ))
                    .companyName("MabsPlace")
                    .build();

            emailService.sendEmail(emailRequest);
            logger.info("Sent payment notification email to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send payment notification email to: {}", user.getEmail(), e);
        }
    }

    // ==================== Mapping Methods ====================

    public ContributorPaymentConfigResponse toConfigResponse(ContributorPaymentConfig config) {
        return ContributorPaymentConfigResponse.builder()
                .id(config.getId())
                .user(ContributorPaymentConfigResponse.UserBasicInfo.builder()
                        .id(config.getUser().getId())
                        .username(config.getUser().getUsername())
                        .email(config.getUser().getEmail())
                        .firstname(config.getUser().getFirstname())
                        .lastname(config.getUser().getLastname())
                        .build())
                .isActive(config.getIsActive())
                .amountType(config.getAmountType().toString())
                .fixedAmount(config.getFixedAmount())
                .percentageValue(config.getPercentageValue())
                .minProfitThreshold(config.getMinProfitThreshold())
                .useGlobalThreshold(config.getUseGlobalThreshold())
                .alwaysPay(config.getAlwaysPay())
                .currency(ContributorPaymentConfigResponse.CurrencyInfo.builder()
                        .id(config.getCurrency().getId())
                        .name(config.getCurrency().getName())
                        .symbol(config.getCurrency().getSymbol())
                        .build())
                .phoneNumber(config.getPhoneNumber())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    public List<ContributorPaymentConfigResponse> toConfigResponseList(List<ContributorPaymentConfig> configs) {
        return configs.stream()
                .map(this::toConfigResponse)
                .collect(Collectors.toList());
    }

    public ContributorPaymentResponse toPaymentResponse(ContributorPayment payment) {
        return ContributorPaymentResponse.builder()
                .id(payment.getId())
                .config(ContributorPaymentResponse.ConfigBasicInfo.builder()
                        .id(payment.getConfig().getId())
                        .amountType(payment.getConfig().getAmountType().toString())
                        .currency(ContributorPaymentResponse.CurrencyInfo.builder()
                                .id(payment.getConfig().getCurrency().getId())
                                .name(payment.getConfig().getCurrency().getName())
                                .symbol(payment.getConfig().getCurrency().getSymbol())
                                .build())
                        .build())
                .user(ContributorPaymentResponse.UserBasicInfo.builder()
                        .id(payment.getUser().getId())
                        .username(payment.getUser().getUsername())
                        .email(payment.getUser().getEmail())
                        .firstname(payment.getUser().getFirstname())
                        .lastname(payment.getUser().getLastname())
                        .build())
                .amountPaid(payment.getAmountPaid())
                .paymentPeriod(payment.getPaymentPeriod())
                .netProfitAtTime(payment.getNetProfitAtTime())
                .paymentStatus(payment.getPaymentStatus().toString())
                .coolpayTransactionRef(payment.getCoolpayTransactionRef())
                .coolpayResponse(payment.getCoolpayResponse())
                .failureReason(payment.getFailureReason())
                .processedAt(payment.getProcessedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public List<ContributorPaymentResponse> toPaymentResponseList(List<ContributorPayment> payments) {
        return payments.stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }
}
