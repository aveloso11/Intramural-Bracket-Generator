# 🏆 Intramural Bracket Generator

A JavaFX-based tournament bracket generator for intramural sports and competitions. Supports 7 tournament formats with live bracket visualization, score reporting, standings, and save/load functionality.

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Tournament Formats](#tournament-formats)
- [How to Use](#how-to-use)
- [Scoring & Standings](#scoring--standings)
- [Score Matrix](#score-matrix)
- [Save & Load](#save--load)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [How to Run](#how-to-run)

---

## About

The Intramural Bracket Generator lets you set up any number of teams, choose a tournament format, and manage the entire bracket from start to champion. Click any match to report a result, and the bracket automatically propagates winners and updates standings in real time.

---

## Features

- 🎮 7 tournament formats supported
- ➕ Add participants by typing and pressing Enter — no buttons needed
- 🔀 Shuffle teams randomly for seeding variety
- ↩️ Undo last action (match result undo stack)
- 🎲 Simulate all pending matches with random scores
- 📊 Live Gantt-style bracket view with color-coded match cards
- 📋 Standings table with tiebreaker sorting
- 🔢 Score Matrix — head-to-head results grid per format
- 💾 Save bracket to `.txt` file; reload anytime
- 📈 Progress bar showing % of matches completed
- 🖥️ 1200×800 dark-themed JavaFX UI

---

## Tournament Formats

| Format | Team Count | Description |
|--------|-----------|-------------|
| **Single Elimination** | 4, 8, 16, 32 | Standard bracket; one loss = eliminated |
| **Double Elimination** | 4, 8, 16, 32 | Winners & Losers brackets; two losses = eliminated; Grand Final |
| **Play-ins SE** | 12, 24 | Top seeds get byes; lower seeds play-in to earn a spot in the main SE bracket |
| **Play-ins DE** | 12, 24 | Same as Play-ins SE but main bracket uses Double Elimination format |
| **Round Robin** | 3–8 | Every team plays every other team; standings by win % and point differential |
| **Swiss System** | 4–20 | Standings-based pairing each round; Buchholz tiebreaker; lazy round generation |
| **Free For All** | 4–12 | Full round-robin schedule; leaderboard ranked by wins, points scored, and points allowed |

### Play-In Details

**12 teams (Play-ins SE or DE):** Top 4 seeds receive byes. Seeds 5–12 (8 teams) compete in a play-in bracket; 4 survivors advance to the main 8-team bracket alongside the 4 bye seeds.

**24 teams (Play-ins SE or DE):** Top 8 seeds receive byes. Seeds 9–24 (16 teams) compete in a play-in bracket; 8 survivors advance to the main 16-team bracket.

---

## How to Use

1. **Enter participants** — click the input box on the left panel and type a name, then press **Enter** to add. Repeat for each team.
2. **Reorder if needed** — drag and drop rows in the participants list to reseed.
3. **Select a bracket type** from the right panel dropdown.
4. **Fill in bracket info** — bracket name, sport/game, and description (optional).
5. **Click a match card** in the bracket view to report a result. Enter both scores; the winner is determined automatically by the higher score. Ties are not allowed.
6. The bracket updates instantly — winners advance, losers drop to the Losers Bracket (in DE formats).
7. Use **SIMULATE** to auto-fill all pending matches with random scores.
8. Use **UNDO** to revert the last reported result.
9. When all matches are complete, the champion is displayed and the bracket status changes to **COMPLETE**.

---

## Scoring & Standings

### Elimination Formats (SE, DE, Play-ins)
- Click any match card to open the score dialog.
- Enter both team scores (no ties allowed).
- The higher score wins automatically — no dropdown selection needed.
- Winners propagate up the bracket; losers drop to the Losers Bracket in DE formats.

### Round Robin Standings

| Column | Description |
|--------|-------------|
| Rank | Based on tiebreaker order below |
| Wins / Losses | Match record |
| Win % | Wins ÷ Total matches × 100 |
| PD | Point Differential (Scored − Allowed) |

Tiebreaker order: Win % → Point Differential → Original seeding

### Swiss System Standings

| Column | Description |
|--------|-------------|
| Wins / Losses | Match record |
| Buchholz | Sum of all opponents' wins (strength-of-schedule) |
| Pts | Total points scored |

Pairings each round: sorted by Wins → Buchholz → Point Differential. Rematches are avoided where possible. Odd team counts receive a bye (auto 1-0 win).

Swiss rounds per team count: 3–8 teams = 3 rounds, 9–16 teams = 4 rounds, 17–20 teams = 5 rounds.

### Free For All Standings

Tiebreaker order: Wins → Points Scored → Fewest Points Allowed

---

## Score Matrix

Click **SCORE MATRIX** in the right panel to view head-to-head results. The display adapts per format:

| Format | Matrix Type |
|--------|-------------|
| Round Robin / Free For All | Grid: row = team, column = opponent; gold cell = winner |
| Swiss System | Round-by-round match result list with pending match reporting |
| Single / Double Elimination | Round-labeled match list grouped by bracket section; click pending matches to report |

---

## Save & Load

- **SAVE BRACKET** — saves the current bracket (teams, type, sport, description, status, and all completed match results) to a `.txt` file in the `saved_brackets/` folder.
- **LOAD BRACKET** — opens a file picker to reload any previously saved bracket. Note: match results are saved as text records; the bracket is rebuilt from teams on load.

---

## Project Structure

```
(project root)/
├── app.java                  # JavaFX entry point and full GUI (panels, bracket views, dialogs)
├── TournamentBracket.java    # Core bracket logic — builds and manages all 7 tournament formats
├── Match.java                # Match data model (teams, winner, score, round, bracket flags)
├── Team.java                 # Team data model (wins, losses, points scored/allowed)
├── ScoreMatrix.java          # 2D score matrix for head-to-head tracking
├── TournamentType.java       # Enum of all supported tournament formats
├── saved_brackets/           # Auto-created folder for saved .txt bracket files
└── README.md
```

---

## Requirements

- Java 17 or higher
- JavaFX 17 SDK (OpenJFX)

---

## How to Run

**1. Compile:**
```bash
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml \
  app.java TournamentBracket.java Match.java Team.java ScoreMatrix.java TournamentType.java
```

**2. Run:**
```bash
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml app
```

> Replace `/path/to/javafx-sdk/lib` with your actual OpenJFX SDK path (e.g. `C:/Users/ADMIN/Documents/openjfx-17.0.19_windows-x64_bin-sdk/lib`).

> **VS Code users:** Configure the JavaFX module path in your `.vscode/launch.json` vmArgs locally. Do not commit the `.vscode` folder as it contains machine-specific paths.

---

## Credits

Developed by **aveloso11** and team. Built with Java 17 + JavaFX.
