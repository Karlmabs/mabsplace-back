package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import com.mabsplace.mabsplaceback.domain.mappers.PromoCodeMapper;
import com.mabsplace.mabsplaceback.domain.services.PromoCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/promoCodes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    private final PromoCodeMapper mapper;

    public PromoCodeController(PromoCodeService promoCodeService, PromoCodeMapper mapper) {
        this.promoCodeService = promoCodeService;
        this.mapper = mapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<PromoCodeResponseDto>> getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeService.getAllPromoCodes();
        return new ResponseEntity<>(mapper.toDtoList(promoCodes), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> getPromoCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(promoCodeService.getPromoCode(id)));
    }
}
