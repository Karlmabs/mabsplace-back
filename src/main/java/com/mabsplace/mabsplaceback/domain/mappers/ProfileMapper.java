package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {SubscriptionMapper.class})
public interface ProfileMapper {

  Profile toEntity(ProfileRequestDto profileRequestDto);

  ProfileResponseDto toDto(Profile profile);

  List<ProfileResponseDto> toDtoList(List<Profile> profiles);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Profile partialUpdate(ProfileRequestDto profileRequestDto, @MappingTarget Profile profile);

}
