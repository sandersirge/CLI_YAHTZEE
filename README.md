# CLI-supported Yahtzee

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
- **Score Sheet**: After every roll you see all 13 categories: what is already scored, what this roll would score in each open one, your upper-section progress towards the bonus (`41/63 - 22 more for +35`), bonuses and the total.
- **Official Extra Rules (optional)**: At the start you choose whether to play with the **Yahtzee bonus** (+100 for every extra Yahtzee while the Yahtzee box holds 50) and the **Joker rule** (a later Yahtzee must fill its matching upper box if open; otherwise it counts as a full Full House, Small or Large Straight). The rules live in `ScoringService`.
- **Forgiving Input**: Every prompt re-asks instead of crashing on a typo. Answers accept `y`/`yes`/`n`/`no` in any case; dice to keep can be typed as `1 3 5`, `1,3,5` or `135`, `all` keeps everything and Enter rerolls all. An empty name becomes "Player N".
- **No Accidental Zeros**: Choosing a category that would score 0 asks for confirmation first.
- **Clear Results**: Turns show the player's name and round (`Round 4/13 - Alice's turn`). The end screen names the winner, reports ties, and lists places (tied players share a place) with upper, bonus, lower and total points.
- **Dice View**: Choose between drawn die faces and a compact one-line row at the start, with `--faces` / `--compact`, or switch any time by typing `v` at the "Roll again?" prompt.
- **Graceful Shutdown**: `Ctrl+C` during a game prints "Game aborted. Closing now…"; a finished game exits quietly, and if input ends (e.g. piped input runs out) the game closes cleanly.
- **Repeatable Games**: `GameManager` and `Die` accept a random source; a seeded one replays the exact same game (used by the tests).
- **Documentation**: Comprehensive Javadoc coverage describes responsibilities, parameters, and return values across the codebase.

```text
  +-------+  +-------+  +-------+  +-------+  +-------+
  | o     |  | o     |  | o     |  | o   o |  | o   o |
  |       |  |       |  |   o   |  |   o   |  |   o   |
  |     o |  |     o |  |     o |  | o   o |  | o   o |
  +-------+  +-------+  +-------+  +-------+  +-------+
     (1)        (2)        (3)        (4)        (5)
```

---

## Setup & Run Instructions

### Prerequisites

- Java Development Kit (JDK) 21 or newer. Gradle itself is downloaded by the included wrapper
  (Gradle 9.9.2, which also runs on the newest JDKs).

### Command line

```powershell
cd CLI_YAHTZEE
.\gradlew run -q --console=plain     # play in the terminal (./gradlew on macOS/Linux)
.\gradlew test                       # run the JUnit tests
.\gradlew build                      # compile, test and build build/libs/cli-yahtzee-1.1.0.jar
java -jar build/libs/cli-yahtzee-1.1.0.jar --faces   # or --compact, --help
```

Follow the prompts: number of players (1–6), names, whether to use the extra rules and how to show the dice; then each turn roll, keep dice and pick a category.

### IntelliJ IDEA

Choose **Open** and select the project folder (or its `build.gradle`). IntelliJ imports it as a Gradle project; run `Game.main` from the gutter icon, or the Gradle `run` / `test` tasks.

### Visual Studio Code

With the **Extension Pack for Java** installed, open the folder; the Gradle project is detected automatically. Run `com.ssander.yahtzee.Game` from **Run and Debug**.

---

## Project Layout & Quality

```text
CLI_YAHTZEE/
├── build.gradle, settings.gradle, gradlew(.bat), gradle/wrapper/   # Gradle build
├── .github/workflows/ci.yml        # build + tests on Windows, macOS, Linux (JDK 21 and 25)
├── src/main/java/com/ssander/yahtzee/
│   ├── Game.java                   # entry point, --faces / --compact / --help
│   ├── cli/                        # ConsoleUserInterface, DiceRenderer
│   ├── combos/                     # the 13 scoring rules
│   ├── management/                 # GameManager, ScoringService, CombinationRegistry
│   ├── player/                     # Player (score sheet), Die
│   └── ui/                         # GameUI contract, InputClosedException
└── src/test/java/com/ssander/yahtzee/   # JUnit 6 tests
```

- **Tests**: 79 JUnit tests cover every scoring rule, the Joker and bonus rules, player totals, input parsing and re-asking, dice drawing, the end screen, and complete seeded games played by a scripted player.
- **Warnings are errors**: the build compiles with `-Xlint:all -Werror` and targets Java 21 bytecode.
- **CI**: every push and pull request runs the build and tests on Windows, macOS and Linux.

---

## Possible Future Enhancements

- **More Rule Variants**: Further house rules beyond the optional Yahtzee bonus and Joker rule.
- **Persistent Scores**: Store game history (e.g., JSON/CSV) for leaderboards or resume functionality.
- **Graphical Interface**: Replace console UI with Swing/JavaFX or a web-based front end.
- **Internationalization**: Reintroduce Estonian strings through resource bundles to support multiple languages.

---

## Contributing

Feel free to fork and extend the project. Pull requests, issue reports, and feature suggestions are welcome!
