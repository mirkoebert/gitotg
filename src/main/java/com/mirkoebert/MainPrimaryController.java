package com.mirkoebert;

import com.mirkoebert.advisor.AdvisorService;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.timeline.TimelineService;
import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


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
        public String getUser(Model model, @AuthenticationPrincipal OAuth2User principal) {
                final String u = currentUserService.getUserId(principal);
                log.info("user page {}", u);
                model.addAttribute("name", principal.getAttributes().get("name"));
                model.addAttribute("email", principal.getAttributes().get("email"));
                model.addAttribute("lastHCP", hcpService.findLatestByUserId(u).getHcp());
                model.addAttribute("lastSGHCP", sgiHcpAggregatedService.getLatestSgiHcpAggregated(u));
                model.addAttribute("advice", advisorService.getAdvise(u));
                String fullUrl = (String) principal.getAttributes().get("picture");
                String pureUrl = fullUrl.substring(0, fullUrl.lastIndexOf("="));
                model.addAttribute("picture", pureUrl);
                return "user";
        }

        @GetMapping("/timeline")
        public String getTimeline(Model m) {
                m.addAttribute("timeline", timeService.getLatestResults(currentUserService.getUserId()));
                return "timeline";
        }

        @GetMapping("/about")
        public String getAbout(final Model m) {
                log.info("about page");
                m.addAttribute("version", this.getClass().getPackage().getImplementationVersion());
                return "about";
        }

}
