package com.mirkoebert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SuppressWarnings("SameReturnValue")
@Controller
@Slf4j
public class LoginPrimaryController {

        @GetMapping("/login")
        public String loginPage() {
                log.info("Login page");
                return "login";
        }
}
