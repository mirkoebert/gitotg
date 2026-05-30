package com.mirkoebert.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    /**
     * Returns the current user's Google "sub" ID from the security context.
     * Use this when you don't have the principal injected as a parameter.
     */
    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalStateException("No authenticated OAuth2 user found in security context");
        }

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        return getUserId(principal);
    }

    /**
     * Returns the user's Google "sub" ID from a provided OAuth2User.
     * Useful when the principal is already injected as a method parameter.
     */
    public String getUserId(OAuth2User principal) {
        if (principal == null) {
            throw new IllegalArgumentException("OAuth2User principal cannot be null");
        }
        Object sub = principal.getAttributes().get("sub");
        if (sub == null) {
            throw new IllegalStateException("Could not extract 'sub' from OAuth2User attributes");
        }
        return (String) sub;
    }

    /**
     * Returns the user's display name (if available).
     */
    public String getName(OAuth2User principal) {
        if (principal == null) return null;
        return (String) principal.getAttributes().get("name");
    }

    /**
     * Returns the user's email (if available).
     */
    public String getEmail(OAuth2User principal) {
        if (principal == null) return null;
        return (String) principal.getAttributes().get("email");
    }
}
