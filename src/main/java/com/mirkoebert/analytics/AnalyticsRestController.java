package com.mirkoebert.analytics;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/api/analytics")
    public List<String> weaknesses() {
        return analyticsService.weaknesses(currentUserService.getCurrentUser().id());
    }
}
