package com.mirkoebert.checklist;

import org.jspecify.annotations.Nullable;

/**
 * One checklist item as defined by the message bundle. Carries the message keys, not the text -
 * the localized wording is resolved through the {@link org.springframework.context.MessageSource}
 * when the page is rendered, so it follows the user's locale preference.
 *
 * @param id      stable item id, also the id stored in {@link GolfCheckEntity#getCheckListItemId()}
 * @param nameKey key of the item label, always present
 * @param descKey key of the optional explanation, {@code null} when the bundle defines none
 */
public record ChecklistItem(long id, String nameKey, @Nullable String descKey) {
}
