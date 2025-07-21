package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentLightweightMapper {

    @Mapping(target = "userName", expression = "java(getUserName(payment))")
    @Mapping(target = "currencySymbol", expression = "java(getCurrencySymbol(payment))")
    @Mapping(target = "serviceName", expression = "java(getServiceName(payment))")
    PaymentLightweightResponseDto toDto(Payment payment);

    default String getUserName(Payment payment) {
        if (payment == null || payment.getUser() == null) {
            return null;
        }
        return payment.getUser().getUsername();
    }

    default String getCurrencySymbol(Payment payment) {
        if (payment == null || payment.getCurrency() == null) {
            return null;
        }
        return payment.getCurrency().getSymbol();
    }

    default String getServiceName(Payment payment) {
        if (payment == null || payment.getService() == null) {
            return null;
        }
        return payment.getService().getName();
    }

    List<PaymentLightweightResponseDto> toDtoList(List<Payment> payments);
}
