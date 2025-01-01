package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import com.mabsplace.mabsplaceback.domain.mappers.PromoCodeMapper;
import com.mabsplace.mabsplaceback.domain.services.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promoCodes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    private final PromoCodeMapper mapper;


    public PromoCodeController(PromoCodeService promoCodeService, PromoCodeMapper mapper) {
        this.promoCodeService = promoCodeService;
        this.mapper = mapper;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<PromoCodeResponseDto>> generatePromoCodes(
            @Valid @RequestBody PromoCodeRequestDto request) {
        return ResponseEntity.ok(promoCodeService.generatePromoCodes(request));
    }

    @PostMapping("/validate/{code}")
    public ResponseEntity<PromoCodeResponseDto> validateCode(@PathVariable String code) {
        return ResponseEntity.ok(promoCodeService.validatePromoCode(code));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromoCodeResponseDto>> getActivePromoCodes() {
        return ResponseEntity.ok(promoCodeService.getActivePromoCodes());
    }

    @GetMapping("/all")
    public ResponseEntity<List<PromoCodeResponseDto>> getAllPromoCodes() {
        List<PromoCodeResponseDto> promoCodes = promoCodeService.getAllPromoCodes();
        return new ResponseEntity<>(promoCodes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> getPromoCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.getPromoCode(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> updatePromoCode(@PathVariable Long id, @RequestBody PromoCodeRequestDto request) {
        return ResponseEntity.ok(promoCodeService.updatePromoCode(id, request));
    }
}
