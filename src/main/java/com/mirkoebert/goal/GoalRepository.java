package com.mirkoebert.goal;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<UsersGoalEntity, Long> {

        @NonNull
        List<UsersGoalEntity> findAllByGoal(@NonNull GoalEnum goalEnum);

        @Override
        @NonNull
        Optional<UsersGoalEntity> findById(@NonNull Long id);

        @Override
        void deleteById(@NonNull Long id);
}
