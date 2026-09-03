package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({TimelineService.class})
class TimelineServiceTest {

    @Autowired
    private TimelineService cut;

    @MockitoBean
    private HcpRepository hcpRepository;

    @MockitoBean
    private SingleTestResultRepository singleTestResultRepository;

    @MockitoBean
    private GMetricRepository gMetricRepository;

    @Test
    void getLatestResults_includesGMetricEntries() {
        when(hcpRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class))).thenReturn(List.of());
        when(singleTestResultRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class)))
                .thenReturn(List.of());
        when(gMetricRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class))).thenReturn(List.of(
                GMetricEntity.builder()
                        .id(7L)
                        .userId("user-123")
                        .date(LocalDate.of(2026, 7, 26))
                        .metricValue(3)
                        .type(GMetricType.LOST_BALLS)
                        .build()
        ));

        List<MeasurementDTO> results = cut.getLatestResults("user-123");

        assertThat(results).hasSize(1);
        MeasurementDTO dto = results.getFirst();
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getType()).isEqualTo(GolfType.COUNT);
        assertThat(dto.getValue()).isEqualTo("3");
        assertThat(dto.getComment()).isEqualTo("Lost Balls");
        assertThat(dto.getDate()).isEqualTo(LocalDate.of(2026, 7, 26));
    }

    @Test
    void getLatestResults_mergesAndLimitsAcrossSourcesByDate() {
        when(hcpRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class))).thenReturn(List.of(
                HcpScoreEntity.builder()
                        .id(1L)
                        .userId("user-123")
                        .date(LocalDate.of(2026, 7, 20))
                        .hcp(15.5)
                        .build()
        ));
        when(singleTestResultRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class)))
                .thenReturn(List.of(
                        SingleTestResultEntity.builder()
                                .id(2L)
                                .userId("user-123")
                                .date(LocalDate.of(2026, 7, 25))
                                .testId(1)
                                .points(5)
                                .hcp(20)
                                .build()
                ));
        when(gMetricRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class))).thenReturn(List.of(
                GMetricEntity.builder()
                        .id(3L)
                        .userId("user-123")
                        .date(LocalDate.of(2026, 7, 26))
                        .metricValue(2)
                        .type(GMetricType.BOGEY_PLUS)
                        .build()
        ));

        List<MeasurementDTO> results = cut.getLatestResults("user-123", TimelineRange.LAST_30);

        assertThat(results).extracting(MeasurementDTO::getType)
                .containsExactly(GolfType.COUNT, GolfType.SGIHCP, GolfType.HCP);
    }

    @Test
    void getLatestResults_last30_limitsTo30Entries() {
        List<HcpScoreEntity> many = IntStream.range(0, 40)
                .mapToObj(i -> HcpScoreEntity.builder()
                        .id((long) i)
                        .userId("user-123")
                        .date(LocalDate.of(2026, 1, 1).plusDays(i))
                        .hcp(10.0 + i)
                        .build())
                .toList();

        when(hcpRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class))).thenReturn(many);
        when(singleTestResultRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class)))
                .thenReturn(List.of());
        when(gMetricRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class)))
                .thenReturn(List.of());

        List<MeasurementDTO> results = cut.getLatestResults("user-123", TimelineRange.LAST_30);

        assertThat(results).hasSize(30);
        assertThat(results.getFirst().getDate()).isEqualTo(LocalDate.of(2026, 1, 1).plusDays(39));
    }

    @Test
    void getLatestResults_all_returnsEverything() {
        List<HcpScoreEntity> many = IntStream.range(0, 40)
                .mapToObj(i -> HcpScoreEntity.builder()
                        .id((long) i)
                        .userId("user-123")
                        .date(LocalDate.of(2026, 1, 1).plusDays(i))
                        .hcp(10.0 + i)
                        .build())
                .toList();

        when(hcpRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class))).thenReturn(many);
        when(singleTestResultRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class)))
                .thenReturn(List.of());
        when(gMetricRepository.findByUserIdOrderByDateDesc(eq("user-123"), any(Pageable.class)))
                .thenReturn(List.of());

        List<MeasurementDTO> results = cut.getLatestResults("user-123", TimelineRange.ALL);

        assertThat(results).hasSize(40);
    }

    @Test
    void deleteEntry_deletesHcpEntryWhenUserOwnsIt() {
        HcpScoreEntity entry = HcpScoreEntity.builder()
                .id(42L)
                .userId("user-123")
                .date(LocalDate.now())
                .hcp(15.5)
                .build();

        when(hcpRepository.findById(42L)).thenReturn(Optional.of(entry));

        cut.deleteEntry(GolfType.HCP, 42L, "user-123");

        verify(hcpRepository).deleteById(42L);
    }

    @Test
    void deleteEntry_doesNotDeleteHcpEntryWhenUserDoesNotOwnIt() {
        HcpScoreEntity entry = HcpScoreEntity.builder()
                .id(42L)
                .userId("user-123")
                .date(LocalDate.now())
                .hcp(15.5)
                .build();

        when(hcpRepository.findById(42L)).thenReturn(Optional.of(entry));

        cut.deleteEntry(GolfType.HCP, 42L, "other-user");

        verify(hcpRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteEntry_deletesSgiEntryWhenUserOwnsIt() {
        SingleTestResultEntity entry = SingleTestResultEntity.builder()
                .id(99L)
                .userId("user-123")
                .date(LocalDate.now())
                .testId(3)
                .points(7)
                .hcp(22)
                .build();

        when(singleTestResultRepository.findById(99L)).thenReturn(Optional.of(entry));

        cut.deleteEntry(GolfType.SGIHCP, 99L, "user-123");

        verify(singleTestResultRepository).deleteById(99L);
    }

    @Test
    void deleteEntry_deletesGMetricEntryWhenUserOwnsIt() {
        GMetricEntity entry = GMetricEntity.builder()
                .id(55L)
                .userId("user-123")
                .date(LocalDate.now())
                .metricValue(4)
                .type(GMetricType.DOUBLE_BOGEY_PLUS)
                .build();

        when(gMetricRepository.findById(55L)).thenReturn(Optional.of(entry));

        cut.deleteEntry(GolfType.COUNT, 55L, "user-123");

        verify(gMetricRepository).deleteById(55L);
    }

    @Test
    void deleteEntry_doesNotDeleteGMetricEntryWhenUserDoesNotOwnIt() {
        GMetricEntity entry = GMetricEntity.builder()
                .id(55L)
                .userId("user-123")
                .date(LocalDate.now())
                .metricValue(4)
                .type(GMetricType.DOUBLE_BOGEY_PLUS)
                .build();

        when(gMetricRepository.findById(55L)).thenReturn(Optional.of(entry));

        cut.deleteEntry(GolfType.COUNT, 55L, "other-user");

        verify(gMetricRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteEntry_doesNothingWhenIdIsNull() {
        cut.deleteEntry(GolfType.HCP, null, "user-123");

        verifyNoInteractions(hcpRepository, singleTestResultRepository, gMetricRepository);
    }

    @Test
    void deleteEntry_doesNothingWhenTypeIsNull() {
        cut.deleteEntry(null, 42L, "user-123");

        verifyNoInteractions(hcpRepository, singleTestResultRepository, gMetricRepository);
    }

    @Test
    void deleteEntry_doesNothingWhenEntryDoesNotExist() {
        when(hcpRepository.findById(123L)).thenReturn(Optional.empty());

        cut.deleteEntry(GolfType.HCP, 123L, "user-123");

        verify(hcpRepository, never()).deleteById(anyLong());
    }
}
