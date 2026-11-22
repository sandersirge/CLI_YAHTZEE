package com.ssander.yahtzee.ui;

import java.util.List;

import com.ssander.yahtzee.management.CategoryOption;
import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.player.Player;

/**
 * Defines the user interaction contract for the Yahtzee game.
 * Allows the core game logic to remain agnostic to input/output specifics.
 *
 * <p>Input methods only return valid values: they keep asking until the answer
 * is usable. When input ends they throw {@link InputClosedException}.</p>
 */
public interface GameUI {
    /** @return number of players, between 1 and {@code maxPlayers} */
    int askPlayerCount(int maxPlayers);

    /** @return a non-empty display name for the player at this position (1-based) */
    String askPlayerName(int orderNumber);

    /** @return true to play with the Yahtzee bonus and Joker rule */
    boolean askUseExtraRules();

    void displayGameStart(List<Player> players, boolean extraRules);

    void displayPlayerTurn(Player player, int round, int totalRounds);

    void displayDice(List<Die> dice);

    /** Shows the player's score sheet with what this roll would score in each open category. */
    void displayScoreSheet(Player player, List<CategoryOption> options, boolean joker);

    /** @return true to roll again; {@code rollsLeft} is shown in the prompt */
    boolean askRollAgain(int rollsLeft);

    /** @return kept[i] is true for each die (in display order) that must not be rerolled */
    boolean[] askKeptDice(int diceCount);

    /** @return the chosen category as a 0-based index in {@code [0, categoryCount)} */
    int askCombination(int categoryCount);

    /** Explains why the chosen category can't be used right now. */
    void displayInvalidCombinationSelection(String reason);

    /** @return true if the player really wants to score 0 in this category */
    boolean confirmScratch(String categoryName);

    void displayBonusMessage(int bonusPoints);

    void displayYahtzeeBonus(int bonusPoints);

    void displayScoreUpdate(Player player, String categoryName, int points);

    /** @param ranking players sorted by total score, highest first */
    void displayGameEnd(List<Player> ranking);

    /** Shown when input ends in the middle of a game. */
    void displayAborted();

    void close();
}
