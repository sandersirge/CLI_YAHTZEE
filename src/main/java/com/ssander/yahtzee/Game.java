package com.ssander.yahtzee;

import java.io.PrintStream;

import com.ssander.yahtzee.cli.ConsoleUserInterface;
import com.ssander.yahtzee.cli.DiceRenderer;
import com.ssander.yahtzee.management.GameManager;

/**
 * Main entry point for the Yahtzee game application.
 *
 * <p>Options: {@code --faces} or {@code --compact} pick the dice view without asking,
 * {@code --help} prints usage.</p>
 */
public final class Game {
    private Game() {
        // Utility class
    }

    /**
     * Launches the Yahtzee game.
     *
     * @param args optional {@code --faces}, {@code --compact} or {@code --help}
     */
    public static void main(String[] args) {
        DiceRenderer.Style style = null;
        for (String arg : args) {
            switch (arg) {
                case "--faces" -> style = DiceRenderer.Style.FACES;
                case "--compact" -> style = DiceRenderer.Style.COMPACT;
                case "--help", "-h" -> {
                    printUsage(System.out);
                    return;
                }
                default -> {
                    System.err.println("Unknown option: " + arg);
                    printUsage(System.err);
                    System.exit(2);
                }
            }
        }
        new GameManager(new ConsoleUserInterface(style)).startGame();
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage: java -jar cli-yahtzee.jar [--faces | --compact]");
        out.println("  --faces    draw dice as faces (default choice is asked at start)");
        out.println("  --compact  show dice as one compact row");
    }
}
