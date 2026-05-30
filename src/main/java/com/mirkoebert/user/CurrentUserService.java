package com.mirkoebert.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CurrentUserService {

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            throw new IllegalStateException("No authenticated OAuth2 user found in security context");
        }

        final OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        log.info("Current user: {}", oauth2User.getAttributes());
        return CurrentUser.from(oauth2User);
    }
    
}
