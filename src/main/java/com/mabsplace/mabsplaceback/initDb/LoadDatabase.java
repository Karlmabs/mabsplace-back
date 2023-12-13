package com.mabsplace.mabsplaceback.initDb;

import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

  @Bean
  CommandLineRunner initDatabase(RoleRepository repository, CurrencyRepository currencyRepository) {
    Role role1 = Role.builder().name("ROLE_ADMIN").code("ADMIN").description("Administrator").build();
    Role role2 = Role.builder().name("ROLE_USER").code("USER").description("For Users").build();

    Currency currency1 = Currency.builder().name("XAF").exchangeRate(608.21).symbol("XAF").build();

    return args -> {
      if (!repository.existsByCode(role1.getCode())) {
        repository.save(role1);
      }
      if (!repository.existsByCode(role2.getCode())) {
        repository.save(role2);
      }
      if (!currencyRepository.existsByName(currency1.getName())) {
        currencyRepository.save(currency1);
      }
    };
  }
}
