package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.mappers.RoleMapper;
import com.mabsplace.mabsplaceback.domain.dtos.role.RoleRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.role.RoleResponseDto;
import com.mabsplace.mabsplaceback.domain.services.RoleService;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleService roleService;
  private final RoleMapper mapper;

  public RoleController(RoleService roleService, RoleMapper mapper) {
    this.roleService = roleService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<RoleResponseDto> createUser(@RequestBody RoleRequestDto roleRequestDto) {
    Role createdRole = roleService.createRole(roleRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdRole), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(roleService.getRoleById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<RoleResponseDto>> getAllUsers() {
    List<Role> roles = roleService.getAllRoles();
    return new ResponseEntity<>(mapper.toDtoList(roles), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<RoleResponseDto> updateUser(@PathVariable Long id, @RequestBody RoleRequestDto updatedRole) {
    Role updated = roleService.updateRole(id, updatedRole);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    roleService.deleteRole(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
