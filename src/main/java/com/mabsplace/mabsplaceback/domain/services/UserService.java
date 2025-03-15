package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.UserProfile;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.repositories.UserProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.minio.MinioService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final MinioService minioService;
    private final UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper mapper, MinioService minioService, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.minioService = minioService;
        this.userProfileRepository = userProfileRepository;
    }

    public User getById(Long id) throws EntityNotFoundException {
        logger.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            logger.error("User not found with ID: {}", id);
            return new EntityNotFoundException("User not found");
        });
        logger.info("Fetched user successfully: {}", user);
        return user;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, UserRequestDto updatedUser) throws EntityNotFoundException {
        System.out.println("referrerId: " + updatedUser.getReferrerId());
        User target = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User updated = mapper.partialUpdate(updatedUser, target);
        if (updatedUser.getProfileName() != null && !updatedUser.getProfileName().isEmpty()) {
            UserProfile defaultProfile = userProfileRepository.findByName(updatedUser.getProfileName()).orElseThrow(() -> new EntityNotFoundException("Profile not found"));
            updated.setUserProfile(defaultProfile);
        }
        if (updatedUser.getReferrerId() != null) {
            User referrer = userRepository.findById(updatedUser.getReferrerId()).orElseThrow(() -> new EntityNotFoundException("Referrer not found"));

            // Check if the user already has a referrer and remove from the referrer's referrals
            if (updated.getReferrer() != null) {
                updated.getReferrer().getReferrals().remove(updated);
            }

            updated.setReferrer(referrer);
        }
        return userRepository.save(updated);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getPaginatedUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        logger.info("Creating user with data: {}", user);
        User createdUser = userRepository.save(user);
        logger.info("User created successfully: {}", createdUser);
        return createdUser;
    }

    public void uploadImage(long userId, String originalFilename, InputStream inputStream, String contentType) {
        logger.info("Uploading image for user ID: {}", userId);
        User userFound = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("User not found with ID: {}", userId);
            return new EntityNotFoundException("User not found");
        });

        if (userFound.getImage() != null) {
            logger.info("Deleting previous image for user ID: {}", userId);
            minioService.deleteImage(userFound.getImage());
        }

        minioService.uploadImage(originalFilename, inputStream, contentType);
        userFound.setImage(originalFilename);
        userRepository.save(userFound);
        logger.info("Image uploaded successfully for user ID: {}", userId);
    }

    public void updateAuthenticationType(String username, String oauth2ClientName) {
        logger.info("Updating authentication type for username: {}, new authentication type: {}", username, oauth2ClientName);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setAuthType(AuthenticationType.valueOf(oauth2ClientName.toUpperCase()));
            userRepository.save(user);
            logger.info("Updated authentication type successfully for username: {}", username);
        } else {
            logger.warn("User not found with username: {}", username);
        }
    }

    public List<Subscription> getSubscriptionsByUserId(Long id) {
        return userRepository.getSubscriptionsByUserId(id);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Attempting password change for username: {}", username);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent() && passwordEncoder.matches(oldPassword, optionalUser.get().getPassword())) {
            optionalUser.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(optionalUser.get());
            logger.info("Password changed successfully for username: {}", username);
            return true;
        } else {
            logger.warn("Password change failed for username: {}, incorrect old password", username);
            return false;
        }
    }
}
