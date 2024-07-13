package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProfileMapper.class})
public interface ServiceAccountMapper {

    ServiceAccount toEntity(ServiceAccountRequestDto serviceAccountRequestDto);

    @Mapping(target = "myServiceId", expression = "java(mapService(serviceAccount.getMyService()))")
    @Mapping(target = "serviceName", expression = "java(mapServiceName(serviceAccount.getMyService().getName()))")
    ServiceAccountResponseDto toDto(ServiceAccount serviceAccount);

    default Long mapService(MyService myService) {
        if (myService == null) {
            return 0L;
        }
        return myService.getId();
    }

    default String mapServiceName(String name) {
        return name;
    }

    List<ServiceAccountResponseDto> toDtoList(List<ServiceAccount> serviceAccounts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ServiceAccount partialUpdate(ServiceAccountRequestDto serviceAccountRequestDto, @MappingTarget ServiceAccount serviceAccount);

}
