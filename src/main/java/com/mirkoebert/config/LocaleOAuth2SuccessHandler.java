package com.mirkoebert.config;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.UserPreferenceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

/**
 * After OAuth login, applies the user's stored language preference to the session.
 */
@Component
@Slf4j
public class LocaleOAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserPreferenceService userPreferenceService;
    private final LocaleResolver localeResolver;

    public LocaleOAuth2SuccessHandler(
            UserPreferenceService userPreferenceService,
            LocaleResolver localeResolver) {
        this.userPreferenceService = userPreferenceService;
        this.localeResolver = localeResolver;
        setDefaultTargetUrl("/cockpit");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            CurrentUser user = CurrentUser.from(oauth2User);
            if (user != null && user.id() != null) {
                userPreferenceService.findLocale(user.id()).ifPresentOrElse(
                        locale -> {
                            localeResolver.setLocale(request, response, locale);
                            log.debug("Restored language {} for user {}", locale.getLanguage(), user.id());
                        },
                        () -> localeResolver.setLocale(request, response, Locale.ENGLISH)
                );
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
