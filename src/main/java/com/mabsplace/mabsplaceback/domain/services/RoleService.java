package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.role.RoleRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.mappers.RoleMapper;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

  private final RoleRepository roleRepository;
  private final RoleMapper mapper;

  public RoleService(RoleRepository roleRepository, RoleMapper mapper) {
    this.roleRepository = roleRepository;
    this.mapper = mapper;
  }

  public Role createRole(RoleRequestDto roleRequestDto) {
    Role role = mapper.toEntity(roleRequestDto);
    return roleRepository.save(role);
  }

  public Role getRoleById(Long id) throws ResourceNotFoundException {
    return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
  }

  public Role updateRole(Long id, RoleRequestDto updatedRole) throws ResourceNotFoundException {
    Role target = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    Role updated = mapper.partialUpdate(updatedRole, target);
    return roleRepository.save(updated);
  }

  public void deleteRole(Long id) {
    roleRepository.deleteById(id);
  }

  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }
}
