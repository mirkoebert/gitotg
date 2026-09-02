package com.mirkoebert.export;

import com.mirkoebert.TestSuite;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.mirkoebert.user.CurrentUser;
import com.mirkoebert.user.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CSV must leave the app as UTF-8 and say so, so a client does not have to guess the encoding.
 * <p>
 * The user is seeded with one row per export so the endpoints return real CSV - an empty export
 * would make the body assertions vacuous.
 */
@SpringBootTest
class CsvExportCharsetTest {

    private static final String TEST_USER = "csv-export-test-user";
    private static final LocalDate DATE = LocalDate.of(2026, 3, 14);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private HcpRepository hcpRepository;

    @Autowired
    private SingleTestResultRepository singleTestResultRepository;

    @Autowired
    private GMetricRepository gMetricRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    private MockMvc mockMvc;

    static Stream<Arguments> exports() {
        return Stream.of(
                Arguments.of("/api/handicap/export",
                        "\"DATE\",\"HCP\"\n\"2026-03-14\",\"18.4\"\n"),
                Arguments.of("/api/sgi/export",
                        "\"DATE\",\"POINTS\",\"TESTID\",\"TESTTYPE\"\n\"2026-03-14\",\"7\",\"3\",\"SGI\"\n"),
                Arguments.of("/api/gmetric/export",
                        "\"DATE\",\"METRICVALUE\",\"TYPE\"\n\"2026-03-14\",\"4\",\"LOST_BALLS\"\n"));
    }

    @BeforeEach
    void seedOneRowPerExport() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(currentUserService.getCurrentUser())
                .thenReturn(new CurrentUser(TEST_USER, "Tester", "tester@example.com", null));

        cleanup();
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER).date(DATE).hcp(18.4).build());
        singleTestResultRepository.save(SingleTestResultEntity.builder()
                .userId(TEST_USER).date(DATE).points(7).testId(3).testType(TestSuite.SGI).hcp(6).build());
        gMetricRepository.save(GMetricEntity.builder()
                .userId(TEST_USER).date(DATE).metricValue(4).type(GMetricType.LOST_BALLS).build());
    }

    @AfterEach
    void cleanup() {
        hcpRepository.findByUserId(TEST_USER).forEach(hcpRepository::delete);
        singleTestResultRepository.findAllByUserId(TEST_USER).forEach(singleTestResultRepository::delete);
        gMetricRepository.findByUserId(TEST_USER).forEach(gMetricRepository::delete);
    }

    @ParameterizedTest
    @MethodSource("exports")
    void export_returnsTheCsvAsUtf8(String endpoint, String expectedCsv) throws Exception {
        var response = mockMvc.perform(get(endpoint))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // the encoding a client is told to use...
        assertThat(response.getContentType()).contains("charset=UTF-8");
        assertThat(response.getCharacterEncoding()).isEqualToIgnoringCase(StandardCharsets.UTF_8.name());
        assertThat(response.getHeader("Content-Disposition")).startsWith("attachment; filename=");

        // ...and the bytes actually written, against the expected CSV rather than against themselves.
        // Every exported column is a date, a number or an enum name, so this pins the content but
        // cannot yet tell UTF-8 from ISO-8859-1 - the charset assertions above are what guard that.
        assertThat(response.getContentAsByteArray()).isEqualTo(expectedCsv.getBytes(StandardCharsets.UTF_8));
        assertThat(response.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(expectedCsv);
    }
}
