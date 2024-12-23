package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.discount.ServiceDiscountDTO;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServiceDiscount;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceDiscountMapper {

    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "global", target = "isGlobal")
    ServiceDiscountDTO toDTO(ServiceDiscount entity);

    List<ServiceDiscountDTO> toDTOs(List<ServiceDiscount> entities);

    @Mapping(target = "service", source = "serviceId", qualifiedByName = "serviceIdToService")
    ServiceDiscount toEntity(ServiceDiscountDTO dto);

    @Named("serviceIdToService")
    default MyService serviceIdToService(Long id) {
        if (id == null) {
            return null;
        }
        MyService service = new MyService();
        service.setId(id);
        return service;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "service", source = "serviceId", qualifiedByName = "serviceIdToService")
    void updateEntityFromDTO(ServiceDiscountDTO dto, @MappingTarget ServiceDiscount entity);


}
