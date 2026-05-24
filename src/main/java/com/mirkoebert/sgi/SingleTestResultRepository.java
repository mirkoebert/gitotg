package com.mirkoebert.sgi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SingleTestResultRepository extends JpaRepository<SingleTestResultEntity, Long> {

        List<SingleTestResultEntity> findByUserIdAndTestId(String user, Integer testId);

        List<SingleTestResultEntity> findAllByUserId(String userId);

        int countByUserId(String userId);
}
