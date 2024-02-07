package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.services.UserService;
import com.mabsplace.mabsplaceback.utils.PageDto;
import com.mabsplace.mabsplaceback.utils.PaginationUtils;
import com.mabsplace.mabsplaceback.utils.Utils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;

    private final SubscriptionMapper subscriptionMapper;

    public UserController(UserService userService, UserMapper mapper, SubscriptionMapper subscriptionMapper) {
        this.userService = userService;
        this.mapper = mapper;
      this.subscriptionMapper = subscriptionMapper;
    }

    /*@PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto user) {
        User createdUser = userService.createUser(mapper.toEntity(user));
        return new ResponseEntity<>(mapper.toDto1(createdUser), HttpStatus.CREATED);
    }*/

    @PostMapping("/{userId}/image")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<String> uploadImage(@PathVariable("userId") long userId ,@RequestParam("file") MultipartFile file) {
        try {
            userService.uploadImage(userId, Utils.generateUniqueName2(userId, "User"), file.getInputStream(), file.getContentType());
            return ResponseEntity.ok("Image uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload image");
        }
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(userService.getById(id)));
    }

    @GetMapping("/{id}/subscriptions")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<List<SubscriptionResponseDto>> getSubscriptionsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionMapper.toDtoList(userService.getSubscriptionsByUserId(id)));
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<PageDto<UserResponseDto>> getAPaginatedUsers(Pageable pageable) {
        return ResponseEntity.ok(
                PaginationUtils.convertEntityPageToDtoPage(
                        userService.getPaginatedUsers( pageable), mapper::toDtoList
                ));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(mapper.toDtoList(users), HttpStatus.OK);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserRequestDto updatedUser) {
        User updated = userService.updateUser(id, updatedUser);
        if (updated != null) {
            return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
