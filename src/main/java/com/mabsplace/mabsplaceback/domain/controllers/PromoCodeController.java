package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.mappers.PromoCodeMapper;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.services.PromoCodeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promoCodes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    private final PromoCodeMapper mapper;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PromoCodeController.class);

    public PromoCodeController(PromoCodeService promoCodeService, PromoCodeMapper mapper, UserRepository userRepository) {
        this.promoCodeService = promoCodeService;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<PromoCodeResponseDto>> generatePromoCodes(
            @Valid @RequestBody PromoCodeRequestDto request) {
        logger.info("Generating promo codes with request: {}", request);
        List<PromoCodeResponseDto> promoCodes = promoCodeService.generatePromoCodes(request);
        logger.info("Generated {} promo codes", promoCodes.size());
        return ResponseEntity.ok(promoCodes);
    }

    @PostMapping("/validate/{code}")
    public ResponseEntity<PromoCodeResponseDto> validateCode(@PathVariable String code, @RequestParam(required = false) Long userId) {
        logger.info("Validating promo code: {}, for user ID: {}", code, userId);
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        PromoCodeResponseDto validatedPromoCode = promoCodeService.validatePromoCode(code, user);
        logger.info("Promo code validation result: {}", validatedPromoCode);
        return ResponseEntity.ok(validatedPromoCode);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromoCodeResponseDto>> getActivePromoCodes() {
        logger.info("Fetching active promo codes");
        List<PromoCodeResponseDto> promoCodes = promoCodeService.getActivePromoCodes();
        logger.info("Fetched {} active promo codes", promoCodes.size());
        return ResponseEntity.ok(promoCodes);
    }

    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_PROMO_CODES')")
    @GetMapping("/all")
    public ResponseEntity<List<PromoCodeResponseDto>> getAllPromoCodes() {
        logger.info("Fetching all promo codes");
        List<PromoCodeResponseDto> promoCodes = promoCodeService.getAllPromoCodes();
        logger.info("Fetched {} promo codes", promoCodes.size());
        return new ResponseEntity<>(promoCodes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> getPromoCodeById(@PathVariable Long id) {
        logger.info("Fetching promo code with ID: {}", id);
        PromoCodeResponseDto promoCode = promoCodeService.getPromoCode(id);
        logger.info("Fetched promo code: {}", promoCode);
        return ResponseEntity.ok(promoCode);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> updatePromoCode(@PathVariable Long id, @RequestBody PromoCodeRequestDto request) {
        logger.info("Updating promo code with ID: {}, Request: {}", id, request);
        PromoCodeResponseDto updatedPromoCode = promoCodeService.updatePromoCode(id, request);
        logger.info("Updated promo code: {}", updatedPromoCode);
        return ResponseEntity.ok(updatedPromoCode);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PromoCodeResponseDto>> getPromoCodesByUserId(@PathVariable Long userId) {
        logger.info("Fetching promo codes for user ID: {}", userId);
        List<PromoCodeResponseDto> promoCodes = promoCodeService.getPromoCodesByUserId(userId);
        logger.info("Fetched {} promo codes for user ID: {}", promoCodes.size(), userId);
        return ResponseEntity.ok(promoCodes);
    }
}
