package com.mirkoebert;

import com.mirkoebert.advisor.AdvisorService;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.timeline.TimelineService;
import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
                return "user";
        }

        @GetMapping("/timeline")
        public String getTimeline(Model m) {
                log.info("timeline page");
                val u = currentUserService.getCurrentUser();
                m.addAttribute("timeline", timeService.getLatestResults(u.id()));
                return "timeline";
        }

        @PostMapping("/timeline/delete")
        public String deleteTimelineEntry(@RequestParam GolfType type,
                                          @RequestParam Long id) {
                log.info("Deleting timeline entry: type={}, id={}", type, id);
                val u = currentUserService.getCurrentUser();
                timeService.deleteEntry(type, id, u.id());
                return "redirect:/timeline";
        }

        @GetMapping("/about")
        public String getAbout(final Model m) {
                log.info("about page");
                m.addAttribute("version", this.getClass().getPackage().getImplementationVersion());
                return "about";
        }

}
