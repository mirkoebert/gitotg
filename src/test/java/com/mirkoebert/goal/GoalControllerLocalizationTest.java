package com.mirkoebert.goal;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The checklist text lives in the message bundle and is resolved through dynamic message keys
 * ({@code #{__${option.nameKey}__}}), so only a rendered page proves the wiring works.
 */
@SpringBootTest
class GoalControllerLocalizationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private CurrentUserService currentUserService;

    private MockMvc mockMvc;

    @BeforeEach
    void loggedIn() {
        // no security filters - the controller reads the user through CurrentUserService, which is mocked
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser())
                .thenReturn(new CurrentUser("user-1", "Tester", "tester@example.com", null));
    }

    @Test
    void goalPage_rendersTheChecklistInEnglish() throws Exception {
        mockMvc.perform(get("/goal/break100").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Break 100")))
                .andExpect(content().string(containsString("Improve your grip")))
                .andExpect(content().string(containsString("learn the right grip")))
                // an unresolved key would be rendered literally
                .andExpect(content().string(not(containsString("checklist.break100."))));
    }

    @Test
    void goalPage_rendersTheChecklistInGerman() throws Exception {
        mockMvc.perform(get("/goal/break100").param("lang", "de"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Die 100 knacken")))
                .andExpect(content().string(containsString("Griff verbessern")))
                .andExpect(content().string(containsString("lerne den richtigen Griff")))
                .andExpect(content().string(not(containsString("checklist.break100."))));
    }
}
