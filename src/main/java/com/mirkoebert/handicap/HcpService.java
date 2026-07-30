package com.mirkoebert.handicap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.mirkoebert.Constants.HCP_Epsilon;

@Service
@Slf4j
@RequiredArgsConstructor
public class HcpService {


    private final HcpRepository repo;
    private final MessageSource messageSource;

    public HcpScoreOutFormatedDTO findLatestByUserId(@NonNull String userId) {
        Locale locale = LocaleContextHolder.getLocale();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd. MMMM yyyy", locale);
        Optional<HcpScoreEntity> last = repo.findFirstByUserIdOrderByDateDesc(userId);
        if (last.isPresent()) {
            // Trend only needs the latest 4 scores
            List<HcpScoreEntity> recent = repo.findTop4ByUserIdOrderByDateDesc(userId);
            return HcpScoreOutFormatedDTO
                    .builder()
                    .hcp(String.format(locale, "%.1f", last.get().getHcp()))
                    .date(df.format(last.get().getDate()))
                    .trend(getTrend(recent))
                    .build();
        }
        String notEnough = msg("trend.notEnoughData");
        return HcpScoreOutFormatedDTO
                .builder()
                .hcp(notEnough)
                .date(notEnough)
                .trend(notEnough)
                .build();
    }

    String getTrend(final List<HcpScoreEntity> unordered) {
        if ((unordered == null) || unordered.size() < 4) {
            return msg("trend.notEnoughData");
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
