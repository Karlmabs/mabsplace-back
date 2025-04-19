package com.mabsplace.mabsplaceback.utils;

import com.mabsplace.mabsplaceback.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("securityExpressionUtil")
public class SecurityExpressionUtil {
    public boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (principal.getUser().getUserProfile() == null ||
            principal.getUser().getUserProfile().getRoles() == null) {
            return false;
        }

        return Arrays.stream(roles)
                .anyMatch(role -> principal.getUser()
                        .getUserProfile()
                        .getRoles()
                        .stream()
                        .anyMatch(r -> r.getName().equals(role)));
    }
}