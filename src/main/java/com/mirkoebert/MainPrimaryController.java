package com.mirkoebert;

import com.mirkoebert.timeline.TimelineRange;
import com.mirkoebert.timeline.TimelineService;
import com.mirkoebert.user.CurrentUserService;
import com.mirkoebert.user.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;


@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
@Slf4j
public class MainPrimaryController {

    private final TimelineService timeService;
    private final CurrentUserService currentUserService;
    private final UserStatsService userStatsService;

    @Value("${app.version:unknown}")
    private String appVersion;

    @GetMapping("/timeline")
    public String getTimeline(Model m,
                              @RequestParam(defaultValue = "last30") String range) {
        log.info("timeline page range={}", range);
        val u = currentUserService.getCurrentUser();
        TimelineRange timelineRange = TimelineRange.fromParam(range);
        m.addAttribute("timeline", timeService.getLatestResults(u.id(), timelineRange));
        m.addAttribute("range", timelineRange.getParam());
        return "timeline";
    }

    @PostMapping("/timeline/delete")
    public String deleteTimelineEntry(@RequestParam GolfType type,
                                      @RequestParam Long id,
                                      @RequestParam(defaultValue = "last30") String range) {
        log.info("Deleting timeline entry: type={}, id={}, range={}", type, id, range);
        val u = currentUserService.getCurrentUser();
        timeService.deleteEntry(type, id, u.id());
        String redirect = UriComponentsBuilder.fromPath("/timeline")
                .queryParam("range", TimelineRange.fromParam(range).getParam())
                .build()
                .toUriString();
        return "redirect:" + redirect;
    }

    @GetMapping("/about")
    public String getAbout(final Model m) {
        log.info("about page");
        m.addAttribute("version", appVersion);
        m.addAttribute("userCount", userStatsService.countUsers());
        return "about";
    }

    @GetMapping("/putting-index")
    public String getPuttingIndex() {
        log.info("putting-index page");
        return "putting-index";
    }

}
