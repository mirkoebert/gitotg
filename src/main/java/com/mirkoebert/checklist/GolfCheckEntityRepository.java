package com.mirkoebert.checklist;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GolfCheckEntityRepository extends JpaRepository<GolfCheckEntity, Long> {

        @NonNull
        List<GolfCheckEntity> findByUserIdAndCheckListItemIdIn(
                @NonNull String userId,
                @NonNull Collection<Long> checkListItemIds);

        void deleteByUserIdAndCheckListItemIdIn(
                @NonNull String userId,
                @NonNull Collection<Long> checkListItemIds);

        @Override
        @NonNull
        Optional<GolfCheckEntity> findById(@NonNull Long id);

        @Override
        void deleteById(@NonNull Long id);
}
