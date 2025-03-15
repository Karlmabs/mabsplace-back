package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.role.RoleRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.mappers.RoleMapper;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

  private final RoleRepository roleRepository;
  private final RoleMapper mapper;
  private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

  public RoleService(RoleRepository roleRepository, RoleMapper mapper) {
    this.roleRepository = roleRepository;
    this.mapper = mapper;
  }

  public Role createRole(RoleRequestDto roleRequestDto) {
    logger.info("Creating new role with data: {}", roleRequestDto);
    Role role = mapper.toEntity(roleRequestDto);
    Role createdRole = roleRepository.save(role);
    logger.info("Role created successfully: {}", createdRole);
    return createdRole;
  }

  public Role getRoleById(Long id) throws ResourceNotFoundException {
    logger.info("Fetching role by ID: {}", id);
    Role role = roleRepository.findById(id).orElseThrow(() -> {
      logger.error("Role not found with ID: {}", id);
      return new ResourceNotFoundException("Role", "id", id);
    });
    logger.info("Retrieved role successfully: {}", role);
    return role;
  }

  public Role updateRole(Long id, RoleRequestDto updatedRole) throws ResourceNotFoundException {
    logger.info("Updating role with ID: {}, data: {}", id, updatedRole);
    Role existingRole = roleRepository.findById(id).orElseThrow(() -> {
      logger.error("Role not found with ID: {}", id);
      return new ResourceNotFoundException("Role", "id", id);
    });

    Role updated = mapper.partialUpdate(updatedRole, existingRole);
    Role savedRole = roleRepository.save(updated);
    logger.info("Role updated successfully: {}", savedRole);
    return savedRole;
  }

  public void deleteRole(Long id) {
    logger.info("Deleting role with ID: {}", id);
    if (!roleRepository.existsById(id)) {
      logger.error("Role not found with ID: {}", id);
      throw new ResourceNotFoundException("Role", "id", id);
    }
    roleRepository.deleteById(id);
    logger.info("Deleted role successfully with ID: {}", id);
  }

  public List<Role> getAllRoles() {
    logger.info("Fetching all roles");
    List<Role> roles = roleRepository.findAll();
    logger.info("Retrieved {} roles", roles.size());
    return roles;
  }
}
