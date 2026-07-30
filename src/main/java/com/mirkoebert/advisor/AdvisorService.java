package com.mirkoebert.advisor;

import com.mirkoebert.handicap.HandicapClassifier;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    static final String[] FRESH_KEYS = {
            "advisor.fresh.0",
            "advisor.fresh.1",
            "advisor.fresh.2",
            "advisor.fresh.3",
            "advisor.fresh.4",
            "advisor.fresh.5"
    };

    static final String[] FEW_KEYS = {
            "advisor.few.0",
            "advisor.few.1",
            "advisor.few.2",
            "advisor.few.3"
    };

    static final String[] HH_KEYS = {
            "advisor.hh.0",
            "advisor.hh.1",
            "advisor.hh.2",
            "advisor.hh.3",
            "advisor.hh.4",
            "advisor.hh.5",
            "advisor.hh.6",
            "advisor.hh.7"
    };

    static final String[] OTHER_KEYS = {
            "advisor.other.0",
            "advisor.other.1",
            "advisor.other.2",
            "advisor.other.3",
            "advisor.other.4",
            "advisor.other.5",
            "advisor.other.6",
            "advisor.other.7",
            "advisor.other.8",
            "advisor.other.9"
    };

    private final Random r = new Random();

    public String getAdvise(@NonNull final String userId){
        int c = hcpRepository.countByUserId(userId);
        c = c  + singleTestResultRepository.countByUserId(userId);

        Optional<HcpScoreEntity> hcp = hcpRepository.findFirstByUserIdOrderByDateDesc(userId);
        log.info("data points {}", c);

        if (c < 5){
            log.info("fresh");
            return message(FRESH_KEYS[r.nextInt(FRESH_KEYS.length)]);
        } else if (c < 25) {
            log.info("newby");
            return message(FEW_KEYS[r.nextInt(FEW_KEYS.length)]);
        } else if (hcp.isPresent() && HandicapClassifier.HIGH_HANDICAPER.equals(handicapClassifier.apply(hcp.get().getHcp()))) {
            log.info(HandicapClassifier.HIGH_HANDICAPER);
            return message(HH_KEYS[r.nextInt(HH_KEYS.length)]);
        }
        // analyze hcp
        // analyze sgi
        log.info("other");
        return message(OTHER_KEYS[r.nextInt(OTHER_KEYS.length)]);
    }

    private String message(final String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }


}
