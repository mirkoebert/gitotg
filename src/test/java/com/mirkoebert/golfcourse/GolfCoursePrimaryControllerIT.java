package com.mirkoebert.golfcourse;

import com.mirkoebert.TestSuite;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class GolfCoursePrimaryControllerIT {

    private static final String TEST_USER = "round-test-user";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockitoBean
    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void seedOneRowPerExport() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser()).thenReturn(new CurrentUser(TEST_USER, "T. Ester", "t.ester@ebert-p.com", null));
    }
    
    @Test
    void submitForm() {
        // TODO
    }

    @SneakyThrows
    @Test
    void index_rendersImportForm() {
        String html = mockMvc.perform(get("/golfcourse"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("id=\"golfcourse-import-form\"");
        assertThat(html).contains("/api/golfcourse/import");
        assertThat(html).contains("name=\"file\"");
        assertThat(html).contains("csv-import.js");
    }

    @SneakyThrows
    @Test
    void deleteRound() {
        mockMvc.perform(post("/golfcourse/delete").param("roundId", "1"))
                .andExpect(status().isFound()) // 302
                .andExpect(redirectedUrl("/golfcourse"));
    }
}