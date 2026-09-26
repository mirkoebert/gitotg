package com.mirkoebert.sgi;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SgiPrimaryControllerIT {

    private static final String TEST_USER = "round-test-user";
    @MockitoBean
    private CurrentUserService currentUserService;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void seedOneRowPerExport() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser()).thenReturn(new CurrentUser(TEST_USER, "T. Ester", "t.ester@ebert-p.com", null));
    }

    @Test
    void getShortGameIndex() {
    }

    @ParameterizedTest
    @CsvSource({
            "1, pitch_50yd_top_view.png, Top view of a 50-yard wedge shot onto the green, Draufsicht: 50-Yard-Wedge auf das Grün",
            "2, pitch_30yd_top_view.png, Top view of a 30-yard wedge shot onto the green, Draufsicht: 30-Yard-Wedge auf das Grün",
            "3, bunker_8yd_top_view.png, Top view of an 8-yard sand shot from the bunker onto the green, Draufsicht: 8-Yard-Sandschlag aus dem Bunker auf das Grün",
            "4, bunker_15yd_top_view.png, Top view of a 15-yard sand shot from the bunker onto the green, Draufsicht: 15-Yard-Sandschlag aus dem Bunker auf das Grün",
            "5, fringe_10yd_top_view.png, Top view of a 10-yard chip from the fringe onto the green, Draufsicht: 10-Yard-Chip vom Vorgrün auf das Grün",
            "6, rough_20yd_top_view.png, Top view of a 20-yard chip from the light rough onto the green, Draufsicht: 20-Yard-Chip aus dem leichten Rough auf das Grün",
            "7, fairway_15yd_top_view.png, Top view of a 15-yard pitch from the fairway onto the green, Draufsicht: 15-Yard-Pitch vom Fairway auf das Grün",
            "8, lob_over_bunker_top_view.png, Top view of a 15-yard pitch over a bunker onto the green, Draufsicht: 15-Yard-Pitch über den Bunker auf das Grün"
    })
    void getShortGameInput(int testId, String diagram, String altEn, String altDe) throws Exception {
        mockMvc.perform(get("/sgi/" + testId).param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("src=\"/images/" + diagram + "\"")))
                .andExpect(content().string(containsString(altEn)))
                .andExpect(content().string(containsString("class=\"sgi-diagram\"")));

        mockMvc.perform(get("/sgi/" + testId).param("lang", "de"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("src=\"/images/" + diagram + "\"")))
                .andExpect(content().string(containsString(altDe)));

        int otherId = testId == 1 ? 8 : 1;
        String otherDiagram = testId == 1 ? "lob_over_bunker_top_view.png" : "pitch_50yd_top_view.png";
        mockMvc.perform(get("/sgi/" + otherId))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(diagram))));
        mockMvc.perform(get("/sgi/" + testId))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(otherDiagram))));
    }

    @SneakyThrows
    @Test
    @Disabled
    void submitForm() {
        // TODO fix NPE creating a model for the return
        final MockHttpServletResponse response = mockMvc.perform(post("/submit").param("roundId", "1"))
                .andExpect(status().isFound()) // 302
                .andReturn()
                .getResponse();
    }
}