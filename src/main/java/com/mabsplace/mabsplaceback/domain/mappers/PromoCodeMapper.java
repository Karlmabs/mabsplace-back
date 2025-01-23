package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.PromoCode;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PromoCodeMapper {

    PromoCode toEntity(PromoCodeRequestDto promoCodeRequestDto);

    @Mapping(target = "userId", expression = "java(mapUser(promoCode.getAssignedUser()))")
    PromoCodeResponseDto toDto(PromoCode promoCode);

    default Long mapUser(User user) {
        if (user == null) {
            return 0L;
        }
        return user.getId();
    }

    List<PromoCodeResponseDto> toDtoList(List<PromoCode> promoCodes);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PromoCode partialUpdate(PromoCodeRequestDto promoCodeRequestDto, @MappingTarget PromoCode promoCode);
    
}
