package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ExchangeRateDto;
import com.mabsplace.mabsplaceback.domain.entities.ExchangeRate;
import com.mabsplace.mabsplaceback.domain.mappers.ExchangeRateMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ExchangeRateRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                                ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    public ExchangeRateDto createExchangeRate(ExchangeRateDto exchangeRateDto) {
        logger.info("Creating exchange rate: {} to {}", exchangeRateDto.getFromCurrency(), exchangeRateDto.getToCurrency());
        ExchangeRate exchangeRate = exchangeRateMapper.toEntity(exchangeRateDto);
        ExchangeRate saved = exchangeRateRepository.save(exchangeRate);
        logger.info("Exchange rate created with ID: {}", saved.getId());
        return exchangeRateMapper.toDto(saved);
    }

    public ExchangeRateDto updateExchangeRate(Long id, ExchangeRateDto exchangeRateDto) {
        logger.info("Updating exchange rate ID: {}", id);
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRate", "id", id));

        exchangeRateMapper.partialUpdate(exchangeRateDto, exchangeRate);
        ExchangeRate updated = exchangeRateRepository.save(exchangeRate);
        logger.info("Exchange rate updated: {}", updated.getId());
        return exchangeRateMapper.toDto(updated);
    }

    public void deleteExchangeRate(Long id) {
        logger.info("Deleting exchange rate ID: {}", id);
        exchangeRateRepository.deleteById(id);
        logger.info("Exchange rate deleted: {}", id);
    }

    public ExchangeRateDto getExchangeRateById(Long id) {
        logger.info("Fetching exchange rate ID: {}", id);
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRate", "id", id));
        return exchangeRateMapper.toDto(exchangeRate);
    }

    public List<ExchangeRateDto> getAllExchangeRates() {
        logger.info("Fetching all exchange rates");
        List<ExchangeRate> exchangeRates = exchangeRateRepository.findAll();
        logger.info("Found {} exchange rates", exchangeRates.size());
        return exchangeRateMapper.toDtoList(exchangeRates);
    }

    public ExchangeRateDto getActiveExchangeRate(String fromCurrency, String toCurrency) {
        logger.info("Fetching active exchange rate: {} to {}", fromCurrency, toCurrency);
        ExchangeRate exchangeRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRate", "fromCurrency-toCurrency",
                        fromCurrency + "-" + toCurrency));
        return exchangeRateMapper.toDto(exchangeRate);
    }
}
