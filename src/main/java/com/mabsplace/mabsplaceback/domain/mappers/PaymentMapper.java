package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, CurrencyMapper.class})
public interface PaymentMapper {
  Payment toEntity(PaymentRequestDto paymentRequestDto);

  @Mapping(target = "userId", expression = "java(mapUser(payment.getUser()))")
  @Mapping(target = "currencyId", expression = "java(mapCurrency(payment.getCurrency()))")
  @Mapping(target = "serviceId", expression = "java(payment.getService().getId())")
  @Mapping(target = "subscriptionPlanId", expression = "java(payment.getSubscriptionPlan().getId())")
  @Mapping(target = "currencyName", expression = "java(payment.getCurrency().getName())")
  @Mapping(target = "serviceName", expression = "java(payment.getService().getName())")
  @Mapping(target = "subscriptionPlanName", expression = "java(payment.getSubscriptionPlan().getName())")
  PaymentResponseDto toDto(Payment payment);

  default Long mapUser(User user) {
    if (user == null) {
      return 0L;
    }
    return user.getId();
  }

  default Long mapCurrency(Currency currency) {
    if (currency == null) {
      return 0L;
    }
    return currency.getId();
  }

  default Long mapService(MyService myService) {
    if (myService == null) {
      return 0L;
    }
    return myService.getId();
  }

  default Long mapSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
    if (subscriptionPlan == null) {
      return 0L;
    }
    return subscriptionPlan.getId();
  }

  List<PaymentResponseDto> toDtoList(List<Payment> payments);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Payment partialUpdate(PaymentRequestDto paymentRequestDto, @MappingTarget Payment payment);
}
