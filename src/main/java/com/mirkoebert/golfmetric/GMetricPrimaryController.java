package com.mirkoebert.golfmetric;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SuppressWarnings("SameReturnValue")
@Controller
@Slf4j
public class GMetricPrimaryController {

    @GetMapping({"/gmetric", "/golfmetric"})
    public String getPage() {
        log.info("Get gmetric page");
        return "gmetric/index";
    }
}
