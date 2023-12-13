package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.currency.CurrencyResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.mappers.CurrencyMapper;
import com.mabsplace.mabsplaceback.domain.services.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {
  
  private final CurrencyService currencyService;
  private final CurrencyMapper mapper;

  public CurrencyController(CurrencyService currencyService, CurrencyMapper mapper) {
    this.currencyService = currencyService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<CurrencyResponseDto> createUser(@RequestBody CurrencyRequestDto currencyRequestDto) {
    Currency createdCurrency = currencyService.createCurrency(currencyRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdCurrency), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<CurrencyResponseDto> getCurrencyById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(currencyService.getCurrencyById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<CurrencyResponseDto>> getAllUsers() {
    List<Currency> Currencys = currencyService.getAllCurrencys();
    return new ResponseEntity<>(mapper.toDtoList(Currencys), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<CurrencyResponseDto> updateUser(@PathVariable Long id, @RequestBody CurrencyRequestDto updatedCurrency) {
    Currency updated = currencyService.updateCurrency(id, updatedCurrency);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
    currencyService.deleteCurrency(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
