package com.mirkoebert.export;

import com.mirkoebert.TestSuite;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.mirkoebert.sgi.calc.PointsToSgiHcpFunction;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({CsvImportService.class})
class CsvImportServiceTest {

    @Autowired
    private CsvImportService cut;

    @MockitoBean
    private HcpRepository hcpRepository;

    @MockitoBean
    private SingleTestResultRepository singleTestResultRepository;

    @MockitoBean
    private PointsToSgiHcpFunction pointsToSgiHcpFunction;

    // HCP tests

    @SneakyThrows
    @Test
    void importHcpData_insertsWhenNoExisting() {
        final String csv = "date,hcp\n2025-01-31,25.5\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        when(hcpRepository.findByUserIdAndDate("u1", LocalDate.of(2025, 1, 31))).thenReturn(Optional.empty());

        int count = cut.importHcpData(is, "u1");

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<HcpScoreEntity> captor = ArgumentCaptor.forClass(HcpScoreEntity.class);
        verify(hcpRepository).save(captor.capture());
        HcpScoreEntity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("u1");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(saved.getHcp()).isEqualTo(25.5);
        assertThat(saved.getId()).isEqualTo(0L); // new
    }

    @SneakyThrows
    @Test
    void importHcpData_replacesWhenSameDateExists() {
        HcpScoreEntity existing = HcpScoreEntity.builder()
                .id(42L)
                .userId("u1")
                .date(LocalDate.of(2025, 1, 21))
                .hcp(20.0)
                .build();

        when(hcpRepository.findByUserIdAndDate("u1", LocalDate.of(2025, 1, 21))).thenReturn(Optional.of(existing));

        final String csv = "date,hcp\n2025-01-21,25.5\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, "u1");

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<HcpScoreEntity> captor = ArgumentCaptor.forClass(HcpScoreEntity.class);
        verify(hcpRepository).save(captor.capture());
        HcpScoreEntity saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(42L);
        assertThat(saved.getHcp()).isEqualTo(25.5);
    }

    @SneakyThrows
    @Test
    void importHcpData_skipsInvalidRowsAndCountsOnlyValid() {
        final String csv = "date,hcp\n2025-01-01,\n,25.5\n2025-01-02,30.0\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        when(hcpRepository.findByUserIdAndDate(anyString(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        int count = cut.importHcpData(is, "u1");

        assertThat(count).isEqualTo(1);
        verify(hcpRepository, times(1)).save(any(HcpScoreEntity.class));
    }

    @Test
    void importHcpData_returnsZeroForNoValidRows() {
        // valid columns but date null -> skipped, no repo interaction
        String csv = "date,hcp\n,25.5\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, "u1");

        assertThat(count).isEqualTo(0);
        verifyNoInteractions(hcpRepository);
    }

    // SGI tests

    @Test
    void importSgiData_insertsAndComputesHcpWhenNoExisting() {
        when(pointsToSgiHcpFunction.apply(1, 10)).thenReturn(35);
        when(singleTestResultRepository.findByUserIdAndDateAndTestId(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(Optional.empty());

        String csv = "date,points,testId,testType\n2025-01-01,10,1,SGI\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, "u1");

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<SingleTestResultEntity> captor = ArgumentCaptor.forClass(SingleTestResultEntity.class);
        verify(singleTestResultRepository).save(captor.capture());
        SingleTestResultEntity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("u1");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(saved.getPoints()).isEqualTo(10);
        assertThat(saved.getTestId()).isEqualTo(1);
        assertThat(saved.getTestType()).isEqualTo(TestSuite.SGI);
        assertThat(saved.getHcp()).isEqualTo(35);
        verify(pointsToSgiHcpFunction).apply(1, 10);
    }

    @Test
    void importSgiData_replacesWhenSameDateAndTestIdExists() {
        SingleTestResultEntity existing = SingleTestResultEntity.builder()
                .id(99L)
                .userId("u1")
                .date(LocalDate.of(2025, 1, 1))
                .testId(1)
                .points(5)
                .testType(TestSuite.SGI)
                .build();

        when(singleTestResultRepository.findByUserIdAndDateAndTestId("u1", LocalDate.of(2025, 1, 1), 1))
                .thenReturn(Optional.of(existing));
        when(pointsToSgiHcpFunction.apply(1, 10)).thenReturn(35);

        String csv = "date,points,testId,testType\n2025-01-01,10,1,SGI\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, "u1");

        assertThat(count).isEqualTo(1);

        ArgumentCaptor<SingleTestResultEntity> captor = ArgumentCaptor.forClass(SingleTestResultEntity.class);
        verify(singleTestResultRepository).save(captor.capture());
        SingleTestResultEntity saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(99L);
        assertThat(saved.getPoints()).isEqualTo(10);
        assertThat(saved.getHcp()).isEqualTo(35);
    }

    @SneakyThrows
    @Test
    void importSgiData_skipsInvalidRows() {
        when(pointsToSgiHcpFunction.apply(anyInt(), anyInt())).thenReturn(0);
        when(singleTestResultRepository.findByUserIdAndDateAndTestId(anyString(), any(), anyInt()))
                .thenReturn(Optional.empty());

        String csv = "date,points,testId,testType\n2025-01-01,10,1,SGI\n,5,1,SGI\n2025-01-02,,2,SGI\n2025-01-03,7,,SGI\n2025-01-04,8,4,\n2025-01-05,9,5,SGI\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, "u1");

        assertThat(count).isEqualTo(2); // first and last valid
        verify(singleTestResultRepository, times(2)).save(any(SingleTestResultEntity.class));
    }

    @Test
    void importSgiData_returnsZeroForNoValidRows() {
        // valid columns but date null -> skipped inside if, no calls to function/repo
        String csv = "date,points,testId,testType\n,10,1,SGI\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, "u1");

        assertThat(count).isEqualTo(0);
        verifyNoInteractions(singleTestResultRepository, pointsToSgiHcpFunction);
    }
}
