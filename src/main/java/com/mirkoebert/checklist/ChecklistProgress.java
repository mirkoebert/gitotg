package com.mirkoebert.checklist;

/**
 * Progress of a user's goal checklist: how many items are checked of the total.
 */
public record ChecklistProgress(int checkedCount, int totalCount, int percentage) {

        public static ChecklistProgress of(int checkedCount, int totalCount) {
                if (totalCount <= 0) {
                        return new ChecklistProgress(0, 0, 0);
                }
                int safeChecked = Math.max(0, Math.min(checkedCount, totalCount));
                int percentage = (int) Math.round(100.0 * safeChecked / totalCount);
                return new ChecklistProgress(safeChecked, totalCount, percentage);
        }

        public boolean isEmpty() {
                return totalCount == 0;
        }
}
