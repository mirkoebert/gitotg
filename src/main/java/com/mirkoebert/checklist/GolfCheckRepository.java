package com.mirkoebert.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GolfCheckRepository extends JpaRepository<GolfCheckEntity, Long> {
}
