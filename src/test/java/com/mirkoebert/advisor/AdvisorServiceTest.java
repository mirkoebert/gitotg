package com.mirkoebert.advisor;

import com.mirkoebert.handicap.HandicapClassifier;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({AdvisorService.class, AdviceCatalog.class, HandicapClassifier.class, AdvisorServiceTest.MessageSourceConfig.class})
@TestPropertySource(properties = "spring.messages.basename=messages")
class AdvisorServiceTest {

    @Autowired
    private AdvisorService cut;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AdviceCatalog adviceCatalog;

    @MockitoBean
    private HcpRepository hcpRepository;

    @MockitoBean
    private SingleTestResultRepository singleTestResultRepository;

    @BeforeEach
    void startFromAKnownState() {
        // cut is a singleton across the test class, so the queue would leak between tests
        cut.lastAdvices.clear();
        // The SUT resolves through LocaleContextHolder while resolve() below pins English. Without
        // an explicit locale the holder falls back to Locale.getDefault(), and the whole class
        // fails on a machine or CI runner whose default is not English.
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void forgetRememberedAdvice() {
        cut.lastAdvices.clear();
        LocaleContextHolder.resetLocaleContext();
    }

    /** The keys a "fresh" user can be shown: the fresh bucket plus the always-mixed-in other one. */
    private List<String> freshUserKeys() {
        return Stream.concat(
                adviceCatalog.keys(AdvisorService.BUCKET_FRESH).stream(),
                adviceCatalog.keys(AdvisorService.BUCKET_OTHER).stream()).toList();
    }

    private void hasDataPoints(String userId, int hcpCount) {
        when(hcpRepository.countByUserId(userId)).thenReturn(hcpCount);
        when(singleTestResultRepository.countByUserId(userId)).thenReturn(0);
    }

    private String[] resolve(String bucket) {
        return adviceCatalog.keys(bucket).stream()
                .map(key -> messageSource.getMessage(key, null, Locale.ENGLISH))
                .toArray(String[]::new);
    }

    @Test
    void getAdvise_returnsFreshMessageForLessThan5DataPoints() {
        when(hcpRepository.countByUserId("u1")).thenReturn(3);
        when(singleTestResultRepository.countByUserId("u1")).thenReturn(1);

        String advice = cut.getAdvise("u1");

        assertThat(advice).isIn(
                Stream.concat(
                        Stream.of(resolve(AdvisorService.BUCKET_FRESH)),
                        Stream.of(resolve(AdvisorService.BUCKET_OTHER))
                ).toArray());

        verify(hcpRepository).countByUserId("u1");
        verify(singleTestResultRepository).countByUserId("u1");
    }

    @Test
    void getAdvise_returnsFewMessageFor5To24DataPoints() {
        when(hcpRepository.countByUserId("u2")).thenReturn(12);
        when(singleTestResultRepository.countByUserId("u2")).thenReturn(8);

        String advice = cut.getAdvise("u2");

        assertThat(advice).isIn(
                Stream.concat(
                        Stream.of(resolve(AdvisorService.BUCKET_FEW)),
                        Stream.of(resolve(AdvisorService.BUCKET_OTHER))
                ).toArray());
    }

    @Test
    void getAdvise_returnsOtherMessageFor25OrMoreDataPoints() {
        when(hcpRepository.countByUserId("u3")).thenReturn(20);
        when(singleTestResultRepository.countByUserId("u3")).thenReturn(10);

        String advice = cut.getAdvise("u3");

        assertThat(advice).isIn(Stream.of(resolve(AdvisorService.BUCKET_OTHER)).toArray());
    }

    @Test
    void getAdvise_sumsCountsFromBothRepositories() {
        when(hcpRepository.countByUserId("u4")).thenReturn(2);
        when(singleTestResultRepository.countByUserId("u4")).thenReturn(2);

        cut.getAdvise("u4");

        // total 4 < 5 -> fresh bucket
        // just verifying the counts were summed via the mocks
        verify(hcpRepository).countByUserId("u4");
        verify(singleTestResultRepository).countByUserId("u4");
    }

    @Test
    void getAdvise_returnsHighHandicaperMessageForEnoughDataAndHighHcp() {
        when(hcpRepository.countByUserId("u5")).thenReturn(30);
        when(singleTestResultRepository.countByUserId("u5")).thenReturn(5);

        HcpScoreEntity entity = HcpScoreEntity.builder().hcp(28.5).build();
        when(hcpRepository.findFirstByUserIdOrderByDateDesc("u5")).thenReturn(Optional.of(entity));

        String advice = cut.getAdvise("u5");

        assertThat(advice).isIn(
                Stream.concat(
                        Stream.of(resolve("hh")),
                        Stream.of(resolve(AdvisorService.BUCKET_OTHER))
                ).toArray());
    }

    @Test
    void getAdvise_returnsLowHandicaperMessageForEnoughDataAndLowHcp() {
        when(hcpRepository.countByUserId("u6")).thenReturn(25);
        when(singleTestResultRepository.countByUserId("u6")).thenReturn(5);

        HcpScoreEntity entity = HcpScoreEntity.builder().hcp(12.0).build();
        when(hcpRepository.findFirstByUserIdOrderByDateDesc("u6")).thenReturn(Optional.of(entity));

        String advice = cut.getAdvise("u6");

        assertThat(advice).isIn(
                Stream.concat(
                        Stream.of(resolve("lh")),
                        Stream.of(resolve(AdvisorService.BUCKET_OTHER))
                ).toArray());
    }

    @Test
    void getAdvise_returnsMidHandicaperMessageForEnoughDataAndMidHcp() {
        when(hcpRepository.countByUserId("u7")).thenReturn(30);
        when(singleTestResultRepository.countByUserId("u7")).thenReturn(5);

        HcpScoreEntity entity = HcpScoreEntity.builder().hcp(20.0).build();
        when(hcpRepository.findFirstByUserIdOrderByDateDesc("u7")).thenReturn(Optional.of(entity));

        String advice = cut.getAdvise("u7");

        assertThat(advice).isIn(
                Stream.concat(
                        Stream.of(resolve("mh")),
                        Stream.of(resolve(AdvisorService.BUCKET_OTHER))
                ).toArray());
    }

    @Test
    void getAdvise_remembersTheMessageKeyRatherThanTheResolvedText() {
        hasDataPoints("u10", 2);

        String advice = cut.getAdvise("u10");

        String remembered = cut.rememberedKey("u10");
        assertThat(remembered)
                .as("a key must be stored, so a repeat re-resolves in the request locale")
                .startsWith("advisor.")
                .isIn(freshUserKeys());
        assertThat(remembered).isNotEqualTo(advice);
    }

    @Test
    void getAdvise_remembersEachUserSeparately() {
        hasDataPoints("u11", 2);
        hasDataPoints("u12", 30);

        cut.getAdvise("u11");
        cut.getAdvise("u12");

        assertThat(cut.rememberedKey("u11")).isIn(freshUserKeys());
        // 30 data points is past the "few" threshold, so only the other bucket applies
        assertThat(cut.rememberedKey("u12")).isIn(adviceCatalog.keys(AdvisorService.BUCKET_OTHER));
    }

    @Test
    void getAdvise_neverRepeatsAKeyThatLeftTheUsersBuckets() {
        hasDataPoints("u13", 2);

        for (int i = 0; i < 50; i++) {
            // re-seeded every iteration: a single seed would be overwritten by the first draw that
            // takes the 60% branch, and the guard would then never be exercised again
            cut.remember("u13", "advisor.hh.1");

            String advice = cut.getAdvise("u13");

            // a handicap-tier key this user no longer qualifies for must not come back; it would
            // show advice for the wrong tier, and a key dropped from the bundle would not resolve
            assertThat(cut.rememberedKey("u13")).isIn(freshUserKeys());
            assertThat(advice).isIn(Stream.concat(
                    Stream.of(resolve(AdvisorService.BUCKET_FRESH)),
                    Stream.of(resolve(AdvisorService.BUCKET_OTHER))).toArray());
        }
    }

    @Test
    void getAdvise_resolvesARepeatInTheCurrentLocale() {
        hasDataPoints("u14", 2);
        String key = adviceCatalog.keys(AdvisorService.BUCKET_OTHER).getFirst();
        String german = messageSource.getMessage(key, null, Locale.GERMAN);
        String english = messageSource.getMessage(key, null, Locale.ENGLISH);
        assertThat(german)
                .as("the fixture key needs differing translations or this test cannot fail")
                .isNotEqualTo(english);

        cut.remember("u14", key);
        LocaleContextHolder.setLocale(Locale.GERMAN);

        // Whichever branch the coin lands on, the text must come from the German bundle. Comparing
        // against the English resolution is what gives this teeth: re-deriving the expected value
        // from the same key and locale the implementation uses would hold by construction.
        String[] englishPool = Stream.concat(
                Stream.of(resolve(AdvisorService.BUCKET_FRESH)),
                Stream.of(resolve(AdvisorService.BUCKET_OTHER))).toArray(String[]::new);
        for (int i = 0; i < 50; i++) {
            String advice = cut.getAdvise("u14");
            assertThat(advice).isNotIn((Object[]) englishPool);
        }
    }

    @Test
    void getAdvise_evictsTheLeastRecentlySeenUserAtTheCap() {
        when(hcpRepository.countByUserId(org.mockito.ArgumentMatchers.anyString())).thenReturn(2);
        when(singleTestResultRepository.countByUserId(org.mockito.ArgumentMatchers.anyString())).thenReturn(0);

        cut.getAdvise("first-user");
        for (int i = 0; i < AdvisorService.MAX_REMEMBERED_USERS; i++) {
            cut.getAdvise("filler-" + i);
        }

        assertThat(cut.lastAdvices)
                .as("the queue must stay bounded rather than growing for the life of the JVM")
                .hasSizeLessThanOrEqualTo(AdvisorService.MAX_REMEMBERED_USERS);
        assertThat(cut.rememberedKey("first-user")).isNull();
        assertThat(cut.rememberedKey("filler-" + (AdvisorService.MAX_REMEMBERED_USERS - 1))).isNotNull();
    }

    @Test
    void getAdvise_keepsOneEntryPerUser() {
        hasDataPoints("u20", 2);

        for (int i = 0; i < 20; i++) {
            cut.getAdvise("u20");
        }

        // the queue has no notion of a key, so remember() dedupes by hand; without that every call
        // would append and the real capacity would collapse to a handful of users
        assertThat(cut.lastAdvices).hasSize(1);
        assertThat(cut.rememberedKey("u20")).isIn(freshUserKeys());
    }

    @Test
    void getAdvise_keepsAUserThatKeepsComingBack() {
        when(hcpRepository.countByUserId(org.mockito.ArgumentMatchers.anyString())).thenReturn(2);
        when(singleTestResultRepository.countByUserId(org.mockito.ArgumentMatchers.anyString())).thenReturn(0);

        cut.getAdvise("regular");
        for (int i = 0; i < AdvisorService.MAX_REMEMBERED_USERS - 1; i++) {
            cut.getAdvise("stranger-" + i);
        }

        // "regular" is now the oldest entry, so coming back must move them to the young end -
        // updating the entry in place would leave them first in line for the next eviction
        cut.getAdvise("regular");
        for (int i = 0; i < 10; i++) {
            cut.getAdvise("latecomer-" + i);
        }

        assertThat(cut.rememberedKey("regular"))
                .as("an active user must not be evicted by strangers passing through")
                .isNotNull();
    }

    @Configuration
    static class MessageSourceConfig {
        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        MessageSource messageSource() {
            ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
            ms.setBasename("messages");
            ms.setDefaultEncoding("UTF-8");
            ms.setFallbackToSystemLocale(false);
            return ms;
        }
    }
}
