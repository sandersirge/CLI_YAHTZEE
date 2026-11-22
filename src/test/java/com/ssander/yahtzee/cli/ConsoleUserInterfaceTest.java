package com.ssander.yahtzee.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ssander.yahtzee.management.CombinationRegistry;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.ui.InputClosedException;

class ConsoleUserInterfaceTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private ConsoleUserInterface ui(String input) {
        return new ConsoleUserInterface(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8),
                DiceRenderer.Style.COMPACT);
    }

    private String printed() {
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void playerCountReasksOnTyposAndOutOfRange() {
        assertEquals(2, ui("two\n0\n9\n2\n").askPlayerCount(6));
        assertTrue(printed().contains("Please enter a number from 1 to 6."));
    }

    @Test
    void emptyNameBecomesDefault() {
        assertEquals("Player 3", ui("\n").askPlayerName(3));
        assertEquals("Ana", ui("  Ana  \n").askPlayerName(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"y", "Y", "yes", "YES", "Yes"})
    void yesAnswersAreAccepted(String answer) {
        assertTrue(ui(answer + "\n").askRollAgain(2));
    }

    @Test
    void rollAgainReasksAndCanToggleDiceView() {
        ConsoleUserInterface console = ui("maybe\nv\nn\n");
        assertFalse(console.askRollAgain(1));
        assertEquals(DiceRenderer.Style.FACES, console.getDiceStyle());
        assertTrue(printed().contains("Please answer y (yes), n (no) or v"));
    }

    @Test
    void keptDiceAcceptSeveralFormats() {
        assertArrayEquals(new boolean[] {true, false, true, false, true}, ui("1 3 5\n").askKeptDice(5));
        assertArrayEquals(new boolean[] {true, false, true, false, true}, ui("1,3,5\n").askKeptDice(5));
        assertArrayEquals(new boolean[] {true, false, true, false, true}, ui("135\n").askKeptDice(5));
        assertArrayEquals(new boolean[] {true, true, true, true, true}, ui("ALL\n").askKeptDice(5));
        assertArrayEquals(new boolean[5], ui("\n").askKeptDice(5));
    }

    @Test
    void keptDiceReasksOnInvalidInput() {
        assertArrayEquals(new boolean[] {false, true, false, false, false}, ui("7\nx\n2\n").askKeptDice(5));
        assertTrue(printed().contains("There are only 5 dice"));
        assertTrue(printed().contains("Use die numbers 1-5"));
    }

    @Test
    void parseDiceNumbersRejectsLetters() {
        assertEquals(List.of(1, 2), ConsoleUserInterface.parseDiceNumbers("1 2"));
        assertNull(ConsoleUserInterface.parseDiceNumbers("1 a"));
    }

    @Test
    void combinationIsZeroBased() {
        assertEquals(12, ui("abc\n14\n13\n").askCombination(13));
    }

    @Test
    void endOfInputThrowsInputClosed() {
        assertThrows(InputClosedException.class, () -> ui("").askPlayerCount(6));
    }

    @Test
    void diceViewIsAskedWhenNotPreset() {
        ConsoleUserInterface console = new ConsoleUserInterface(
                new ByteArrayInputStream("3\nfaces\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        console.askDisplayOptions();
        assertEquals(DiceRenderer.Style.FACES, console.getDiceStyle());
    }

    @Test
    void endScreenReportsTieAndSharedPlaces() {
        CombinationRegistry registry = new CombinationRegistry();
        Player alice = new Player("Alice", registry.getAllCombos());
        Player bob = new Player("Bob", registry.getAllCombos());
        Player cy = new Player("Cy", registry.getAllCombos());
        alice.applyScore(registry.getComboByIndex(12), 41);
        bob.applyScore(registry.getComboByIndex(12), 41);
        cy.applyScore(registry.getComboByIndex(12), 20);
        ui("").displayGameEnd(List.of(alice, bob, cy));
        String text = printed();
        assertTrue(text.contains("It's a tie between Alice and Bob with 41 points!"));
        assertTrue(text.contains("3.     Cy"));
        assertEquals(2, text.split("1\\. ", -1).length - 1);
    }

    @Test
    void endScreenNamesSingleWinner() {
        CombinationRegistry registry = new CombinationRegistry();
        Player alice = new Player("Alice", registry.getAllCombos());
        Player bob = new Player("Bob", registry.getAllCombos());
        alice.applyScore(registry.getComboByIndex(12), 30);
        ui("").displayGameEnd(List.of(alice, bob));
        assertTrue(printed().contains("Winner: Alice with 30 points!"));
    }
}
