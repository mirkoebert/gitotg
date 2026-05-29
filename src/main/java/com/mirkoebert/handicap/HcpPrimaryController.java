package com.mirkoebert.handicap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
@Slf4j
public class HcpPrimaryController {

        private final HcpRepository repo;
        private final HcpService hcpService;

        @GetMapping({"/handicap/input"})
        public String getInputPage(
                final Model model,
                @AuthenticationPrincipal final OAuth2User principal
        ) {
                log.info("Get hcp page");
                model.addAttribute("lastResult", hcpService.findLatestByUserId((String) principal.getAttributes().get("sub")));
                model.addAttribute("hcpScore", new HcpScoreDTO());
                return "hcp/input";
        }

        @PostMapping("/handicap/submit")
        public String submitForm(
                @ModelAttribute("hcpScore") final HcpScoreDTO score,
                final Model model,
                @AuthenticationPrincipal final OAuth2User principal
        ) {
                try {
                        log.info("Input processing: date {}, hcp {}", score.getSelectedDate(), score.getHcp());
                        val he = HcpScoreEntity
                                .builder()
                                .hcp(score.getHcp())
                                .date(score.getSelectedDate())
                                .userId((String) principal.getAttributes().get("sub"))
                                .build();
                        repo.save(he);
                } catch (Exception e) {
                        log.warn("Can't process hcp {}", score, e);
                }
                model.addAttribute("lastResult", hcpService.findLatestByUserId((String) principal.getAttributes().get("sub")));
                model.addAttribute("hcpScore", new HcpScoreDTO());
                return "hcp/input";
        }

}
