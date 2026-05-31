package com.mirkoebert.handicap;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        private final CurrentUserService currentUserService;

        @GetMapping({"/handicap/input"})
        public String getInputPage(final Model model){
                log.info("Get hcp page");
                val u = currentUserService.getCurrentUser();
                model.addAttribute("lastResult", hcpService.findLatestByUserId(u.id()));
                model.addAttribute("hcpScore", new HcpScoreDTO());
                return "hcp/input";
        }

        @PostMapping("/handicap/submit")
        public String submitForm(
                @ModelAttribute("hcpScore") @Valid final HcpScoreDTO score,
                BindingResult bindingResult,
                final Model model
        ) {
                val u = currentUserService.getCurrentUser();

                if (bindingResult.hasErrors()) {
                        model.addAttribute("lastResult", hcpService.findLatestByUserId(u.id()));
                        return "hcp/input";
                }

                try {
                        log.info("Input processing: date {}, hcp {}", score.getSelectedDate(), score.getHcp());
                        val he = HcpScoreEntity
                                .builder()
                                .hcp(score.getHcp())
                                .date(score.getSelectedDate())
                                .userId(u.id())
                                .build();
                        repo.save(he);
                } catch (Exception e) {
                        log.warn("Can't process hcp {}", score, e);
                }
                model.addAttribute("lastResult", hcpService.findLatestByUserId(u.id()));
                model.addAttribute("hcpScore", new HcpScoreDTO());
                return "hcp/input";
        }

}
