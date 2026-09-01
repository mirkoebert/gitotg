package com.mirkoebert.golfcourse;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayedRoundRepository extends JpaRepository<PlayedRoundEntity, Long> {

    @NonNull
    List<PlayedRoundEntity> findByUserId(@NonNull String userId);

    @NonNull
    List<PlayedRoundEntity> findTop10ByUserIdOrderByDateDesc(@NonNull String userId);
}
