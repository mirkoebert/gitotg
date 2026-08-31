package com.mirkoebert.advisor;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class AdviceCatalogTest {

    private final AdviceCatalog cut = new AdviceCatalog("messages");

    /** Deliberately not asserting exact counts - adding a tip must not break a test. */
    @Test
    void keys_discoversTheBucketsFromTheMessageBundle() {
        assertThat(cut.buckets()).contains("fresh", "few", "hh", "mh", "other");
        assertThat(cut.buckets()).allSatisfy(bucket -> assertThat(cut.keys(bucket)).isNotEmpty());
    }

    @Test
    void keys_ordersNumericSuffixesNumerically() {
        List<String> other = cut.keys("other");
        // a plain string sort would put advisor.other.10 before advisor.other.9
        assertThat(other).containsSubsequence("advisor.other.9", "advisor.other.10");
        assertThat(other).startsWith("advisor.other.0");
    }

    @Test
    void keys_returnsEmptyListForABucketWithoutTips() {
        // the handicap tiers that have no advice yet must stay silent, not blow up
        assertThat(cut.keys("does-not-exist")).isEmpty();
    }

    @Test
    void keys_isEmptyWhenTheBundleIsMissing() {
        assertThat(new AdviceCatalog("no-such-bundle").keys("other")).isEmpty();
    }

    @Test
    void everyDiscoveredKeyIsTranslatedToGerman() throws IOException {
        // Discovery reads the English bundle only, so a tip added to one file and not the other
        // would fail with NoSuchMessageException at request time for German users.
        Properties german = PropertiesLoaderUtils.loadProperties(new ClassPathResource("messages_de.properties"));

        List<String> allKeys = cut.buckets().stream()
                .flatMap(bucket -> cut.keys(bucket).stream())
                .toList();

        assertThat(allKeys).isNotEmpty();
        assertThat(allKeys).allSatisfy(key ->
                assertThat(german.getProperty(key))
                        .as("missing German translation for %s", key)
                        .isNotNull());
    }
}
