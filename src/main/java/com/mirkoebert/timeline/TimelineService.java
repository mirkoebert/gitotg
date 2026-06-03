package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

        private final SingleTestResultRepository singleTestResultRepository;
        private final HcpRepository hcpRepository;

        public List<MeasurmentDTO> getLatestResults(final String userId) {
                List<HcpScoreEntity> h = hcpRepository.findByUserId(userId);
                List<MeasurmentDTO> hm = h
                        .stream()
                        .map(hc -> MeasurmentDTO
                                .builder()
                                .id(hc.getId())
                                .value(String.format("%.1f", hc.getHcp()))
                                .userId(userId)
                                .type(GolfType.HCP)
                                .date(hc.getDate())
                                .comment("Handicap")
                                .build()
                ).toList();

                List<SingleTestResultEntity> sgiTests = singleTestResultRepository.findAllByUserId(userId);


                List<MeasurmentDTO> sgi = sgiTests
                        .stream()
                        .map(m -> MeasurmentDTO
                                .builder()
                                .id(m.getId())
                                .value("" + m.getHcp())
                                .type(GolfType.SGIHCP)
                                .userId(userId)
                                .comment("Short Game Test " + m.getTestId())
                                .date(m.getDate())
                                .build())
                        .toList();

                return Stream.concat(hm.stream(), sgi.stream())
                        .sorted(Comparator.comparing(MeasurmentDTO::getDate, Comparator.reverseOrder()))
                        .limit(12L)
                        .toList();
        }

        public void deleteEntry(GolfType type, Long id, String currentUserId) {
                if (id == null || type == null) {
                        return;
                }

                if (type == GolfType.HCP) {
                        hcpRepository.findById(id).ifPresent(entry -> {
                                if (entry.getUserId().equals(currentUserId)) {
                                        hcpRepository.deleteById(id);
                                }
                        });
                } else if (type == GolfType.SGIHCP) {
                        singleTestResultRepository.findById(id).ifPresent(entry -> {
                                if (entry.getUserId().equals(currentUserId)) {
                                        singleTestResultRepository.deleteById(id);
                                }
                        });
                }
        }
}
