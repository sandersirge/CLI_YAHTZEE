package com.ssander.yahtzee.ui;

import java.util.List;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.combos.Combination;

/**
 * Defines the user interaction contract for the Yahtzee game.
 * Allows the core game logic to remain agnostic to input/output specifics.
 */
public interface GameUI {
    int askPlayerCount();
    String askPlayerName(int orderNumber);
    void displayGameStart();
    void displayPlayerTurn(int orderNumber, Player player);
    void displayDice(List<Die> dice);
    void displayPossibleCombos(List<Combination> combos, List<Integer> values, boolean[] usedCombos);
    void displayInvalidCombinationSelection();
    boolean askRollAgain();
    int[] askKeptDice();
    int askCombination();
    void displayBonusMessage();
    void displayScoreUpdate(int playerNumber, Player player);
    void displayGameEnd(List<Player> players);
    void close();
}
