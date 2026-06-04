package com.mirkoebert.sgi;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SingleTestResultRepository extends JpaRepository<SingleTestResultEntity, Long> {

        @NonNull
        List<SingleTestResultEntity> findByUserIdAndTestId(@NonNull String user, @NonNull Integer testId);

        @NonNull
        List<SingleTestResultEntity> findAllByUserId(@NonNull String userId);

        int countByUserId(@NonNull String userId);

        @NonNull
        Optional<SingleTestResultEntity> findByUserIdAndDateAndTestId(@NonNull String userId, @NonNull LocalDate date, @NonNull Integer testId);

        @Override
        @NonNull
        Optional<SingleTestResultEntity> findById(@NonNull Long id);

        @Override
        void deleteById(@NonNull Long id);
}
