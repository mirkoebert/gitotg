package com.mirkoebert.sgi;

import com.mirkoebert.Constants;
import com.mirkoebert.TestSuite;
import com.mirkoebert.sgi.calc.PointsToSgiHcpFunction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.mirkoebert.Constants.ME;

@ConditionalOnProperty(prefix = "features", name = "load-dummy-data", havingValue = "true")
@Service
@RequiredArgsConstructor
@Slf4j
class DummySgiResultGenerator {

        private final SingleTestResultRepository repo;
        private final PointsToSgiHcpFunction pointsToSgiHcpFunction;

        @PostConstruct
        void init() {
                if (repo.countByUserId(ME) > 0) {
                        log.debug("There is data in the db. Skip loading initial data set");
                        return;
                }
                log.info("Load dummy data");
                LocalDate ld1;
                repo.deleteAll();

                ld1 = LocalDate.of(2025, 5, 2);
                SingleTestResultEntity d1 = SingleTestResultEntity
                        .builder()
                        .testId(1)
                        .points(5)
                        .hcp(pointsToSgiHcpFunction.apply(1, 5))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(2)
                        .points(3)
                        .hcp(pointsToSgiHcpFunction.apply(2, 3))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(3)
                        .points(9)
                        .hcp(pointsToSgiHcpFunction.apply(3, 9))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(4)
                        .points(1)
                        .hcp(pointsToSgiHcpFunction.apply(4, 1))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(5)
                        .points(7)
                        .hcp(pointsToSgiHcpFunction.apply(5, 7))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(6)
                        .points(3)
                        .hcp(pointsToSgiHcpFunction.apply(6, 3))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);


                ld1 = LocalDate.of(2025, 6, 21);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(1)
                        .points(2)
                        .hcp(pointsToSgiHcpFunction.apply(1, 2))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(2)
                        .points(5)
                        .hcp(pointsToSgiHcpFunction.apply(2, 5))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(3)
                        .points(4)
                        .hcp(pointsToSgiHcpFunction.apply(3, 4))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(4)
                        .points(1)
                        .hcp(pointsToSgiHcpFunction.apply(4, 1))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(5)
                        .points(11)
                        .hcp(pointsToSgiHcpFunction.apply(5, 11))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(6)
                        .points(7)
                        .hcp(pointsToSgiHcpFunction.apply(6, 7))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(7)
                        .points(18)
                        .hcp(pointsToSgiHcpFunction.apply(7, 18))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);
                d1 = SingleTestResultEntity
                        .builder()
                        .testId(8)
                        .points(4)
                        .hcp(pointsToSgiHcpFunction.apply(8, 4))
                        .date(ld1)
                        .testType(TestSuite.SGI)
                        .userId(Constants.ME)
                        .build();
                repo.save(d1);

                repo.flush();
        }

}
