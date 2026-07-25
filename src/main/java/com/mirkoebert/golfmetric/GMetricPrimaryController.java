package com.mirkoebert.golfmetric;

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

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
@Slf4j
public class GMetricPrimaryController {

        private final GMetricRepository repo;
        private final CurrentUserService currentUserService;

        @GetMapping({"/gmetric", "/golfmetric"})
        public String getPage(final Model model) {
                log.info("Get gmetric page");
                model.addAttribute("gMetric", emptyForm());
                return "gmetric/index";
        }

        @PostMapping("/gmetric/submit")
        public String submitForm(
                        @ModelAttribute("gMetric") @Valid final GMetricDTO form,
                        BindingResult bindingResult,
                        final Model model) {
                val u = currentUserService.getCurrentUser();

                if (bindingResult.hasErrors()) {
                        return "gmetric/index";
                }

                try {
                        log.info("Input processing: date {}, value {}, type {}",
                                        form.getSelectedDate(), form.getMetricValue(), form.getType());
                        val entity = GMetricEntity
                                        .builder()
                                        .date(form.getSelectedDate())
                                        .metricValue(form.getMetricValue())
                                        .type(form.getType())
                                        .userId(u.id())
                                        .build();
                        repo.save(entity);
                } catch (Exception e) {
                        log.warn("Can't process gmetric {}", form, e);
                }

                model.addAttribute("gMetric", emptyForm());
                return "gmetric/index";
        }

        private static GMetricDTO emptyForm() {
                return GMetricDTO.builder()
                                .selectedDate(java.time.LocalDate.now())
                                .metricValue(0)
                                .type(GMetricType.LOST_BALLS)
                                .build();
        }
}
