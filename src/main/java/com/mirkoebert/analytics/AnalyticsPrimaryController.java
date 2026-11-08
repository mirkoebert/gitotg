package com.mirkoebert.analytics;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AnalyticsPrimaryController {

    private final AnalyticsService analyticsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/analytics")
    public String page(final Model model) {
        String userId = currentUserService.getCurrentUser().id();
        model.addAttribute("weaknesses", analyticsService.weaknesses(userId));
        return "analytics/index";
    }
}
