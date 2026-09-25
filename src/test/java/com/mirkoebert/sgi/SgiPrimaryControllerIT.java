package com.mirkoebert.sgi;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

    @Test
    void getShortGameInput() throws Exception {
        mockMvc.perform(get("/sgi/8").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("src=\"/images/lob_over_bunker_top_view.png\"")))
                .andExpect(content().string(containsString("Top view of a 15-yard pitch over a bunker onto the green")));

        mockMvc.perform(get("/sgi/8").param("lang", "de"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Draufsicht: 15-Yard-Pitch über den Bunker auf das Grün")));

        mockMvc.perform(get("/sgi/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("lob_over_bunker_top_view.png"))));
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