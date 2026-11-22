package com.ssander.yahtzee.management;

/**
 * One row of the score sheet as shown to the player for the current roll.
 *
 * @param number 1-based number the player types to choose this category
 * @param name category name
 * @param scored points already recorded, or {@code null} if the category is open
 * @param points points this roll would score here (only meaningful when open)
 * @param allowed whether this category may be chosen now (the Joker rule can force a choice)
 */
public record CategoryOption(int number, String name, Integer scored, int points, boolean allowed) {

    /** @return true when the category is still open */
    public boolean isOpen() {
        return scored == null;
    }
}
