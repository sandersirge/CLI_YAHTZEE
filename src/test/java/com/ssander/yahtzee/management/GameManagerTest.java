package com.ssander.yahtzee.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.ui.GameUI;
import com.ssander.yahtzee.ui.InputClosedException;

/** Plays complete games through GameManager with seeded dice and a scripted player. */
class GameManagerTest {

    /**
     * A simple bot: keeps the most common face, rerolls the rest, then scores the
     * allowed category worth the most points (first one on ties).
     */
    static class ScriptedUI implements GameUI {
        final int players;
        final boolean extraRules;
        int failAfterChoices = Integer.MAX_VALUE;

        List<Integer> dice = List.of();
        List<CategoryOption> options = List.of();
        List<Player> ranking;
        int turns;
        int invalidChoices;
        int choices;
        boolean aborted;
        boolean closed;

        ScriptedUI(int players, boolean extraRules) {
            this.players = players;
            this.extraRules = extraRules;
        }

        @Override public int askPlayerCount(int maxPlayers) { return players; }
        @Override public String askPlayerName(int orderNumber) { return "P" + orderNumber; }
        @Override public boolean askUseExtraRules() { return extraRules; }
        @Override public void displayGameStart(List<Player> list, boolean extra) { }
        @Override public void displayPlayerTurn(Player player, int round, int totalRounds) { turns++; }
        @Override public void displayDice(List<Die> list) {
            dice = list.stream().map(Die::getCurrentValue).toList();
        }
        @Override public void displayScoreSheet(Player player, List<CategoryOption> list, boolean joker) {
            options = list;
        }
        @Override public boolean askRollAgain(int rollsLeft) { return true; }

        @Override
        public boolean[] askKeptDice(int diceCount) {
            int[] counts = new int[7];
            dice.forEach(v -> counts[v]++);
            int best = 1;
            for (int face = 2; face <= 6; face++) {
                if (counts[face] >= counts[best]) {
                    best = face;
                }
            }
            boolean[] kept = new boolean[diceCount];
            for (int i = 0; i < diceCount; i++) {
                kept[i] = dice.get(i) == best;
            }
            return kept;
        }

        @Override
        public int askCombination(int categoryCount) {
            if (++choices > failAfterChoices) {
                throw new InputClosedException();
            }
            return options.stream()
                    .filter(o -> o.isOpen() && o.allowed())
                    .max(Comparator.comparingInt(CategoryOption::points)
                            .thenComparing(o -> -o.number()))
                    .orElseThrow()
                    .number() - 1;
        }

        @Override public void displayInvalidCombinationSelection(String reason) { invalidChoices++; }
        @Override public boolean confirmScratch(String categoryName) { return true; }
        @Override public void displayBonusMessage(int bonusPoints) { }
        @Override public void displayYahtzeeBonus(int bonusPoints) { }
        @Override public void displayScoreUpdate(Player player, String categoryName, int points) { }
        @Override public void displayGameEnd(List<Player> list) { ranking = new ArrayList<>(list); }
        @Override public void displayAborted() { aborted = true; }
        @Override public void close() { closed = true; }
    }

    @ParameterizedTest(name = "{0} players, extra rules {1}, seed {2}")
    @CsvSource({"1, false, 1", "3, true, 2", "6, true, 3", "2, false, 4"})
    void fullGameFinishesWithSortedRanking(int players, boolean extraRules, long seed) {
        ScriptedUI ui = new ScriptedUI(players, extraRules);
        new GameManager(ui, new Random(seed)).startGame();

        assertNotNull(ui.ranking, "game must reach the end screen");
        assertEquals(players, ui.ranking.size());
        assertEquals(players * 13, ui.turns, "13 rounds per player");
        assertEquals(0, ui.invalidChoices);
        assertFalse(ui.aborted);
        assertTrue(ui.closed);
        for (Player player : ui.ranking) {
            assertTrue(player.isFinished());
        }
        for (int i = 1; i < ui.ranking.size(); i++) {
            assertTrue(ui.ranking.get(i - 1).getTotalScore() >= ui.ranking.get(i).getTotalScore());
        }
    }

    @Test
    void sameSeedGivesSameGame() {
        ScriptedUI first = new ScriptedUI(2, true);
        ScriptedUI second = new ScriptedUI(2, true);
        new GameManager(first, new Random(99)).startGame();
        new GameManager(second, new Random(99)).startGame();
        assertEquals(
                first.ranking.stream().map(Player::getTotalScore).toList(),
                second.ranking.stream().map(Player::getTotalScore).toList());
    }

    @Test
    void endOfInputClosesCleanly() {
        ScriptedUI ui = new ScriptedUI(2, false);
        ui.failAfterChoices = 5;
        new GameManager(ui, new Random(5)).startGame();
        assertTrue(ui.aborted);
        assertTrue(ui.closed);
        assertEquals(null, ui.ranking);
    }
}
