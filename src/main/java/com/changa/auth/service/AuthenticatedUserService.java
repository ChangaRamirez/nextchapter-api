package com.changa.auth.service;

import com.changa.user.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated users found");
        }

        if(!(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Authenticated principal is not a User");
        }

        return user;
    }
}
