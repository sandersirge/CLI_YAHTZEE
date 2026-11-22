package com.ssander.yahtzee.combos;

import java.util.List;

/**
 * Abstract base class representing a scoring combination in Yahtzee.
 * Each concrete implementation defines a specific scoring rule and validation logic.
 * 
 * @author Yahtzee Game Project
 * @version 1.0
 */
public abstract class Combination {
    private final String comboName;
    private final int index;
    
    /**
     * Constructs a new combination with a name and index.
     * 
     * @param comboName the display name of this combination
     * @param index the position of this combination in the score sheet (0-12)
     */
    public Combination(String comboName, int index){
        this.comboName = comboName;
        this.index = index;
    }
    
    /**
     * Gets the display name of this combination.
     * 
     * @return the combination name
     */
    public String getComboName() {
        return comboName;
    }
    
    /**
     * Gets the index of this combination in the score sheet.
     * 
     * @return the combination index (0-12)
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Calculates the score for this combination based on the given dice values.
     * If the combination is not possible, should return 0.
     * 
     * @param values the list of dice values (typically 5 dice, each 1-6)
     * @return the calculated score, or 0 if combination is invalid
     */
    public abstract int calculatePoints(List<Integer> values);
    
    /**
     * Checks if this combination can be formed with the given dice values.
     * 
     * @param values the list of dice values (typically 5 dice, each 1-6)
     * @return true if the combination is valid, false otherwise
     */
    public abstract boolean isPossible(List<Integer> values);

    /**
     * Points this category gives when a Yahtzee is used as a Joker.
     * Under the official Joker rule a Yahtzee counts as a valid Full House,
     * Small Straight or Large Straight (those classes override this method);
     * every other category scores normally.
     *
     * @param values the dice values (all equal when this is called)
     * @return the points scored under the Joker rule
     */
    public int jokerPoints(List<Integer> values) {
        return calculatePoints(values);
    }

    /**
     * Whether this category belongs to the upper section (Ones to Sixes).
     *
     * @return true for upper-section categories
     */
    public boolean isUpperSection() {
        return false;
    }

    /**
     * Returns the string representation of this combination.
     * 
     * @return the combination name
     */
    public String toString() {
        return comboName;
    }
}
