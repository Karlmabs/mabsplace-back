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
    UserResponseDto toDto(User user);

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
