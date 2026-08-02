package com.mirkoebert.handicap;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HcpRepository extends JpaRepository<HcpScoreEntity, Long> {

    @Query("select distinct h.userId from HcpScoreEntity h where h.userId is not null")
    @NonNull
    List<String> findDistinctUserIds();

    @NonNull
    List<HcpScoreEntity> findByUserId(@NonNull String userId);

    @NonNull
    List<HcpScoreEntity> findByUserIdOrderByDateDesc(@NonNull String userId, Pageable pageable);

    @NonNull
    List<HcpScoreEntity> findTop12ByUserIdOrderByDateDesc(@NonNull String userId);

    @NonNull
    List<HcpScoreEntity> findTop4ByUserIdOrderByDateDesc(@NonNull String userId);

    int countByUserId(@NonNull String userId);

    @NonNull
    Optional<HcpScoreEntity> findFirstByUserIdOrderByDateDesc(@NonNull String userId);

    @NonNull
    Optional<HcpScoreEntity> findByUserIdAndDate(@NonNull String userId, @NonNull LocalDate date);

    @Override
    @NonNull
    Optional<HcpScoreEntity> findById(@NonNull Long id);

    @Override
    void deleteById(@NonNull Long id);
}
