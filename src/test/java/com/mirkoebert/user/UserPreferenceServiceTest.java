package com.mirkoebert.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserPreferenceRepository repository;

    private UserPreferenceService service;

    @BeforeEach
    void setUp() {
        service = new UserPreferenceService(repository);
    }

    @Test
    void saveAndFindLocale_roundTrip() {
        when(repository.findById("user-1")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(UserPreferenceEntity.builder().userId("user-1").language("de").build()));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.saveLanguage("user-1", "de");

        ArgumentCaptor<UserPreferenceEntity> captor = ArgumentCaptor.forClass(UserPreferenceEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("user-1");
        assertThat(captor.getValue().getLanguage()).isEqualTo("de");
        assertThat(service.findLocale("user-1")).contains(Locale.forLanguageTag("de"));
    }

    @Test
    void saveLanguage_updatesExisting() {
        UserPreferenceEntity existing = UserPreferenceEntity.builder().userId("user-2").language("en").build();
        when(repository.findById("user-2")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.saveLanguage("user-2", "de");

        assertThat(existing.getLanguage()).isEqualTo("de");
        verify(repository).save(existing);
    }

    @Test
    void saveLanguage_ignoresUnsupported() {
        service.saveLanguage("user-3", "fr");

        verify(repository, never()).save(any());
        assertThat(service.findLocale("user-3")).isEmpty();
    }

    @Test
    void isSupported_onlyEnAndDe() {
        assertThat(service.isSupported("en")).isTrue();
        assertThat(service.isSupported("DE")).isTrue();
        assertThat(service.isSupported("fr")).isFalse();
        assertThat(service.isSupported("")).isFalse();
    }

    @Test
    void findLocale_emptyWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThat(service.findLocale("missing")).isEmpty();
    }
}
