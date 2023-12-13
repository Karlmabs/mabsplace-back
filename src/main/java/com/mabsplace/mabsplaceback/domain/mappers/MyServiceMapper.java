package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ServiceAccountMapper.class})
public interface MyServiceMapper {
  MyService toEntity(MyServiceRequestDto myServiceRequestDto);

  MyServiceResponseDto toDto(MyService myService);

  List<MyServiceResponseDto> toDtoList(List<MyService> myServices);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  MyService partialUpdate(MyServiceRequestDto myServiceRequestDto, @MappingTarget MyService myService);
}
