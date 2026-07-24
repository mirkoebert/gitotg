package com.mirkoebert.config;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import com.mirkoebert.user.UserPreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * Persists language changes for authenticated users and restores stored prefs
 * when the session does not yet have a locale.
 */
@Component
@RequiredArgsConstructor
public class LocalePreferenceInterceptor implements HandlerInterceptor {

    static final String LOCALE_SESSION_ATTRIBUTE_NAME =
            SessionLocaleResolver.class.getName() + ".LOCALE";

    private final UserPreferenceService userPreferenceService;
    private final CurrentUserService currentUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String langParam = request.getParameter("lang");
        if (langParam != null && userPreferenceService.isSupported(langParam)) {
            persistIfAuthenticated(langParam);
            return true;
        }

        restoreFromPreferenceIfNeeded(request, response);
        return true;
    }

    private void persistIfAuthenticated(String language) {
        if (!isOAuth2Authenticated()) {
            return;
        }
        try {
            CurrentUser user = currentUserService.getCurrentUser();
            userPreferenceService.saveLanguage(user.id(), language);
        } catch (IllegalStateException ignored) {
            // not authenticated
        }
    }

    private void restoreFromPreferenceIfNeeded(HttpServletRequest request, HttpServletResponse response) {
        if (!isOAuth2Authenticated()) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME) != null) {
            return;
        }
        try {
            CurrentUser user = currentUserService.getCurrentUser();
            userPreferenceService.findLocale(user.id()).ifPresent(locale -> setSessionLocale(request, response, locale));
        } catch (IllegalStateException ignored) {
            // not authenticated
        }
    }

    private void setSessionLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, locale);
        }
    }

    private static boolean isOAuth2Authenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof OAuth2User;
    }
}
