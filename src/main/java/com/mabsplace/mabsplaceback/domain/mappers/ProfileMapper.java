package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {SubscriptionMapper.class})
public interface ProfileMapper {

  Profile toEntity(ProfileRequestDto profileRequestDto);

  @Mapping(target = "subscriptionId", expression = "java(mapSubscription(profile.getSubscription()))")
  @Mapping(target = "serviceAccountId", expression = "java(mapServiceAccount(profile.getServiceAccount()))")
  ProfileResponseDto toDto(Profile profile);

  default Long mapSubscription(Subscription subscription) {
    if (subscription == null) {
      return 0L;
    }
    return subscription.getId();
  }

  default Long mapServiceAccount(ServiceAccount serviceAccount) {
    if (serviceAccount == null) {
      return null;
    }
    return serviceAccount.getId();
  }


  List<ProfileResponseDto> toDtoList(List<Profile> profiles);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Profile partialUpdate(ProfileRequestDto profileRequestDto, @MappingTarget Profile profile);

}
