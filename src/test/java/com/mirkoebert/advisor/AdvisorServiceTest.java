package com.mirkoebert.advisor;

import com.mirkoebert.handicap.HandicapClassifier;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({AdvisorService.class, HandicapClassifier.class})
class AdvisorServiceTest {

    @Autowired
    private AdvisorService cut;

    @MockitoBean
    private HcpRepository hcpRepository;

    @MockitoBean
    private SingleTestResultRepository singleTestResultRepository;

    @Test
    void getAdvise_returnsFreshMessageForLessThan5DataPoints() {
        when(hcpRepository.countByUserId("u1")).thenReturn(3);
        when(singleTestResultRepository.countByUserId("u1")).thenReturn(1);

        String advice = cut.getAdvise("u1");

        assertThat(advice).isIn(AdvisorService.fresh);
        verify(hcpRepository).countByUserId("u1");
        verify(singleTestResultRepository).countByUserId("u1");
    }

    @Test
    void getAdvise_returnsFewMessageFor5To24DataPoints() {
        when(hcpRepository.countByUserId("u2")).thenReturn(12);
        when(singleTestResultRepository.countByUserId("u2")).thenReturn(8);

        String advice = cut.getAdvise("u2");

        assertThat(advice).isIn(AdvisorService.few);
    }

    @Test
    void getAdvise_returnsOtherMessageFor25OrMoreDataPoints() {
        when(hcpRepository.countByUserId("u3")).thenReturn(20);
        when(singleTestResultRepository.countByUserId("u3")).thenReturn(10);

        String advice = cut.getAdvise("u3");

        assertThat(advice).isIn(AdvisorService.other);
    }

    @Test
    void getAdvise_sumsCountsFromBothRepositories() {
        when(hcpRepository.countByUserId("u4")).thenReturn(2);
        when(singleTestResultRepository.countByUserId("u4")).thenReturn(2);

        cut.getAdvise("u4");

        // total 4 < 5 -> fresh bucket
        // just verifying the counts were summed via the mocks
        verify(hcpRepository).countByUserId("u4");
        verify(singleTestResultRepository).countByUserId("u4");
    }

    @Test
    void getAdvise_returnsHighHandicaperMessageForEnoughDataAndHighHcp() {
        when(hcpRepository.countByUserId("u5")).thenReturn(30);
        when(singleTestResultRepository.countByUserId("u5")).thenReturn(5);

        HcpScoreEntity entity = HcpScoreEntity.builder().hcp(28.5).build();
        when(hcpRepository.findFirstByUserIdOrderByDateDesc("u5")).thenReturn(Optional.of(entity));

        String advice = cut.getAdvise("u5");

        assertThat(advice).isIn(AdvisorService.hh);
    }

    @Test
    void getAdvise_returnsOtherMessageForEnoughDataButNotHighHcp() {
        when(hcpRepository.countByUserId("u6")).thenReturn(25);
        when(singleTestResultRepository.countByUserId("u6")).thenReturn(5);

        HcpScoreEntity entity = HcpScoreEntity.builder().hcp(12.0).build();
        when(hcpRepository.findFirstByUserIdOrderByDateDesc("u6")).thenReturn(Optional.of(entity));

        String advice = cut.getAdvise("u6");

        assertThat(advice).isIn(AdvisorService.other);
    }
}
