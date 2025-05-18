package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.PromoCodeStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PromoCodeMapper;
import com.mabsplace.mabsplaceback.domain.repositories.PromoCodeRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.PromoCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromoCodeService {
    private static final Logger logger = LoggerFactory.getLogger(PromoCodeService.class);
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;
    private final PromoCodeGenerator promoCodeGenerator;
    private final UserRepository userRepository;

    @Autowired
    public PromoCodeService(
            PromoCodeRepository promoCodeRepository,
            PromoCodeMapper promoCodeMapper,
            PromoCodeGenerator promoCodeGenerator, UserRepository userRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.promoCodeMapper = promoCodeMapper;
        this.promoCodeGenerator = promoCodeGenerator;
        this.userRepository = userRepository;
    }

    public List<PromoCodeResponseDto> generatePromoCodes(PromoCodeRequestDto request) {

        User user = null;
        if (request.getUserId() != 0) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        }

        List<PromoCode> generatedCodes = new ArrayList<>();
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        for (int i = 0; i < quantity; i++) {
            String code = generateUniqueCode();
            PromoCode promoCode = PromoCode.builder()
                    .code(code)
                    .discountAmount(request.getDiscountAmount())
                    .expirationDate(request.getExpirationDate())
                    .maxUsage(request.getMaxUsage())
                    .usedCount(0)
                    .assignedUser(user)
                    .status(request.getStatus())
                    .build();

            generatedCodes.add(promoCodeRepository.save(promoCode));
        }

        return promoCodeMapper.toDtoList(generatedCodes);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = promoCodeGenerator.generateCode();
        } while (promoCodeRepository.existsByCodeIgnoreCase(code));
        return code;
    }

    public PromoCodeResponseDto validatePromoCode(String code, User user) {
        logger.info("Validating promo code: {} for user: {}", code, user != null ? user.getId() : "null");
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> {
                    logger.error("Promo code not found: {}", code);
                    return new ResourceNotFoundException("PromoCode", "code", code);
                });

        // Add detailed logging to identify the exact validation issue
        if (!promoCode.isValid()) {
            logger.warn("Promo code {} is not valid. Expired: {}, Exhausted: {}, Status: {}",
                    code, promoCode.isExpired(), promoCode.isExhausted(), promoCode.getStatus());
            throw new IllegalStateException("Promo code is not valid (expired, exhausted, or inactive)");
        }

        if (!promoCode.isAssignedToUser(user)) {
            logger.warn("Promo code {} is not assigned to user {}. Assigned to: {}",
                    code, user != null ? user.getId() : "null",
                    promoCode.getAssignedUser() != null ? promoCode.getAssignedUser().getId() : "null");
            throw new IllegalStateException("Promo code is not assigned to this user");
        }

        logger.info("Promo code validated successfully: {}", promoCode);
        return promoCodeMapper.toDto(promoCode);
    }

    @Transactional
    public void applyPromoCode(String code, Payment payment) {
        logger.info("Applying promo code: {}", code);

        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> {
                    logger.error("Promo code not found: {}", code);
                    return new RuntimeException("Invalid promo code: " + code);
                });

        if (!promoCode.isValid() || !promoCode.isAssignedToUser(payment.getUser())) {
            logger.warn("Promo code {} is not valid or not assigned to the provided user", code);
            throw new IllegalStateException("Promo code is not valid or not assigned to this user");
        }

        // Calculate discount
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                promoCode.getDiscountAmount().divide(BigDecimal.valueOf(100)));
        BigDecimal discountedAmount = payment.getAmount().multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        logger.info("Discounted amount: {}", discountedAmount);

        // Apply discount to payment
        payment.setAmount(discountedAmount);
        payment.setPromoCode(promoCode);

        // Increment usage count
        promoCode.setUsedCount(promoCode.getUsedCount() + 1);

        // Update status if exhausted
        if (promoCode.isExhausted()) {
            logger.error("Promo code {} is exhausted", code);
            promoCode.setStatus(PromoCodeStatus.INACTIVE);
        }

        promoCodeRepository.save(promoCode);
    }

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void deleteInactivePromoCodes() {
        logger.info("Starting scheduled task to delete inactive promo codes");
        List<PromoCode> inactiveCodes = promoCodeRepository.findByStatus(PromoCodeStatus.INACTIVE);
        if (!inactiveCodes.isEmpty()) {
            logger.info("Deleting {} inactive promo codes", inactiveCodes.size());
            promoCodeRepository.deleteAll(inactiveCodes);
        } else {
            logger.info("No inactive promo codes found");
        }
        logger.info("Completed scheduled task to delete inactive promo codes");
    }

    public PromoCodeResponseDto getPromoCode(Long id) {
        logger.info("Fetching promo code with ID: {}", id);
        PromoCode promoCode = promoCodeRepository.findById(id).orElseThrow(() -> {
            logger.error("PromoCode not found with ID: {}", id);
            return new ResourceNotFoundException("PromoCode", "id", id);
        });
        logger.info("Fetched promo code successfully: {}", promoCode);
        return promoCodeMapper.toDto(promoCode);
    }

    public List<PromoCodeResponseDto> getAllPromoCodes() {
        logger.info("Fetching all promo codes");
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        logger.info("Fetched {} promo codes", promoCodes.size());
        return promoCodeMapper.toDtoList(promoCodes);
    }

    public void deletePromoCode(Long id) {
        logger.info("Attempting to delete promo code with ID: {}", id);
        if (!promoCodeRepository.existsById(id)) {
            logger.error("PromoCode not found with ID: {}", id);
            throw new ResourceNotFoundException("PromoCode", "id", id);
        }
        promoCodeRepository.deleteById(id);
        logger.info("Deleted promo code successfully with ID: {}", id);
    }

    public List<PromoCodeResponseDto> getActivePromoCodes() {
        logger.info("Fetching active promo codes");
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        List<PromoCodeResponseDto> activePromoCodes = new ArrayList<>();
        for (PromoCode promoCode : promoCodes) {
            if (promoCode.isValid()) {
                activePromoCodes.add(promoCodeMapper.toDto(promoCode));
            }
        }
        logger.info("Fetched {} active promo codes", activePromoCodes.size());
        return activePromoCodes;
    }

    public PromoCodeResponseDto updatePromoCode(Long id, PromoCodeRequestDto request) {
        logger.info("Updating promo code with ID: {}, data: {}", id, request);
        PromoCode promoCode = promoCodeRepository.findById(id).orElseThrow(() -> {
            logger.error("PromoCode not found with ID: {}", id);
            return new ResourceNotFoundException("PromoCode", "id", id);
        });

        promoCode.setDiscountAmount(request.getDiscountAmount());
        promoCode.setExpirationDate(request.getExpirationDate());
        promoCode.setMaxUsage(request.getMaxUsage());
        promoCode.setStatus(request.getStatus());

        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);
        logger.info("Updated promo code successfully: {}", savedPromoCode);
        return promoCodeMapper.toDto(savedPromoCode);
    }

    public String generatePromoCodeForReferrer(User referrer, BigDecimal referralDiscountRate) {
        logger.info("Generating promo code for referrer user ID: {}", referrer.getId());
        String code = generateUniqueCode();
        PromoCode promoCode = PromoCode.builder()
                .code(code)
                .discountAmount(referralDiscountRate)
                .expirationDate(LocalDateTime.now().plusMonths(1))
                .maxUsage(1)
                .assignedUser(referrer)
                .usedCount(0)
                .status(PromoCodeStatus.ACTIVE)
                .build();

        promoCodeRepository.save(promoCode);
        logger.info("Promo code generated successfully for referrer (User ID: {}): {}", referrer.getId(), code);
        return code;
    }

    public String generatePromoCodeForReferrer2(User referrer, BigDecimal referralDiscountRate) {
        logger.info("Generating promo code for referrer (User ID: {})", referrer.getId());
        LocalDateTime startOfCurrentMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfNextMonth = startOfCurrentMonth.plusMonths(2).minusSeconds(1);

        List<PromoCode> existingValidCodes = promoCodeRepository.findByAssignedUser(referrer).stream()
                .filter(promoCode -> promoCode.isValid() &&
                        promoCode.getExpirationDate().isAfter(startOfCurrentMonth) &&
                        promoCode.getExpirationDate().isBefore(endOfNextMonth)).toList();

        if (!existingValidCodes.isEmpty()) {
            PromoCode existingCode = existingValidCodes.get(0);
            existingCode.setDiscountAmount(existingCode.getDiscountAmount().add(referralDiscountRate));
            promoCodeRepository.save(existingCode);
            logger.info("Existing promo code updated with new discount rate for referrer (User ID: {})", referrer.getId());
            return existingCode.getCode();
        }

        String code = generateUniqueCode();
        PromoCode promoCode = PromoCode.builder()
                .assignedUser(referrer)
                .code(code)
                .discountAmount(referralDiscountRate)
                .expirationDate(endOfNextMonth)
                .maxUsage(1)
                .usedCount(0)
                .status(PromoCodeStatus.ACTIVE)
                .build();

        promoCodeRepository.save(promoCode);
        logger.info("New promo code generated successfully for referrer (User ID: {})", referrer.getId());
        return code;
    }

    public List<PromoCodeResponseDto> getPromoCodesByUserId(Long userId) {
        logger.info("Fetching promo codes by user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        List<PromoCode> promoCodes = promoCodeRepository.findByAssignedUser(user).stream()
                .filter(PromoCode::isValid)
                .collect(Collectors.toList());

        logger.info("Fetched {} promo codes", promoCodes.size());

        return promoCodeMapper.toDtoList(promoCodes);
    }

    public String getUserPersonalPromoCode(User user) {
        logger.info("Fetching personal promo code for user ID: {}", user.getId());

        List<PromoCode> promoCodes = promoCodeRepository.findByAssignedUser(user);
        if (promoCodes.isEmpty()) {
            logger.error("Promo code not found for user ID: {}", user.getId());
            return null;
        }
        PromoCode promoCode = promoCodes.get(0);

        logger.info("Fetched personal promo code successfully: {}", promoCode);
        return promoCode.getCode();
    }
}