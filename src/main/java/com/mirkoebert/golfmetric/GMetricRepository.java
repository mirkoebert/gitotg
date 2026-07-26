package com.mirkoebert.golfmetric;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GMetricRepository extends JpaRepository<GMetricEntity, Long> {

        @Query("select distinct g.userId from GMetricEntity g where g.userId is not null")
        @NonNull
        List<String> findDistinctUserIds();

        @NonNull
        List<GMetricEntity> findByUserId(@NonNull String userId);

        @NonNull
        List<GMetricEntity> findByUserIdOrderByDateDesc(@NonNull String userId);

        @NonNull
        List<GMetricEntity> findByType(@NonNull GMetricType type);

        @NonNull
        List<GMetricEntity> findByUserIdAndType(@NonNull String userId, @NonNull GMetricType type);

        @NonNull
        List<GMetricEntity> findByUserIdAndTypeOrderByDateDesc(@NonNull String userId, @NonNull GMetricType type);

        @NonNull
        Optional<GMetricEntity> findByUserIdAndDateAndType(
                        @NonNull String userId, @NonNull LocalDate date, @NonNull GMetricType type);

        @Override
        @NonNull
        Optional<GMetricEntity> findById(@NonNull Long id);

        @Override
        void deleteById(@NonNull Long id);
}
