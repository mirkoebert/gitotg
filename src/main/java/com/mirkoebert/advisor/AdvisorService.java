package com.mirkoebert.advisor;

import com.mirkoebert.handicap.HandicapClassifier;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvisorService {

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
    static final String[] MH_KEYS = {
            "advisor.mh.0",
            "advisor.mh.1",
            "advisor.mh.2",
            "advisor.mh.3",
            "advisor.mh.4",
            "advisor.mh.5",
            "advisor.mh.6",
            "advisor.mh.7"
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
            "advisor.other.9",
            "advisor.other.10",
            "advisor.other.11"
    };
    private final HcpRepository hcpRepository;
    private final SingleTestResultRepository singleTestResultRepository;
    private final HandicapClassifier handicapClassifier;
    private final MessageSource messageSource;
    private final Random r = new Random();


    private String message(final String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }


    public @NonNull List<String> getAdviseListAmountOfDataPoints(@NonNull final String userId) {
        int c = hcpRepository.countByUserId(userId);
        c = c + singleTestResultRepository.countByUserId(userId);
        log.info("data points {}", c);

        if (c < 5) {
            log.info("fresh");
            return Arrays.stream(FRESH_KEYS).map(this::message).toList();
        } else if (c < 25) {
            log.info("newby");
            return Arrays.stream(FEW_KEYS).map(this::message).toList();
        }
        return Collections.emptyList();
    }

    public String getAdvise(@NonNull final String userId) {
        val resultlist = new ArrayList<String>();
        resultlist.addAll(getAdviseListAmountOfDataPoints(userId));
        resultlist.addAll(getAdviseListHandicap(userId));
        resultlist.addAll(getAdviseOthers());
        return resultlist.get(r.nextInt(resultlist.size()));
    }

    private @NonNull Collection<String> getAdviseOthers() {
        return Arrays.stream(OTHER_KEYS).map(this::message).toList();
    }

    private @NonNull Collection<String> getAdviseListHandicap(@NonNull String userId) {
        Optional<HcpScoreEntity> hcp = hcpRepository.findFirstByUserIdOrderByDateDesc(userId);
        if (hcp.isEmpty()) {
            return Collections.emptyList();
        }
        String tier = handicapClassifier.apply(hcp.get().getHcp());
        if (HandicapClassifier.HIGH_HANDICAPER.equals(tier)) {
            log.info(HandicapClassifier.HIGH_HANDICAPER);
            return Arrays.stream(HH_KEYS).map(this::message).toList();
        } else if (HandicapClassifier.MID_HANDICAPER.equals(tier)) {
            log.info(HandicapClassifier.MID_HANDICAPER);
            return Arrays.stream(MH_KEYS).map(this::message).toList();
        }
        return Collections.emptyList();
    }
}
