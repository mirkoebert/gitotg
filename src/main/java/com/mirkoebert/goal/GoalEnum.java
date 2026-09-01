package com.mirkoebert.goal;

import java.util.Arrays;
import java.util.Optional;

public enum GoalEnum {

    BREAK100("break100"),
    BREAK90("break90"),
    BREAK80("break80");

    private final String slug;

    GoalEnum(String slug) {
        this.slug = slug;
    }

    public static Optional<GoalEnum> fromSlug(String slug) {
        return Arrays.stream(values())
                .filter(g -> g.slug.equalsIgnoreCase(slug))
                .findFirst();
    }

    /**
     * Key of the localized goal title in the message bundle.
     */
    public String getTitleKey() {
        return "goal." + slug + ".title";
    }

    public String getSlug() {
        return slug;
    }
}
