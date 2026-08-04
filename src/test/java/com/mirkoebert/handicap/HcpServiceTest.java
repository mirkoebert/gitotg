package com.mirkoebert.handicap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HcpServiceTest {

    @Mock
    private HcpRepository repo;

    private HcpService cut;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        cut = new HcpService(repo, messageSource);
        Locale.setDefault(Locale.ENGLISH);
    }

    private static HcpScoreEntity entity(LocalDate date, double hcp) {
        return HcpScoreEntity.builder().date(date).hcp(hcp).build();
    }

    @Test
    void findLatestByUserId_returnsNotEnoughData_whenNoRecords() {
        when(repo.findFirstByUserIdOrderByDateDesc("u1")).thenReturn(Optional.empty());

        HcpScoreOutFormatedDTO result = cut.findLatestByUserId("u1");

        assertThat(result.getHcp()).isEqualTo("not enough data available");
        assertThat(result.getDate()).isEqualTo("not enough data available");
        assertThat(result.getTrend()).isEqualTo("not enough data available");
    }

    @Test
    void findLatestByUserId_formatsLatestScoreAndTrend_whenRecordExists() {
        HcpScoreEntity latest = entity(LocalDate.of(2026, 1, 10), 10.0);
        when(repo.findFirstByUserIdOrderByDateDesc("u2")).thenReturn(Optional.of(latest));
        when(repo.findTop4ByUserIdOrderByDateDesc("u2")).thenReturn(List.of(
                latest,
                entity(LocalDate.of(2026, 1, 7), 15.0),
                entity(LocalDate.of(2026, 1, 4), 15.0),
                entity(LocalDate.of(2026, 1, 1), 15.0)
        ));

        HcpScoreOutFormatedDTO result = cut.findLatestByUserId("u2");

        assertThat(result.getHcp()).isEqualTo("10.0");
        assertThat(result.getDate()).isEqualTo("10. January 2026");
        assertThat(result.getTrend()).isEqualTo("improving");
    }

    @Test
    void getTrend_notEnoughData_whenFewerThan4Scores() {
        List<HcpScoreEntity> scores = List.of(
                entity(LocalDate.of(2026, 1, 3), 10.0),
                entity(LocalDate.of(2026, 1, 2), 10.0),
                entity(LocalDate.of(2026, 1, 1), 10.0)
        );

        assertThat(cut.getTrend(scores)).isEqualTo("not enough data available");
    }

    @Test
    void getTrend_notEnoughData_whenNull() {
        assertThat(cut.getTrend(null)).isEqualTo("not enough data available");
    }

    @Test
    void getTrend_improving_whenLatestHcpLowerThanPreviousAverage() {
        // lower hcp is better: latest well below the average of the previous 3 -> improving
        List<HcpScoreEntity> scores = List.of(
                entity(LocalDate.of(2026, 1, 4), 10.0),
                entity(LocalDate.of(2026, 1, 3), 15.0),
                entity(LocalDate.of(2026, 1, 2), 15.0),
                entity(LocalDate.of(2026, 1, 1), 15.0)
        );

        assertThat(cut.getTrend(scores)).isEqualTo("improving");
    }

    @Test
    void getTrend_worsening_whenLatestHcpHigherThanPreviousAverage() {
        List<HcpScoreEntity> scores = List.of(
                entity(LocalDate.of(2026, 1, 4), 15.0),
                entity(LocalDate.of(2026, 1, 3), 10.0),
                entity(LocalDate.of(2026, 1, 2), 10.0),
                entity(LocalDate.of(2026, 1, 1), 10.0)
        );

        assertThat(cut.getTrend(scores)).isEqualTo("worsening");
    }

    @Test
    void getTrend_stable_whenLatestWithinEpsilonOfPreviousAverage() {
        List<HcpScoreEntity> scores = List.of(
                entity(LocalDate.of(2026, 1, 4), 10.0),
                entity(LocalDate.of(2026, 1, 3), 10.0),
                entity(LocalDate.of(2026, 1, 2), 10.0),
                entity(LocalDate.of(2026, 1, 1), 10.0)
        );

        assertThat(cut.getTrend(scores)).isEqualTo("stable");
    }

    @Test
    void getTrend_sortsUnorderedInputByDateDescBeforeComparing() {
        // deliberately out of order; latest by date is the last element here
        List<HcpScoreEntity> scores = List.of(
                entity(LocalDate.of(2026, 1, 1), 15.0),
                entity(LocalDate.of(2026, 1, 3), 15.0),
                entity(LocalDate.of(2026, 1, 4), 10.0),
                entity(LocalDate.of(2026, 1, 2), 15.0)
        );

        assertThat(cut.getTrend(scores)).isEqualTo("improving");
    }
}
