package com.mirkoebert.goal;

import com.mirkoebert.checklist.GolfCheckListItem;
import com.mirkoebert.checklist.GolfCheckListItemRepository;
import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class Break100Controller {

        private final GolfCheckListItemRepository goalRepository;
        private final CurrentUserService currentUserService;

        @GetMapping("/goal/break100")
        public String getBreak100(@AuthenticationPrincipal final OAuth2User oauth2User, Model model) {
                log.info("page getBreak100");
                String userId = currentUserService.getUserId(oauth2User);
                log.info("for user {}", userId);


                MyForm form = new MyForm();
                // form.setSelectedOptions(Arrays.asList("option2")); // pre-select some

                final List<GolfCheckListItem> allOptions = goalRepository.findByGoal(GoalEnum.BREAK100.name());


                model.addAttribute("myForm", form);
                model.addAttribute("allOptions", allOptions);
                return "goal/break100";
        }

        @PostMapping("/goal/break100/save")
        public String save(@ModelAttribute MyForm myForm) {
                log.info("save getBreak100");
                // myForm.getSelectedOptions() contains selected IDs
                return "redirect:/";
        }

}

