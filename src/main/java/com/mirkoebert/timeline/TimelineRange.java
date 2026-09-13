package com.mirkoebert.timeline;

import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * How many timeline entries to show. Limits are applied after merging sources.
 */
@Getter
public enum TimelineRange {
    LAST_30("last30", 30),
    LAST_100("last100", 100),
    ALL("all", null);

    private final String param;
    private final Integer limit;

    TimelineRange(String param, Integer limit) {
        this.param = param;
        this.limit = limit;
    }

    /**
     * Cap per source so we never load more rows than needed for the merge.
     * Unpaged for {@link #ALL}.
     */
    public Pageable toPageable() {
        if (limit == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(0, limit);
    }

    public static TimelineRange fromParam(String param) {
        if (param == null || param.isBlank()) {
            return LAST_30;
        }
        for (TimelineRange range : values()) {
            if (range.param.equalsIgnoreCase(param.trim())) {
                return range;
            }
        }
        return LAST_30;
    }
}
