package com.mirkoebert.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Discovers the advice message keys by naming convention instead of hard-coding them.
 * <p>
 * Every key of the form {@code advisor.<bucket>.<suffix>} found in the base message bundle
 * (English) becomes part of {@code <bucket>}. Adding a tip therefore only means adding a line to
 * {@code messages.properties} and its translations - no Java change. The base bundle decides
 * <em>which</em> keys exist; the localized text is still resolved through the
 * {@link org.springframework.context.MessageSource} at request time.
 */
@Component
@Slf4j
public class AdviceCatalog {

    private static final Pattern ADVICE_KEY = Pattern.compile("^advisor\\.([^.]+)\\.(.+)$");
    private static final Pattern NUMERIC = Pattern.compile("^\\d+$");

    private final Map<String, List<String>> keysByBucket;

    public AdviceCatalog(@Value("${spring.messages.basename:messages}") final String basename) {
        this.keysByBucket = load(baseBundleResource(basename));
    }

    private static String baseBundleResource(final String basename) {
        // spring.messages.basename may be a comma separated list - the first entry is our base bundle
        final String first = basename.split(",")[0].trim();
        return first.replace('.', '/') + ".properties";
    }

    private static Map<String, List<String>> load(final String resource) {
        final Properties properties;
        try {
            properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource(resource));
        } catch (IOException e) {
            // Fail soft: no tips is better than a broken cockpit page.
            log.warn("Cannot read advice keys from {} - advisor stays silent", resource, e);
            return Collections.emptyMap();
        }
        final Map<String, List<String>> buckets = properties.stringPropertyNames().stream()
                .map(ADVICE_KEY::matcher)
                .filter(Matcher::matches)
                .collect(Collectors.groupingBy(
                        m -> m.group(1),
                        Collectors.mapping(Matcher::group, Collectors.toList())));
        buckets.replaceAll((bucket, keys) -> keys.stream().sorted(bySuffix()).toList());
        log.info("advice buckets {}", buckets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));
        return Map.copyOf(buckets);
    }

    /**
     * Orders {@code advisor.other.10} after {@code advisor.other.9} - a plain string sort would not.
     * Non numeric suffixes fall back to lexicographic order and sort after the numbered ones.
     */
    private static Comparator<String> bySuffix() {
        return Comparator.comparingInt(AdviceCatalog::orderOf).thenComparing(AdviceCatalog::suffixOf);
    }

    private static int orderOf(final String key) {
        final String suffix = suffixOf(key);
        if (NUMERIC.matcher(suffix).matches()) {
            return Integer.parseInt(suffix);
        }
        return Integer.MAX_VALUE;
    }

    private static String suffixOf(final String key) {
        final Matcher m = ADVICE_KEY.matcher(key);
        if (m.matches()) {
            return m.group(2);
        }
        return key;
    }

    /**
     * @return every bucket the message bundle defines tips for.
     */
    public @NonNull Set<String> buckets() {
        return keysByBucket.keySet();
    }

    /**
     * @return the keys of that bucket in stable order, empty when the bucket has no tips (yet).
     */
    public @NonNull List<String> keys(@NonNull final String bucket) {
        return keysByBucket.getOrDefault(bucket, List.of());
    }
}
