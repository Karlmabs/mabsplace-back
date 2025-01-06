package com.mabsplace.mabsplaceback.security.config;

import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.security.UserPrincipal;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return hasUserProfileRole(userPrincipal.getUser(), permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || targetType == null || !(permission instanceof String)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return hasUserProfileRole(userPrincipal.getUser(), permission.toString());
    }

    private boolean hasUserProfileRole(User user, String requiredRole) {
        if (user.getUserProfile() == null) {
            return false;
        }

        return user.getUserProfile().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals(requiredRole));
    }
}
