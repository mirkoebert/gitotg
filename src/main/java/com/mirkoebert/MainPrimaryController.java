package com.mirkoebert;

import com.mirkoebert.advisor.AdvisorService;
import com.mirkoebert.checklist.ChecklistService;
import com.mirkoebert.goal.GoalEnum;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
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
    private final HcpService hcpService;
    private final SgiHcpAggregatedService sgiHcpAggregatedService;
    private final AdvisorService advisorService;
    private final CurrentUserService currentUserService;
    private final ChecklistService checklistService;
    private final UserStatsService userStatsService;

    @Value("${app.version:unknown}")
    private String appVersion;

    @GetMapping("/user-page")
    public String getUser(Model model) {
        val u = currentUserService.getCurrentUser();
        log.debug("Rendering user page for {}", u);
        model.addAttribute("name", u.name());
        model.addAttribute("email", u.email());
        model.addAttribute("lastHCP", hcpService.findLatestByUserId(u.id()).getHcp());
        model.addAttribute("lastSGHCP", sgiHcpAggregatedService.getLatestSgiHcpAggregated(u.id()));
        model.addAttribute("advice", advisorService.getAdvise(u.id()));
        model.addAttribute("picture", u.pictureUrl());
        model.addAttribute("break100Progress", checklistService.getProgress(u.id(), GoalEnum.BREAK100).percentage());
        model.addAttribute("break90Progress", checklistService.getProgress(u.id(), GoalEnum.BREAK90).percentage());
        model.addAttribute("break80Progress", checklistService.getProgress(u.id(), GoalEnum.BREAK80).percentage());
        return "user";
    }

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
