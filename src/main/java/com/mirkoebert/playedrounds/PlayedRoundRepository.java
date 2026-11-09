package com.mirkoebert.playedrounds;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlayedRoundRepository extends JpaRepository<PlayedRoundEntity, Long> {
        @NonNull
        List<PlayedRoundEntity> findByUserId(@NonNull String userId);

        long countByUserIdAndDateBetween(@NonNull String userId, @NonNull LocalDate start, @NonNull LocalDate end);

        @NonNull
        List<PlayedRoundEntity> findTop10ByUserIdOrderByDateDesc(@NonNull String userId);

        @NonNull
        List<PlayedRoundEntity> findAllByUserIdAndCourseName(String userId, String courseNAme);
}
