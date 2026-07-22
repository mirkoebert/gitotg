package com.mirkoebert.handicap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mirkoebert.Constants.HCP_Epsilon;
import static com.mirkoebert.Constants.NOT_ENOUGH_DATA_AVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class HcpService {


        private final HcpRepository repo;
        private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd. MMMM yyyy");

        public HcpScoreOutFormatedDTO findLatestByUserId(@NonNull String userId) {
                Optional<HcpScoreEntity> last = repo.findFirstByUserIdOrderByDateDesc(userId);
                if (last.isPresent()) {
                        // Trend only needs the latest 4 scores
                        List<HcpScoreEntity> recent = repo.findTop4ByUserIdOrderByDateDesc(userId);
                        return HcpScoreOutFormatedDTO
                                .builder()
                                .hcp(String.format("%.1f", last.get().getHcp()))
                                .date(df.format(last.get().getDate()))
                                .trend(getTrend(recent))
                                .build();
                }
                return HcpScoreOutFormatedDTO
                        .builder()
                        .hcp(NOT_ENOUGH_DATA_AVAILABLE)
                        .date(NOT_ENOUGH_DATA_AVAILABLE)
                        .trend(NOT_ENOUGH_DATA_AVAILABLE)
                        .build();
        }

        String getTrend(final List<HcpScoreEntity> unordered) {
                if ((unordered == null) || unordered.size() < 4) {
                        return NOT_ENOUGH_DATA_AVAILABLE;
                }
                List<HcpScoreEntity> sorted = unordered
                        .stream()
                        .sorted(Comparator.comparing(HcpScoreEntity::getDate, Comparator.reverseOrder()))
                        .peek(x -> log.debug("X {}", x))
                        .collect(Collectors.toList());
                final HcpScoreEntity latest = sorted.getFirst();
                log.debug("Latest HCP: {}", latest);
                sorted.removeFirst();
                final DoubleSummaryStatistics stats = sorted
                        .stream()
                        .limit(3)
                        .map(HcpScoreEntity::getHcp)
                        .mapToDouble(x -> x)
                        .summaryStatistics();
                log.debug("HCP: {}", stats);
                if (stats.getAverage() > (latest.getHcp() + HCP_Epsilon)) {
                        return "improving";
                } else if ((stats.getAverage() + HCP_Epsilon) < latest.getHcp()) {
                        return "worsening";
                }
                return "stable";
        }
}
