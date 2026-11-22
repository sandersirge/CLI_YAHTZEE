package com.ssander.yahtzee.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.ssander.yahtzee.management.CombinationRegistry;

class PlayerTest {
    private final CombinationRegistry registry = new CombinationRegistry();

    @Test
    void newPlayerHasEmptySheet() {
        Player player = new Player("A", registry.getAllCombos());
        assertEquals(0, player.getTotalScore());
        assertEquals(0, player.getRolledComboCount());
        assertNull(player.getScore(0));
        assertFalse(player.isFinished());
    }

    @Test
    void upperBonusAt63() {
        Player player = new Player("A", registry.getAllCombos());
        int[] upper = {3, 6, 9, 12, 15, 17}; // 62
        for (int i = 0; i < 6; i++) {
            player.applyScore(registry.getComboByIndex(i), upper[i]);
        }
        assertFalse(player.isUpperSectionBonusAwarded());
        assertEquals(62, player.getTotalScore());

        Player exact = new Player("B", registry.getAllCombos());
        int[] reaches = {3, 6, 9, 12, 15, 18}; // 63
        for (int i = 0; i < 6; i++) {
            exact.applyScore(registry.getComboByIndex(i), reaches[i]);
        }
        assertTrue(exact.isUpperSectionBonusAwarded());
        assertEquals(63 + Player.UPPER_BONUS_POINTS, exact.getTotalScore());
    }

    @Test
    void totalsCombineSectionsAndBonuses() {
        Player player = new Player("A", registry.getAllCombos());
        player.applyScore(registry.getComboByIndex(0), 3);
        player.applyScore(registry.getComboByIndex(12), 20);
        player.addYahtzeeBonus();
        assertEquals(3, player.getUpperSectionScore());
        assertEquals(20, player.getLowerSectionScore());
        assertEquals(123, player.getTotalScore());
    }

    @Test
    void categoryCanOnlyBeFilledOnce() {
        Player player = new Player("A", registry.getAllCombos());
        player.applyScore(registry.getComboByIndex(5), 18);
        assertThrows(IllegalStateException.class,
                () -> player.applyScore(registry.getComboByIndex(5), 6));
    }

    @Test
    void finishedAfterAllCategories() {
        Player player = new Player("A", registry.getAllCombos());
        registry.getAllCombos().forEach(combo -> player.applyScore(combo, 0));
        assertTrue(player.isFinished());
    }

    @Test
    void keptDiceAreNotRerolled() {
        Random random = new Random(7);
        List<Die> dice = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Die die = new Die(random);
            die.setCurrentValue(6);
            dice.add(die);
        }
        new Player("A", registry.getAllCombos()).rollDice(dice, new boolean[] {true, true, true, true, false});
        for (int i = 0; i < 4; i++) {
            assertEquals(6, dice.get(i).getCurrentValue());
        }
        int last = dice.get(4).getCurrentValue();
        assertTrue(last >= 1 && last <= 6);
    }

    @Test
    void seededDiceAreRepeatable() {
        Die a = new Die(new Random(42));
        Die b = new Die(new Random(42));
        for (int i = 0; i < 20; i++) {
            a.roll();
            b.roll();
            assertEquals(a.getCurrentValue(), b.getCurrentValue());
        }
    }
}
