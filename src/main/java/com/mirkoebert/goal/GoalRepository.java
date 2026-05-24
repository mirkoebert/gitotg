package com.mirkoebert.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<UsersGoalEntity, Long> {

        List<UsersGoalEntity> findAllByGoal(GoalEnum goalEnum);
}
