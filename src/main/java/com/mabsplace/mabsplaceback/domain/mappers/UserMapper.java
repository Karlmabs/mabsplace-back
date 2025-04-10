package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileDTO;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {WalletMapper.class, RoleMapper.class})
public interface UserMapper {
    User toEntity(UserRequestDto userRequestDto);

    @Mapping(target = "userProfile", expression = "java(mapUserProfile(user))")
//    @Mapping(target = "referrerId", expression = "java(mapReferrer(user))")
    @Mapping(target = "referrals", expression = "java(mapReferrals(user.getReferrals()))")
    @Mapping(source = "referralCode", target = "referralCode")
    @Mapping(target = "referrerName", expression = "java(mapReferrerName(user.getReferrer()))")
    UserResponseDto toDto(User user);

    default List<UserResponseDto> mapReferrals(List<User> referrals) {
        if (referrals == null) {
            return null;
        }
        return referrals.stream()
                .map(referral -> {
                    UserResponseDto dto = new UserResponseDto();
                    dto.setId(referral.getId());
                    dto.setUsername(referral.getUsername());
                    dto.setEmail(referral.getEmail());
                    dto.setFirstname(referral.getFirstname());
                    dto.setLastname(referral.getLastname());
                    dto.setImage(referral.getImage());
                    dto.setReferralCode(referral.getReferralCode());
                    // Still don't include nested referrals to avoid recursion
                    return dto;
                })
                .collect(Collectors.toList());
    }

    default String mapReferrerName(User user) {
        if (user == null || user.getReferrer() == null) {
            return "No referrer";
        }
        return user.getReferrer().getUsername();
    }

    default Long mapReferrer(User user) {
        if (user == null || user.getReferrer() == null) {
            return null;
        }
        return user.getReferrer().getId();
    }

    default UserProfileDTO mapUserProfile(User user) {
        if (user == null) {
            return null;
        }
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setId(user.getUserProfile().getId());
        userProfileDTO.setDescription(user.getUserProfile().getDescription());
        userProfileDTO.setName(user.getUserProfile().getName());
        userProfileDTO.setRoleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        userProfileDTO.setCreatedAt(user.getUserProfile().getCreatedAt());
        userProfileDTO.setUpdatedAt(user.getUserProfile().getUpdatedAt());
        return userProfileDTO;
    }

    List<UserResponseDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserRequestDto userRequestDto, @MappingTarget User user);
}
