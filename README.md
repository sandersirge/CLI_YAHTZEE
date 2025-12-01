# Yahtzee OOP Refactor

## Project Overview

This project implements the classic Yahtzee dice game in Java. It started as a coursework-style exercise with a single monolithic class and has since been fully refactored into a modular, object-oriented design with clear package boundaries, testable components, and English-language documentation.

---

## Legacy Implementation (Prior State)

- **Structure**: All gameplay logic lived inside one 300+ line `Mäng.java` file written in Estonian, with large switch statements handling the thirteen scoring categories.
- **Hardcoded Logic**: Dice combination checks and scoring had duplicated code paths. Validation relied on positional assumptions rather than reusable logic.
- **Tight Coupling**: User interaction (console I/O), game management, and scoring logic were interwoven, making changes risky and unit testing impractical.
- **Minimal Abstractions**: Combination rules were not encapsulated; there was no registry or polymorphism to manage scoring categories.
- **Language Barrier**: Class names, variables, and console prompts were in Estonian, limiting accessibility for broader collaboration.

---

## Current Architecture (Enhanced Version)

The codebase now follows a modular package structure within the namespace `com.ssander.yahtzee`, with clear responsibilities:

- **`com.ssander.yahtzee.combos`** – Contains the abstract `Combination` base class and concrete scoring rules (`Numbers`, `ThreeOfKind`, `FullHouse`, `SmallStraight`, `Yahtzee`, etc.). Each combination handles its own validation (`isPossible`) and scoring (`calculatePoints`).
- **`com.ssander.yahtzee.management`** – Coordinates gameplay through `GameManager` and maintains combination instances via `CombinationRegistry`.
- **`com.ssander.yahtzee.player`** – Defines player-related entities (`Player`, `Die`, `Rollable`). Players track upper/lower section totals, bonus state, and scored combinations.
- **`com.ssander.yahtzee.ui`** – Declares the `GameUI` contract so the core game logic can work with any front end.
- **`com.ssander.yahtzee.cli`** – Provides `ConsoleUserInterface`, the console-based implementation of the UI contract.
- **`com.ssander.yahtzee.Game`** – Minimal entry point that wires up the UI implementation and the `GameManager`.

### Key Improvements

- **Polymorphic Scoring**: Each combination class implements its rule, reducing duplication and clarifying intent.
- **Robust Validation**: Frequency maps and set logic avoid positional assumptions, ensuring correctness for three/four of a kind, straights, full house, and Yahtzee checks.
- **English Translation**: All classes, prompts, and documentation use English terminology.
- **Dedicated UI Layer**: Console interactions are isolated behind a `GameUI` contract, enabling future UI changes without touching game logic.
- **Enhanced UX**: Dice output uses friendly labels (e.g., `1. die - value: 5`), bonus awards are announced, and score updates are shown after each turn.
- **Graceful Shutdown**: Pressing `Ctrl+C` cleanly terminates the game with an “Game aborted. Closing now…” message.
- **Documentation**: Comprehensive Javadoc coverage describes responsibilities, parameters, and return values across the codebase.

---

## Setup & Run Instructions

### Prerequisites

- Java Development Kit (JDK) 21 or newer installed and available on your `PATH`.

### IntelliJ IDEA

1. Launch IntelliJ IDEA and choose **Open**.
2. Point to the project root (`PROJEKT_YAHTZEE`). IntelliJ detects the `src` folder automatically.
3. If prompted, mark `src` as a **Sources Root** (the base package is `com.ssander.yahtzee`).
4. Press `Ctrl+Shift+F9` (Build Project) or use the **Build** menu to compile.
5. Run the application via the green gutter icon next to the `main` method or through a Run Configuration (Main class: `com.ssander.yahtzee.Game`).

### Visual Studio Code

1. Install the **Extension Pack for Java** (or at minimum the Language Support for Java extension).
2. Open the project folder in VS Code.
3. When prompted, trust the authors and allow VS Code to configure the Java workspace.
4. Use the **Run and Debug** sidebar, select `com.ssander.yahtzee.Game` as the main class, and click **Run**. VS Code will compile classes into the auto-generated `bin` folder.

### Command Line (PowerShell)

1. From the project root, compile the sources into an output directory:

   ```powershell
   cd CLI_YAHTZEE
   javac -d out (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
   ```

2. Start the game using the compiled classes:

   ```powershell
   java -cp out com.ssander.yahtzee.Game
   ```

3. Follow the console prompts to enter the number of players (1–3), roll dice, choose combinations, and track scores.

---

## Possible Future Enhancements

- **Automated Testing**: Introduce JUnit tests for combination validation and scoring logic.
- **Configuration Options**: Allow variable player counts, dice numbers, or rule variants (e.g., Joker rules).
- **Persistent Scores**: Store game history (e.g., JSON/CSV) for leaderboards or resume functionality.
- **Graphical Interface**: Replace console UI with Swing/JavaFX or a web-based front end.
- **Internationalization**: Reintroduce Estonian strings through resource bundles to support multiple languages.

---

## Contributing

Feel free to fork and extend the project. Pull requests, issue reports, and feature suggestions are welcome!
