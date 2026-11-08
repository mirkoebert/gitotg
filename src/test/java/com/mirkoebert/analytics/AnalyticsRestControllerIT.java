package com.mirkoebert.analytics;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AnalyticsRestControllerIT {

    private static final String USER = "analytics-api-user";
    private static final String OTHER_USER = "analytics-api-other";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private HcpRepository hcpRepository;
    @MockitoBean
    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void loggedInUserWithAHighHandicap() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser()).thenReturn(new CurrentUser(USER, "Tester", "tester@example.com", null));
        cleanup();
        save(USER, 25.0);
        save(OTHER_USER, 5.0);
    }

    @AfterEach
    void cleanup() {
        hcpRepository.findByUserId(USER).forEach(hcpRepository::delete);
        hcpRepository.findByUserId(OTHER_USER).forEach(hcpRepository::delete);
    }

    @Test
    void weaknesses_returnsTheSignedInUsersList() throws Exception {
        mockMvc.perform(get("/api/analytics")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4)).andExpect(jsonPath("$[0]").value("course management")).andExpect(jsonPath("$[1]").value("mental game")).andExpect(jsonPath("$[2]").value("playing")).andExpect(jsonPath("$[3]").value("short game"));
    }

    private void save(final String userId, final double handicap) {
        hcpRepository.save(HcpScoreEntity.builder().userId(userId).date(LocalDate.now()).hcp(handicap).build());
    }
}
