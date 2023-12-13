package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {WalletMapper.class, RoleMapper.class})
public interface UserMapper {
  User toEntity(UserRequestDto userRequestDto);

  UserResponseDto toDto(User user);

  List<UserResponseDto> toDtoList(List<User> users);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  User partialUpdate(UserRequestDto userRequestDto, @MappingTarget User user);
}
