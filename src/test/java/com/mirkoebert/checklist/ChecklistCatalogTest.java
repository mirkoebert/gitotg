package com.mirkoebert.checklist;

import com.mirkoebert.goal.GoalEnum;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ChecklistCatalogTest {

    private final ChecklistCatalog cut = new ChecklistCatalog("messages");

    /** Deliberately not asserting exact counts - adding a checklist item must not break a test. */
    @Test
    void items_discoversAChecklistForEveryGoal() {
        for (GoalEnum goal : GoalEnum.values()) {
            assertThat(cut.items(goal))
                    .as("checklist of %s", goal)
                    .isNotEmpty();
        }
    }

    @Test
    void items_areOrderedByIdAndCarryTheirMessageKeys() {
        List<ChecklistItem> items = cut.items(GoalEnum.BREAK100);

        assertThat(items).isSortedAccordingTo(Comparator.comparingLong(ChecklistItem::id));
        assertThat(items).extracting(ChecklistItem::nameKey)
                .allSatisfy(key -> assertThat(key).startsWith("checklist.break100.").endsWith(".name"));
        assertThat(items.getFirst().id()).isEqualTo(1L);
        assertThat(items.getFirst().nameKey()).isEqualTo("checklist.break100.1.name");
        assertThat(items.getFirst().descKey()).isEqualTo("checklist.break100.1.desc");
    }

    @Test
    void items_keepTheIdsStoredPerUser() {
        // GolfCheckEntity rows reference these ids - renumbering would silently lose a user's checks
        assertThat(cut.items(GoalEnum.BREAK80)).extracting(ChecklistItem::id)
                .contains(19L, 20L);
    }

    @Test
    void items_isEmptyWhenTheBundleIsMissing() {
        assertThat(new ChecklistCatalog("no-such-bundle").items(GoalEnum.BREAK100)).isEmpty();
    }

    @Test
    void everyDiscoveredKeyIsTranslatedToGerman() throws IOException {
        // Discovery reads the English bundle only, so an item added to one file and not the other
        // would fail with NoSuchMessageException at request time for German users.
        Properties german = PropertiesLoaderUtils.loadProperties(new ClassPathResource("messages_de.properties"));

        List<String> allKeys = cut.allKeys();

        assertThat(allKeys).isNotEmpty();
        assertThat(allKeys).allSatisfy(key ->
                assertThat(german.getProperty(key))
                        .as("missing German translation for %s", key)
                        .isNotNull());
    }

    @Test
    void everyGoalTitleIsTranslatedToGerman() throws IOException {
        Properties english = PropertiesLoaderUtils.loadProperties(new ClassPathResource("messages.properties"));
        Properties german = PropertiesLoaderUtils.loadProperties(new ClassPathResource("messages_de.properties"));

        for (GoalEnum goal : GoalEnum.values()) {
            assertThat(english.getProperty(goal.getTitleKey()))
                    .as("missing English title for %s", goal)
                    .isNotNull();
            assertThat(german.getProperty(goal.getTitleKey()))
                    .as("missing German title for %s", goal)
                    .isNotNull();
        }
    }
}
