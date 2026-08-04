package com.mirkoebert.sgi;

import com.mirkoebert.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SgiHcpAggregatedServiceTest {

    @Mock
    private SingleTestResultRepository repo;

    private SgiHcpAggregatedService cut;

    @BeforeEach
    void setUp() {
        cut = new SgiHcpAggregatedService(repo, new SgiTestRepo(), new SgiTestSuiteHcpFunction());
    }

    private static SingleTestResultEntity withPoints(int points) {
        return SingleTestResultEntity.builder()
                .date(LocalDate.of(2026, 1, 1)).points(points).testType(TestSuite.SGI).build();
    }

    @Test
    void getLatestSgiHcpAggregated_returnsBeginnerHcp_whenNoResultsForAnyTest() {
        for (int testId = 1; testId <= 8; testId++) {
            when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u1", testId)).thenReturn(Optional.empty());
        }

        // sum of points is 0 -> below the lowest table bucket -> worst (beginner) HCP
        assertThat(cut.getLatestSgiHcpAggregated("u1")).isEqualTo(40);
    }

    @Test
    void getLatestSgiHcpAggregated_sumsLatestPointsPerTestAndConvertsToHcp() {
        when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u2", 1)).thenReturn(Optional.of(withPoints(5)));
        when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u2", 2)).thenReturn(Optional.empty());
        when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u2", 3)).thenReturn(Optional.of(withPoints(15)));
        for (int testId : new int[]{4, 5, 6, 7, 8}) {
            when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u2", testId)).thenReturn(Optional.empty());
        }

        // sum = 5 + 15 = 20 -> table bucket 20,21 -> 35
        assertThat(cut.getLatestSgiHcpAggregated("u2")).isEqualTo(35);
    }

    @Test
    void getLatestSgiHcpAggregated_onlyConsidersTheLatestResultPerTest() {
        // findFirstByUserIdAndTestIdOrderByDateDesc already returns only the latest per test;
        // verify the aggregate uses exactly that single value, not a sum of history
        when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u3", 1)).thenReturn(Optional.of(withPoints(45)));
        for (int testId = 2; testId <= 8; testId++) {
            when(repo.findFirstByUserIdAndTestIdOrderByDateDesc("u3", testId)).thenReturn(Optional.empty());
        }

        // sum = 45 -> table bucket 44,45 -> 23
        assertThat(cut.getLatestSgiHcpAggregated("u3")).isEqualTo(23);
    }
}
