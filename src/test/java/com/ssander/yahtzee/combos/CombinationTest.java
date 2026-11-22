package com.ssander.yahtzee.combos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CombinationTest {

    private static List<Integer> dice(String values) {
        return Arrays.stream(values.split(" ")).map(Integer::parseInt).toList();
    }

    @ParameterizedTest(name = "{0} in {1}s -> {2}")
    @CsvSource({
        "1 1 2 3 1, 1, 3",
        "6 6 6 6 6, 6, 30",
        "2 3 4 5 6, 1, 0",
        "5 5 1 2 5, 5, 15",
    })
    void numbersCountMatchingFaces(String roll, int face, int points) {
        Numbers numbers = new Numbers("n", face - 1, face);
        assertEquals(points, numbers.calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "three of a kind {0} -> {1}")
    @CsvSource({"3 3 3 4 5, 18", "2 2 2 2 6, 14", "1 2 3 4 5, 0", "6 6 1 1 2, 0"})
    void threeOfAKindSumsAllDice(String roll, int points) {
        assertEquals(points, new ThreeOfKind(6).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "four of a kind {0} -> {1}")
    @CsvSource({"4 4 4 4 1, 17", "5 5 5 5 5, 25", "3 3 3 2 2, 0"})
    void fourOfAKindSumsAllDice(String roll, int points) {
        assertEquals(points, new FourOfKind(7).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "full house {0} -> {1}")
    @CsvSource({"2 2 3 3 3, 25", "6 6 6 1 1, 25", "4 4 4 4 1, 0", "5 5 5 5 5, 0", "1 2 3 4 5, 0"})
    void fullHouseNeedsThreeAndTwo(String roll, int points) {
        assertEquals(points, new FullHouse(8).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "small straight {0} -> {1}")
    @CsvSource({"1 2 3 4 6, 30", "3 4 5 6 6, 30", "2 3 4 5 5, 30", "1 2 3 5 6, 0", "1 1 2 2 3, 0"})
    void smallStraightNeedsFourInARow(String roll, int points) {
        assertEquals(points, new SmallStraight(9).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "large straight {0} -> {1}")
    @CsvSource({"1 2 3 4 5, 40", "2 3 4 5 6, 40", "1 2 3 4 6, 0", "1 3 4 5 6, 0"})
    void largeStraightNeedsFiveInARow(String roll, int points) {
        assertEquals(points, new LargeStraight(10).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "yahtzee {0} -> {1}")
    @CsvSource({"4 4 4 4 4, 50", "4 4 4 4 3, 0"})
    void yahtzeeNeedsFiveOfAKind(String roll, int points) {
        assertEquals(points, new Yahtzee(11).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "chance {0} -> {1}")
    @CsvSource({"1 2 3 4 5, 15", "6 6 6 6 6, 30"})
    void chanceSumsEverything(String roll, int points) {
        assertEquals(points, new Chance(12).calculatePoints(dice(roll)));
    }

    @ParameterizedTest(name = "joker points of {0}")
    @CsvSource({"FullHouse, 25", "SmallStraight, 30", "LargeStraight, 40", "Chance, 15"})
    void jokerGivesFixedScoresOnlyToFullHouseAndStraights(String category, int points) {
        Combination combo = switch (category) {
            case "FullHouse" -> new FullHouse(8);
            case "SmallStraight" -> new SmallStraight(9);
            case "LargeStraight" -> new LargeStraight(10);
            default -> new Chance(12);
        };
        assertEquals(points, combo.jokerPoints(dice("3 3 3 3 3")));
    }
}
