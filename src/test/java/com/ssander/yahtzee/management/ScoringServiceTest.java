package com.ssander.yahtzee.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ssander.yahtzee.player.Player;

class ScoringServiceTest {
    private static final List<Integer> FIVES = List.of(5, 5, 5, 5, 5);
    private static final int FULL_HOUSE = 8;
    private static final int SMALL_STRAIGHT = 9;
    private static final int LARGE_STRAIGHT = 10;
    private static final int CHANCE = 12;

    private CombinationRegistry registry;
    private ScoringService extra;
    private ScoringService plain;
    private Player player;
    private int yahtzee;
    private int fivesBox;

    @BeforeEach
    void setUp() {
        registry = new CombinationRegistry();
        extra = new ScoringService(registry, true);
        plain = new ScoringService(registry, false);
        player = new Player("A", registry.getAllCombos());
        yahtzee = registry.yahtzeeIndex();
        fivesBox = registry.upperIndexFor(5);
    }

    private void fill(int index, int points) {
        player.applyScore(registry.getComboByIndex(index), points);
    }

    @Test
    void firstYahtzeeScoresFiftyWithoutJoker() {
        assertEquals(50, extra.pointsFor(player, yahtzee, FIVES));
        assertFalse(extra.isJoker(player, FIVES));
        assertFalse(extra.earnsYahtzeeBonus(player, FIVES));
    }

    @Test
    void extraYahtzeeEarnsBonusOnlyWhenBoxHoldsFifty() {
        fill(yahtzee, 50);
        assertTrue(extra.earnsYahtzeeBonus(player, FIVES));

        Player scratched = new Player("B", registry.getAllCombos());
        scratched.applyScore(registry.getComboByIndex(yahtzee), 0);
        assertTrue(extra.isJoker(scratched, FIVES));
        assertFalse(extra.earnsYahtzeeBonus(scratched, FIVES));
    }

    @Test
    void jokerForcesMatchingUpperBoxWhileOpen() {
        fill(yahtzee, 50);
        assertTrue(extra.isAllowed(player, fivesBox, FIVES));
        assertFalse(extra.isAllowed(player, FULL_HOUSE, FIVES));
        assertFalse(extra.isAllowed(player, CHANCE, FIVES));
    }

    @Test
    void jokerThenAllowsLowerBoxesAtFullValue() {
        fill(yahtzee, 50);
        fill(fivesBox, 25);
        assertTrue(extra.isAllowed(player, FULL_HOUSE, FIVES));
        assertFalse(extra.isAllowed(player, 0, FIVES));
        assertEquals(25, extra.pointsFor(player, FULL_HOUSE, FIVES));
        assertEquals(30, extra.pointsFor(player, SMALL_STRAIGHT, FIVES));
        assertEquals(40, extra.pointsFor(player, LARGE_STRAIGHT, FIVES));
        assertEquals(25, extra.pointsFor(player, CHANCE, FIVES));
    }

    @Test
    void jokerFallsBackToScratchingAnUpperBox() {
        for (int i = 6; i <= 12; i++) {
            fill(i, 0);
        }
        fill(fivesBox, 0);
        assertTrue(extra.isAllowed(player, 0, FIVES));
        assertEquals(0, extra.pointsFor(player, 0, FIVES));
        assertFalse(extra.isAllowed(player, fivesBox, FIVES));
    }

    @Test
    void plainRulesHaveNoJokerOrBonus() {
        fill(yahtzee, 50);
        assertFalse(plain.isJoker(player, FIVES));
        assertFalse(plain.earnsYahtzeeBonus(player, FIVES));
        assertEquals(0, plain.pointsFor(player, FULL_HOUSE, FIVES));
        assertTrue(plain.isAllowed(player, CHANCE, FIVES));
    }

    @Test
    void usedOrOutOfRangeCategoriesAreNotAllowed() {
        fill(CHANCE, 20);
        List<Integer> roll = List.of(1, 2, 3, 4, 6);
        assertFalse(extra.isAllowed(player, CHANCE, roll));
        assertFalse(extra.isAllowed(player, -1, roll));
        assertFalse(extra.isAllowed(player, registry.size(), roll));
    }

    @Test
    void optionsListEveryCategoryWithScoredAndPotentialPoints() {
        fill(CHANCE, 20);
        List<CategoryOption> options = extra.options(player, List.of(1, 2, 3, 4, 5));
        assertEquals(13, options.size());
        assertEquals(20, options.get(CHANCE).scored());
        assertFalse(options.get(CHANCE).allowed());
        assertEquals(40, options.get(LARGE_STRAIGHT).points());
        assertTrue(options.get(LARGE_STRAIGHT).isOpen());
    }
}
