package com.mirkoebert.handicap;

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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class HcpPrimaryRestControllerIT {

    private static final String USER = "chart-data-user";
    private static final String OTHER_USER = "chart-data-other";
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM-yyyy");

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private HcpRepository hcpRepository;
    @MockitoBean
    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void loggedInWithTwoUsersOfScores() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser())
                .thenReturn(new CurrentUser(USER, "Tester", "tester@example.com", null));
        cleanup();
        save(USER, YearMonth.now().minusMonths(30), 40.0);
        save(USER, YearMonth.now(), 12.0);
        save(USER, YearMonth.now(), 18.0);
        save(OTHER_USER, YearMonth.now(), 99.0);
    }

    @AfterEach
    void cleanup() {
        hcpRepository.findByUserId(USER).forEach(hcpRepository::delete);
        hcpRepository.findByUserId(OTHER_USER).forEach(hcpRepository::delete);
    }

    @Test
    void chartData_defaultsToTheLastTwoYearsOfTheSignedInUser() throws Exception {
        mockMvc.perform(get("/api/handicap/chart-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels.length()").value(1))
                .andExpect(jsonPath("$.labels[0]").value(MONTH.format(YearMonth.now())))
                .andExpect(jsonPath("$.hcp.length()").value(1))
                .andExpect(jsonPath("$.hcp[0]").value(15.0));
    }

    @Test
    void chartData_allStartsAtTheEarliestMonthAndIgnoresOtherUsers() throws Exception {
        YearMonth earliest = YearMonth.now().minusMonths(30);

        mockMvc.perform(get("/api/handicap/chart-data").param("range", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels.length()").value(31))
                .andExpect(jsonPath("$.labels[0]").value(MONTH.format(earliest)))
                .andExpect(jsonPath("$.labels[30]").value(MONTH.format(YearMonth.now())))
                .andExpect(jsonPath("$.hcp[0]").value(40.0))
                .andExpect(jsonPath("$.hcp[1]").value(nullValue()))
                .andExpect(jsonPath("$.hcp[30]").value(15.0));
    }

    private void save(String userId, YearMonth month, double hcp) {
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(userId)
                .date(month.atDay(1))
                .hcp(hcp)
                .build());
    }
}
