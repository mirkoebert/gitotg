package com.mirkoebert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class UserPrimaryController {

        @GetMapping({"/user", "/home"})
        public Map<String, Object> userOauth2CallBack(@AuthenticationPrincipal final OAuth2User principal) {
                log.debug("user {}", principal.getAttributes());
                return principal.getAttributes();  // Returns Google user info like name, email, picture
        }
}
