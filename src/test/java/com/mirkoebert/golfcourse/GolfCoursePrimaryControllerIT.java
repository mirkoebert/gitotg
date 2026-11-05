package com.mirkoebert.golfcourse;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
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

    @SneakyThrows
    @Test
    void index_rendersImportForm() {
        String html = mockMvc.perform(get("/golfcourse").param("lang", "en"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("id=\"golfcourse-import-form\"");
        assertThat(html).contains("/api/golfcourse/export");
        assertThat(html).contains("/api/golfcourse/import");
        assertThat(html).contains("name=\"file\"");
        assertThat(html).contains("csv-import.js");
        assertThat(html).contains("<title>Gitotg: Played Rounds</title>");
        assertThat(html).contains(">Played Rounds</h1>");
        assertThat(html).contains(">Golf Course</label>");

        String german = mockMvc.perform(get("/golfcourse").param("lang", "de"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(german).contains("<title>Gitotg: Gespielte Runden</title>");
        assertThat(german).contains(">Gespielte Runden</h1>");
        assertThat(german).contains(">Golfplatz</label>");
    }

    @SneakyThrows
    @Test
    void deleteRound() {
        mockMvc.perform(post("/golfcourse/delete").param("roundId", "1"))
                .andExpect(status().isFound()) // 302
                .andExpect(redirectedUrl("/golfcourse"));
    }
}
