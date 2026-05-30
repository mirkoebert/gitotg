package com.mirkoebert;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserPrimaryController {

        private final CurrentUserService currentUserService;

        @GetMapping({"/user", "/home"})
        public Map<String, String> userOauth2CallBack() {
                log.debug("user");
                val u = currentUserService.getCurrentUser();
                val m = Map.of("name", u.name(), "email", u.email(), "pictureUrl", u.pictureUrl());
                return m;
        }
}
