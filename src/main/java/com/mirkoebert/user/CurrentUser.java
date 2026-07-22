package com.mirkoebert.user;

import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Authenticated user view used across the app.
 * Supports Google (OIDC) and GitHub attribute shapes.
 */
public record CurrentUser(
        String id,
        String name,
        String email,
        String pictureUrl
) {

    public static CurrentUser from(OAuth2User oauth2User) {
        if (oauth2User == null) {
            return null;
        }

        Map<String, Object> attributes = oauth2User.getAttributes();

        // Google OIDC: "sub"; GitHub: numeric "id"
        String id = firstNonBlank(
                asString(attributes.get("sub")),
                asString(attributes.get("id")),
                oauth2User.getName()
        );

        // Google: "name"; GitHub: "name" (optional) then "login"
        String name = firstNonBlank(
                asString(attributes.get("name")),
                asString(attributes.get("login")),
                oauth2User.getName()
        );

        String email = asString(attributes.get("email"));

        // Google: "picture"; GitHub: "avatar_url"
        String pictureUrl = firstNonBlank(
                asString(attributes.get("picture")),
                asString(attributes.get("avatar_url"))
        );
        pictureUrl = cleanGooglePictureUrl(pictureUrl);

        return new CurrentUser(id, name, email, pictureUrl);
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value);
        return s.isBlank() ? null : s;
    }

    private static String firstNonBlank(String... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * Google profile picture URLs often end with size/crop parameters like "=s96-c".
     * Only rewrite googleusercontent URLs so GitHub avatars (e.g. {@code ?v=4}) stay intact.
     */
    private static String cleanGooglePictureUrl(String url) {
        if (url != null && url.contains("googleusercontent.com") && url.contains("=")) {
            return url.substring(0, url.lastIndexOf("="));
        }
        return url;
    }

    @Override
    public @NonNull String toString() {
        return "CurrentUser[id=" + id + "]";
    }

}
