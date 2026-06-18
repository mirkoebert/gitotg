package com.mirkoebert.config;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
@Setter
public class SecureOauthCredentialChecker {

    private String clientId;

    @PostConstruct
    public void checkCredentials() {
        if (clientId == null || clientId.isBlank()) {
            log.warn("Google client-id is missing!");
        }
        log.info("Google OAuth2 client-id loaded successfully: {}", clientId.substring(0, Math.min(4, clientId.length())) + "...");
    }
}
