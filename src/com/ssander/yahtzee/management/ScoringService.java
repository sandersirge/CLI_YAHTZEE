package com.ssander.yahtzee.management;

import java.util.ArrayList;
import java.util.List;

import com.ssander.yahtzee.combos.Combination;
import com.ssander.yahtzee.player.Player;

/**
 * Decides what each category is worth for a roll, and which categories may be chosen.
 *
 * <p>With {@code extraRules} on, the official Yahtzee bonus and Joker rule apply when a
 * player rolls a Yahtzee after the Yahtzee box is already filled:</p>
 * <ul>
 *   <li><b>Yahtzee bonus:</b> +100 if the Yahtzee box holds 50 (not if it was scratched).</li>
 *   <li><b>Joker:</b> the matching upper box (e.g. Fives for five 5s) must be used if open;
 *       otherwise any open lower box, where Full House, Small and Large Straight score their
 *       full value; if both are full, any open upper box (scoring 0).</li>
 * </ul>
 */
public class ScoringService {
    private static final int YAHTZEE_POINTS = 50;

    private final CombinationRegistry registry;
    private final boolean extraRules;

    public ScoringService(CombinationRegistry registry, boolean extraRules) {
        this.registry = registry;
        this.extraRules = extraRules;
    }

    public boolean isExtraRules() {
        return extraRules;
    }

    /**
     * True when this roll is a Yahtzee and the Yahtzee box is already filled,
     * so the Joker rule decides where it may go.
     */
    public boolean isJoker(Player player, List<Integer> values) {
        int yahtzee = registry.yahtzeeIndex();
        return extraRules
                && registry.getComboByIndex(yahtzee).isPossible(values)
                && player.isCombinationUsed(yahtzee);
    }

    /** True when this roll earns a +100 Yahtzee bonus for the player. */
    public boolean earnsYahtzeeBonus(Player player, List<Integer> values) {
        if (!isJoker(player, values)) {
            return false;
        }
        Integer yahtzeeBox = player.getScore(registry.yahtzeeIndex());
        return yahtzeeBox != null && yahtzeeBox == YAHTZEE_POINTS;
    }

    /** Points the category at {@code index} would score for this roll. */
    public int pointsFor(Player player, int index, List<Integer> values) {
        Combination combo = registry.getComboByIndex(index);
        return isJoker(player, values) ? combo.jokerPoints(values) : combo.calculatePoints(values);
    }

    /** Whether the player may put this roll in the category at {@code index}. */
    public boolean isAllowed(Player player, int index, List<Integer> values) {
        if (index < 0 || index >= registry.size() || player.isCombinationUsed(index)) {
            return false;
        }
        if (!isJoker(player, values)) {
            return true;
        }
        int matchingUpper = registry.upperIndexFor(values.get(0));
        if (matchingUpper >= 0 && !player.isCombinationUsed(matchingUpper)) {
            return index == matchingUpper;
        }
        boolean anyLowerOpen = false;
        for (Combination combo : registry.getAllCombos()) {
            if (!combo.isUpperSection() && !player.isCombinationUsed(combo.getIndex())) {
                anyLowerOpen = true;
                break;
            }
        }
        return anyLowerOpen ? !registry.isUpperSection(index) : registry.isUpperSection(index);
    }

    /** The whole score sheet for this player and roll, ready to display. */
    public List<CategoryOption> options(Player player, List<Integer> values) {
        List<CategoryOption> rows = new ArrayList<>(registry.size());
        for (Combination combo : registry.getAllCombos()) {
            int index = combo.getIndex();
            Integer scored = player.getScore(index);
            int points = scored == null ? pointsFor(player, index, values) : scored;
            rows.add(new CategoryOption(index + 1, combo.getComboName(), scored, points,
                    isAllowed(player, index, values)));
        }
        return rows;
    }
}
