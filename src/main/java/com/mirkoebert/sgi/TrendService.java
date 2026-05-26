package com.mirkoebert.sgi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import static com.mirkoebert.Constants.HCP_Epsilon;
import static com.mirkoebert.Constants.NOT_ENOUGH_DATA_AVAILABLE;

@Service
@RequiredArgsConstructor
@Slf4j
class TrendService {

        private final SingleTestResultRepository resultRepository;

        public String getTrend(final Integer testId, final String userId) {
                log.debug("get Trend for user {} abd testId {}", userId, testId);
                List<SingleTestResultEntity> a = resultRepository.findByUserIdAndTestId(userId, testId);
                if (a.size() > 3) {
                        return getTrendFromList(a);
                }
                return NOT_ENOUGH_DATA_AVAILABLE;
        }

        String getTrendFromList(final List<SingleTestResultEntity> unordered){
                List<SingleTestResultEntity> sorted = unordered
                        .stream()
                        .sorted(Comparator.comparing(SingleTestResultEntity::getDate, Comparator.reverseOrder()))
                        .peek(x -> log.debug("X {}", x))
                        .collect(Collectors.toList());
                final SingleTestResultEntity latest = sorted.getFirst();
                log.debug("Latest HCP: {}", latest);
                sorted.removeFirst();
                final DoubleSummaryStatistics stats = sorted
                        .stream()
                        .limit(3)
                        .map(SingleTestResultEntity::getHcp)
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
