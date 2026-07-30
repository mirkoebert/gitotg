package com.mirkoebert.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferenceService {

    public static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "de");

    private final UserPreferenceRepository repository;

    private static String normalize(String language) {
        if (language == null) {
            return "";
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isSupported(@NonNull String language) {
        return SUPPORTED_LANGUAGES.contains(normalize(language));
    }

    public Optional<Locale> findLocale(@NonNull String userId) {
        return repository.findById(userId)
                .map(UserPreferenceEntity::getLanguage)
                .filter(this::isSupported)
                .map(Locale::forLanguageTag);
    }

    @Transactional
    public void saveLanguage(@NonNull String userId, @NonNull String language) {
        String normalized = normalize(language);
        if (!SUPPORTED_LANGUAGES.contains(normalized)) {
            log.warn("Ignoring unsupported language preference '{}' for user {}", language, userId);
            return;
        }
        UserPreferenceEntity entity = repository.findById(userId)
                .orElseGet(() -> UserPreferenceEntity.builder().userId(userId).build());
        entity.setLanguage(normalized);
        repository.save(entity);
        log.debug("Saved language preference {} for user {}", normalized, userId);
    }
}
