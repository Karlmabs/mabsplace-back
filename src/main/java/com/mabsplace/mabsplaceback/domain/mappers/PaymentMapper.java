package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, CurrencyMapper.class})
public interface PaymentMapper {
  Payment toEntity(PaymentRequestDto paymentRequestDto);

  PaymentResponseDto toDto(Payment payment);

  List<PaymentResponseDto> toDtoList(List<Payment> payments);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Payment partialUpdate(PaymentRequestDto paymentRequestDto, @MappingTarget Payment payment);
}
