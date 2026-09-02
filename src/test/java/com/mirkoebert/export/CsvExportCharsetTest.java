package com.mirkoebert.export;

import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CSV must leave the app as UTF-8 and say so, so a client does not have to guess the encoding.
 */
@SpringBootTest
class CsvExportCharsetTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private CurrentUserService currentUserService;

    private MockMvc mockMvc;

    @BeforeEach
    void loggedIn() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser())
                .thenReturn(new CurrentUser("user-1", "Tester", "tester@example.com", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/handicap/export", "/api/sgi/export", "/api/gmetric/export"})
    void export_declaresUtf8AndEncodesTheBodyAsUtf8(String endpoint) throws Exception {
        var response = mockMvc.perform(get(endpoint))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(response.getContentType()).contains("charset=UTF-8");
        assertThat(response.getCharacterEncoding()).isEqualToIgnoringCase(StandardCharsets.UTF_8.name());
        assertThat(response.getHeader("Content-Disposition")).startsWith("attachment; filename=");
        // the bytes on the wire must be the UTF-8 encoding of the CSV
        assertThat(response.getContentAsByteArray())
                .isEqualTo(response.getContentAsString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8));
    }
}
