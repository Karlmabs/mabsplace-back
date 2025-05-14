package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.mappers.MyServiceMapper;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.services.MyServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class MyServiceController {

  private static final Logger logger = LoggerFactory.getLogger(MyServiceController.class);

  private final MyServiceService myServiceService;

  private final MyServiceMapper mapper;

  private final SubscriptionPlanMapper subscriptionPlanMapper;

  public MyServiceController(MyServiceService myServiceService, MyServiceMapper mapper, @Qualifier("customSubscriptionPlanMapper") SubscriptionPlanMapper subscriptionPlanMapper) {
    this.myServiceService = myServiceService;
    this.mapper = mapper;
    this.subscriptionPlanMapper = subscriptionPlanMapper;
  }

    @PostMapping
    public ResponseEntity<MyServiceResponseDto> createService(@RequestBody MyServiceRequestDto myServiceRequestDto) {
        logger.info("Creating new service with request: {}", myServiceRequestDto);
        MyService service = myServiceService.createService(mapper.toEntity(myServiceRequestDto));
        logger.info("Service created: {}", mapper.toDto(service));
        return new ResponseEntity<>(mapper.toDto(service), HttpStatus.CREATED);
    }

  @GetMapping("/{id}")
  public ResponseEntity<MyServiceResponseDto> getServiceById(@PathVariable Long id) {
      logger.info("Fetching service with ID: {}", id);
      MyService service = myServiceService.getService(id);
      logger.info("Fetched service: {}", mapper.toDto(service));
      return ResponseEntity.ok(mapper.toDto(service));
  }

  @GetMapping("/{id}/subscriptionPlans")
  public ResponseEntity<List<SubscriptionPlanResponseDto>> getSubscriptionPlansByServiceId(@PathVariable Long id) {
      logger.info("Fetching subscription plans for service ID: {}", id);
      List<SubscriptionPlanResponseDto> plans = subscriptionPlanMapper.toDtoList(myServiceService.getSubscriptionPlansByServiceId(id));
      logger.info("Fetched {} subscription plans for service ID: {}", plans.size(), id);
      return ResponseEntity.ok(plans);
  }

  @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_SERVICES')")
  @GetMapping("/all")
  public ResponseEntity<List<MyServiceResponseDto>> getAllServices() {
      logger.info("Fetching all services");
      List<MyService> services = myServiceService.getAllServices();
      logger.info("Fetched {} services", services.size());
      return ResponseEntity.ok(mapper.toDtoList(services));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MyServiceResponseDto> updateService(@PathVariable Long id, @RequestBody MyServiceRequestDto requestDTO) {
      logger.info("Updating service with ID: {}, Request: {}", id, requestDTO);
      MyService updatedService = myServiceService.updateService(id, requestDTO);
      if (updatedService != null) {
          logger.info("Service updated successfully: {}", mapper.toDto(updatedService));
          return ResponseEntity.ok(mapper.toDto(updatedService));
      }
      logger.warn("Service not found with ID: {}", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMyService(@PathVariable Long id) {
      logger.info("Deleting service with ID: {}", id);
      myServiceService.deleteService(id);
      logger.info("Deleted service successfully with ID: {}", id);
      return ResponseEntity.noContent().build();
  }

}
