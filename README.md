# 🏆 Intramural Bracket Generator

A JavaFX desktop app for generating and managing intramural sports tournament brackets.

---

## Features

- 7 tournament formats: Single Elimination, Double Elimination, Round Robin, Swiss, Free For All, Play-In SE, Play-In DE
- Auto-handles byes, seeding, and bracket propagation
- Click any match to enter scores — winners advance automatically
- Full undo stack for reverting results
- Drag-and-drop participant reordering
- Save and load bracket state to file
- Live progress bar and completion status

---

## Setup

**Requirements:** Java 11+, JavaFX SDK

**Compile:**
```bash
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml *.java
```

**Run:**
```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml app
```

---

## How to Use

1. Add team names in the left panel and drag to reorder seeds
2. Select teams to include, then choose a tournament format
3. Click **Generate Bracket**
4. Click a match card to enter scores — the bracket updates automatically
5. Use **Undo** to revert any result
6. Save your bracket at any time and reload it later

---

## Project Structure

| File | Role |
|---|---|
| `app.java` | UI, event handling, bracket rendering |
| `TournamentBracket.java` | Bracket logic and winner propagation |
| `Match.java` | Match node: teams, score, round, flags |
| `Team.java` | Team stats: wins, losses, points |
| `TournamentType.java` | Enum of all 7 tournament formats |
