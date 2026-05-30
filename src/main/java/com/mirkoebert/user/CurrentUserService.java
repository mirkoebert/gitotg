package com.mirkoebert.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    /**
     * Returns the current user's Google "sub" ID from the security context.
     * Use this when you don't have the oauth2User injected as a parameter.
     */
    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalStateException("No authenticated OAuth2 user found in security context");
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        return getUserId(oauth2User);
    }

    /**
     * Returns the user's Google "sub" ID from a provided OAuth2User.
     * Useful when the oauth2User is already injected as a method parameter.
     */
    public String getUserId(final OAuth2User oauth2User) {
        if (oauth2User == null) {
            throw new IllegalArgumentException("OAuth2User cannot be null");
        }
        Object sub = oauth2User.getAttributes().get("sub");
        if (sub == null) {
            throw new IllegalStateException("Could not extract 'sub' from OAuth2User attributes");
        }
        return (String) sub;
    }

    /**
     * Returns the user's display name (if available).
     */
    public String getName(final OAuth2User oauth2User) {
        if (oauth2User == null) return null;
        return (String) oauth2User.getAttributes().get("name");
    }

    /**
     * Returns the user's email (if available).
     */
    public String getEmail(final OAuth2User oauth2User) {
        if (oauth2User == null) return null;
        return (String) oauth2User.getAttributes().get("email");
    }
}
