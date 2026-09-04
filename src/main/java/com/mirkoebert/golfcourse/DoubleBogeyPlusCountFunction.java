package com.mirkoebert.golfcourse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.ToIntFunction;

@Service
@Slf4j
@RequiredArgsConstructor
class DoubleBogeyPlusCountFunction implements ToIntFunction<PlayedRoundDto> {

    private final GolfCourseCatalog catalog;

    @Override
    public int applyAsInt(@NonNull final PlayedRoundDto round) {
        val course = catalog.findByName(round.getCourseName());
        if (course.isEmpty()) {
            log.warn("Cannot count double bogeys: unknown course {}", round.getCourseName());
            return 0;
        }

        List<Hole> holes = course.get().getHoles();
        List<Integer> strokes = round.getHoleStrokes();
        if (holes.size() != strokes.size()) {
            log.warn("Cannot count double bogeys: hole count mismatch, course {}, holes {}, strokes {}",
                    round.getCourseName(), holes.size(), strokes.size());
            return 0;
        }

        int count = 0;
        for (int i = 0; i < holes.size(); i++) {
            if (strokes.get(i) - holes.get(i).getPar() >= 2) {
                count++;
            }
        }
        return count;
    }
}
