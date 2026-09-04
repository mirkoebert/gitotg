package com.mirkoebert.user;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CurrentUserService {

    public @NonNull CurrentUser getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new IllegalStateException("No authenticated OAuth2 user found in security context");
        }

        val u = CurrentUser.from(oauth2User);
        log.debug("Authenticated user: {}", u);
        return u;
    }

}
