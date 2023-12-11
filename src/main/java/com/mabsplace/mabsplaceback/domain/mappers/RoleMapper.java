package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.role.RoleRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.role.RoleResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {
  Role toEntity(RoleRequestDto roleRequestDto);

  RoleResponseDto toDto(Role role);

  List<RoleResponseDto> toDtoList(List<Role> roles);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Role partialUpdate(RoleRequestDto roleRequestDto, @MappingTarget Role role);
}
