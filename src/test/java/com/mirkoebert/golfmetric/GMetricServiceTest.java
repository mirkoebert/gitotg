package com.mirkoebert.golfmetric;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GMetricServiceTest {

    @Mock
    private GMetricRepository repo;

    @InjectMocks
    private GMetricService cut;

    @Test
    void findByUserId_delegatesToRepositoryOrderedByDateDesc() {
        GMetricEntity entity = GMetricEntity.builder()
                .userId("u1").date(LocalDate.of(2026, 1, 1)).metricValue(2).type(GMetricType.LOST_BALLS).build();
        when(repo.findByUserIdOrderByDateDesc("u1")).thenReturn(List.of(entity));

        List<GMetricEntity> result = cut.findByUserId("u1");

        assertThat(result).containsExactly(entity);
        verify(repo).findByUserIdOrderByDateDesc("u1");
    }

    @Test
    void findByUserIdAndType_delegatesToRepositoryWithType() {
        GMetricEntity entity = GMetricEntity.builder()
                .userId("u2").date(LocalDate.of(2026, 1, 2)).metricValue(1).type(GMetricType.BOGEY).build();
        when(repo.findByUserIdAndTypeOrderByDateDesc("u2", GMetricType.BOGEY)).thenReturn(List.of(entity));

        List<GMetricEntity> result = cut.findByUserIdAndType("u2", GMetricType.BOGEY);

        assertThat(result).containsExactly(entity);
        verify(repo).findByUserIdAndTypeOrderByDateDesc("u2", GMetricType.BOGEY);
    }

    @Test
    void findLatestByUserIdAndType_delegatesToFirstByDateDesc() {
        GMetricEntity entity = GMetricEntity.builder()
                .userId("u3").date(LocalDate.of(2026, 3, 1)).metricValue(4).type(GMetricType.DOUBLE_BOGEY).build();
        when(repo.findFirstByUserIdAndTypeOrderByDateDesc("u3", GMetricType.DOUBLE_BOGEY))
                .thenReturn(Optional.of(entity));

        Optional<GMetricEntity> result = cut.findLatestByUserIdAndType("u3", GMetricType.DOUBLE_BOGEY);

        assertThat(result).contains(entity);
        verify(repo).findFirstByUserIdAndTypeOrderByDateDesc("u3", GMetricType.DOUBLE_BOGEY);
    }
}
