package com.mabsplace.mabsplaceback.domain.services;


import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.minio.MinioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final MinioService minioService;

    public UserService(UserRepository userRepository, UserMapper mapper, MinioService minioService) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.minioService = minioService;
    }


    public User getById(Long id) throws EntityNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, UserRequestDto updatedUser) throws EntityNotFoundException {
        User target = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User updated = mapper.partialUpdate(updatedUser, target);
//        updated.setAdditionalFields(updatedUser.getAdditionalFields());
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
        return userRepository.save(user);
    }

    public void uploadImage(long userId, String originalFilename, InputStream inputStream, String contentType) throws RuntimeException {
        User userFound = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        minioService.uploadImage(originalFilename, inputStream, contentType);

        userFound.setImage(originalFilename);
        userRepository.save(userFound);
    }

    public void updateAuthenticationType(String username, String oauth2ClientName) {
        AuthenticationType authType = AuthenticationType.valueOf(oauth2ClientName.toUpperCase());
        userRepository.updateAuthenticationType(username, authType);
    }

    public List<Subscription> getSubscriptionsByUserId(Long id) {
        return userRepository.getSubscriptionsByUserId(id);
    }
}
