package com.mirkoebert.golfcourse;

import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {

    private final GolfCourseCatalog catalog;
    private final PlayedRoundRepository playedRoundRepository;
    private final BogeyPlusCountFunction bogeyPlusCountFunction;
    private final DoubleBogeyPlusCountFunction doubleBogeyPlusCountFunction;
    private final GMetricRepository repo;

    public @NonNull List<PlayedRoundEntity> findRoundsForUser(@NonNull final String userId) {
        return playedRoundRepository.findTop10ByUserIdOrderByDateDesc(userId);
    }

    public boolean submitRound(
            @NonNull final String userId,
            @NonNull final String courseName,
            @NonNull final LocalDate date,
            @NonNull final List<Integer> holeStrokes,
            Integer lostBalls
    ) {
        log.info("submitRound");
        val course = catalog.findByName(courseName);
        if (course.isEmpty() || course.get().getHoles().size() != holeStrokes.size()) {
            log.warn("Round submission mismatch: course {}, holes {}", courseName, holeStrokes.size());
            return false;
        }

        final PlayedRoundDto playedRoundDto = PlayedRoundDto
                .builder()
                .courseName(courseName)
                .selectedDate(date)
                .holeStrokes(holeStrokes)
                .lostBalls(lostBalls)
                .build();
        val doubleBogeysPlus = doubleBogeyPlusCountFunction.applyAsInt(playedRoundDto);
        val bogeysPlus = bogeyPlusCountFunction.applyAsInt(playedRoundDto);


        val entity = PlayedRoundEntity
                .builder()
                .userId(userId)
                .date(date)
                .courseName(courseName)
                .holeStrokes(holeStrokes)
                .lostBalls(lostBalls)
                .doubleBogeysPlus(doubleBogeysPlus)
                .bogeysPlus(bogeysPlus)
                .build();
        log.info("Saving played round:  {}", entity);
        PlayedRoundEntity e = playedRoundRepository.save(entity);
        log.info("Saving played round:  {}", e);

        log.info("Create metric entries");
        var gme = GMetricEntity
                .builder()
                .date(date)
                .metricValue(doubleBogeysPlus)
                .type(GMetricType.DOUBLE_BOGEY_PLUS)
                .userId(userId)
                .build();
        repo.save(gme);
        gme = GMetricEntity
                .builder()
                .date(date)
                .metricValue(bogeysPlus)
                .type(GMetricType.BOGEY_PLUS)
                .userId(userId)
                .build();
        repo.save(gme);
        Float lostBallNormalized = getNormaizedValue(lostBalls, course.get());
        if (lostBallNormalized != null) {
            gme = GMetricEntity
                    .builder()
                    .date(date)
                    .metricValue(lostBallNormalized)
                    .type(GMetricType.LOST_BALLS_PER_9HOLES)
                    .userId(userId)
                    .build();
            repo.save(gme);
        }
        return true;
    }

    Float getNormaizedValue(final @Nullable Integer lostBalls, final GolfCourse course){
        if (lostBalls == null){
            log.warn("Can't get calc metric {}} because lostball count is null", GMetricType.LOST_BALLS_PER_9HOLES);
            return null;
        }
        if (course.getHoles().size() == 9) {
            return (float) lostBalls;
        } else if (course.getHoles().size() == 18) {
            return  ((float)lostBalls / 2);
        }
        log.warn("Can't get calc metric {}} because course {} has unsupported whole count", GMetricType.LOST_BALLS_PER_9HOLES, course);
        return null;
    }

    public void deleteRound(@NonNull final String userId, long id) {
        final Optional<PlayedRoundEntity> playedRound = playedRoundRepository.findById(id);
        if (playedRound.isPresent() && playedRound.get().getUserId().equals(userId)) {
            log.info("Deleting round {} for user {}", id, userId);
            playedRoundRepository.deleteById(id);
        } else {
            log.warn("No round deleted. No round found with id {} for user {}", id, userId);
        }
    }
}
