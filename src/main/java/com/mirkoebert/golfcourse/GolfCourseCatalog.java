package com.mirkoebert.golfcourse;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class GolfCourseCatalog {

    private static final List<GolfCourse> COURSES = List.of(
            course("Fischland", 5,4,2,5,3,4,4,3,4),
            course("Tessin",    4,5,3,4,4,5,4,4,3)
    );

    private static GolfCourse course(final String name, final int... pars) {
        return GolfCourse.builder()
                .name(name)
                .holes(IntStream.range(0, pars.length)
                        .mapToObj(i -> Hole.builder().number(i + 1).par(pars[i]).build())
                        .toList())
                .build();
    }

    public @NonNull List<GolfCourse> findAll() {
        return COURSES;
    }

    public @NonNull Optional<GolfCourse> findByName(final String name) {
        return COURSES.stream().filter(c -> c.getName().equals(name)).findFirst();
    }
}
