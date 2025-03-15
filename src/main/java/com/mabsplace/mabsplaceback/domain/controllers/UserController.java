package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.PasswordChangeDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.services.UserService;
import com.mabsplace.mabsplaceback.utils.PageDto;
import com.mabsplace.mabsplaceback.utils.PaginationUtils;
import com.mabsplace.mabsplaceback.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, UserMapper mapper, SubscriptionMapper subscriptionMapper) {
        this.userService = userService;
        this.mapper = mapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    @PostMapping("/{userId}/image")
    public ResponseEntity<String> uploadImage(@PathVariable("userId") long userId, @RequestParam("file") MultipartFile file) {
        logger.info("Uploading image for user ID: {}", userId);
        try {
            userService.uploadImage(userId, Utils.generateUniqueName2(userId, "User"), file.getInputStream(), file.getContentType());
            logger.info("Image uploaded successfully for user ID: {}", userId);
            return ResponseEntity.ok("Image uploaded successfully");
        } catch (IOException e) {
            logger.error("Failed to upload image for user ID: {}", userId, e);
            return ResponseEntity.status(500).body("Failed to upload image");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        UserResponseDto user = mapper.toDto(userService.getById(id));
        logger.info("Fetched user: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/subscriptions")
    public ResponseEntity<List<SubscriptionResponseDto>> getSubscriptionsByUserId(@PathVariable Long id) {
        logger.info("Fetching subscriptions for user ID: {}", id);
        List<SubscriptionResponseDto> subscriptions = subscriptionMapper.toDtoList(userService.getSubscriptionsByUserId(id));
        logger.info("Fetched {} subscriptions for user ID: {}", subscriptions.size(), id);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping
    public ResponseEntity<PageDto<UserResponseDto>> getAPaginatedUsers(Pageable pageable) {
        logger.info("Fetching paginated users with pageable: {}", pageable);
        PageDto<UserResponseDto> paginatedUsers = PaginationUtils.convertEntityPageToDtoPage(
                userService.getPaginatedUsers(pageable), mapper::toDtoList
        );
        logger.info("Fetched paginated users: {}", paginatedUsers);
        return ResponseEntity.ok(paginatedUsers);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        logger.info("Fetched {} users", users.size());
        return new ResponseEntity<>(mapper.toDtoList(users), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserRequestDto updatedUser) {
        logger.info("Updating user with ID: {}, Request: {}", id, updatedUser);
        User updated = userService.updateUser(id, updatedUser);
        if (updated != null) {
            logger.info("Updated user successfully: {}", updated);
            return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
        }
        logger.warn("User not found with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        logger.info("Deleted user successfully with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{username}/password")
    public ResponseEntity<?> changePassword(@PathVariable String username, @RequestBody PasswordChangeDto passwordChangeDto) {
        logger.info("Password change requested for username: {}", username);
        boolean isPasswordChanged = userService.changePassword(username, passwordChangeDto.getOldPassword(), passwordChangeDto.getNewPassword());
        if (isPasswordChanged) {
            logger.info("Password changed successfully for username: {}", username);
            return ResponseEntity.ok("Password changed successfully");
        } else {
            logger.warn("Password change failed for username: {}, incorrect old password", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Old password is incorrect");
        }
    }
}
