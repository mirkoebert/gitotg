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

        String pictureUrl = (String) attributes.get("picture");
        if (pictureUrl != null && pictureUrl.contains("=")) {
            pictureUrl = pictureUrl.substring(0, pictureUrl.lastIndexOf("="));
        }

        return new CurrentUser(id, name, email, pictureUrl);
    }

    public boolean hasPicture() {
        return pictureUrl != null && !pictureUrl.isBlank();
    }
}
