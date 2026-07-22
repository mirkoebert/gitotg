package com.mirkoebert.sgi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SgiHcpAggregatedService {

        private final SingleTestResultRepository repo;
        private final SgiTestRepo sgiTestRepo;
        private final SgiTestSuiteHcpFunction sgiTestSuiteHcpFunction;

        public int getLatestSgiHcpAggregated(final String userId) {
                int sumPoints = 0;
                for (int i = 1; i <= sgiTestRepo.count(); i++) {
                        Optional<SingleTestResultEntity> s1l = repo.findFirstByUserIdAndTestIdOrderByDateDesc(userId, i);
                        if (s1l.isPresent()) {
                                sumPoints = sumPoints + s1l.get().getPoints();
                        }
                }

                return sgiTestSuiteHcpFunction.apply(sumPoints);
        }

}
