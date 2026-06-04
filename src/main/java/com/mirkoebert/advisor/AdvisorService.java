package com.mirkoebert.advisor;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.handicap.HandicapClassifier;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvisorService {

        private final HcpRepository hcpRepository;
        private final SingleTestResultRepository singleTestResultRepository;
        private final HandicapClassifier handicapClassifier;

        static final String[] fresh = {
                "I need more data. Please start a test.",
                "Don't be shy, you can export all your data if you want.",
                "Let's produce some data.",
                "Data is what I need. Start a test.",
                "Data is what I need. Go to the chipping green and start a training.",
                "Monitoring your results is a cornerstone of improvement."
        };

        final static String[] few = {
                "I need more data. Please start a test.",
                "Keep up on golfing. I need more data to give more sophisticated advices.",
                "Keep up on golfing. You should start a Short Game test.",
                "Monitoring your results is a cornerstone of improvement."
        };


        final static String[] hh = {
                "High Handicaper lower sores faster by improving the short game.",
                "Short game is a cornerstone of improvement for High Handicapers."
        };

        final static String[] other ={
                "I have to think about it. Go on.",
                "Nice data. Keep up on golfing."
        };

        private final Random r = new Random();

        public String getAdvise(@NonNull final String userId){
                int c = hcpRepository.countByUserId(userId);
                c = c  + singleTestResultRepository.countByUserId(userId);

                Optional<HcpScoreEntity> hcp = hcpRepository.findFirstByUserIdOrderByDateDesc(userId);
                log.info("data points {}", c);
                
                if (c < 5){
                        log.info("fresh");
                        return fresh[r.nextInt(fresh.length)];
                } else if (c < 25) {
                        log.info("newby");
                        return few[r.nextInt(few.length)];
                } else if (hcp.isPresent() && HandicapClassifier.HIGH_HANDICAPER.equals(handicapClassifier.apply(hcp.get().getHcp()))) {
                        log.info(HandicapClassifier.HIGH_HANDICAPER);
                        return hh[r.nextInt(hh.length)];
                }
                // analyze hcp
                // analyze sgi
                log.info("other");
                return other[r.nextInt(other.length)];
        }
}
