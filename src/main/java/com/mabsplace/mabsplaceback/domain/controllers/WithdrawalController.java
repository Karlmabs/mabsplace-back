package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.withdrawal.WithdrawalResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Withdrawal;
import com.mabsplace.mabsplaceback.domain.enums.WithdrawalStatus;
import com.mabsplace.mabsplaceback.domain.mappers.WithdrawalLightweightMapper;
import com.mabsplace.mabsplaceback.domain.mappers.WithdrawalMapper;
import com.mabsplace.mabsplaceback.domain.services.WithdrawalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

  private final WithdrawalService withdrawalService;
  private final WithdrawalMapper withdrawalMapper;
  private final WithdrawalLightweightMapper lightweightMapper;
  private static final Logger logger = LoggerFactory.getLogger(WithdrawalController.class);

  public WithdrawalController(WithdrawalService withdrawalService,
                             WithdrawalMapper withdrawalMapper,
                             WithdrawalLightweightMapper lightweightMapper) {
    this.withdrawalService = withdrawalService;
    this.withdrawalMapper = withdrawalMapper;
    this.lightweightMapper = lightweightMapper;
  }

  @PostMapping
  // @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'CREATE_TRANSACTION', 'MANAGE_TRANSACTIONS')")
  public ResponseEntity<WithdrawalResponseDto> createWithdrawal(
      @RequestBody WithdrawalRequestDto withdrawalRequestDto) {
    logger.info("Creating withdrawal: {}", withdrawalRequestDto);
    Withdrawal withdrawal = withdrawalService.processWithdrawal(withdrawalRequestDto);
    logger.info("Withdrawal created successfully with ID: {}", withdrawal.getId());
    return new ResponseEntity<>(withdrawalMapper.toDto(withdrawal), HttpStatus.CREATED);
  }

  @GetMapping
  // @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_TRANSACTIONS', 'MANAGE_TRANSACTIONS')")
  public ResponseEntity<List<WithdrawalLightweightResponseDto>> getAllWithdrawals() {
    logger.info("Fetching all withdrawals");
    List<Withdrawal> withdrawals = withdrawalService.getAllWithdrawals();
    logger.info("Fetched {} withdrawals", withdrawals.size());
    return ResponseEntity.ok(lightweightMapper.toDtoList(withdrawals));
  }

  @GetMapping("/{id}")
  // @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_TRANSACTIONS', 'MANAGE_TRANSACTIONS')")
  public ResponseEntity<WithdrawalResponseDto> getWithdrawalById(@PathVariable Long id) {
    logger.info("Fetching withdrawal by ID: {}", id);
    Withdrawal withdrawal = withdrawalService.getWithdrawalById(id);
    logger.info("Fetched withdrawal: {}", withdrawal.getId());
    return ResponseEntity.ok(withdrawalMapper.toDto(withdrawal));
  }

  @GetMapping("/user/{userId}")
  // @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_TRANSACTIONS', 'MANAGE_TRANSACTIONS')")
  public ResponseEntity<List<WithdrawalLightweightResponseDto>> getWithdrawalsByUserId(
      @PathVariable Long userId) {
    logger.info("Fetching withdrawals for user ID: {}", userId);
    List<Withdrawal> withdrawals = withdrawalService.getWithdrawalsByUserId(userId);
    logger.info("Fetched {} withdrawals for user ID: {}", withdrawals.size(), userId);
    return ResponseEntity.ok(lightweightMapper.toDtoList(withdrawals));
  }

  @GetMapping("/status/{status}")
  // @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_TRANSACTIONS', 'MANAGE_TRANSACTIONS')")
  public ResponseEntity<List<WithdrawalLightweightResponseDto>> getWithdrawalsByStatus(
      @PathVariable WithdrawalStatus status) {
    logger.info("Fetching withdrawals with status: {}", status);
    List<Withdrawal> withdrawals = withdrawalService.getWithdrawalsByStatus(status);
    logger.info("Fetched {} withdrawals with status: {}", withdrawals.size(), status);
    return ResponseEntity.ok(lightweightMapper.toDtoList(withdrawals));
  }
}
