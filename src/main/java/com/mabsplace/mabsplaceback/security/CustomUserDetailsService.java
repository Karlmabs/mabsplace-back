package com.mabsplace.mabsplaceback.security;


import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        // Normalize input
        String normalizedUsername = username.trim().toLowerCase();

        // Try exact match first
        Optional<User> userOptional = userRepository.findByUsername(normalizedUsername);

        // Fallback to case-insensitive search for legacy users
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsernameIgnoreCase(normalizedUsername);
        }

        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("User", "id", id)
        );

        return UserPrincipal.create(user);
    }
}