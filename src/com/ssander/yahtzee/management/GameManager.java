package com.ssander.yahtzee.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ssander.yahtzee.combos.Combination;
import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.ui.GameUI;

/**
 * Manages the overall flow of a Yahtzee game.
 * Coordinates player turns, dice rolling, scoring, and game completion.
 */
public class GameManager {
    private final List<Player> players;
    private final List<Die> dice;
    private final CombinationRegistry registry;
    private final GameUI ui;

    /**
     * Constructs a new game manager.
     * Initializes empty player list, creates 5 dice, combination registry, and user interface.
     *
     * @param ui user interface implementation to interact with players
     */
    public GameManager(GameUI ui) {
        this.players = new ArrayList<>();
        this.dice = new ArrayList<>(5);
        this.registry = new CombinationRegistry();
        this.ui = Objects.requireNonNull(ui, "Game UI must not be null");

        for (int i = 0; i < 5; i++) {
            dice.add(new Die());
        }

        // Add shutdown hook for Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.println("\n\nGame aborted. Closing now...")
        ));
    }

    /**
     * Starts and manages the complete game flow.
     * Sets up players, runs all game rounds, displays results, and cleans up resources.
     */
    public void startGame() {
        int playerCount = ui.askPlayerCount();

        for (int i = 0; i < playerCount; i++) {
            String name = ui.askPlayerName(i + 1);
            players.add(new Player(name));
        }

        ui.displayGameStart();
        playRounds();
        displayFinalResults();
        ui.close();
    }

    /**
     * Executes the main game loop.
     * Continues until all players have completed all 13 combinations.
     */
    private void playRounds() {
        while (!allFinished()) {
            for (Player player : players) {
                if (player.getRolledComboCount() < 13) {
                    playOneTurn(player);
                }
            }
        }
    }

    /**
     * Checks if all players have completed their games.
     *
     * @return true if all players have rolled all 13 combinations, false otherwise
     */
    private boolean allFinished() {
        for (Player player : players) {
            if (player.getRolledComboCount() < 13) {
                return false;
            }
        }
        return true;
    }

    /**
     * Manages one complete turn for a player.
     * Allows up to 3 dice rolls, displays options after each roll, and saves the chosen combination.
     * Also checks and awards the 35-point upper section bonus if applicable.
     *
     * @param player the player taking this turn
     */
    private void playOneTurn(Player player) {
        int[] kept = new int[5];

        int playerNumber = players.indexOf(player) + 1;
        ui.displayPlayerTurn(playerNumber, player);

        rollAndDisplay(kept, player);
        List<Integer> values = collectValues();
        ui.displayPossibleCombos(registry.getAllCombos(), values, player.getUsedCombosSnapshot());

        int rollCount = 1;
        while (rollCount < 3 && ui.askRollAgain()) {
            kept = ui.askKeptDice();
            rollAndDisplay(kept, player);
            values = collectValues();
            ui.displayPossibleCombos(registry.getAllCombos(), values, player.getUsedCombosSnapshot());
            rollCount++;
        }

        int chosenIndex = requestCombinationIndex(player);
        saveScore(player, chosenIndex, values);
        awardBonusIfEligible(player);
        ui.displayScoreUpdate(playerNumber, player);
    }

    private void rollAndDisplay(int[] kept, Player player) {
        player.rollDice(dice, kept);
        Collections.sort(dice);
        ui.displayDice(dice);
    }

    /**
     * Collects current values from all dice into a list.
     *
     * @return list of current dice values
     */
    private List<Integer> collectValues() {
        List<Integer> values = new ArrayList<>(dice.size());
        for (Die die : dice) {
            values.add(die.getCurrentValue());
        }
        return values;
    }

    private int requestCombinationIndex(Player player) {
        int index = ui.askCombination();
        while (!isValidCombinationChoice(index, player)) {
            ui.displayInvalidCombinationSelection();
            index = ui.askCombination();
        }
        return index;
    }

    private boolean isValidCombinationChoice(int index, Player player) {
        return index >= 0 && index < registry.getAllCombos().size() && !player.isCombinationUsed(index);
    }

    /**
     * Saves a player's chosen combination and updates their score.
     *
     * @param player the player whose score is being saved
     * @param index the index of the chosen combination
     * @param values the current dice values
     */
    private void saveScore(Player player, int index, List<Integer> values) {
        Combination chosen = registry.getComboByIndex(index);
        int points = chosen.calculatePoints(values);
        player.applyScore(chosen, points, registry.isUpperSection(index));
    }

    private void awardBonusIfEligible(Player player) {
        if (!player.isUpperSectionBonusAwarded() && player.getUpperSectionScore() >= 63) {
            ui.displayBonusMessage();
            player.setTotalScore(player.getTotalScore() + 35);
            player.setUpperSectionBonusAwarded(true);
        }
    }

    /**
     * Displays final game results.
     * Sorts players by score in descending order and displays rankings.
     */
    private void displayFinalResults() {
        players.sort(Collections.reverseOrder());
        ui.displayGameEnd(players);
    }
}
