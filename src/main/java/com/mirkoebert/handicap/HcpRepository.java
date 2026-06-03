package com.mirkoebert.handicap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HcpRepository extends JpaRepository<HcpScoreEntity, Long> {

        List<HcpScoreEntity> findByUserId(String userId);

        int countByUserId(String userId);

        Optional<HcpScoreEntity> findFirstByUserIdOrderByDateDesc(String userId);
}
