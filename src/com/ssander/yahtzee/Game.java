package com.ssander.yahtzee;

import com.ssander.yahtzee.cli.ConsoleUserInterface;
import com.ssander.yahtzee.management.GameManager;
import com.ssander.yahtzee.ui.GameUI;

/**
 * Main entry point for the Yahtzee game application.
 * Creates a game manager and starts the game.
 */
public final class Game {
    private Game() {
        // Utility class
    }

    /**
     * Main method to launch the Yahtzee game.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        GameUI ui = new ConsoleUserInterface();
        GameManager manager = new GameManager(ui);
        manager.startGame();
    }
}
