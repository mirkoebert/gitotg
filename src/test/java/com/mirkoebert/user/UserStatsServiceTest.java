package com.mirkoebert.user;

import com.mirkoebert.checklist.GolfCheckEntityRepository;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

        @Mock
        private HcpRepository hcpRepository;
        @Mock
        private SingleTestResultRepository sgiRepository;
        @Mock
        private UserPreferenceRepository userPreferenceRepository;
        @Mock
        private GMetricRepository gMetricRepository;
        @Mock
        private GolfCheckEntityRepository golfCheckEntityRepository;

        @InjectMocks
        private UserStatsService cut;

        @Test
        void countUsers_unionsDistinctIdsAcrossSources() {
                when(hcpRepository.findDistinctUserIds()).thenReturn(List.of("u1", "u2"));
                when(sgiRepository.findDistinctUserIds()).thenReturn(List.of("u2", "u3"));
                when(userPreferenceRepository.findDistinctUserIds()).thenReturn(List.of("u1"));
                when(gMetricRepository.findDistinctUserIds()).thenReturn(List.of("u4"));
                when(golfCheckEntityRepository.findDistinctUserIds()).thenReturn(List.of());

                assertThat(cut.countUsers()).isEqualTo(4);
        }

        @Test
        void countUsers_ignoresNullAndBlank() {
                when(hcpRepository.findDistinctUserIds()).thenReturn(Arrays.asList("u1", "", null));
                when(sgiRepository.findDistinctUserIds()).thenReturn(List.of());
                when(userPreferenceRepository.findDistinctUserIds()).thenReturn(List.of());
                when(gMetricRepository.findDistinctUserIds()).thenReturn(List.of());
                when(golfCheckEntityRepository.findDistinctUserIds()).thenReturn(List.of());

                assertThat(cut.countUsers()).isEqualTo(1);
        }
}
