package com.ssander.yahtzee.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.ssander.yahtzee.management.CategoryOption;
import com.ssander.yahtzee.player.Die;
import com.ssander.yahtzee.player.Player;
import com.ssander.yahtzee.ui.GameUI;
import com.ssander.yahtzee.ui.InputClosedException;

/**
 * Console implementation of the {@link GameUI} contract.
 *
 * <p>Every prompt re-asks on invalid input instead of crashing, accepts
 * forgiving answers (y/yes/n/no in any case, "1 3 5" or "135" for dice), and
 * turns the end of input into an {@link InputClosedException}.</p>
 */
public class ConsoleUserInterface implements GameUI {
    private static final Set<String> YES = Set.of("y", "yes");
    private static final Set<String> NO = Set.of("n", "no");
    private static final String RULE = "-".repeat(52);

    private final BufferedReader input;
    private final PrintStream out;
    /** Chosen dice view; null means "ask at the start". */
    private DiceRenderer.Style diceStyle;
    private List<Integer> lastDice = List.of();

    /** Console UI on standard input/output; asks for the dice view at the start. */
    public ConsoleUserInterface() {
        this(System.in, System.out, null);
    }

    /** Console UI on standard input/output with a preset dice view (null = ask). */
    public ConsoleUserInterface(DiceRenderer.Style diceStyle) {
        this(System.in, System.out, diceStyle);
    }

    /** Console UI on the given streams (used for scripted runs and tests). */
    public ConsoleUserInterface(InputStream in, PrintStream out) {
        this(in, out, null);
    }

    /**
     * Console UI on the given streams with a preset dice view.
     *
     * @param in input stream
     * @param out output stream
     * @param diceStyle dice view, or null to ask the player
     */
    public ConsoleUserInterface(InputStream in, PrintStream out, DiceRenderer.Style diceStyle) {
        this.input = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
        this.diceStyle = diceStyle;
    }

    /** @return the dice view currently in use (null until chosen) */
    public DiceRenderer.Style getDiceStyle() {
        return diceStyle;
    }

