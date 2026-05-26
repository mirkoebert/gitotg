package com.mirkoebert.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GolfCheckListItemRepository extends JpaRepository<GolfCheckListItem, Long> {

        List<GolfCheckListItem> findByGoal(final String goal);
}
