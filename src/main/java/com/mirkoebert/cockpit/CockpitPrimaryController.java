package com.mirkoebert.cockpit;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CockpitPrimaryController {

    private final CockpitService cockpitService;
    private final CurrentUserService currentUserService;

    @GetMapping("/cockpit")
    public String getCockpit(Model model) {
        val u = currentUserService.getCurrentUser();
        log.info("cockpit page");
        model.addAttribute("cockpit", cockpitService.load(u.id()));
        return "cockpit";
    }
}
