package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProfileMapper.class} )
public interface ServiceAccountMapper {

  ServiceAccount toEntity(ServiceAccountRequestDto serviceAccountRequestDto);

  @Mapping(target = "myServiceId", expression = "java(mapService(serviceAccount.getMyService()))")
  ServiceAccountResponseDto toDto(ServiceAccount serviceAccount);

  default Long mapService(MyService myService) {
    if (myService == null) {
      return null;
    }
    return myService.getId();
  }

  List<ServiceAccountResponseDto> toDtoList(List<ServiceAccount> serviceAccounts);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  ServiceAccount partialUpdate(ServiceAccountRequestDto serviceAccountRequestDto, @MappingTarget ServiceAccount serviceAccount);

}
