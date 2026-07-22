package com.mirkoebert.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

class CurrentUserTest {

    @Test
    void from_mapsGoogleOidcAttributes() {
        OAuth2User oauth2User = user(Map.of(
                "sub", "google-sub-123",
                "name", "Ada Lovelace",
                "email", "ada@example.com",
                "picture", "https://lh3.googleusercontent.com/a/photo=s96-c"
        ), "sub");

        CurrentUser user = CurrentUser.from(oauth2User);

        assertThat(user.id()).isEqualTo("google-sub-123");
        assertThat(user.name()).isEqualTo("Ada Lovelace");
        assertThat(user.email()).isEqualTo("ada@example.com");
        assertThat(user.pictureUrl()).isEqualTo("https://lh3.googleusercontent.com/a/photo");
    }

    @Test
    void from_mapsGitHubAttributesWithNumericId() {
        OAuth2User oauth2User = user(Map.of(
                "id", 42,
                "login", "octocat",
                "name", "The Octocat",
                "email", "octocat@github.com",
                "avatar_url", "https://avatars.githubusercontent.com/u/42?v=4"
        ), "id");

        CurrentUser user = CurrentUser.from(oauth2User);

        assertThat(user.id()).isEqualTo("42");
        assertThat(user.name()).isEqualTo("The Octocat");
        assertThat(user.email()).isEqualTo("octocat@github.com");
        assertThat(user.pictureUrl()).isEqualTo("https://avatars.githubusercontent.com/u/42?v=4");
    }

    @Test
    void from_fallsBackToLoginWhenGitHubNameMissing() {
        OAuth2User oauth2User = user(Map.of(
                "id", 7,
                "login", "hubot",
                "avatar_url", "https://avatars.githubusercontent.com/u/7?v=4"
        ), "id");

        CurrentUser user = CurrentUser.from(oauth2User);

        assertThat(user.id()).isEqualTo("7");
        assertThat(user.name()).isEqualTo("hubot");
        assertThat(user.email()).isNull();
        assertThat(user.pictureUrl()).isEqualTo("https://avatars.githubusercontent.com/u/7?v=4");
    }

    @Test
    void from_returnsNullForNullPrincipal() {
        assertThat(CurrentUser.from(null)).isNull();
    }

    private static OAuth2User user(Map<String, Object> attributes, String nameAttributeKey) {
        return new DefaultOAuth2User(
                createAuthorityList("ROLE_USER"),
                attributes,
                nameAttributeKey
        );
    }
}
