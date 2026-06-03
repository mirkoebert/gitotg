package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void deleteEntry_doesNothingWhenIdIsNull() {
        cut.deleteEntry(GolfType.HCP, null, "user-123");

        verifyNoInteractions(hcpRepository, singleTestResultRepository);
    }

    @Test
    void deleteEntry_doesNothingWhenTypeIsNull() {
        cut.deleteEntry(null, 42L, "user-123");

        verifyNoInteractions(hcpRepository, singleTestResultRepository);
    }

    @Test
    void deleteEntry_doesNothingWhenEntryDoesNotExist() {
        when(hcpRepository.findById(123L)).thenReturn(Optional.empty());

        cut.deleteEntry(GolfType.HCP, 123L, "user-123");

        verify(hcpRepository, never()).deleteById(anyLong());
    }
}
