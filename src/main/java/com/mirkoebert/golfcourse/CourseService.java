package com.mirkoebert.golfcourse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {

    private final GolfCourseCatalog catalog;
    private final PlayedRoundRepository playedRoundRepository;
    private final BogeyPlusCountFunction bogeyPlusCountFunction;
    private final DoubleBogeyPlusCountFunction doubleBogeyPlusCountFunction;

    public @NonNull List<GolfCourse> findAllCourses() {
        return catalog.findAll();
    }

    public @NonNull List<PlayedRoundEntity> findRoundsForUser(@NonNull final String userId) {
        return playedRoundRepository.findTop10ByUserIdOrderByDateDesc(userId);
    }

    public boolean submitRound(
            @NonNull final String userId,
            @NonNull final String courseName,
            @NonNull final LocalDate date,
            @NonNull final List<Integer> holeStrokes,
            int lostBalls
    ) {
        val course = catalog.findByName(courseName);
        if (course.isEmpty() || course.get().getHoles().size() != holeStrokes.size()) {
            log.warn("Round submission mismatch: course {}, holes {}", courseName, holeStrokes.size());
            return false;
        }

        final PlayedRoundDto playedRoundDto = PlayedRoundDto.builder()
                .courseName(courseName)
                .selectedDate(date)
                .holeStrokes(holeStrokes)
                .lostBalls(lostBalls)
                .build();
        val doubleBogeysPlus = doubleBogeyPlusCountFunction.applyAsInt(playedRoundDto);
        val bogeysPlus = bogeyPlusCountFunction.applyAsInt(playedRoundDto);


        val entity = PlayedRoundEntity.builder()
                .userId(userId)
                .date(date)
                .courseName(courseName)
                .holeStrokes(holeStrokes)
                .lostBalls(lostBalls)
                .doubleBogeysPlus(doubleBogeysPlus)
                .bogeysPlus(bogeysPlus)
                .build();
        log.info("Saving played round:  {}", entity);
        playedRoundRepository.save(entity);
        return true;
    }

    public void deleteRound(@NonNull final String userId, long id) {
        playedRoundRepository.findById(id).ifPresent(round -> {
            if (round.getUserId().equals(userId)) {
                log.info("Deleting round {} for user {}", id, userId);
                playedRoundRepository.deleteById(id);
            }
        });
    }
}
