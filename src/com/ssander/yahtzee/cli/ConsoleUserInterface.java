package com.ssander.yahtzee.cli;

import java.util.List;
import java.util.Scanner;

import com.ssander.yahtzee.combos.Combination;
import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.ui.GameUI;

/**
 * Console implementation of the {@link GameUI} contract.
 * Handles user prompts, dice display, and result summaries via standard input/output.
 */
public class ConsoleUserInterface implements GameUI {
    private final Scanner input;
    
    /**
     * Constructs a new console user interface with a scanner for console input.
     */
    public ConsoleUserInterface() {
        this.input = new Scanner(System.in);
    }
    
    /** {@inheritDoc} */
    @Override
    public int askPlayerCount() {
        System.out.print("Enter number of players (maximum 3): ");
        int count = Integer.parseInt(input.nextLine());
        while (count > 3 || count < 1) {
            System.out.println("Invalid number. Please try again!");
            System.out.print("Enter number of players (maximum 3): ");
            count = Integer.parseInt(input.nextLine());
        }
        return count;
    }
    
    /** {@inheritDoc} */
    @Override
    public String askPlayerName(int orderNumber) {
        System.out.print("Enter player " + orderNumber + " name: ");
        return input.nextLine();
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayGameStart() {
        System.out.println("\nGame starts!\n");
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayPlayerTurn(int orderNumber, Player player) {
        System.out.println("Player " + orderNumber + "'s turn to roll dice:");
        System.out.println(player);
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayDice(List<Die> dice) {
        System.out.println("Values are:");
        int dieNumber = 1;
        for (Die die : dice) {
            System.out.println((dieNumber++) + ". die - value: " + die.getCurrentValue());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayPossibleCombos(List<Combination> combos, List<Integer> values, boolean[] usedCombos) {
        System.out.println("\nAvailable combinations:");
        for (int i = 0; i < combos.size(); i++) {
            if (!usedCombos[i]) {
                Combination combo = combos.get(i);
                if (combo.isPossible(values)) {
                    System.out.println((i + 1) + ". " + combo.getComboName() + " - " + combo.calculatePoints(values));
                } else {
                    System.out.println((i + 1) + ". " + combo.getComboName() + " - /");
                }
            }
        }
        System.out.println();
    }

    /** {@inheritDoc} */
    @Override
    public void displayInvalidCombinationSelection() {
        System.out.println("Invalid choice or already used combination. Please pick an available option.");
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean askRollAgain() {
        System.out.print("Do you want to roll dice again? (yes/no): ");
        String answer = input.nextLine();
        while (!answer.equals("no") && !answer.equals("yes")) {
            System.out.print("Please try again, you can only enter 'yes' or 'no': ");
            answer = input.nextLine();
        }
        return answer.equals("yes");
    }
    
    /** {@inheritDoc} */
    @Override
    public int[] askKeptDice() {
        System.out.println("""
                
                Which dice do you want to keep?\
                
                Enter the dice numbers one by one on separate lines (will not be rerolled/'ENTER' to finish):\s""");
        int[] kept = new int[5];
        while (true) {
            String diceChoice = input.nextLine();
            if (diceChoice.isEmpty()) break;
            int index = Integer.parseInt(diceChoice) - 1;
            if (index >= 0 && index < 5) {
                kept[index] = 1;
            }
        }
        return kept;
    }
    
    /** {@inheritDoc} */
    @Override
    public int askCombination() {
        System.out.print("\nWhich combination do you want to score? (enter combination line number): ");
        return Integer.parseInt(input.nextLine()) - 1;
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayBonusMessage() {
        System.out.println("\nYou got 35 bonus points for the upper section. Good job!");
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayScoreUpdate(int playerNumber, Player player) {
        System.out.println("\nUpdated score of Player " + playerNumber + ":\n");
        System.out.println(player);
    }
    
    /** {@inheritDoc} */
    @Override
    public void displayGameEnd(List<Player> players) {
        System.out.println("\nGame over! Here are the results:");
        System.out.println("Winner!!!");
        for (Player player : players) {
            System.out.println(player);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void close() {
        input.close();
    }
}
