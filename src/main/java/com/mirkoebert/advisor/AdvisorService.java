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

    static final String BUCKET_FRESH = "fresh";
    static final String BUCKET_FEW = "few";
    static final String BUCKET_OTHER = "other";

    /**
     * Handicap tier to advice bucket. Buckets without tips in the message bundle simply stay empty,
     * so a new tier only needs {@code advisor.<bucket>.<n>} entries in messages.properties.
     */
    static final Map<String, String> TIER_BUCKETS = Map.of(
            HandicapClassifier.HIGH_HANDICAPER, "hh",
            HandicapClassifier.MID_HANDICAPER, "mh",
            HandicapClassifier.LOW_HANDICAPER, "lh",
            HandicapClassifier.SINGLE_FIGURE_PLAYER, "sfp",
            HandicapClassifier.SCRATCH_PLAYER, "scratch"
    );

    private final HcpRepository hcpRepository;
    private final SingleTestResultRepository singleTestResultRepository;
    private final HandicapClassifier handicapClassifier;
    private final MessageSource messageSource;
    private final AdviceCatalog adviceCatalog;
    private final Random r = new Random();


    private String message(final String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private @NonNull List<String> messages(@NonNull final String bucket) {
        return adviceCatalog.keys(bucket).stream().map(this::message).toList();
    }


    private @NonNull List<String> getAdviseListAmountOfDataPoints(@NonNull final String userId) {
        int c = hcpRepository.countByUserId(userId);
        c = c + singleTestResultRepository.countByUserId(userId);
        log.info("data points {}", c);

        if (c < 5) {
            log.info(BUCKET_FRESH);
            return messages(BUCKET_FRESH);
        } else if (c < 25) {
            log.info("newby");
            return messages(BUCKET_FEW);
        }
        return Collections.emptyList();
    }

    public @NonNull String getAdvise(@NonNull final String userId) {
        val resultlist = new ArrayList<String>();
        resultlist.addAll(getAdviseListAmountOfDataPoints(userId));
        resultlist.addAll(getAdviseListHandicap(userId));
        resultlist.addAll(getAdviseOthers());
        if (resultlist.isEmpty()) {
            log.warn("no advice available");
            return "";
        }
        return resultlist.get(r.nextInt(resultlist.size()));
    }

    private @NonNull Collection<String> getAdviseOthers() {
        return messages(BUCKET_OTHER);
    }

    private @NonNull Collection<String> getAdviseListHandicap(@NonNull String userId) {
        Optional<HcpScoreEntity> hcp = hcpRepository.findFirstByUserIdOrderByDateDesc(userId);
        if (hcp.isEmpty()) {
            return Collections.emptyList();
        }
        String tier = handicapClassifier.apply(hcp.get().getHcp());
        String bucket = TIER_BUCKETS.get(tier);
        if (bucket == null) {
            return Collections.emptyList();
        }
        log.info(tier);
        return messages(bucket);
    }
}
