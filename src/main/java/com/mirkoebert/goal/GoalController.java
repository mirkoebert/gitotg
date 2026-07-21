package com.mirkoebert.goal;

import com.mirkoebert.checklist.ChecklistProgress;
import com.mirkoebert.checklist.ChecklistService;
import com.mirkoebert.checklist.GolfCheckListItem;
import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GoalController {

        private final ChecklistService checklistService;
        private final CurrentUserService currentUserService;

        @GetMapping("/goal/{goalSlug}")
        public String getGoal(
                @PathVariable String goalSlug,
                @RequestParam(value = "saved", required = false) Boolean saved,
                Model model) {
                GoalEnum goal = GoalEnum.fromSlug(goalSlug)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown goal: " + goalSlug));

                log.info("page getGoal {}", goal);
                val user = currentUserService.getCurrentUser();
                String userId = user.id();
                log.info("for user {}", userId);

                final List<GolfCheckListItem> allOptions = checklistService.getForGoal(goal);
                final List<Long> selectedIds = checklistService.getSelectedItemIds(userId, goal);
                final ChecklistProgress progress = ChecklistProgress.of(selectedIds.size(), allOptions.size());

                MyForm form = new MyForm();
                form.setSelectedOptions(new ArrayList<>(selectedIds));

                model.addAttribute("myForm", form);
                model.addAttribute("allOptions", allOptions);
                model.addAttribute("progress", progress);
                model.addAttribute("goal", goal);
                model.addAttribute("goalTitle", goal.getFull());
                model.addAttribute("goalSlug", goal.getSlug());
                model.addAttribute("saved", Boolean.TRUE.equals(saved));
                return "goal/break100";
        }

        @PostMapping("/goal/{goalSlug}/save")
        public String save(
                @PathVariable String goalSlug,
                @ModelAttribute MyForm myForm,
                RedirectAttributes redirectAttributes) {
                GoalEnum goal = GoalEnum.fromSlug(goalSlug)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown goal: " + goalSlug));

                val user = currentUserService.getCurrentUser();
                checklistService.saveSelected(user.id(), goal, myForm.getSelectedOptions());
                log.info("save goal {} options={}", goal, myForm.getSelectedOptions());
                redirectAttributes.addAttribute("saved", true);
                return "redirect:/goal/" + goal.getSlug();
        }

}
