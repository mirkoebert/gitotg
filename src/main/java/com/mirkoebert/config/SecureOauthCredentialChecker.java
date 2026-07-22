package com.mirkoebert.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Logs whether configured OAuth2 client registrations have a client-id at startup.
 */
@Service
@Slf4j
public class SecureOauthCredentialChecker {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri:}")
    private String githubRedirectUri;

    @PostConstruct
    public void checkCredentials() {
        logRegistration("google", googleClientId, googleRedirectUri);
        logRegistration("github", githubClientId, githubRedirectUri);
    }

    private void logRegistration(String registrationId, String clientId, String redirectUri) {
        if (clientId == null || clientId.isBlank()) {
            log.error("OAuth2 client-id is missing for registration '{}'", registrationId);
            return;
        }
        String preview = clientId.substring(0, Math.min(4, clientId.length())) + "...";
        log.info("OAuth2 registration '{}' client-id loaded: {}", registrationId, preview);
        if (redirectUri != null && !redirectUri.isBlank()) {
            log.info("OAuth2 registration '{}' redirect-uri: {}", registrationId, redirectUri);
        }
    }
}
