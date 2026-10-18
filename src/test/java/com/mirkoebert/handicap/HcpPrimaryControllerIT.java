package com.mirkoebert.handicap;

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

@SpringBootTest
class HcpPrimaryControllerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockitoBean
    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void loggedIn() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser())
                .thenReturn(new CurrentUser("hcp-page-user", "Tester", "tester@example.com", null));
    }

    @Test
    void inputPage_bindsImportMessagesInTheSameScript() throws Exception {
        mockMvc.perform(get("/handicap/input").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("csv-import.js")))
                .andExpect(content().string(containsString("id=\"hcp-import-form\"")))
                .andExpect(content().string(containsString(
                        "GitotgCsvImport.bindImportForm('hcp-import-form', 'hcp-import-status'")))
                .andExpect(content().string(containsString("uploading: \"Uploading...\"")))
                .andExpect(content().string(not(containsString("i18nUploading"))));
    }

    @Test
    void inputPage_capsHandicapChartAt54() throws Exception {
        mockMvc.perform(get("/handicap/input"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("y: {beginAtZero: true, max: 54}")));
    }
}
