package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
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
    private final GMetricRepository gMetricRepository;

    public List<MeasurementDTO> getLatestResults(@NonNull final String userId) {
        return getLatestResults(userId, TimelineRange.LAST_30);
    }

    public List<MeasurementDTO> getLatestResults(@NonNull final String userId, @NonNull final TimelineRange range) {
        // Cap each source so we never materialize full history for a limited timeline view
        Pageable pageable = range.toPageable();

        List<MeasurementDTO> hm = hcpRepository.findByUserIdOrderByDateDesc(userId, pageable)
                .stream()
                .map(hc -> MeasurementDTO
                        .builder()
                        .id(hc.getId())
                        .value(String.format("%.1f", hc.getHcp()))
                        .userId(userId)
                        .type(GolfType.HCP)
                        .date(hc.getDate())
                        .comment("Handicap")
                        .build()
                ).toList();

        List<MeasurementDTO> sgi = singleTestResultRepository.findByUserIdOrderByDateDesc(userId, pageable)
                .stream()
                .map(m -> MeasurementDTO
                        .builder()
                        .id(m.getId())
                        .value("" + m.getHcp())
                        .type(GolfType.SGIHCP)
                        .userId(userId)
                        .comment("Short Game Test " + m.getTestId())
                        .date(m.getDate())
                        .build())
                .toList();

        List<MeasurementDTO> gm = gMetricRepository.findByUserIdOrderByDateDesc(userId, pageable)
                .stream()
                .map(m -> MeasurementDTO
                        .builder()
                        .id(m.getId())
                        .value(String.valueOf(m.getMetricValue()))
                        .type(GolfType.COUNT)
                        .userId(userId)
                        .comment(commentFor(m.getType()))
                        .date(m.getDate())
                        .build())
                .toList();

        Stream<MeasurementDTO> merged = Stream.of(hm.stream(), sgi.stream(), gm.stream())
                .flatMap(s -> s)
                .sorted(Comparator.comparing(MeasurementDTO::getDate, Comparator.reverseOrder()));

        Integer limit = range.getLimit();
        if (limit != null) {
            return merged.limit(limit.longValue()).toList();
        }
        return merged.toList();
    }

    public void deleteEntry(GolfType type, Long id, @NonNull String currentUserId) {
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
        } else if (type == GolfType.COUNT) {
            gMetricRepository.findById(id).ifPresent(entry -> {
                if (entry.getUserId().equals(currentUserId)) {
                    gMetricRepository.deleteById(id);
                }
            });
        }
    }

    private static String commentFor(GMetricType type) {
        if (type == null) {
            return "Golf Metric";
        }
        return switch (type) {
            case LOST_BALLS -> "Lost Balls";
            case DOUBLE_BOGEY_PLUS -> "Double Bogey";
            case BOGEY -> "Bogey";
        };
    }
}
