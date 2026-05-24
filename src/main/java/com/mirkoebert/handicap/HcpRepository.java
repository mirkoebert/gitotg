package com.mirkoebert.handicap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HcpRepository extends JpaRepository<HcpScoreEntity, Long> {

        List<HcpScoreEntity> findByUserId(String userId);

        @Query(value = "select top(1) from HcpScoreEntity where userId = :userId order by selectedDate ASC", nativeQuery = true)
        Optional<HcpScoreEntity> findLatestByUserId(String userId);

        int countByUserId(String userId);
}
