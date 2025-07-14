package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.UserProfile;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.repositories.UserProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.minio.MinioService;
import com.mabsplace.mabsplaceback.utils.PromoCodeGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PromoCodeGenerator promoCodeGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper mapper, MinioService minioService, 
                      UserProfileRepository userProfileRepository, PromoCodeGenerator promoCodeGenerator) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.minioService = minioService;
        this.userProfileRepository = userProfileRepository;
        this.promoCodeGenerator = promoCodeGenerator;
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
        logger.info("Updating user with ID: {}", id);
        User target = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Validate unique constraints before updating
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(target.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + updatedUser.getEmail());
            }
        }
        
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(target.getUsername())) {
            if (userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + updatedUser.getUsername());
            }
        }
        
        if (updatedUser.getPhonenumber() != null && !updatedUser.getPhonenumber().equals(target.getPhonenumber())) {
            if (userRepository.existsByPhonenumber(updatedUser.getPhonenumber())) {
                throw new IllegalArgumentException("Phone number already exists: " + updatedUser.getPhonenumber());
            }
        }
        
        // Store the original referral code to preserve it
        String originalReferralCode = target.getReferralCode();
        
        User updated = mapper.partialUpdate(updatedUser, target);
        
        // Handle password encoding if password is provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            logger.info("Encoding new password for user ID: {}", id);
            updated.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        
        if (updatedUser.getProfileName() != null && !updatedUser.getProfileName().isEmpty()) {
            UserProfile defaultProfile = userProfileRepository.findByName(updatedUser.getProfileName()).orElseThrow(() -> new EntityNotFoundException("Profile not found"));
            updated.setUserProfile(defaultProfile);
        }
        
        // Handle referral code properly - preserve the user's own referral code
        // The referralCode in the request represents the user's own code, not a referrer lookup
        if (updatedUser.getReferralCode() != null && !updatedUser.getReferralCode().isEmpty()) {
            // If a referral code is provided, update the user's own referral code
            logger.info("Updating user's own referral code from {} to {}", originalReferralCode, updatedUser.getReferralCode());
            updated.setReferralCode(updatedUser.getReferralCode());
        } else if (originalReferralCode != null) {
            // If no referral code provided but user had one, preserve the original
            logger.info("Preserving user's original referral code: {}", originalReferralCode);
            updated.setReferralCode(originalReferralCode);
        }
        
        return userRepository.save(updated);
    }

    /**
     * Sets a referrer for a user using a referral code
     * This is separate from regular user updates to avoid confusion
     */
    @Transactional
    public User setUserReferrer(Long userId, String referrerCode) throws EntityNotFoundException {
        logger.info("Setting referrer for user ID: {} using referral code: {}", userId, referrerCode);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        User referrer = userRepository.findByReferralCode(referrerCode)
                .orElseThrow(() -> new EntityNotFoundException("Referrer not found with code: " + referrerCode));

        // Prevent self-referral
        if (referrer.getId().equals(user.getId()) ||
            referrer.getUsername().equals(user.getUsername()) ||
            referrer.getEmail().equals(user.getEmail())) {
            logger.warn("User {} attempted to use their own referral code", user.getUsername());
            throw new IllegalArgumentException("Cannot use your own referral code");
        }

        // Check if the user already has a referrer and remove from the referrer's referrals
        if (user.getReferrer() != null) {
            user.getReferrer().getReferrals().remove(user);
        }

        user.setReferrer(referrer);
        logger.info("User {} is now referred by {}", user.getUsername(), referrer.getUsername());
        
        return userRepository.save(user);
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

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("User not found with username: {}", username);
                return new EntityNotFoundException("User not found with username: " + username);
            });
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
    
    /**
     * Generates a unique referral code for a user
     * @param user The user to generate a referral code for
     * @return The generated referral code
     */
    @Transactional
    public String generateReferralCode(User user) {
        logger.info("Generating referral code for user ID: {}", user.getId());
        
        // Check if user already has a referral code
        if (user.getReferralCode() != null && !user.getReferralCode().isEmpty()) {
            logger.info("User already has a referral code: {}", user.getReferralCode());
            return user.getReferralCode();
        }
        
        String referralCode;
        boolean isUnique;
        
        // Generate a unique referral code
        do {
            referralCode = promoCodeGenerator.generateReferralCode();
            isUnique = !userRepository.existsByReferralCode(referralCode);
        } while (!isUnique);
        
        user.setReferralCode(referralCode);
        userRepository.save(user);
        
        logger.info("Generated referral code for user ID {}: {}", user.getId(), referralCode);
        return referralCode;
    }
    
    /**
     * Generates referral codes for all users who don't have one yet
     * @return The number of referral codes generated
     */
    @Transactional
    public int generateMissingReferralCodes() {
        logger.info("Generating missing referral codes for all users");
        
        List<User> usersWithoutReferralCode = userRepository.findAll().stream()
                .filter(user -> user.getReferralCode() == null || user.getReferralCode().isEmpty())
                .toList();
        
        int count = 0;
        for (User user : usersWithoutReferralCode) {
            generateReferralCode(user);
            count++;
        }
        
        logger.info("Generated {} missing referral codes", count);
        return count;
    }
    
    /**
     * Finds a user by their referral code
     * @param referralCode The referral code to look up
     * @return The user with the given referral code, if found
     */
    public Optional<User> findByReferralCode(String referralCode) {
        logger.info("Finding user by referral code: {}", referralCode);
        return userRepository.findByReferralCode(referralCode);
    }
}
