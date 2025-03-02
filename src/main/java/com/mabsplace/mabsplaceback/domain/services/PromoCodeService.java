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
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class PromoCodeService {
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
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Invalid promo code: " + code));

        if (!promoCode.isValid() || !promoCode.isAssignedToUser(user)) {
            throw new IllegalStateException("Promo code is not valid or not assigned to this user");
        }

        return promoCodeMapper.toDto(promoCode);
    }

    @Transactional
    public void applyPromoCode(String code, Payment payment) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Invalid promo code: " + code));

        if (!promoCode.isValid() || !promoCode.isAssignedToUser(payment.getUser())) {
            throw new IllegalStateException("Promo code is not valid or not assigned to this user");
        }

        // Calculate discount
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                promoCode.getDiscountAmount().divide(BigDecimal.valueOf(100)));
        BigDecimal discountedAmount = payment.getAmount().multiply(discountMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // Apply discount to payment
        payment.setAmount(discountedAmount);
        payment.setPromoCode(promoCode);

        // Increment usage count
        promoCode.setUsedCount(promoCode.getUsedCount() + 1);

        // Update status if exhausted
        if (promoCode.isExhausted()) {
            promoCode.setStatus(PromoCodeStatus.INACTIVE);
        }

        promoCodeRepository.save(promoCode);
    }

    public PromoCodeResponseDto getPromoCode(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promo code not found: " + id));
        return promoCodeMapper.toDto(promoCode);
    }

    public List<PromoCodeResponseDto> getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        return promoCodeMapper.toDtoList(promoCodes);
    }

    public void deletePromoCode(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promo code not found: " + id));
        promoCodeRepository.delete(promoCode);
    }

    public List<PromoCodeResponseDto> getActivePromoCodes() {
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        List<PromoCodeResponseDto> activePromoCodes = new ArrayList<>();
        for (PromoCode promoCode : promoCodes) {
            if (promoCode.isValid()) {
                activePromoCodes.add(promoCodeMapper.toDto(promoCode));
            }
        }
        return activePromoCodes;
    }

    public PromoCodeResponseDto updatePromoCode(Long id, PromoCodeRequestDto request) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promo code not found: " + id));

        promoCode.setDiscountAmount(request.getDiscountAmount());
        promoCode.setExpirationDate(request.getExpirationDate());
        promoCode.setMaxUsage(request.getMaxUsage());
        promoCode.setStatus(request.getStatus());

        return promoCodeMapper.toDto(promoCodeRepository.save(promoCode));
    }

    public String generatePromoCodeForReferrer(User referrer, BigDecimal referralDiscountRate) {
        String code = generateUniqueCode();
        PromoCode promoCode = PromoCode.builder()
                .code(code)
                .discountAmount(referralDiscountRate)
                .expirationDate(LocalDateTime.of(LocalDate.now().plusMonths(1), LocalDate.now().atStartOfDay().toLocalTime()))
                .maxUsage(1)
                .assignedUser(referrer)
                .usedCount(0)
                .status(PromoCodeStatus.ACTIVE)
                .build();

        promoCodeRepository.save(promoCode);
        return code;
    }

    public String generatePromoCodeForReferrer2(User referrer, BigDecimal referralDiscountRate) {
        LocalDateTime startOfCurrentMonth = LocalDate.now().atStartOfDay();
        LocalDateTime endOfNextMonth = LocalDate.now().plusMonths(1).atStartOfDay();

        List<PromoCode> existingValidCodes = promoCodeRepository.findByAssignedUser(referrer).stream()
                .filter(code -> code.isValid() &&
                        code.getExpirationDate().isAfter(startOfCurrentMonth) &&
                        code.getExpirationDate().isBefore(endOfNextMonth))
                .toList();

        if (!existingValidCodes.isEmpty()) {
            PromoCode existingCode = existingValidCodes.get(0);
            existingCode.setDiscountAmount(existingCode.getDiscountAmount().add(referralDiscountRate));
            promoCodeRepository.save(existingCode);
            return existingCode.getCode();
        }

        String code = generateUniqueCode();
        PromoCode promoCode = PromoCode.builder()
                .code(code)
                .discountAmount(referralDiscountRate)
                .expirationDate(endOfNextMonth)
                .maxUsage(1)
                .assignedUser(referrer)
                .usedCount(0)
                .status(PromoCodeStatus.ACTIVE)
                .build();

        promoCodeRepository.save(promoCode);
        return code;
    }

    public List<PromoCodeResponseDto> getPromoCodesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        List<PromoCode> promoCodes = promoCodeRepository.findByAssignedUser(user).stream()
                .filter(PromoCode::isValid)
                .collect(Collectors.toList());
        return promoCodeMapper.toDtoList(promoCodes);
    }
}