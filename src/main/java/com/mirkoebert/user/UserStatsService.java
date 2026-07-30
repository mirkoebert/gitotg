package com.mirkoebert.user;

import com.mirkoebert.checklist.GolfCheckEntityRepository;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Aggregates user statistics from stored app data (OAuth identities appear as userId columns).
 */
@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final HcpRepository hcpRepository;
    private final SingleTestResultRepository sgiRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final GMetricRepository gMetricRepository;
    private final GolfCheckEntityRepository golfCheckEntityRepository;

    public long countUsers() {
        Set<String> userIds = new HashSet<>();
        userIds.addAll(hcpRepository.findDistinctUserIds());
        userIds.addAll(sgiRepository.findDistinctUserIds());
        userIds.addAll(userPreferenceRepository.findDistinctUserIds());
        userIds.addAll(gMetricRepository.findDistinctUserIds());
        userIds.addAll(golfCheckEntityRepository.findDistinctUserIds());
        userIds.remove(null);
        userIds.remove("");
        return userIds.size();
    }
}
