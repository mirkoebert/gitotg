package com.mirkoebert;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * English is the base bundle and German is the only translation. A key in one file and not the
 * other fails at request time for that locale, because nothing checks the catalogs at build time.
 */
class KeyParityTest {

    @Test
    void englishAndGermanMessageKeysMatch() throws IOException {
        Set<String> english = keys("messages.properties");
        Set<String> german = keys("messages_de.properties");

        Set<String> onlyEnglish = new TreeSet<>(english);
        onlyEnglish.removeAll(german);
        Set<String> onlyGerman = new TreeSet<>(german);
        onlyGerman.removeAll(english);

        assertThat(english).isNotEmpty();
        assertThat(onlyEnglish)
                .as("keys present in messages.properties but missing from messages_de.properties")
                .isEmpty();
        assertThat(onlyGerman)
                .as("keys present in messages_de.properties but missing from messages.properties")
                .isEmpty();
    }

    private static Set<String> keys(String resource) throws IOException {
        Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource(resource));
        return properties.stringPropertyNames();
    }
}
