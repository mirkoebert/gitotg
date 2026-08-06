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
    private final RoundRepository roundRepository;

    public @NonNull List<GolfCourse> findAllCourses() {
        return catalog.findAll();
    }

    public @NonNull List<RoundEntity> findRoundsForUser(@NonNull String userId) {
        return roundRepository.findByUserIdOrderByDateDesc(userId);
    }

    public boolean submitRound(
            @NonNull String userId,
            @NonNull String courseName,
            @NonNull LocalDate date,
            @NonNull List<Integer> holeStrokes,
            int lostBalls
    ) {
        val course = catalog.findByName(courseName);
        if (course.isEmpty() || course.get().getHoles().size() != holeStrokes.size()) {
            log.warn("Round submission mismatch: course {}, holes {}", courseName, holeStrokes.size());
            return false;
        }

        log.info("Saving round: user {}, course {}, date {}, holes {}, lostBalls {}",
                userId, courseName, date, holeStrokes.size(), lostBalls);
        val entity = RoundEntity.builder()
                .userId(userId)
                .date(date)
                .courseName(courseName)
                .holeStrokes(holeStrokes)
                .lostBalls(lostBalls)
                .build();
        roundRepository.save(entity);
        return true;
    }

    public void deleteRound(@NonNull String userId, long id) {
        roundRepository.findById(id).ifPresent(round -> {
            if (round.getUserId().equals(userId)) {
                log.info("Deleting round {} for user {}", id, userId);
                roundRepository.deleteById(id);
            }
        });
    }
}
