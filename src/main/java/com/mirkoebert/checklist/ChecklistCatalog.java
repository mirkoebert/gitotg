package com.mirkoebert.checklist;

import com.mirkoebert.goal.GoalEnum;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Discovers the goal checklists by naming convention instead of hard-coding them, the same way
 * {@link com.mirkoebert.advisor.AdviceCatalog} discovers the advisor tips.
 * <p>
 * Every key of the form {@code checklist.<goalSlug>.<id>.name} found in the base message bundle
 * (English) becomes one item of that goal's checklist; an optional
 * {@code checklist.<goalSlug>.<id>.desc} adds the explanation below it. Adding an item therefore
 * only means adding a line to {@code messages.properties} and its translations - no Java change and
 * no database row. The ids stay stable because {@link GolfCheckEntity} stores them per user.
 */
@Component
@Slf4j
public class ChecklistCatalog {

    private static final Pattern NAME_KEY = Pattern.compile("^checklist\\.([^.]+)\\.(\\d+)\\.name$");
    private static final String DESC_SUFFIX = ".desc";

    private final Map<String, List<ChecklistItem>> itemsByGoalSlug;

    public ChecklistCatalog(@Value("${spring.messages.basename:messages}") final String basename) {
        this.itemsByGoalSlug = load(baseBundleResource(basename));
    }

    private static String baseBundleResource(final String basename) {
        // spring.messages.basename may be a comma separated list - the first entry is our base bundle
        final String first = basename.split(",")[0].trim();
        return first.replace('.', '/') + ".properties";
    }

    private static Map<String, List<ChecklistItem>> load(final String resource) {
        final Properties properties;
        try {
            properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource(resource));
        } catch (IOException e) {
            // Fail soft: an empty checklist is better than a broken goal page.
            log.warn("Cannot read checklist keys from {} - checklists stay empty", resource, e);
            return Collections.emptyMap();
        }
        final Map<String, List<ChecklistItem>> byGoal = properties.stringPropertyNames().stream()
                .map(NAME_KEY::matcher)
                .filter(Matcher::matches)
                .collect(Collectors.groupingBy(
                        m -> m.group(1),
                        Collectors.mapping(m -> toItem(properties, m), Collectors.toList())));
        byGoal.replaceAll((goal, items) -> items.stream()
                .sorted(Comparator.comparingLong(ChecklistItem::id))
                .toList());
        log.info("checklist items {}", byGoal.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));
        return Map.copyOf(byGoal);
    }

    private static ChecklistItem toItem(final Properties properties, final Matcher nameKey) {
        final String prefix = "checklist." + nameKey.group(1) + "." + nameKey.group(2);
        final String descKey = prefix + DESC_SUFFIX;
        return new ChecklistItem(
                Long.parseLong(nameKey.group(2)),
                nameKey.group(),
                properties.containsKey(descKey) ? descKey : null);
    }

    /**
     * @return the checklist of that goal in stable id order, empty when the bundle defines none (yet).
     */
    public @NonNull List<ChecklistItem> items(@NonNull final GoalEnum goal) {
        return itemsByGoalSlug.getOrDefault(goal.getSlug(), List.of());
    }

    /**
     * @return every message key the checklists use, for translation coverage checks.
     */
    public @NonNull List<String> allKeys() {
        return itemsByGoalSlug.values().stream()
                .flatMap(List::stream)
                .flatMap(item -> item.descKey() == null
                        ? Stream.of(item.nameKey())
                        : Stream.of(item.nameKey(), item.descKey()))
                .toList();
    }
}
