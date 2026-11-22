package com.ssander.yahtzee.management;

import java.util.List;

import com.ssander.yahtzee.combos.Chance;
import com.ssander.yahtzee.combos.Combination;
import com.ssander.yahtzee.combos.FourOfKind;
import com.ssander.yahtzee.combos.FullHouse;
import com.ssander.yahtzee.combos.LargeStraight;
import com.ssander.yahtzee.combos.Numbers;
import com.ssander.yahtzee.combos.SmallStraight;
import com.ssander.yahtzee.combos.ThreeOfKind;
import com.ssander.yahtzee.combos.Yahtzee;

/**
 * Registry of the 13 Yahtzee scoring categories, in score-sheet order.
 * Upper section (0-5): Ones through Sixes. Lower section (6-12): Three of a Kind through Chance.
 *
 * <p>Everything else (score sheet size, upper section, where Yahtzee is) is derived
 * from this list, so the rest of the game has no hardcoded category numbers.</p>
 */
public class CombinationRegistry {
    private final List<Combination> allCombos = List.of(
            new Numbers("Ones", 0, 1),
            new Numbers("Twos", 1, 2),
            new Numbers("Threes", 2, 3),
            new Numbers("Fours", 3, 4),
            new Numbers("Fives", 4, 5),
            new Numbers("Sixes", 5, 6),
            new ThreeOfKind(6),
            new FourOfKind(7),
            new FullHouse(8),
            new SmallStraight(9),
            new LargeStraight(10),
            new Yahtzee(11),
            new Chance(12));

    /**
     * Gets the complete, unmodifiable list of all combinations in score-sheet order.
     *
     * @return list containing all 13 combinations
     */
    public List<Combination> getAllCombos() {
        return allCombos;
    }

    /**
     * Number of categories on the score sheet (13), i.e. the number of rounds.
     *
     * @return category count
     */
    public int size() {
        return allCombos.size();
    }

    /**
     * Retrieves a specific combination by its index.
     *
     * @param index the combination index (0-12)
     * @return the combination at the specified index
     */
    public Combination getComboByIndex(int index) {
        return allCombos.get(index);
    }

    /**
     * Checks if a combination index belongs to the upper section (Ones to Sixes).
     *
     * @param index the combination index to check
     * @return true if index is a valid upper-section category
     */
    public boolean isUpperSection(int index) {
        return index >= 0 && index < allCombos.size() && allCombos.get(index).isUpperSection();
    }

    /**
     * Index of the Yahtzee category.
     *
     * @return index of the Yahtzee combination
     */
    public int yahtzeeIndex() {
        for (Combination combo : allCombos) {
            if (combo instanceof Yahtzee) {
                return combo.getIndex();
            }
        }
        throw new IllegalStateException("Registry has no Yahtzee category");
    }

    /**
     * Index of the upper-section category that counts the given face (e.g. 5 -> Fives).
     *
     * @param face die face 1-6
     * @return matching upper-section index, or -1 if none
     */
    public int upperIndexFor(int face) {
        for (Combination combo : allCombos) {
            if (combo instanceof Numbers numbers && numbers.getTargetNumber() == face) {
                return combo.getIndex();
            }
        }
        return -1;
    }
}
