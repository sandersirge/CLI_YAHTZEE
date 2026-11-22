package com.ssander.yahtzee.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns dice values into console text, either as drawn die faces or as a compact row.
 *
 * <p>Plain ASCII is used on purpose so the dice look the same in every console
 * (Windows code pages included).</p>
 */
public final class DiceRenderer {

    /** How dice are shown. */
    public enum Style {
        /** Each die drawn as a small box with pips. */
        FACES,
        /** One line of values: {@code [3]  [5]  [5] ...} */
        COMPACT;

        /**
         * Parses a user answer: "1"/"faces"/"f" or "2"/"compact"/"c".
         *
         * @param text user input
         * @return the style, or null if the text matches neither
         */
        public static Style parse(String text) {
            return switch (text.trim().toLowerCase(java.util.Locale.ROOT)) {
                case "1", "f", "face", "faces" -> FACES;
                case "2", "c", "compact", "row" -> COMPACT;
                default -> null;
            };
        }
    }

    // Pip layout on a 3x3 grid, row by row ('o' = pip).
    private static final String[][] PIPS = {
        {"     ", "  o  ", "     "},
        {"o    ", "     ", "    o"},
        {"o    ", "  o  ", "    o"},
        {"o   o", "     ", "o   o"},
        {"o   o", "  o  ", "o   o"},
        {"o   o", "o   o", "o   o"},
    };
    private static final String TOP = "+-------+";
    private static final String GAP = "  ";

    private DiceRenderer() {
    }

    /**
     * Renders the dice with their position numbers (1-based) underneath.
     *
     * @param values die values 1-6, in display order
     * @param style face drawing or compact row
     * @return lines to print
     */
    public static List<String> render(List<Integer> values, Style style) {
        return style == Style.FACES ? faces(values) : compact(values);
    }

    private static List<String> faces(List<Integer> values) {
        List<StringBuilder> rows = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            rows.add(new StringBuilder("  "));
        }
        for (int d = 0; d < values.size(); d++) {
            String[] pips = PIPS[values.get(d) - 1];
            String sep = d == 0 ? "" : GAP;
            rows.get(0).append(sep).append(TOP);
            for (int r = 0; r < 3; r++) {
                rows.get(r + 1).append(sep).append("| ").append(pips[r]).append(" |");
            }
            rows.get(4).append(sep).append(TOP);
            rows.get(5).append(sep).append(String.format("   (%d)   ", d + 1));
        }
        List<String> lines = new ArrayList<>();
        for (StringBuilder row : rows) {
            lines.add(row.toString().stripTrailing());
        }
        return lines;
    }

    private static List<String> compact(List<Integer> values) {
        StringBuilder numbers = new StringBuilder("  Die: ");
        StringBuilder faces = new StringBuilder("  Roll:");
        for (int i = 0; i < values.size(); i++) {
            numbers.append(String.format("  %d  ", i + 1));
            faces.append(String.format(" [%d] ", values.get(i)));
        }
        return List.of(numbers.toString().stripTrailing(), faces.toString().stripTrailing());
    }
}
