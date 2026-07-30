package com.mirkoebert.golfmetric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GMetricService {

    private final GMetricRepository repo;

    public @NonNull List<GMetricEntity> findByUserId(@NonNull String userId) {
        log.debug("findByUserId {}", userId);
        return repo.findByUserIdOrderByDateDesc(userId);
    }

    public @NonNull List<GMetricEntity> findByUserIdAndType(@NonNull String userId, @NonNull GMetricType type) {
        log.debug("findByUserIdAndType {} {}", userId, type);
        return repo.findByUserIdAndTypeOrderByDateDesc(userId, type);
    }
}
