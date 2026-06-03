package com.mirkoebert.sgi;

import com.mirkoebert.TestSuite;
import com.mirkoebert.sgi.calc.PointsToSgiHcpFunction;
import com.mirkoebert.user.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
@Slf4j
public class SgiPrimaryController {

        private final SingleTestResultRepository repo;
        private final PointsToSgiHcpFunction pointsToSgiHcpFunction;
        private final SgiTestRepo sgiTestRepo;
        private final TrendService trendService;
        private final SgiHcpAggregatedService sgiHcpAggregatedService;
        private final CurrentUserService currentUserService;

        @GetMapping("/short-game-index")
        public String getShortGameIndex(Model m) {
                log.info("short-game-index  page");
                val u = currentUserService.getCurrentUser();
                m.addAttribute("lastResult", sgiHcpAggregatedService.getLatestSgiHcpAggregated(u.id()));
                return "sgi/index";
        }

        @GetMapping("/sgi/{testId}")
        public String getShortGameInput(Model m, @PathVariable @Min(1) @Max(8) int testId) {
                log.info("short-game-input {}", testId);
                val u = currentUserService.getCurrentUser();
                m.addAttribute("sgitest", sgiTestRepo.getTestById(testId));
                m.addAttribute("sgitest1score", SgiTestScoreDTO.builder().type(TestSuite.SGI).testId(testId).build());
                m.addAttribute("testId", testId);
                m.addAttribute("trend", trendService.getTrend(testId, u.id()));
                return "sgi/input";
        }

        @PostMapping("/submit")
        public String submitForm(@ModelAttribute @Valid final SgiTestScoreDTO score, Model m) {
                log.info("Submit {}", score);
                val u = currentUserService.getCurrentUser();
                var s = SingleTestResultEntity
                        .builder()
                        .testType(score.getType())
                        .testId(score.getTestId())
                        .date(LocalDate.now())
                        .points(score.getPoints())
                        .hcp(pointsToSgiHcpFunction.apply(score.getTestId(), score.getPoints()))
                        .userId(u.id())
                        .build();
                repo.save(s);
                m.addAttribute("sgitest", sgiTestRepo.getTestById(score.getTestId()));
                m.addAttribute("sgitest1score", SgiTestScoreDTO.builder().type(TestSuite.SGI).testId(score.getTestId()).build());
                m.addAttribute("testId", score.getTestId());
                m.addAttribute("trend", trendService.getTrend(score.getTestId(), u.id()));
                return "sgi/input";
        }

}
