package com.mirkoebert.sgi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import static com.mirkoebert.Constants.HCP_Epsilon;

@Service
@RequiredArgsConstructor
@Slf4j
class TrendService {

    private final SingleTestResultRepository resultRepository;
    private final MessageSource messageSource;

    public String getTrend(final Integer testId, final String userId) {
        log.debug("get Trend for user {} and testId {}", userId, testId);
        // Only the newest 4 results are required for trend calculation
        List<SingleTestResultEntity> a = resultRepository.findTop4ByUserIdAndTestIdOrderByDateDesc(userId, testId);
        if (a.size() > 3) {
            return getTrendFromList(a);
        }
        return msg("trend.notEnoughData");
    }

    String getTrendFromList(final List<SingleTestResultEntity> unordered) {
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
            return msg("trend.improving");
        } else if ((stats.getAverage() + HCP_Epsilon) < latest.getHcp()) {
            return msg("trend.worsening");
        }
        return msg("trend.stable");
    }

    private String msg(final String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
