package com.mirkoebert.advisor;

import com.mirkoebert.handicap.HandicapClassifier;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

    /** Chance of showing the same tip again, so a tip stays put long enough to be read. */
    private static final double REPEAT_PROBABILITY = 0.4;

    /**
     * Cap on remembered users. Far above the real user count of this personal app, but it bounds
     * the one piece of process-lifetime state on a deliberately memory-tuned JVM. Evicting a user
     * costs nothing: their next visit simply draws a fresh tip.
     */
    static final int MAX_REMEMBERED_USERS = 500;

    private final HcpRepository hcpRepository;
    private final SingleTestResultRepository singleTestResultRepository;
    private final HandicapClassifier handicapClassifier;
    private final MessageSource messageSource;
    private final AdviceCatalog adviceCatalog;
    private final Random r = new Random();

    /** One user's remembered tip. */
    record RememberedAdvice(String userId, String adviceKey) {
    }

    /**
     * Last advice key per user, oldest first, bounded by construction - a full queue drops its head
     * on the next add. Message keys rather than resolved text, so a repeat is re-resolved in
     * whatever locale the current request uses. In memory only - deliberately not persisted, so a
     * restart simply starts everyone fresh.
     * <p>
     * {@link CircularFifoQueue} is not thread safe and both accessors below are compound
     * operations, so every access goes through them and each is synchronized. The read in
     * {@code chooseKey} and the later write are separate critical sections, so two simultaneous
     * requests for one user can lose an update - the queue stays intact and the only effect is a
     * tip that fails to stick. Holding a lock across {@code getAdvise} is not worth it: it would
     * serialise the repository queries that dominate the call.
     */
    final CircularFifoQueue<RememberedAdvice> lastAdvices = new CircularFifoQueue<>(MAX_REMEMBERED_USERS);

    /** @return the key last returned to this user, or {@code null} if they are not remembered. */
    synchronized @Nullable String rememberedKey(final String userId) {
        return lastAdvices.stream()
                .filter(a -> a.userId().equals(userId))
                .map(RememberedAdvice::adviceKey)
                .findFirst()
                .orElse(null);
    }

    synchronized void remember(final String userId, final String adviceKey) {
        // One entry per user, and re-adding moves them to the young end, so an active user is not
        // evicted by MAX_REMEMBERED_USERS strangers passing through.
        lastAdvices.removeIf(a -> a.userId().equals(userId));
        lastAdvices.add(new RememberedAdvice(userId, adviceKey));
    }


    private String message(final String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }



    private @NonNull List<String> getAdviseListAmountOfDataPoints(@NonNull final String userId) {
        int c = hcpRepository.countByUserId(userId);
        c = c + singleTestResultRepository.countByUserId(userId);
        log.info("data points {}", c);

        if (c < 5) {
            log.info(BUCKET_FRESH);
            return adviceCatalog.keys(BUCKET_FRESH);
        } else if (c < 25) {
            log.info("newby");
            return adviceCatalog.keys(BUCKET_FEW);
        }
        return Collections.emptyList();
    }

    public @NonNull String getAdvise(@NonNull final String userId) {
        val candidateKeys = new ArrayList<String>();
        candidateKeys.addAll(getAdviseListAmountOfDataPoints(userId));
        candidateKeys.addAll(getAdviseListHandicap(userId));
        candidateKeys.addAll(getAdviseOthers());
        if (candidateKeys.isEmpty()) {
            log.warn("no advice available");
            return "";
        }
        final String key = chooseKey(userId, candidateKeys);
        remember(userId, key);
        return message(key);
    }

    /**
     * The remembered key is only repeated while it is still a candidate. A user's buckets move as
     * they log data or their handicap changes, and a stale key would either show advice for a tier
     * they left or, once removed from the bundle, fail to resolve at all.
     */
    private String chooseKey(final String userId, final List<String> candidateKeys) {
        final String lastKey = rememberedKey(userId);
        if (lastKey != null && candidateKeys.contains(lastKey) && r.nextDouble() < REPEAT_PROBABILITY) {
            log.debug("repeating advice {} for user {}", lastKey, userId);
            return lastKey;
        }
        return candidateKeys.get(r.nextInt(candidateKeys.size()));
    }

    private @NonNull Collection<String> getAdviseOthers() {
        return adviceCatalog.keys(BUCKET_OTHER);
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
        return adviceCatalog.keys(bucket);
    }
}
