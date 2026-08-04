package com.mirkoebert.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

class CurrentUserServiceTest {

    private final CurrentUserService cut = new CurrentUserService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsMappedUser_whenOAuth2UserAuthenticated() {
        OAuth2User oauth2User = new DefaultOAuth2User(
                createAuthorityList("ROLE_USER"),
                Map.of("sub", "google-sub-1", "name", "Ada Lovelace", "email", "ada@example.com"),
                "sub"
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(oauth2User, null, oauth2User.getAuthorities()));

        CurrentUser result = cut.getCurrentUser();

        assertThat(result.id()).isEqualTo("google-sub-1");
        assertThat(result.name()).isEqualTo("Ada Lovelace");
        assertThat(result.email()).isEqualTo("ada@example.com");
    }

    @Test
    void getCurrentUser_throws_whenNoAuthenticationPresent() {
        SecurityContextHolder.clearContext();

        assertThatIllegalStateException()
                .isThrownBy(cut::getCurrentUser)
                .withMessageContaining("No authenticated OAuth2 user");
    }

    @Test
    void getCurrentUser_throws_whenPrincipalIsNotAnOAuth2User() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("plain-user", "password"));

        assertThatIllegalStateException()
                .isThrownBy(cut::getCurrentUser)
                .withMessageContaining("No authenticated OAuth2 user");
    }
}
