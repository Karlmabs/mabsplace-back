package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.mappers.MyServiceMapper;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.services.MyServiceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class MyServiceController {

  private final MyServiceService myServiceService;

  private final MyServiceMapper mapper;

  private final SubscriptionPlanMapper subscriptionPlanMapper;

  public MyServiceController(MyServiceService myServiceService, MyServiceMapper mapper, @Qualifier("customSubscriptionPlanMapper") SubscriptionPlanMapper subscriptionPlanMapper) {
    this.myServiceService = myServiceService;
    this.mapper = mapper;
    this.subscriptionPlanMapper = subscriptionPlanMapper;
  }

    @PostMapping
    //    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<MyServiceResponseDto> createService(@RequestBody MyServiceRequestDto myService) {
      MyService service = myServiceService.createService(mapper.toEntity(myService));
      return new ResponseEntity<>(mapper.toDto(service), HttpStatus.CREATED);
    }


  @GetMapping("/{id}")
  //@PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<MyServiceResponseDto> getServiceById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(myServiceService.getService(id)));
  }

  @GetMapping("/{id}/subscriptionPlans")
  //@PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<SubscriptionPlanResponseDto> > getSubscriptionPlansByServiceId(@PathVariable Long id) {
    return ResponseEntity.ok(subscriptionPlanMapper.toDtoList(myServiceService.getSubscriptionPlansByServiceId(id)));
  }

  /*@GetMapping
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<PageDto<UserResponseDto>> getAPaginatedUsers(Pageable pageable) {
    return ResponseEntity.ok(
            PaginationUtils.convertEntityPageToDtoPage(
                    userService.getPaginatedUsers( pageable), mapper::toDtoList
            ));
  }*/

  @GetMapping("/all")
  public ResponseEntity<List<MyServiceResponseDto>> getAllServices() {
    List<MyService> users = myServiceService.getAllServices();
    return new ResponseEntity<>(mapper.toDtoList(users), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<MyServiceResponseDto> updateService(@PathVariable Long id, @RequestBody MyServiceRequestDto updatedUser) {
    MyService updated = myServiceService.updateService(id, updatedUser);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteMyService(@PathVariable Long id) {
    myServiceService.deleteService(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
