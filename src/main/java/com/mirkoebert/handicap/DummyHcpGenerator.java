package com.mirkoebert.handicap;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.mirkoebert.Constants.ME;

//@ConditionalOnProperty(prefix = "features", name = "load-dummy-data", havingValue = "true")
@Service
@RequiredArgsConstructor
@Slf4j
class DummyHcpGenerator {

        private final HcpRepository repo;

        @PostConstruct
        void init() {
                if (repo.countByUserId(ME) > 0) {
                        log.debug("There is data in the db. Skip loading initial data set");
                        return;
                }
                log.info("Load dummy data into empty table");
                repo.deleteAll();


                LocalDate ld1 = LocalDate.of(2025, 9, 7);
                HcpScoreEntity d1 = HcpScoreEntity
                        .builder()
                        .hcp(26.0)
                        .date(ld1)
                        .userId(ME)
                        .build();
                repo.save(d1);
                ld1 = LocalDate.of(2025, 8, 16);
                d1 = HcpScoreEntity
                        .builder()
                        .hcp(28.9)
                        .date(ld1)
                        .userId(ME)
                        .build();
                ld1 = LocalDate.of(2025, 6, 9);
                d1 = HcpScoreEntity
                        .builder()
                        .hcp(29.4)
                        .date(ld1)
                        .userId(ME)
                        .build();
                repo.save(d1);
                ld1 = LocalDate.of(2024, 10, 2);
                d1 = HcpScoreEntity
                        .builder()
                        .hcp(30.2)
                        .date(ld1)
                        .userId(ME)
                        .build();
                repo.save(d1);
                ld1 = LocalDate.of(2024, 9, 1);
                d1 = HcpScoreEntity
                        .builder()
                        .hcp(32.1)
                        .date(ld1)
                        .userId(ME)
                        .build();
                repo.save(d1);
                ld1 = LocalDate.of(2024, 8, 14);
                d1 = HcpScoreEntity
                        .builder()
                        .hcp(32.4)
                        .date(ld1)
                        .userId(ME)
                        .build();
                repo.save(d1);
                repo.flush();
        }

}