    // Input helpers -----------------------------------------------------------

    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            String line = input.readLine();
            if (line == null) {
                out.println();
                throw new InputClosedException();
            }
            return line.trim();
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    private int readNumber(String prompt, int min, int max) {
        while (true) {
            String answer = readLine(prompt);
            try {
                int value = Integer.parseInt(answer);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the hint below
            }
            out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt).toLowerCase(Locale.ROOT);
            if (YES.contains(answer)) {
                return true;
            }
            if (NO.contains(answer)) {
                return false;
            }
            out.println("Please answer y (yes) or n (no).");
        }
    }

    // Setup -------------------------------------------------------------------

    @Override
    public int askPlayerCount(int maxPlayers) {
        return readNumber("Number of players (1-" + maxPlayers + "): ", 1, maxPlayers);
    }

    @Override
    public String askPlayerName(int orderNumber) {
        String name = readLine("Name of player " + orderNumber + " (Enter for \"Player " + orderNumber + "\"): ");
        return name.isEmpty() ? "Player " + orderNumber : name;
    }

    @Override
    public void askDisplayOptions() {
        if (diceStyle != null) {
            return;
        }
        out.println();
        out.println("How should dice be shown?");
        out.println("  1) Die faces   +-------+");
        out.println("                 | o   o |   (drawn dice)");
        out.println("                 +-------+");
        out.println("  2) Compact     [3]  [5]  [6]  (one line)");
        while (diceStyle == null) {
            diceStyle = DiceRenderer.Style.parse(readLine("Dice view (1/2): "));
            if (diceStyle == null) {
                out.println("Please enter 1 for die faces or 2 for compact.");
            }
        }
        out.println("Tip: type v at \"Roll again?\" to switch the dice view.");
    }

    @Override
    public boolean askUseExtraRules() {
        out.println();
        out.println("Extra rules: +100 for every extra Yahtzee, and the Joker rule");
        out.println("(a Yahtzee can fill Full House or a Straight once the Yahtzee box is used).");
        return readYesNo("Play with the extra rules? (y/n): ");
    }

    @Override
    public void displayGameStart(List<Player> players, boolean extraRules) {
        out.println();
        out.println(RULE);
        out.println("Game starts! Players: "
                + String.join(", ", players.stream().map(Player::getPlayerName).toList()));
        out.println("Extra rules: " + (extraRules ? "on" : "off"));
        out.println(RULE);
    }

    // Turn ----------------------------------------------------------------------

    @Override
    public void displayPlayerTurn(Player player, int round, int totalRounds) {
        out.println();
        out.println("=== Round " + round + "/" + totalRounds + " - " + player.getPlayerName()
                + "'s turn (score " + player.getTotalScore() + ") ===");
    }

    @Override
    public void displayDice(List<Die> dice) {
        lastDice = dice.stream().map(Die::getCurrentValue).toList();
        printDice();
    }

    private void printDice() {
        out.println();
        DiceRenderer.Style style = diceStyle != null ? diceStyle : DiceRenderer.Style.COMPACT;
        for (String line : DiceRenderer.render(lastDice, style)) {
            out.println(line);
        }
    }

    @Override
    public void displayScoreSheet(Player player, List<CategoryOption> options, boolean joker) {
        out.println();
        out.printf("  %-3s %-17s %7s   %s%n", "#", "Category", "Score", "This roll");
        for (CategoryOption row : options) {
            String scored = row.isOpen() ? "-" : String.valueOf(row.scored());
            String roll = "";
            if (row.isOpen()) {
                if (!row.allowed()) {
                    roll = "(not now)";
                } else if (row.points() == 0) {
                    roll = "0 (scratch)";
                } else {
                    roll = String.valueOf(row.points());
                }
            }
            out.printf("  %-3d %-17s %7s   %s%n", row.number(), row.name(), scored, roll);
            if (row.number() == lastUpperNumber(options)) {
                printUpperSubtotal(player);
            }
        }
        if (player.getYahtzeeBonusCount() > 0) {
            out.printf("      %-17s %7s%n", "Yahtzee bonus", "+" + player.getYahtzeeBonusPoints());
        }
        out.printf("      %-17s %7d%n", "TOTAL", player.getTotalScore());
        if (joker) {
            out.println("  Joker! This Yahtzee can only go in a category that shows points.");
        }
    }

    /** The upper section is Ones to Sixes: the first six rows. */
    private static int lastUpperNumber(List<CategoryOption> options) {
        return Math.min(6, options.size());
    }

    private void printUpperSubtotal(Player player) {
        int upper = player.getUpperSectionScore();
        String bonus = player.isUpperSectionBonusAwarded()
                ? "bonus +" + Player.UPPER_BONUS_POINTS
                : (Player.UPPER_BONUS_THRESHOLD - upper) + " more for +" + Player.UPPER_BONUS_POINTS;
        out.printf("      %-17s %7s   %s%n", "Upper subtotal", upper + "/" + Player.UPPER_BONUS_THRESHOLD, bonus);
    }

    @Override
    public boolean askRollAgain(int rollsLeft) {
        String prompt = "Roll again? (" + rollsLeft + (rollsLeft == 1 ? " roll" : " rolls")
                + " left, y/n, v = switch dice view): ";
        while (true) {
            String answer = readLine(prompt).toLowerCase(Locale.ROOT);
            if (YES.contains(answer)) {
                return true;
            }
            if (NO.contains(answer)) {
                return false;
            }
            if (answer.equals("v")) {
                diceStyle = diceStyle == DiceRenderer.Style.FACES
                        ? DiceRenderer.Style.COMPACT
                        : DiceRenderer.Style.FACES;
                printDice();
                continue;
            }
            out.println("Please answer y (yes), n (no) or v (switch dice view).");
        }
    }

    @Override
    public boolean[] askKeptDice(int diceCount) {
        while (true) {
            String answer = readLine("Dice to keep (e.g. \"1 3 5\" or \"135\"; Enter = reroll all, \"all\" = keep all): ")
                    .toLowerCase(Locale.ROOT);
            boolean[] kept = new boolean[diceCount];
            if (answer.equals("all")) {
                Arrays.fill(kept, true);
                return kept;
            }
            List<Integer> chosen = parseDiceNumbers(answer);
            if (chosen == null) {
                out.println("Use die numbers 1-" + diceCount + ", separated by spaces or commas.");
                continue;
            }
            boolean valid = true;
            for (int number : chosen) {
                if (number < 1 || number > diceCount) {
                    valid = false;
                    break;
                }
                kept[number - 1] = true;
            }
            if (valid) {
                return kept;
            }
            out.println("There are only " + diceCount + " dice (1-" + diceCount + ").");
        }
    }

    /** "1 3 5", "1,3,5" and "135" all mean dice 1, 3 and 5; null if anything else is typed. */
    static List<Integer> parseDiceNumbers(String answer) {
        List<Integer> numbers = new ArrayList<>();
        for (char c : answer.toCharArray()) {
            if (Character.isDigit(c)) {
                numbers.add(c - '0');
            } else if (c != ' ' && c != ',' && c != ';') {
                return null;
            }
        }
        return numbers;
    }

    @Override
    public int askCombination(int categoryCount) {
        return readNumber("Which category do you score? (1-" + categoryCount + "): ", 1, categoryCount) - 1;
    }

    @Override
    public void displayInvalidCombinationSelection(String reason) {
        out.println(reason);
    }

    @Override
    public boolean confirmScratch(String categoryName) {
        return readYesNo("That scores 0 in " + categoryName + " and uses it up. Are you sure? (y/n): ");
    }

    @Override
    public void displayBonusMessage(int bonusPoints) {
        out.println("Upper section bonus! +" + bonusPoints + " points.");
    }

    @Override
    public void displayYahtzeeBonus(int bonusPoints) {
        out.println("Another Yahtzee! +" + bonusPoints + " bonus points.");
    }

    @Override
    public void displayScoreUpdate(Player player, String categoryName, int points) {
        out.println(player.getPlayerName() + " scored " + points + " in " + categoryName
                + ". Total: " + player.getTotalScore());
    }

    // End -------------------------------------------------------------------------

    @Override
    public void displayGameEnd(List<Player> ranking) {
        out.println();
        out.println(RULE);
        out.println("Game over!");
        int best = ranking.get(0).getTotalScore();
        List<String> winners = ranking.stream()
                .filter(p -> p.getTotalScore() == best)
                .map(Player::getPlayerName)
                .toList();
        if (ranking.size() == 1) {
            out.println("Final score: " + best);
        } else if (winners.size() == 1) {
            out.println("Winner: " + winners.get(0) + " with " + best + " points!");
        } else {
            out.println("It's a tie between " + String.join(" and ", winners) + " with " + best + " points!");
        }
        out.println();
        out.printf("  %-6s %-16s %6s %7s %7s %7s%n", "Place", "Player", "Upper", "Bonus", "Lower", "Total");
        int place = 0;
        int previousScore = Integer.MIN_VALUE;
        for (int i = 0; i < ranking.size(); i++) {
            Player player = ranking.get(i);
            if (player.getTotalScore() != previousScore) {
                place = i + 1; // equal scores share a place
                previousScore = player.getTotalScore();
            }
            int bonus = player.getUpperBonusPoints() + player.getYahtzeeBonusPoints();
            out.printf("  %-6s %-16s %6d %7d %7d %7d%n", place + ".", player.getPlayerName(),
                    player.getUpperSectionScore(), bonus, player.getLowerSectionScore(),
                    player.getTotalScore());
        }
        out.println(RULE);
    }

    @Override
    public void displayAborted() {
        out.println("Input ended - game closed.");
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException ignored) {
            // nothing useful to do when closing fails
        }
    }
}
