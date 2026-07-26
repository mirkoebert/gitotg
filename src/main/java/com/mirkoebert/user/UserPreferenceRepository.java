package com.mirkoebert.user;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, String> {

        @Query("select distinct u.userId from UserPreferenceEntity u where u.userId is not null")
        @NonNull
        List<String> findDistinctUserIds();
}
