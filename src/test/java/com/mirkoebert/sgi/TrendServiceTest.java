package com.mirkoebert.sgi;

import com.mirkoebert.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrendServiceTest {

    @Mock
    private SingleTestResultRepository resultRepository;

    private TrendService cut;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        cut = new TrendService(resultRepository, messageSource);
    }

    private static SingleTestResultEntity entity(LocalDate date, int hcp) {
        return SingleTestResultEntity.builder()
                .date(date).points(10).testType(TestSuite.SGI).testId(1).hcp(hcp).build();
    }

    @Test
    void getTrend_notEnoughData_whenFewerThan4Results() {
        when(resultRepository.findTop4ByUserIdAndTestIdOrderByDateDesc("u1", 1)).thenReturn(List.of(
                entity(LocalDate.of(2026, 1, 3), 10),
                entity(LocalDate.of(2026, 1, 2), 10),
                entity(LocalDate.of(2026, 1, 1), 10)
        ));

        assertThat(cut.getTrend(1, "u1")).isEqualTo("not enough data available");
    }

    @Test
    void getTrend_delegatesToGetTrendFromList_whenAtLeast4Results() {
        when(resultRepository.findTop4ByUserIdAndTestIdOrderByDateDesc("u2", 1)).thenReturn(List.of(
                entity(LocalDate.of(2026, 1, 4), 10),
                entity(LocalDate.of(2026, 1, 3), 15),
                entity(LocalDate.of(2026, 1, 2), 15),
                entity(LocalDate.of(2026, 1, 1), 15)
        ));

        assertThat(cut.getTrend(1, "u2")).isEqualTo("improving");
    }

    @Test
    void getTrendFromList_improving_whenLatestHcpLowerThanPreviousAverage() {
        List<SingleTestResultEntity> results = List.of(
                entity(LocalDate.of(2026, 1, 4), 10),
                entity(LocalDate.of(2026, 1, 3), 15),
                entity(LocalDate.of(2026, 1, 2), 15),
                entity(LocalDate.of(2026, 1, 1), 15)
        );

        assertThat(cut.getTrendFromList(results)).isEqualTo("improving");
    }

    @Test
    void getTrendFromList_worsening_whenLatestHcpHigherThanPreviousAverage() {
        List<SingleTestResultEntity> results = List.of(
                entity(LocalDate.of(2026, 1, 4), 15),
                entity(LocalDate.of(2026, 1, 3), 10),
                entity(LocalDate.of(2026, 1, 2), 10),
                entity(LocalDate.of(2026, 1, 1), 10)
        );

        assertThat(cut.getTrendFromList(results)).isEqualTo("worsening");
    }

    @Test
    void getTrendFromList_stable_whenLatestWithinEpsilonOfPreviousAverage() {
        List<SingleTestResultEntity> results = List.of(
                entity(LocalDate.of(2026, 1, 4), 10),
                entity(LocalDate.of(2026, 1, 3), 10),
                entity(LocalDate.of(2026, 1, 2), 10),
                entity(LocalDate.of(2026, 1, 1), 10)
        );

        assertThat(cut.getTrendFromList(results)).isEqualTo("stable");
    }
}
