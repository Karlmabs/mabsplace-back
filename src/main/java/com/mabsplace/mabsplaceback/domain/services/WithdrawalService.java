package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PayoutRequest;
import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.Withdrawal;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalStatus;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.repositories.WithdrawalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WithdrawalService {

  private final WithdrawalRepository withdrawalRepository;
  private final CurrencyRepository currencyRepository;
  private final UserRepository userRepository;
  private final CoolPayService coolPayService;
  private static final Logger logger = LoggerFactory.getLogger(WithdrawalService.class);

  public WithdrawalService(WithdrawalRepository withdrawalRepository,
                          CurrencyRepository currencyRepository,
                          UserRepository userRepository,
                          CoolPayService coolPayService) {
    this.withdrawalRepository = withdrawalRepository;
    this.currencyRepository = currencyRepository;
    this.userRepository = userRepository;
    this.coolPayService = coolPayService;
  }

  @Transactional
  public Withdrawal processWithdrawal(WithdrawalRequestDto requestDto) {
    logger.info("Processing withdrawal for transaction ref: {}", requestDto.getAppTransactionRef());

    // Create withdrawal entity with PENDING status
    Withdrawal withdrawal = createWithdrawalEntity(requestDto);
    withdrawal.setStatus(WithdrawalStatus.PENDING);
    withdrawal = withdrawalRepository.save(withdrawal);
    logger.info("Withdrawal saved with PENDING status, ID: {}", withdrawal.getId());

    try {
      // Create PayoutRequest for CoolPay API
      PayoutRequest payoutRequest = new PayoutRequest();
      payoutRequest.setTransaction_amount(requestDto.getTransactionAmount().doubleValue());
      payoutRequest.setTransaction_currency(requestDto.getTransactionCurrency());
      payoutRequest.setTransaction_reason(requestDto.getTransactionReason());
      payoutRequest.setTransaction_operator(requestDto.getTransactionOperator().toString());
      payoutRequest.setApp_transaction_ref(requestDto.getAppTransactionRef());
      payoutRequest.setCustomer_phone_number(requestDto.getCustomerPhoneNumber());
      payoutRequest.setCustomer_name(requestDto.getCustomerName());
      payoutRequest.setCustomer_email(requestDto.getCustomerEmail());
      payoutRequest.setCustomer_lang(requestDto.getCustomerLang());

      // Call CoolPay API
      Object apiResponse = coolPayService.processPayout(payoutRequest);
      logger.info("CoolPay API response: {}", apiResponse);

      // Parse response to extract transaction_ref
      String coolpayTransactionRef = extractTransactionRef(apiResponse);

      // Update withdrawal with success status
      withdrawal.setCoolpayTransactionRef(coolpayTransactionRef);
      withdrawal.setStatus(WithdrawalStatus.SUCCESS);
      withdrawal = withdrawalRepository.save(withdrawal);
      logger.info("Withdrawal successful, ID: {}, CoolPay Ref: {}", withdrawal.getId(), coolpayTransactionRef);

      return withdrawal;

    } catch (Exception e) {
      logger.error("Withdrawal failed for transaction ref: {}. Error: {}", requestDto.getAppTransactionRef(), e.getMessage());

      // Update withdrawal with failed status
      withdrawal.setStatus(WithdrawalStatus.FAILED);
      withdrawal.setErrorMessage(e.getMessage());
      withdrawalRepository.save(withdrawal);

      throw new RuntimeException("Withdrawal failed: " + e.getMessage(), e);
    }
  }

  private Withdrawal createWithdrawalEntity(WithdrawalRequestDto requestDto) {
    Withdrawal withdrawal = new Withdrawal();
    withdrawal.setAmount(requestDto.getTransactionAmount());
    withdrawal.setTransactionOperator(requestDto.getTransactionOperator());
    withdrawal.setCustomerName(requestDto.getCustomerName());
    withdrawal.setCustomerPhoneNumber(requestDto.getCustomerPhoneNumber());
    withdrawal.setCustomerEmail(requestDto.getCustomerEmail());
    withdrawal.setCustomerLang(requestDto.getCustomerLang());
    withdrawal.setCustomerUsername(requestDto.getCustomerUsername());
    withdrawal.setTransactionReason(requestDto.getTransactionReason());
    withdrawal.setAppTransactionRef(requestDto.getAppTransactionRef());

    // Set currency
    if (requestDto.getCurrencyId() != null) {
      Currency currency = currencyRepository.findById(requestDto.getCurrencyId())
          .orElseThrow(() -> new RuntimeException("Currency not found"));
      withdrawal.setCurrency(currency);
    }

    // Set created by user
    if (requestDto.getCreatedByUserId() != null) {
      User user = userRepository.findById(requestDto.getCreatedByUserId())
          .orElseThrow(() -> new RuntimeException("User not found"));
      withdrawal.setCreatedBy(user);
    }

    return withdrawal;
  }

  private String extractTransactionRef(Object apiResponse) {
    if (apiResponse instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> responseMap = (LinkedHashMap<String, Object>) apiResponse;
      Object transactionRef = responseMap.get("transaction_ref");
      return transactionRef != null ? transactionRef.toString() : null;
    }
    return null;
  }

  public List<Withdrawal> getAllWithdrawals() {
    logger.info("Fetching all withdrawals");
    return withdrawalRepository.findAllByOrderByCreatedAtDesc();
  }

  public Withdrawal getWithdrawalById(Long id) {
    logger.info("Fetching withdrawal by ID: {}", id);
    return withdrawalRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Withdrawal not found with ID: " + id));
  }

  public List<Withdrawal> getWithdrawalsByUserId(Long userId) {
    logger.info("Fetching withdrawals for user ID: {}", userId);
    return withdrawalRepository.findByCreatedById(userId);
  }

  public List<Withdrawal> getWithdrawalsByStatus(WithdrawalStatus status) {
    logger.info("Fetching withdrawals with status: {}", status);
    return withdrawalRepository.findByStatusOrderByCreatedAtDesc(status);
  }
}
