package com.mirkoebert.golfcourse;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {

    @NonNull
    List<RoundEntity> findByUserIdOrderByDateDesc(@NonNull String userId);
}
