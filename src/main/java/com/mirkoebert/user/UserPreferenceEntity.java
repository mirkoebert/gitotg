package com.mirkoebert.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preference")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    /**
     * ISO language code, e.g. {@code en} or {@code de}.
     */
    @Column(nullable = false, length = 8)
    private String language;
}
