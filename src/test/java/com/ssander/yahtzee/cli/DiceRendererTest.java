package com.ssander.yahtzee.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DiceRendererTest {

    private static long pips(List<String> lines) {
        // Rows 1-3 are the inside of the faces; count the pip marks.
        return lines.subList(1, 4).stream().mapToLong(line -> line.chars().filter(c -> c == 'o').count()).sum();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void eachFaceShowsItsNumberOfPips(int value) {
        List<String> lines = DiceRenderer.render(List.of(value), DiceRenderer.Style.FACES);
        assertEquals(6, lines.size());
        assertEquals(value, pips(lines));
    }

    @Test
    void facesAreNumberedUnderneath() {
        List<String> lines = DiceRenderer.render(List.of(1, 2, 3, 4, 5), DiceRenderer.Style.FACES);
        assertTrue(lines.get(5).contains("(1)") && lines.get(5).contains("(5)"));
        assertEquals(1 + 2 + 3 + 4 + 5, pips(lines));
    }

    @Test
    void compactIsTwoLines() {
        List<String> lines = DiceRenderer.render(List.of(3, 5, 6), DiceRenderer.Style.COMPACT);
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("[3]  [5]  [6]"));
    }

    @Test
    void styleParsing() {
        assertEquals(DiceRenderer.Style.FACES, DiceRenderer.Style.parse(" Faces "));
        assertEquals(DiceRenderer.Style.COMPACT, DiceRenderer.Style.parse("2"));
        assertNull(DiceRenderer.Style.parse("3"));
    }
}
