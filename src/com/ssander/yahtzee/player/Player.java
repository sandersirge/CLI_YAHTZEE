package com.ssander.yahtzee.player;

import java.util.List;

import com.ssander.yahtzee.combos.Combination;

/**
 * A player and their score sheet.
 *
 * <p>The sheet stores one score per category ({@code null} while still open).
 * Totals are always calculated from the sheet plus the two bonuses, so they can
 * never drift out of sync with what is shown.</p>
 */
public class Player implements Comparable<Player> {
    /** Upper-section total needed for the bonus. */
    public static final int UPPER_BONUS_THRESHOLD = 63;
    /** Bonus for reaching the threshold. */
    public static final int UPPER_BONUS_POINTS = 35;
    /** Bonus for each extra Yahtzee (official rule, optional in this game). */
    public static final int YAHTZEE_BONUS_POINTS = 100;

    private final String playerName;
    private final Integer[] scores;
    private final boolean[] upperCategory;
    private int yahtzeeBonusCount;

    /**
     * Creates a player with an empty score sheet.
     *
     * @param name display name
     * @param categories all categories in score-sheet order (defines sheet size and sections)
     */
    public Player(String name, List<Combination> categories) {
        this.playerName = name;
        this.scores = new Integer[categories.size()];
        this.upperCategory = new boolean[categories.size()];
        for (Combination combo : categories) {
            upperCategory[combo.getIndex()] = combo.isUpperSection();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    /**
     * Records the points for a category.
     *
     * @param combination the category being filled
     * @param points points scored (0 for a scratch)
     * @throws IllegalStateException if the category is already filled
     */
    public void applyScore(Combination combination, int points) {
        int index = combination.getIndex();
        if (scores[index] != null) {
            throw new IllegalStateException("Combination already used: " + combination.getComboName());
        }
        scores[index] = points;
    }

    /** Adds one Yahtzee bonus (+100). */
    public void addYahtzeeBonus() {
        yahtzeeBonusCount++;
    }

    public int getYahtzeeBonusCount() {
        return yahtzeeBonusCount;
    }

    public int getYahtzeeBonusPoints() {
        return yahtzeeBonusCount * YAHTZEE_BONUS_POINTS;
    }

    /**
     * Score in a category, or {@code null} if it is still open.
     *
     * @param index category index
     * @return points scored, or null
     */
    public Integer getScore(int index) {
        return scores[index];
    }

    public boolean isCombinationUsed(int index) {
        return scores[index] != null;
    }

    /** @return how many categories are filled */
    public int getRolledComboCount() {
        int filled = 0;
        for (Integer score : scores) {
            if (score != null) {
                filled++;
            }
        }
        return filled;
    }

    /** @return true when every category is filled */
    public boolean isFinished() {
        return getRolledComboCount() == scores.length;
    }

    /** @return total of Ones to Sixes (without the bonus) */
    public int getUpperSectionScore() {
        return sectionScore(true);
    }

    /** @return total of the lower-section categories (without Yahtzee bonuses) */
    public int getLowerSectionScore() {
        return sectionScore(false);
    }

    /** @return true once the upper section reaches {@value #UPPER_BONUS_THRESHOLD} */
    public boolean isUpperSectionBonusAwarded() {
        return getUpperSectionScore() >= UPPER_BONUS_THRESHOLD;
    }

    /** @return 35 if the upper bonus is reached, otherwise 0 */
    public int getUpperBonusPoints() {
        return isUpperSectionBonusAwarded() ? UPPER_BONUS_POINTS : 0;
    }

    /** @return grand total: both sections plus all bonuses */
    public int getTotalScore() {
        return getUpperSectionScore() + getUpperBonusPoints() + getLowerSectionScore()
                + getYahtzeeBonusPoints();
    }

    /**
     * Rerolls every die that is not kept.
     *
     * @param dice the dice, in display order
     * @param kept kept[i] is true when die i must not be rerolled
     */
    public void rollDice(List<Die> dice, boolean[] kept) {
        for (int i = 0; i < dice.size(); i++) {
            if (!kept[i]) {
                dice.get(i).roll();
            }
        }
    }

    private int sectionScore(boolean upper) {
        int sum = 0;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] != null && upperCategory[i] == upper) {
                sum += scores[i];
            }
        }
        return sum;
    }

    @Override
    public String toString() {
        return playerName + " (" + getTotalScore() + ")";
    }

    @Override
    public int compareTo(Player other) {
        return Integer.compare(getTotalScore(), other.getTotalScore());
    }
}
