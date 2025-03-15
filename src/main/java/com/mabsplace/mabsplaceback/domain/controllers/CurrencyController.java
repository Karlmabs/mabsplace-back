package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.mappers.CurrencyMapper;
import com.mabsplace.mabsplaceback.domain.services.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

  private final CurrencyService currencyService;
  private final CurrencyMapper mapper;
  private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

  public CurrencyController(CurrencyService currencyService, CurrencyMapper mapper) {
    this.currencyService = currencyService;
    this.mapper = mapper;
  }

  @PostMapping
  public ResponseEntity<CurrencyResponseDto> create(@RequestBody CurrencyRequestDto currencyRequestDto) {
    logger.info("Creating currency with request: {}", currencyRequestDto);
    Currency createdCurrency = currencyService.createCurrency(currencyRequestDto);
    logger.info("Currency created successfully: {}", createdCurrency);
    return new ResponseEntity<>(mapper.toDto(createdCurrency), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CurrencyResponseDto> getCurrencyById(@PathVariable Long id) {
    logger.info("Fetching currency by ID: {}", id);
    Currency currency = currencyService.getCurrencyById(id);
    logger.info("Currency fetched: {}", currency);
    return ResponseEntity.ok(mapper.toDto(currency));
  }

  @GetMapping
  public ResponseEntity<List<CurrencyResponseDto>> getAllCurrencies() {
    logger.info("Fetching all currencies");
    List<Currency> currencies = currencyService.getAllCurrencys();
    logger.info("Currencies fetched: {}", currencies.size());
    return new ResponseEntity<>(mapper.toDtoList(currencies), HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CurrencyResponseDto> updateCurrency(@PathVariable Long id, @RequestBody CurrencyRequestDto updatedCurrency) {
    logger.info("Updating currency with ID: {}, Request: {}", id, updatedCurrency);
    Currency updated = currencyService.updateCurrency(id, updatedCurrency);
    if (updated != null) {
      logger.info("Currency updated successfully: {}", updated);
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    logger.warn("Currency update failed, currency not found with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
    logger.info("Deleting currency with ID: {}", id);
    currencyService.deleteCurrency(id);
    logger.info("Currency deleted successfully: {}", id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
