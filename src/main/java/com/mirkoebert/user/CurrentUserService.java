package com.mirkoebert.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalStateException("No authenticated OAuth2 user found in security context");
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        return getUserId(oauth2User);
    }

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

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalStateException("No authenticated OAuth2 user found in security context");
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        return CurrentUser.from(oauth2User);
    }


}
