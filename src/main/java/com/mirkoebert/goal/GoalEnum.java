package com.mirkoebert.goal;

import java.util.Arrays;
import java.util.Optional;

public enum GoalEnum {

        BREAK100("Break 100", "break100"),
        BREAK90("Break 90", "break90");

        private final String full;
        private final String slug;

        GoalEnum(String full, String slug) {
                this.full = full;
                this.slug = slug;
        }

        public String getFull() {
                return full;
        }

        public String getSlug() {
                return slug;
        }

        public static Optional<GoalEnum> fromSlug(String slug) {
                return Arrays.stream(values())
                        .filter(g -> g.slug.equalsIgnoreCase(slug))
                        .findFirst();
        }
}
