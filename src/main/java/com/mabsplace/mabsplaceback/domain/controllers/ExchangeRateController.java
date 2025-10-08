package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ExchangeRateDto;
import com.mabsplace.mabsplaceback.domain.services.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateController.class);

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping
    public ResponseEntity<ExchangeRateDto> createExchangeRate(@RequestBody ExchangeRateDto exchangeRateDto) {
        logger.info("Creating exchange rate: {} to {}", exchangeRateDto.getFromCurrency(), exchangeRateDto.getToCurrency());
        ExchangeRateDto created = exchangeRateService.createExchangeRate(exchangeRateDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExchangeRateDto> updateExchangeRate(@PathVariable Long id, @RequestBody ExchangeRateDto exchangeRateDto) {
        logger.info("Updating exchange rate ID: {}", id);
        ExchangeRateDto updated = exchangeRateService.updateExchangeRate(id, exchangeRateDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeRate(@PathVariable Long id) {
        logger.info("Deleting exchange rate ID: {}", id);
        exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeRateDto> getExchangeRateById(@PathVariable Long id) {
        logger.info("Fetching exchange rate ID: {}", id);
        ExchangeRateDto exchangeRate = exchangeRateService.getExchangeRateById(id);
        return ResponseEntity.ok(exchangeRate);
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRateDto>> getAllExchangeRates() {
        logger.info("Fetching all exchange rates");
        List<ExchangeRateDto> exchangeRates = exchangeRateService.getAllExchangeRates();
        return ResponseEntity.ok(exchangeRates);
    }

    @GetMapping("/active")
    public ResponseEntity<ExchangeRateDto> getActiveExchangeRate(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        logger.info("Fetching active exchange rate: {} to {}", fromCurrency, toCurrency);
        ExchangeRateDto exchangeRate = exchangeRateService.getActiveExchangeRate(fromCurrency, toCurrency);
        return ResponseEntity.ok(exchangeRate);
    }
}
