package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.mappers.CurrencyMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {
  private final CurrencyRepository currencyRepository;
  private final CurrencyMapper currencyMapper;

  public CurrencyService(CurrencyRepository currencyRepository, CurrencyMapper currencyMapper) {
    this.currencyRepository = currencyRepository;
    this.currencyMapper = currencyMapper;
  }

  public Currency createCurrency(CurrencyRequestDto currencyRequestDto) {
    Currency currency = currencyMapper.toEntity(currencyRequestDto);
    return currencyRepository.save(currency);
  }

  public Currency getCurrencyById(Long id) throws ResourceNotFoundException {
    return currencyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", id));
  }

  public List<Currency> getAllCurrencys() {
    return currencyRepository.findAll();
  }

  public Currency updateCurrency(Long id, CurrencyRequestDto updatedCurrency) {
    Currency target = currencyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", id));
    Currency updated = currencyMapper.partialUpdate(updatedCurrency, target);
    return currencyRepository.save(updated);
  }

  public void deleteCurrency(Long id) {
    currencyRepository.deleteById(id);
  }
}
