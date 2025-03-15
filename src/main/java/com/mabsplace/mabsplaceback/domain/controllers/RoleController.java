package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.mappers.RoleMapper;
import com.mabsplace.mabsplaceback.domain.dtos.role.RoleRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.role.RoleResponseDto;
import com.mabsplace.mabsplaceback.domain.services.RoleService;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleService roleService;
  private final RoleMapper mapper;
  private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

  public RoleController(RoleService roleService, RoleMapper mapper) {
    this.roleService = roleService;
    this.mapper = mapper;
  }

  @PostMapping
  public ResponseEntity<RoleResponseDto> createRole(@RequestBody RoleRequestDto roleRequestDto) {
    logger.info("Creating role with request: {}", roleRequestDto);
    Role createdRole = roleService.createRole(roleRequestDto);
    logger.info("Created role: {}", mapper.toDto(createdRole));
    return new ResponseEntity<>(mapper.toDto(createdRole), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
    logger.info("Fetching role with ID: {}", id);
    Role role = roleService.getRoleById(id);
    if (role != null) {
      logger.info("Fetched role: {}", role);
      return ResponseEntity.ok(mapper.toDto(role));
    }
    logger.warn("Role not found with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @GetMapping
  public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
    logger.info("Fetching all roles");
    List<Role> roles = roleService.getAllRoles();
    logger.info("Fetched {} roles", roles.size());
    return ResponseEntity.ok(mapper.toDtoList(roles));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RoleResponseDto> updateRole(@PathVariable Long id, @RequestBody RoleRequestDto updatedRole) {
    logger.info("Updating role with ID: {}, Request: {}", id, updatedRole);
    Role role = roleService.updateRole(id, updatedRole);
    if (role != null) {
      logger.info("Updated role successfully: {}", role);
      return ResponseEntity.ok(mapper.toDto(role));
    }
    logger.warn("Role not found with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
    logger.info("Deleting role with ID: {}", id);
    roleService.deleteRole(id);
    logger.info("Deleted role successfully with ID: {}", id);
    return ResponseEntity.noContent().build();
  }

}
