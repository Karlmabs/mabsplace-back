package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PromoCodeMapper {

    PromoCode toEntity(PromoCodeRequestDto promoCodeRequestDto);

    PromoCodeResponseDto toDto(PromoCode promoCode);

    List<PromoCodeResponseDto> toDtoList(List<PromoCode> promoCodes);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PromoCode partialUpdate(PromoCodeRequestDto promoCodeRequestDto, @MappingTarget PromoCode promoCode);
    
}
