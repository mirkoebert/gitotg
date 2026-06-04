package com.mirkoebert.checklist;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GolfCheckRepository extends JpaRepository<GolfCheckEntity, Long> {

        @Override
        @NonNull
        Optional<GolfCheckEntity> findById(@NonNull Long id);

        @Override
        void deleteById(@NonNull Long id);
}
