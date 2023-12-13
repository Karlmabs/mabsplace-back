package com.mabsplace.mabsplaceback.domain.services;


import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import org.springframework.stereotype.Service;

@Service
public class MyServiceService {

  private final MyServiceRepository myServiceRepository;

  public MyServiceService(MyServiceRepository myServiceRepository) {
    this.myServiceRepository = myServiceRepository;
  }

  public MyService createService(MyService service) {
    return myServiceRepository.save(service);
  }

  public MyService getService(Long id) {
    return myServiceRepository.findById(id).orElse(null);
  }

  public MyService updateService(MyService service) {
    return myServiceRepository.save(service);
  }

  public void deleteService(Long id) {
    myServiceRepository.deleteById(id);
  }
}
