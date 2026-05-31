package com.mirkoebert.user;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

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

        String id = (String) attributes.get("sub");
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");

        String pictureUrl = cleanGooglePictureUrl((String) attributes.get("picture"));

        return new CurrentUser(id, name, email, pictureUrl);
    }

    /**
     * Google profile picture URLs often end with size/crop parameters like "=s96-c".
     * Stripping everything after the last '=' gives us the full-size image.
     */
    private static String cleanGooglePictureUrl(String url) {
        if (url != null && url.contains("=")) {
            return url.substring(0, url.lastIndexOf("="));
        }
        return url;
    }

}
