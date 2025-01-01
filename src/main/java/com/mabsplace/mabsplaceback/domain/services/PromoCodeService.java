package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import com.mabsplace.mabsplaceback.domain.enums.PromoCodeStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PromoCodeMapper;
import com.mabsplace.mabsplaceback.domain.repositories.PromoCodeRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.PromoCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;
    private final PromoCodeGenerator promoCodeGenerator;

    @Autowired
    public PromoCodeService(
            PromoCodeRepository promoCodeRepository,
            PromoCodeMapper promoCodeMapper,
            PromoCodeGenerator promoCodeGenerator) {
        this.promoCodeRepository = promoCodeRepository;
        this.promoCodeMapper = promoCodeMapper;
        this.promoCodeGenerator = promoCodeGenerator;
    }

    public List<PromoCodeResponseDto> generatePromoCodes(PromoCodeRequestDto request) {
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

    public PromoCodeResponseDto validatePromoCode(String code) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Invalid promo code: " + code));

        if (!promoCode.isValid()) {
            throw new IllegalStateException("Promo code is not valid");
        }

        return promoCodeMapper.toDto(promoCode);
    }

    @Transactional
    public void applyPromoCode(String code, Payment payment) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Invalid promo code: " + code));

        if (!promoCode.isValid()) {
            throw new IllegalStateException("Promo code is not valid");
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
}