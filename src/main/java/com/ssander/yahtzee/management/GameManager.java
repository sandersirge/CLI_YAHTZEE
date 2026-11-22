package com.ssander.yahtzee.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

import com.ssander.yahtzee.combos.Combination;
import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.ui.GameUI;
import com.ssander.yahtzee.ui.InputClosedException;

/**
 * Manages the overall flow of a Yahtzee game.
 * Coordinates player turns, dice rolling, scoring, and game completion.
 */
public class GameManager {
    /** Most players one game supports. */
    public static final int MAX_PLAYERS = 6;
    /** Dice in play. */
    public static final int DICE_COUNT = 5;
    /** Rolls per turn (the first roll plus two rerolls). */
    public static final int ROLLS_PER_TURN = 3;

    private final List<Player> players = new ArrayList<>();
    private final List<Die> dice = new ArrayList<>(DICE_COUNT);
    private final CombinationRegistry registry = new CombinationRegistry();
    private final GameUI ui;
    private ScoringService scoring;

    /** Set while a game is running; the shutdown hook only reports an abort then. */
    private volatile boolean inProgress;

    /**
     * Creates a game manager with fair, unpredictable dice.
     *
     * @param ui user interface implementation to interact with players
     */
    public GameManager(GameUI ui) {
        this(ui, ThreadLocalRandom.current());
    }

    /**
     * Creates a game manager whose dice use the given random source
     * (a seeded generator makes a game repeatable, e.g. for testing).
     *
     * @param ui user interface implementation
     * @param random random source for the dice
     */
    public GameManager(GameUI ui, RandomGenerator random) {
        this.ui = Objects.requireNonNull(ui, "Game UI must not be null");
        for (int i = 0; i < DICE_COUNT; i++) {
            dice.add(new Die(random));
        }
        // Only an interrupted game (e.g. Ctrl+C) prints this, not a normal finish.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (inProgress) {
                System.out.println("\n\nGame aborted. Closing now...");
            }
        }));
    }

    /**
     * Starts and manages the complete game flow.
     * Sets up players, runs all rounds, displays results, and cleans up resources.
     * If the input ends part-way, the game closes cleanly instead of crashing.
     */
    public void startGame() {
        inProgress = true;
        try {
            setUpPlayers();
            playRounds();
            displayFinalResults();
        } catch (InputClosedException e) {
            ui.displayAborted();
        } finally {
            inProgress = false;
            ui.close();
        }
    }

    private void setUpPlayers() {
        int playerCount = ui.askPlayerCount(MAX_PLAYERS);
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player(ui.askPlayerName(i + 1), registry.getAllCombos()));
        }
        scoring = new ScoringService(registry, ui.askUseExtraRules());
        ui.askDisplayOptions();
        ui.displayGameStart(players, scoring.isExtraRules());
    }

    /** One round per score-sheet category; every player takes one turn per round. */
    private void playRounds() {
        int rounds = registry.size();
        for (int round = 1; round <= rounds; round++) {
            for (Player player : players) {
                if (!player.isFinished()) {
                    playOneTurn(player, round, rounds);
                }
            }
        }
    }

    /**
     * One turn: up to three rolls, then the player fills one category.
     * Also applies the upper bonus and (with extra rules) the Yahtzee bonus.
     */
    private void playOneTurn(Player player, int round, int rounds) {
        ui.displayPlayerTurn(player, round, rounds);

        boolean[] kept = new boolean[DICE_COUNT];
        rollAndShow(player, kept);
        for (int rollsLeft = ROLLS_PER_TURN - 1; rollsLeft > 0 && ui.askRollAgain(rollsLeft); rollsLeft--) {
            kept = ui.askKeptDice(DICE_COUNT);
            rollAndShow(player, kept);
        }

        List<Integer> values = collectValues();
        boolean hadBonus = player.isUpperSectionBonusAwarded();
        if (scoring.earnsYahtzeeBonus(player, values)) {
            player.addYahtzeeBonus();
            ui.displayYahtzeeBonus(Player.YAHTZEE_BONUS_POINTS);
        }

        int index = requestCombinationIndex(player, values);
        Combination chosen = registry.getComboByIndex(index);
        int points = scoring.pointsFor(player, index, values);
        player.applyScore(chosen, points);

        if (!hadBonus && player.isUpperSectionBonusAwarded()) {
            ui.displayBonusMessage(Player.UPPER_BONUS_POINTS);
        }
        ui.displayScoreUpdate(player, chosen.getComboName(), points);
    }

    private void rollAndShow(Player player, boolean[] kept) {
        player.rollDice(dice, kept);
        Collections.sort(dice);
        List<Integer> values = collectValues();
        ui.displayDice(dice);
        ui.displayScoreSheet(player, scoring.options(player, values), scoring.isJoker(player, values));
    }

    private List<Integer> collectValues() {
        List<Integer> values = new ArrayList<>(dice.size());
        for (Die die : dice) {
            values.add(die.getCurrentValue());
        }
        return values;
    }

    /** Asks until the player picks a category they may use (confirming a 0-point scratch). */
    private int requestCombinationIndex(Player player, List<Integer> values) {
        while (true) {
            int index = ui.askCombination(registry.size());
            String name = registry.getComboByIndex(index).getComboName();
            if (player.isCombinationUsed(index)) {
                ui.displayInvalidCombinationSelection(name + " is already filled. Pick an open category.");
            } else if (!scoring.isAllowed(player, index, values)) {
                ui.displayInvalidCombinationSelection(
                        "Joker rule: this Yahtzee must go in a category that shows points.");
            } else if (scoring.pointsFor(player, index, values) == 0 && !ui.confirmScratch(name)) {
                // player changed their mind; ask again
            } else {
                return index;
            }
        }
    }

    /** Sorts players by score (highest first) and shows the results. */
    private void displayFinalResults() {
        List<Player> ranking = new ArrayList<>(players);
        ranking.sort(Collections.reverseOrder());
        ui.displayGameEnd(ranking);
    }
}
