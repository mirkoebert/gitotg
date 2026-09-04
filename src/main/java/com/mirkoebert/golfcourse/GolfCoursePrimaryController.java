package com.mirkoebert.golfcourse;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GolfCoursePrimaryController {

    private final CourseService courseService;
    private final CurrentUserService currentUserService;

    private static PlayedRoundDto emptyForm() {
        return PlayedRoundDto.builder().selectedDate(LocalDate.now()).build();
    }

    @GetMapping("/golfcourse")
    public String getPage(final Model model) {
        log.info("Get golf course page");
        final CurrentUser u = currentUserService.getCurrentUser();
        model.addAttribute("courses", courseService.findAllCourses());
        model.addAttribute("round", emptyForm());
        model.addAttribute("rounds", courseService.findRoundsForUser(u.id()));
        return "golfcourse/index";
    }

    @PostMapping("/golfcourse/submit")
    public String submitForm(
            @ModelAttribute("round") @Valid final PlayedRoundDto form,
            BindingResult bindingResult,
            final Model model
    ) {
        log.info("Submit golf course results");
        val u = currentUserService.getCurrentUser();
        if (bindingResult.hasErrors()
                || !courseService.submitRound(u.id(), form.getCourseName(), form.getSelectedDate(), form.getHoleStrokes(), form.getLostBalls())) {
            model.addAttribute("error", "golfcourse.error.mismatch");
        }

        model.addAttribute("courses", courseService.findAllCourses());
        model.addAttribute("round", emptyForm());
        model.addAttribute("rounds", courseService.findRoundsForUser(u.id()));
        return "golfcourse/index";
    }

    @PostMapping("/golfcourse/delete")
    public String deleteRound(@RequestParam final long roundId) {
        log.info("Delete golf course result {}", roundId);
        val u = currentUserService.getCurrentUser();
        log.info("Delete played round: roundId {}", roundId);
        courseService.deleteRound(u.id(), roundId);
        return "redirect:/golfcourse";
    }
}
