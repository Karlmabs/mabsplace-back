package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.mappers.CurrencyMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {
  private final CurrencyRepository currencyRepository;
  private final CurrencyMapper currencyMapper;
  private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

  public CurrencyService(CurrencyRepository currencyRepository, CurrencyMapper currencyMapper) {
    this.currencyRepository = currencyRepository;
    this.currencyMapper = currencyMapper;
  }

  public Currency createCurrency(CurrencyRequestDto currencyRequestDto) {
    logger.info("Creating currency with data: {}", currencyRequestDto);
    Currency currency = currencyMapper.toEntity(currencyRequestDto);
    Currency savedCurrency = currencyRepository.save(currency);
    logger.info("Currency created successfully: {}", savedCurrency);
    return savedCurrency;
  }

  public Currency getCurrencyById(Long id) throws ResourceNotFoundException {
    logger.info("Retrieving currency with ID: {}", id);
    Currency currency = currencyRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("Currency not found with ID: {}", id);
            return new ResourceNotFoundException("Currency", "id", id);
        });
    logger.info("Retrieved currency: {}", currency);
    return currency;
  }

  public List<Currency> getAllCurrencys() {
    logger.info("Retrieving all currencies");
    List<Currency> currencies = currencyRepository.findAll();
    logger.info("Retrieved {} currencies", currencies.size());
    return currencies;
  }

  public Currency updateCurrency(Long id, CurrencyRequestDto updatedCurrency) {
    logger.info("Updating currency with ID: {} using data: {}", id, updatedCurrency);
    Currency currency = currencyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", id));
    Currency updated = currencyMapper.toEntity(updatedCurrency);
    updated.setId(id);
    Currency savedCurrency = currencyRepository.save(updated);
    logger.info("Updated currency: {}", currency);
    return savedCurrency;
  }

  public void deleteCurrency(Long id) {
    logger.info("Deleting currency with ID: {}", id);
    currencyRepository.deleteById(id);
    logger.info("Deleted currency successfully with ID: {}", id);
  }
}
