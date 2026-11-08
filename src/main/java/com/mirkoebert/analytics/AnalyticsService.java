package com.mirkoebert.analytics;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.playedrounds.PlayedRoundEntity;
import com.mirkoebert.playedrounds.PlayedRoundRepository;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final HcpRepository hcpRepository;
    private final SingleTestResultRepository singleTestResultRepository;
    private final SgiHcpAggregatedService sgiHcpAggregatedService;
    private final PlayedRoundRepository playedRoundRepository;
    private final WeaknessAnalyzer weaknessAnalyzer;

    public @NonNull List<String> weaknesses(@NonNull String userId) {
        return weaknessAnalyzer.analyze(snapshot(userId, LocalDate.now()));
    }

    private GolfSnapshot snapshot(String userId, LocalDate today) {
        List<DatedHandicap> handicaps = hcpRepository.findByUserId(userId).stream()
                .filter(score -> score.getDate() != null && score.getHcp() != null)
                .map(score -> new DatedHandicap(score.getDate(), score.getHcp()))
                .toList();
        Double handicap = handicaps.stream()
                .max(Comparator.comparing(DatedHandicap::date))
                .map(DatedHandicap::handicap)
                .orElse(null);

        int[] entries = shortGameEntries(userId);
        Integer shortGameIndex = singleTestResultRepository.countByUserId(userId) == 0
                ? null
                : sgiHcpAggregatedService.getLatestSgiHcpAggregated(userId);

        PlayedRoundEntity latestRound = latestRound(userId);
        int year = today.getYear();
        Integer lostBalls = latestRound == null ? null : latestRound.getLostBalls();

        return new GolfSnapshot(
                handicap,
                shortGameIndex,
                lostBalls == null ? null : lostBalls.doubleValue(),
                handicaps,
                playedRoundRepository.countByUserIdAndDateBetween(
                        userId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)),
                playedRoundRepository.countByUserIdAndDateBetween(
                        userId, LocalDate.of(year - 1, 1, 1), LocalDate.of(year - 1, 12, 31)),
                entries,
                latestRound == null ? null : latestRound.getDate(),
                today
        );
    }

    private int[] shortGameEntries(String userId) {
        int[] entries = new int[GolfSnapshot.SHORT_GAME_TEST_COUNT];
        for (SingleTestResultEntity result : singleTestResultRepository.findAllByUserId(userId)) {
            Integer testId = result.getTestId();
            if (testId != null && testId >= 1 && testId <= GolfSnapshot.SHORT_GAME_TEST_COUNT) {
                entries[testId - 1]++;
            }
        }
        return entries;
    }

    private PlayedRoundEntity latestRound(String userId) {
        return playedRoundRepository.findByUserId(userId).stream()
                .filter(round -> round.getDate() != null)
                .max(Comparator.comparing(PlayedRoundEntity::getDate))
                .orElse(null);
    }
}
