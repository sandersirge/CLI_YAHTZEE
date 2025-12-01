package com.ssander.yahtzee.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ssander.yahtzee.combos.Combination;

/**
 * Represents a player in the Yahtzee game.
 * Tracks player score, used combinations, and game statistics.
 * Players are comparable based on their total score.
 *
 * <p>Responsibility for updating combination usage and section totals now
 * lives inside this class via {@link #applyScore(Combination, int, boolean)},
 * reducing direct score mutations from the game manager.</p>
 */
public class Player implements Comparable<Player> {
    private String playerName;
    private int totalScore;
    private final boolean[] usedCombos;
    private int upperSectionScore;
    private int lowerSectionScore;
    private boolean upperSectionBonusAwarded;
    private final List<String> rolledComboNames;
    private final List<Integer> rolledComboScores;

    /**
     * Constructs a new player with the given name.
     * Initializes score to 0 and creates empty combination tracking lists.
     *
     * @param name the player's name
     */
    public Player(String name) {
        this.playerName = name;
        this.totalScore = 0;
        this.rolledComboNames = new ArrayList<>();
        this.rolledComboScores = new ArrayList<>();
        this.usedCombos = new boolean[13];
        this.upperSectionScore = 0;
        this.lowerSectionScore = 0;
        this.upperSectionBonusAwarded = false;
    }

    /**
     * Gets the player's name.
     *
     * @return the player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the player's name.
     *
     * @param playerName the new player name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Gets the player's total score including bonuses.
     *
     * @return the total score
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Sets the player's total score.
     * Primarily used for awarding bonus points.
     *
     * @param totalScore the new total score
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * Applies the result of scoring a combination to this player.
     * Handles marking the combination as used, tracking history,
     * and updating section totals.
     *
     * @param combination the combination that was scored
     * @param points the points earned for this combination
     * @param upperSection true if the combination belongs to the upper section
     * @throws IllegalStateException if the combination was already used
     */
    public void applyScore(Combination combination, int points, boolean upperSection) {
        int index = combination.getIndex();
        if (usedCombos[index]) {
            throw new IllegalStateException("Combination already used: " + combination.getComboName());
        }

        usedCombos[index] = true;
        rolledComboNames.add(combination.getComboName());
        rolledComboScores.add(points);

        totalScore += points;
        if (upperSection) {
            upperSectionScore += points;
        } else {
            lowerSectionScore += points;
        }
    }

    /**
     * Gets the number of combinations the player has rolled.
     *
     * @return the count of rolled combinations
     */
    public int getRolledComboCount() {
        return rolledComboNames.size();
    }

    /**
     * Provides a defensive copy of combination usage flags for presentation.
     *
     * @return a copy of the used combination flags
     */
    public boolean[] getUsedCombosSnapshot() {
        return Arrays.copyOf(usedCombos, usedCombos.length);
    }

    /**
     * Indicates whether a combination has already been scored.
     *
     * @param index the combination index to check
     * @return true if the combination was scored before
     */
    public boolean isCombinationUsed(int index) {
        return usedCombos[index];
    }

    /**
     * Checks if the upper section bonus has been awarded.
     *
     * @return true if bonus has been awarded, false otherwise
     */
    public boolean isUpperSectionBonusAwarded() {
        return upperSectionBonusAwarded;
    }

    /**
     * Sets whether the upper section bonus has been awarded.
     *
     * @param upperSectionBonusAwarded true if bonus awarded, false otherwise
     */
    public void setUpperSectionBonusAwarded(boolean upperSectionBonusAwarded) {
        this.upperSectionBonusAwarded = upperSectionBonusAwarded;
    }

    /**
     * Gets the upper section score (sum of Ones through Sixes).
     *
     * @return the upper section score
     */
    public int getUpperSectionScore() {
        return upperSectionScore;
    }

    /**
     * Gets the lower section score (sum of all combination scores).
     *
     * @return the lower section score
     */
    public int getLowerSectionScore() {
        return lowerSectionScore;
    }

    /**
     * Rolls all dice that are not marked as kept.
     *
     * @param dice the list of dice to potentially roll
     * @param kept array where 0 means roll the die, 1 means keep it
     */
    public void rollDice(List<Die> dice, int[] kept) {
        int counter = 0;
        for (Die die : dice) {
            if (kept[counter++] == 0) die.roll();
        }
    }

    /**
     * Creates a formatted string displaying all rolled combinations and their scores.
     *
     * @return formatted string of all rolled combinations
     */
    public String displayCombos() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < rolledComboNames.size(); i++) {
            result.append("\n").append(rolledComboNames.get(i)).append(" - earned points: ").append(rolledComboScores.get(i));
        }
        return result + "\n";
    }

    /**
     * Returns a string representation of the player including name, score, and all rolled combinations.
     *
     * @return formatted player information
     */
    @Override
    public String toString() {
        return "PLAYER NAME: " + playerName + "\n" +
               "SCORE: [ " + totalScore + " ]\n" +
               "ROLLED COMBOS:" + displayCombos();
    }

    /**
     * Compares this player to another based on total score.
     *
     * @param p the other player to compare to
     * @return negative if this player has lower score, positive if higher, 0 if equal
     */
    @Override
    public int compareTo(Player p) {
        return Integer.compare(this.getTotalScore(), p.getTotalScore());
    }
}
