import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class app extends Application {

    private TournamentBracket tournament;
    private Team[] teams;
    private VBox participantsList;
    private VBox bracketView;
    private TextField bracketNameField;
    private ComboBox<String> bracketTypeCombo;
    private TextField sportField;
    private TextArea descriptionArea;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Label progressLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Intramural Sports Bracket Generator");

        teams = new Team[0];
        tournament = null;

        VBox leftPanel   = createParticipantsPanel();
        VBox centerPanel = createBracketViewPanel();
        VBox rightPanel  = createInformationPanel();
        HBox bottomPanel = createBottomProgressPanel();

        HBox contentArea = new HBox(10, leftPanel, centerPanel, rightPanel);
        contentArea.setPadding(new Insets(10));
        contentArea.setFillHeight(true);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        leftPanel.setPrefWidth(250);
        centerPanel.setPrefWidth(500);
        rightPanel.setPrefWidth(280);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(contentArea);
        mainLayout.setBottom(bottomPanel);
        mainLayout.setTop(createHeaderBar());
        mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to right, #040D43, #7F8EE3);");

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.setFill(Color.web("#040D43"));
        primaryStage.setScene(scene);
        primaryStage.show();

        bracketView.getChildren().clear();
        Label emptyLabel = new Label("No teams added yet.\nClick 'ADD' to add participants.");
        emptyLabel.setFont(Font.font("Arial", 14));
        emptyLabel.setTextFill(Color.web("#E0E6ED"));
        emptyLabel.setAlignment(Pos.CENTER);
        bracketView.getChildren().add(emptyLabel);

        updateProgress();
    }

    // =========================================================================
    // HEADER
    // =========================================================================

    private HBox createHeaderBar() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: linear-gradient(to right, #040D43, #7F8EE3);");

        Label title = new Label("🏆 INTRAMURAL BRACKET MAKER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#FFD862"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // =========================================================================
    // PARTICIPANTS PANEL
    // =========================================================================

    private void updateParticipantsList() {
        participantsList.getChildren().clear();
        if (teams.length == 0) {
            Label emptyLabel = new Label("There are no participants yet.\nClick 'ADD' to add participants.");
            emptyLabel.setStyle("-fx-text-fill: #E0E6ED; -fx-font-size: 12px;");
            participantsList.getChildren().add(emptyLabel);
            return;
        }
        for (Team team : teams) {
            CheckBox cb = new CheckBox(team.getName());
            cb.setUserData(team);
            cb.setStyle("-fx-font-size: 12px; -fx-padding: 5;");
            participantsList.getChildren().add(cb);
        }
    }

    private VBox createParticipantsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("PARTICIPANTS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));

        participantsList = new VBox(5);
        participantsList.setPadding(new Insets(5));
        updateParticipantsList();

        ScrollPane scrollPane = new ScrollPane(participantsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #152055; -fx-border-color: #7F8EE3;");

        Button addTeamBtn    = createStyledButton("ADD",     "#7F8EE3");
        Button removeTeamBtn = createStyledButton("REMOVE",  "#e74c3c");
        Button shuffleBtn    = createStyledButton("SHUFFLE", "#27ae60");

        String addNormal = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String addHover  = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String remNormal = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String remHover  = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String shuNormal = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String shuHover  = "-fx-background-color: #1e8449; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        addTeamBtn.setStyle(addNormal);
        removeTeamBtn.setStyle(remNormal);
        shuffleBtn.setStyle(shuNormal);
        addTeamBtn.setOnMouseEntered(e -> addTeamBtn.setStyle(addHover));
        addTeamBtn.setOnMouseExited(e  -> addTeamBtn.setStyle(addNormal));
        removeTeamBtn.setOnMouseEntered(e -> removeTeamBtn.setStyle(remHover));
        removeTeamBtn.setOnMouseExited(e  -> removeTeamBtn.setStyle(remNormal));
        shuffleBtn.setOnMouseEntered(e -> shuffleBtn.setStyle(shuHover));
        shuffleBtn.setOnMouseExited(e  -> shuffleBtn.setStyle(shuNormal));
        addTeamBtn.setOnAction(e    -> addTeam());
        removeTeamBtn.setOnAction(e -> removeSelectedTeams());
        shuffleBtn.setOnAction(e    -> shuffleTeams());

        HBox buttonBox = new HBox(10, addTeamBtn, removeTeamBtn);
        buttonBox.setAlignment(Pos.CENTER);
        HBox shuffleBox = new HBox(shuffleBtn);
        shuffleBox.setAlignment(Pos.CENTER);
        shuffleBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(shuffleBtn, Priority.ALWAYS);

        Label tip = new Label("Quick Tip: Add participants by checking the box above");
        tip.setFont(Font.font("Arial", 10));
        tip.setTextFill(Color.web("#E0E6ED"));
        tip.setWrapText(true);

        panel.getChildren().addAll(title, scrollPane, buttonBox, shuffleBox, tip);
        return panel;
    }

    private VBox createBracketViewPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("BRACKET VIEW");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));

        bracketView = new VBox(10);
        bracketView.setPadding(new Insets(10));
        bracketView.setStyle("-fx-background-color: #152055; -fx-border-color: #7F8EE3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        panel.getChildren().addAll(title, bracketView);
        VBox.setVgrow(bracketView, Priority.ALWAYS);
        return panel;
    }

    // =========================================================================
    // ADD / REMOVE TEAMS
    // =========================================================================

    private String resolveActualType(String selectedType, int teamCount) {
        if ("Single Elimination".equals(selectedType) && (teamCount == 10 || teamCount == 20)) {
            return "Play-In Single Elimination";
        }
        if ("Double Elimination".equals(selectedType) && !isPowerOfTwo(teamCount)) {
            return "Play-In Double Elimination";
        }
        return selectedType;
    }

    /** Returns true when the team count is valid for the selected bracket type. */
    private boolean isValidTeamCount(int n, String bracketType) {
        if (n == 0) return false;
        switch (bracketType) {
            case "Single Elimination":
                return n == 4 || n == 8 || n == 10 || n == 16 || n == 20 || n == 32;
            case "Double Elimination":
                return (isPowerOfTwo(n) || n == 6 || n == 10 || n == 12 || n == 20 || n == 24) && n >= 4 && n <= 32;
            // ── FIX: Round Robin, Swiss, Free For All only require 3+ teams ──
            default:
                return n >= 3;
        }
    }

    private void rebuildTournament() {
        String btype = bracketTypeCombo.getValue();
        if (isValidTeamCount(teams.length, btype)) {
            tournament = new TournamentBracket(teams, resolveActualType(btype, teams.length));
        } else {
            tournament = null;
        }
    }

    private void addTeam() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Team");
        dialog.setHeaderText("Enter team name:");
        dialog.setContentText("Team name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                if (teams.length >= 32) {
                    showAlert("Team Limit Reached", "Maximum of 32 teams allowed.");
                    return;
                }
                Team[] newTeams = new Team[teams.length + 1];
                System.arraycopy(teams, 0, newTeams, 0, teams.length);
                newTeams[teams.length] = new Team(teams.length, name.trim());
                teams = newTeams;

                rebuildTournament();
                updateBracketView();
                updateParticipantsList();
                updateProgress();
            }
        });
    }

    private void removeSelectedTeams() {
        List<Team> teamsToRemove = new ArrayList<>();

        for (javafx.scene.Node node : participantsList.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Team)
                    teamsToRemove.add((Team) cb.getUserData());
            }
        }

        if (teamsToRemove.isEmpty()) {
            showAlert("No Selection", "Please check the box next to the team(s) you want to remove.");
            return;
        }

        List<Team> remaining = new ArrayList<>();
        for (Team t : teams) {
            boolean remove = false;
            for (Team rt : teamsToRemove) if (t.getId() == rt.getId()) { remove = true; break; }
            if (!remove) remaining.add(t);
        }

        List<Team> reindexed = new ArrayList<>();
        for (int i = 0; i < remaining.size(); i++)
            reindexed.add(new Team(i, remaining.get(i).getName()));
        teams = reindexed.toArray(new Team[0]);

        rebuildTournament();
        updateParticipantsList();
        updateBracketView();
        updateProgress();

        String msg = teamsToRemove.size() + " team(s) removed. Remaining: " + teams.length;
        if (!isValidTeamCount(teams.length, bracketTypeCombo.getValue()) && teams.length > 0)
            msg += "\n" + getNotReadyMessage(teams.length, bracketTypeCombo.getValue());
        showAlert("Teams Removed", msg);
    }

    private void shuffleTeams() {
        if (teams.length < 2) {
            showAlert("Shuffle", "Add at least 2 teams before shuffling.");
            return;
        }
        List<String> names = new ArrayList<>();
        for (Team t : teams) names.add(t.getName());
        Collections.shuffle(names);
        teams = new Team[names.size()];
        for (int i = 0; i < names.size(); i++) teams[i] = new Team(i, names.get(i));

        rebuildTournament();
        updateParticipantsList();
        updateBracketView();
        updateProgress();
    }

    // =========================================================================
    // INFORMATION PANEL
    // =========================================================================

    private VBox createInformationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label title = new Label("BRACKET INFORMATION");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));

        Label nameLabel = new Label("Bracket Name:");
        nameLabel.setTextFill(Color.web("#FFFFFF"));
        bracketNameField = new TextField("");
        bracketNameField.setPromptText("Enter bracket name...");
        bracketNameField.setStyle("-fx-background-color: #152055; -fx-text-fill: #E0E6ED; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");

        Label typeLabel = new Label("Bracket Type:");
        typeLabel.setTextFill(Color.web("#FFFFFF"));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        bracketTypeCombo = new ComboBox<>();
        bracketTypeCombo.getItems().addAll(
            "Single Elimination",
            "Double Elimination",
            "Round Robin",
            "Swiss System",
            "Free For All"
        );
        bracketTypeCombo.setValue("Single Elimination");
        bracketTypeCombo.setStyle("-fx-background-color: #152055; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");

        bracketTypeCombo.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? null : "-fx-text-fill: #E0E6ED; -fx-background-color: #152055;");
            }
        });
        bracketTypeCombo.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? null : "-fx-text-fill: #E0E6ED;");
            }
        });

        bracketTypeCombo.setOnAction(e -> {
            rebuildTournament();
            updateBracketView();
            updateProgress();
        });

        Label sportLabel = new Label("Sport/Game:");
        sportLabel.setTextFill(Color.web("#FFFFFF"));
        sportLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        sportField = new TextField("");
        sportField.setPromptText("e.g. Basketball, Valorant...");
        sportField.setStyle("-fx-background-color: #152055; -fx-text-fill: #E0E6ED; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");

        Label descLabel = new Label("Bracket Description:");
        descLabel.setTextFill(Color.web("#FFFFFF"));
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter tournament description...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setStyle(
            "-fx-control-inner-background: #152055; -fx-background-color: transparent;" +
            "-fx-border-color: #7F8EE3; -fx-border-radius: 3;" +
            "-fx-prompt-text-fill: #e0e6edc2; -fx-text-fill: #FFFFFF;");

        Label statusTitle = new Label("BRACKET STATUS:");
        statusTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #FFD862;");
        statusLabel = new Label("PENDING");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e74c3c;");

        Separator separator = new Separator();

        String blueN = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String blueH = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String redN  = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String redH  = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        Button loadBtn       = new Button("LOAD BRACKET");
        Button saveBracketBtn = new Button("SAVE BRACKET");
        Button exitBtn       = new Button("EXIT");

        for (Button b : new Button[]{loadBtn, saveBracketBtn}) {
            b.setPrefWidth(Double.MAX_VALUE);
            b.setStyle(blueN);
            b.setOnMouseEntered(e -> b.setStyle(blueH));
            b.setOnMouseExited(e  -> b.setStyle(blueN));
        }
        exitBtn.setPrefWidth(Double.MAX_VALUE);
        exitBtn.setStyle(redN);
        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(redH));
        exitBtn.setOnMouseExited(e  -> exitBtn.setStyle(redN));

        loadBtn.setOnAction(e       -> loadBracket());
        saveBracketBtn.setOnAction(e -> saveBracket());
        exitBtn.setOnAction(e       -> System.exit(0));

        Label tip = new Label("To save your bracket, click the 'Save Bracket' button.");
        tip.setFont(Font.font("Arial", 10));
        tip.setTextFill(Color.web("#7f8c8d"));
        tip.setWrapText(true);

        panel.getChildren().addAll(
            title, nameLabel, bracketNameField,
            typeLabel, bracketTypeCombo,
            sportLabel, sportField,
            descLabel, descriptionArea,
            statusTitle, statusLabel,
            separator, loadBtn, saveBracketBtn, exitBtn, tip
        );
        return panel;
    }

    // =========================================================================
    // BOTTOM PROGRESS BAR
    // =========================================================================

    private HBox createBottomProgressPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10, 15, 10, 15));
        panel.setStyle("-fx-background-color: linear-gradient(to right, #040D43, #7F8EE3); -fx-border-width: 1 0 0 0;");
        panel.setAlignment(Pos.CENTER_LEFT);

        Label progressTitle = new Label("Progress:");
        progressTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        progressTitle.setTextFill(Color.web("#FFFFFF"));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        progressLabel = new Label("0% Complete");
        progressLabel.setTextFill(Color.web("#FFFFFF"));
        progressLabel.setFont(Font.font("Arial", 11));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        panel.getChildren().addAll(progressTitle, progressBar, progressLabel, spacer);
        return panel;
    }

    // =========================================================================
    // BRACKET VIEW ROUTER
    // =========================================================================

    private String getNotReadyMessage(int n, String type) {
        if (n == 0) return "Click 'ADD' to add participants.\n(Valid counts depend on bracket type)";
        if ("Single Elimination".equals(type)) {
            if (n < 4) return "Need at least 4 teams for Single Elimination.";
            int[] valid = {4, 8, 10, 16, 20, 32};
            for (int v : valid) {
                if (n == v) return "";
                if (v > n)  return "Add " + (v - n) + " more team(s) to reach " + v + " teams.\n(Valid: 4, 8, 10, 16, 20, 32)";
            }
            return "Single Elimination supports 4, 8, 10, 16, 20, or 32 teams.\nCurrent: " + n;
        }
        if ("Double Elimination".equals(type)) {
            if (n < 4)  return "Need at least 4 teams for Double Elimination.";
            if (isPowerOfTwo(n) || n == 6 || n == 10 || n == 12 || n == 20 || n == 24) return "";
            int[] valid = {4, 6, 8, 10, 12, 16, 20, 24, 32};
            for (int v : valid) if (v > n) return "Add " + (v - n) + " more team(s) for " + v + "-team Double Elimination.\n(Valid: 4, 6, 8, 10, 12, 16, 20, 24, 32)";
            return "Double Elimination supports 4, 6, 8, 10, 12, 16, 20, 24, or 32 teams.\nCurrent: " + n;
        }
        // Round Robin, Swiss, Free For All: only 3+ required
        if (n < 3) return "Need at least 3 teams for " + type + ".";
        return "";
    }

    private void updateBracketView() {
        bracketView.getChildren().clear();
        String currentType = bracketTypeCombo.getValue();

        if ("Double Elimination".equals(currentType)) {
            boolean valid = isPowerOfTwo(teams.length)
                    || teams.length == 6  || teams.length == 10
                    || teams.length == 12 || teams.length == 20
                    || teams.length == 24;
            if (teams.length >= 4 && !valid) {
                Label msg = new Label(getNotReadyMessage(teams.length, currentType));
                msg.setFont(Font.font("Arial", 13));
                msg.setTextFill(Color.web("#FF6B6B"));
                msg.setAlignment(Pos.CENTER);
                msg.setWrapText(true);
                bracketView.getChildren().add(msg);
                return;
            }
        }

        if (tournament == null || !isValidTeamCount(teams.length, currentType)) {
            Label msg = new Label(getNotReadyMessage(teams.length, currentType));
            msg.setFont(Font.font("Arial", 13));
            msg.setTextFill(Color.web("#FFD862"));
            msg.setAlignment(Pos.CENTER);
            msg.setWrapText(true);
            bracketView.getChildren().add(msg);
            return;
        }

        switch (currentType) {
            case "Single Elimination":
                if (teams.length == 10 || teams.length == 20) displayPlayInSEUnified();
                else                                           displaySingleElimination();
                break;
            case "Double Elimination":
                if (teams.length == 6 || teams.length == 10 || teams.length == 12
                        || teams.length == 20 || teams.length == 24) displayPlayInDE();
                else                                                   displayDoubleElimination();
                break;
            case "Round Robin":  displayRoundRobin();  break;
            case "Swiss System": displaySwissSystem();  break;
            case "Free For All": displayFreeForAll();   break;
            default:             displaySingleElimination();
        }
    }

    // =========================================================================
    // SINGLE ELIMINATION
    // =========================================================================

    private void displaySingleElimination() {
        final double CARD_W      = 180;
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;
        final double TOP_PAD     = 70;

        int n           = teams.length;
        boolean hasByes = !isPowerOfTwo(n);

        int totalRounds = tournament.getTotalRounds();
        Map<Integer, List<Match>> byRound = new HashMap<>();
        for (int r = 1; r <= totalRounds; r++) {
            List<Match> ms = tournament.getMatchesByRound(r);
            if (!ms.isEmpty()) byRound.put(r, ms);
        }

        Map<Integer, List<Match>> visibleByRound = new HashMap<>();
        if (hasByes) {
            List<Match> r1All  = byRound.getOrDefault(1, new ArrayList<>());
            List<Match> r1Real = new ArrayList<>();
            for (Match m : r1All) {
                boolean isBye = m.isCompleted() && (m.getTeam1() == null || m.getTeam2() == null);
                if (!isBye) r1Real.add(m);
            }
            if (!r1Real.isEmpty()) visibleByRound.put(1, r1Real);
            for (int r = 2; r <= totalRounds; r++) {
                List<Match> ms = byRound.get(r);
                if (ms != null && !ms.isEmpty()) visibleByRound.put(r, ms);
            }
        } else {
            visibleByRound.putAll(byRound);
        }

        List<Integer> visRoundKeys = new ArrayList<>(visibleByRound.keySet());
        java.util.Collections.sort(visRoundKeys);
        int visRounds = visRoundKeys.size();

        Map<Match, Double> matchY = new HashMap<>();
        if (!visRoundKeys.isEmpty()) {
            List<Match> anchor = visibleByRound.get(visRoundKeys.get(0));
            if (anchor != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < anchor.size(); i++)
                    matchY.put(anchor.get(i), TOP_PAD + i * slotH);
            }
        }

        for (int ri = 1; ri < visRoundKeys.size(); ri++) {
            int r = visRoundKeys.get(ri);
            List<Match> curr = visibleByRound.get(r);
            if (curr == null) continue;
            for (Match m : curr) {
                Match lc = m.getLeftChild(), rc = m.getRightChild();
                Double ly = lc != null ? matchY.get(lc) : null;
                Double ry = rc != null ? matchY.get(rc) : null;
                if (ly == null && lc != null && !matchY.containsKey(lc)) ly = ry;
                if (ry == null && rc != null && !matchY.containsKey(rc)) ry = ly;
                if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                else if (ly != null)               matchY.put(m, ly);
                else if (ry != null)               matchY.put(m, ry);
            }
        }

        double colStride = CARD_W + COL_GAP;
        double[] colX    = new double[visRounds];
        for (int i = 0; i < visRounds; i++) colX[i] = 20 + i * colStride;

        double maxY    = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double canvasW = visRounds > 0 ? colX[visRounds - 1] + CARD_W + 40 : 400;
        double canvasH = maxY + CARD_H + 60;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, canvasH);
        pane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#27ae60"));
        gc.setLineWidth(2);

        for (int ri = 1; ri < visRoundKeys.size(); ri++) {
            int r = visRoundKeys.get(ri);
            List<Match> curr = visibleByRound.get(r);
            if (curr == null) continue;
            double srcRX = colX[ri - 1] + CARD_W, dstLX = colX[ri], midX = (srcRX + dstLX) / 2.0;
            for (Match m : curr) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                Match lc = m.getLeftChild(), rc = m.getRightChild();
                if (lc != null && matchY.containsKey(lc))
                    drawBracketLine(gc, lc, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                if (rc != null && matchY.containsKey(rc))
                    drawBracketLine(gc, rc, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }
        pane.getChildren().add(canvas);

        for (int ri = 0; ri < visRoundKeys.size(); ri++) {
            int fromEnd = visRounds - 1 - ri;
            String rName = fromEnd == 0 ? "Final"
                         : fromEnd == 1 ? "Semifinals"
                         : fromEnd == 2 ? "Quarterfinals"
                         : "Round " + (ri + 1);
            List<Match> rMs = visibleByRound.get(visRoundKeys.get(ri));
            if (rMs == null) continue;
            double topY = rMs.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label lbl = styledLabel(rName, "#2c3e50", FontWeight.BOLD, 11);
            lbl.setLayoutX(colX[ri]); lbl.setLayoutY(topY - 18);
            pane.getChildren().add(lbl);
        }

        for (int ri = 0; ri < visRoundKeys.size(); ri++) {
            boolean isLast = (ri == visRounds - 1);
            List<Match> ms = visibleByRound.get(visRoundKeys.get(ri));
            if (ms == null) continue;
            for (Match m : ms) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, isLast);
                card.setLayoutX(colX[ri]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }

        addScrollPane(pane, 550);
        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    // =========================================================================
    // DOUBLE ELIMINATION (standard: 4, 8, 16, 32)
    // =========================================================================

    private void displayDoubleElimination() {
        final double CARD_W      = 180;
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;
        final double W_TOP_PAD   = 80;
        final double SECTION_GAP = 100;

        int winnersRounds = tournament.getWinnersRounds();
        Map<Integer, List<Match>> winnersByRound = new HashMap<>();
        for (int r = 1; r <= winnersRounds; r++) {
            List<Match> ms = tournament.getWinnersMatchesByRound(r);
            if (!ms.isEmpty()) winnersByRound.put(r, ms);
        }

        List<Match> losersAll = tournament.getLosersBracketMatches();
        Map<Integer, List<Match>> losersByRound = new HashMap<>();
        for (Match m : losersAll)
            losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);

        Match grandFinal = tournament.getGrandFinals();

        List<Integer> lRounds = new ArrayList<>(losersByRound.keySet());
        java.util.Collections.sort(lRounds);
        Map<Integer, Integer> lRoundToCol = new HashMap<>();
        for (int i = 0; i < lRounds.size(); i++) lRoundToCol.put(lRounds.get(i), i);

        int wCols = winnersByRound.isEmpty() ? 0 : winnersRounds;
        int lCols = lRounds.size();
        double colStride = CARD_W + COL_GAP;

        double[] wColX = new double[Math.max(wCols, 1)];
        for (int c = 0; c < wCols; c++) wColX[c] = 20 + c * colStride;
        double[] lColX = new double[Math.max(lCols, 1)];
        for (int c = 0; c < lCols; c++) lColX[c] = 20 + c * colStride;

        double gfX = 20;
        if (wCols > 0) gfX = Math.max(gfX, wColX[wCols - 1] + CARD_W + COL_GAP);
        if (lCols > 0) gfX = Math.max(gfX, lColX[lCols - 1] + CARD_W + COL_GAP);

        Map<Match, Double> matchY = new HashMap<>();

        if (!winnersByRound.isEmpty()) {
            List<Match> r1 = winnersByRound.get(1);
            if (r1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < r1.size(); i++) matchY.put(r1.get(i), W_TOP_PAD + i * slotH);
            }
            for (int r = 2; r <= winnersRounds; r++) {
                List<Match> curr = winnersByRound.get(r);
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                    else {
                        List<Match> prev = winnersByRound.get(r - 1);
                        if (prev != null) {
                            int idx = curr.indexOf(m), i1 = idx * 2, i2 = idx * 2 + 1;
                            if (i2 < prev.size())
                                matchY.put(m, (matchY.getOrDefault(prev.get(i1), W_TOP_PAD) + matchY.getOrDefault(prev.get(i2), W_TOP_PAD)) / 2.0);
                            else if (i1 < prev.size())
                                matchY.put(m, matchY.getOrDefault(prev.get(i1), W_TOP_PAD));
                        }
                    }
                }
            }
        }

        double winnersHeight = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double lOffsetY = winnersHeight + CARD_H + SECTION_GAP;

        if (!lRounds.isEmpty()) {
            List<Match> lr1 = losersByRound.get(lRounds.get(0));
            if (lr1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < lr1.size(); i++) matchY.put(lr1.get(i), lOffsetY + 60 + i * slotH);
            }
            for (int ri = 1; ri < lRounds.size(); ri++) {
                List<Match> curr = losersByRound.get(lRounds.get(ri));
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                    else {
                        List<Match> prev = losersByRound.get(lRounds.get(ri - 1));
                        if (prev != null) {
                            int idx = curr.indexOf(m), i1 = idx * 2, i2 = idx * 2 + 1;
                            if (curr.size() == prev.size() && idx < prev.size() && matchY.containsKey(prev.get(idx)))
                                matchY.put(m, matchY.get(prev.get(idx)));
                            else if (i2 < prev.size()) {
                                Double y1 = matchY.get(prev.get(i1)), y2 = matchY.get(prev.get(i2));
                                if (y1 != null && y2 != null) matchY.put(m, (y1 + y2) / 2.0);
                                else if (y1 != null) matchY.put(m, y1);
                                else if (y2 != null) matchY.put(m, y2);
                            } else if (i1 < prev.size() && matchY.containsKey(prev.get(i1)))
                                matchY.put(m, matchY.get(prev.get(i1)));
                        }
                    }
                }
            }
        }

        double lowestY = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);

        double wbTopY = W_TOP_PAD;
        double lbBotY = lowestY + CARD_H;
        double gfY    = (wbTopY + lbBotY) / 2.0 - CARD_MID;

        double maxY = lbBotY + SECTION_GAP / 2.0 + 60;
        double lastWbRightEdge = (wCols > 0) ? wColX[wCols - 1] + CARD_W : 0;
        double lastLbRightEdge = (lCols > 0) ? lColX[lCols - 1] + CARD_W : 0;
        gfX = Math.max(lastWbRightEdge, lastLbRightEdge) + COL_GAP;
        double canvasW = gfX + CARD_W + 60;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, maxY);
        pane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(canvasW, maxY);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

        gc.setStroke(Color.web("#27ae60"));
        for (int r = 2; r <= winnersRounds; r++) {
            List<Match> curr = winnersByRound.get(r);
            if (curr == null) continue;
            double srcRX = wColX[r - 2] + CARD_W, dstLX = wColX[r - 1], midX = (srcRX + dstLX) / 2.0;
            for (Match m : curr) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                drawBracketLine(gc, m.getLeftChild(),  matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                drawBracketLine(gc, m.getRightChild(), matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }
        gc.setStroke(Color.web("#e74c3c"));
        for (int ri = 1; ri < lRounds.size(); ri++) {
            List<Match> currMs = losersByRound.get(lRounds.get(ri - 1));
            List<Match> nextMs = losersByRound.get(lRounds.get(ri));
            if (currMs == null || nextMs == null) continue;
            double srcRX = lColX[lRoundToCol.get(lRounds.get(ri - 1))] + CARD_W;
            double dstLX = lColX[lRoundToCol.get(lRounds.get(ri))];
            double midX  = (srcRX + dstLX) / 2.0;
            for (Match m : nextMs) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                Match lc = m.getLeftChild(), rc = m.getRightChild();
                if (lc != null && matchY.containsKey(lc) && currMs.contains(lc))
                    drawBracketLine(gc, lc, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                if (rc != null && matchY.containsKey(rc) && currMs.contains(rc))
                    drawBracketLine(gc, rc, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }
        gc.setStroke(Color.web("#f39c12")); gc.setLineWidth(2.5);
        double gfCY = gfY + CARD_MID, mergeX = gfX - COL_GAP / 2.0;
        if (!winnersByRound.isEmpty()) {
            List<Match> lastW = winnersByRound.get(winnersRounds);
            if (lastW == null) for (int r = winnersRounds; r >= 1; r--) if (winnersByRound.containsKey(r)) { lastW = winnersByRound.get(r); break; }
            if (lastW != null && !lastW.isEmpty() && matchY.containsKey(lastW.get(0))) {
                double wx = wColX[winnersRounds - 1] + CARD_W, wy = matchY.get(lastW.get(0)) + CARD_MID;
                gc.strokeLine(wx, wy, mergeX, wy); gc.strokeLine(mergeX, wy, mergeX, gfCY); gc.strokeLine(mergeX, gfCY, gfX, gfCY);
            }
        }
        if (!lRounds.isEmpty()) {
            List<Match> lastL = losersByRound.get(lRounds.get(lRounds.size() - 1));
            if (lastL != null && !lastL.isEmpty() && matchY.containsKey(lastL.get(0))) {
                double lx = lColX[lRoundToCol.get(lRounds.get(lRounds.size() - 1))] + CARD_W;
                double ly = matchY.get(lastL.get(0)) + CARD_MID;
                gc.strokeLine(lx, ly, mergeX, ly); gc.strokeLine(mergeX, ly, mergeX, gfCY); gc.strokeLine(mergeX, gfCY, gfX, gfCY);
            }
        }
        pane.getChildren().add(canvas);

        addPaneLabel(pane, "🏆 WINNERS BRACKET", wCols > 0 ? wColX[0] : 20, W_TOP_PAD - 42, "#27ae60", FontWeight.BOLD, 13);
        addPaneLabel(pane, "💀 LOSERS BRACKET",  lCols > 0 ? lColX[0] : 20, lOffsetY + 8,   "#e74c3c", FontWeight.BOLD, 13);

        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int r = e.getKey();
            double topY = e.getValue().stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label rl = styledLabel(getWinnersRoundName(r, winnersRounds), "#2c3e50", FontWeight.BOLD, 11);
            rl.setLayoutX(wColX[r - 1]); rl.setLayoutY(topY - 18);
            pane.getChildren().add(rl);
        }
        for (int r : lRounds) {
            double topY = losersByRound.get(r).stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label rl = styledLabel("Lower R" + (lRoundToCol.get(r) + 1), "#c0392b", FontWeight.BOLD, 11);
            rl.setLayoutX(lColX[lRoundToCol.get(r)]); rl.setLayoutY(topY - 18);
            pane.getChildren().add(rl);
        }
        Label gfLabel = styledLabel("GRAND FINAL", "#e67e22", FontWeight.BOLD, 11);
        gfLabel.setLayoutX(gfX); gfLabel.setLayoutY(gfY - 18);
        pane.getChildren().add(gfLabel);

        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int r = e.getKey();
            for (Match m : e.getValue()) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, false);
                card.setLayoutX(wColX[r - 1]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }
        for (int r : lRounds) {
            for (Match m : losersByRound.get(r)) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, false, false);
                card.setLayoutX(lColX[lRoundToCol.get(r)]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }
        if (grandFinal != null) {
            VBox gfCard = createDeMatchCard(grandFinal, false, true);
            gfCard.setLayoutX(gfX); gfCard.setLayoutY(gfY);
            gfCard.setPrefWidth(CARD_W + 20); gfCard.setMaxWidth(CARD_W + 20);
            pane.getChildren().add(gfCard);
        }

        addScrollPane(pane, 600);
        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    // =========================================================================
    // PLAY-IN DOUBLE ELIMINATION
    // Only renders play-in sections (WB / LB / GF) when they actually have matches.
    // =========================================================================

    private void displayPlayInDE() {
        final double CARD_W      = 180;
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;
        final double TOP_PAD     = 80;
        final double SECTION_GAP = 120;

        // ── Gather play-in WB matches ─────────────────────────────────
        int piWbRounds = tournament.getPlayInWinnersRounds();
        Map<Integer, List<Match>> piWbByRound = new HashMap<>();
        for (int r = 1; r <= piWbRounds; r++) {
            List<Match> ms = tournament.getPlayInWinnersMatchesByRound(r);
            if (!ms.isEmpty()) piWbByRound.put(r, ms);
        }
        boolean hasPiWb = !piWbByRound.isEmpty();

        // ── Gather play-in LB matches ─────────────────────────────────
        Map<Integer, List<Match>> piLbByRound = new HashMap<>();
        for (Match m : tournament.getLosersBracketMatches())
            if (m.isPlayIn()) piLbByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        boolean hasPiLb = !piLbByRound.isEmpty();

        List<Integer> piLbRounds = new ArrayList<>(piLbByRound.keySet());
        java.util.Collections.sort(piLbRounds);
        Map<Integer, Integer> piLbRoundToCol = new HashMap<>();
        for (int i = 0; i < piLbRounds.size(); i++) piLbRoundToCol.put(piLbRounds.get(i), i);

        Match piGF = tournament.getPlayInGrandFinal();
        boolean hasPiGF = piGF != null;

        // Determine whether any play-in content exists at all
        boolean hasPlayIn = hasPiWb || hasPiLb || hasPiGF;

        int totalPiCols = Math.max(hasPiWb ? piWbRounds : 0, hasPiLb ? piLbRounds.size() : 0);
        double colStride = CARD_W + COL_GAP;
        double[] colX = new double[Math.max(totalPiCols + 1, 1)];
        for (int c = 0; c <= totalPiCols; c++) colX[c] = 20 + c * colStride;
        double piGfX = totalPiCols > 0 ? colX[totalPiCols] : 20;

        // ── Y positions: play-in WB ───────────────────────────────────
        Map<Match, Double> matchY = new HashMap<>();
        if (hasPiWb && piWbByRound.containsKey(1)) {
            List<Match> r1 = piWbByRound.get(1);
            double slotH = CARD_H + MATCH_V_GAP;
            for (int i = 0; i < r1.size(); i++) matchY.put(r1.get(i), TOP_PAD + i * slotH);
        }
        for (int r = 2; r <= piWbRounds; r++) {
            List<Match> curr = piWbByRound.get(r);
            if (curr == null) continue;
            for (Match m : curr) {
                Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                else if (ly != null)               matchY.put(m, ly);
                else if (ry != null)               matchY.put(m, ry);
            }
        }

        // ── Y positions: play-in LB ───────────────────────────────────
        double wbMaxY = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double lbOffsetY = wbMaxY + (hasPiWb ? CARD_H + SECTION_GAP : TOP_PAD);
        if (hasPiLb && !piLbRounds.isEmpty()) {
            List<Match> lr1 = piLbByRound.get(piLbRounds.get(0));
            if (lr1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < lr1.size(); i++) matchY.put(lr1.get(i), lbOffsetY + 60 + i * slotH);
            }
            for (int ri = 1; ri < piLbRounds.size(); ri++) {
                List<Match> curr = piLbByRound.get(piLbRounds.get(ri));
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                }
            }
        }

        // ── Y position: play-in GF ────────────────────────────────────
        double piLbBotY = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(TOP_PAD) + CARD_H;
        double piGfY    = (TOP_PAD + piLbBotY) / 2.0 - CARD_MID;
        if (hasPiGF) matchY.put(piGF, piGfY);

        // ── Main bracket layout ───────────────────────────────────────
        double mainStartX = hasPlayIn ? piGfX + CARD_W + COL_GAP * 2 : 20;
        double mainX0 = mainStartX;
        double mainX1 = mainX0 + CARD_W + COL_GAP;
        double mainX2 = mainX1 + CARD_W + COL_GAP;

        List<Match> mainMatches = tournament.getMainBracketMatches();
        List<Match> sfMatches   = new ArrayList<>();
        List<Match> ffMatches   = new ArrayList<>();
        Match champMatch        = null;

        for (Match m : mainMatches) {
            if      (m == tournament.getGrandFinals())                      champMatch = m;
            else if (m.getLeftChild() == null && m.getRightChild() == null) sfMatches.add(m);
            else                                                            ffMatches.add(m);
        }
        sfMatches.sort(Comparator.comparingInt(Match::getMatchId));
        ffMatches.sort(Comparator.comparingInt(Match::getMatchId));

        double sfSlotH = CARD_H + MATCH_V_GAP * 3;
        for (int i = 0; i < sfMatches.size(); i++)
            matchY.put(sfMatches.get(i), TOP_PAD + i * sfSlotH);

        for (Match ff : ffMatches) {
            Double ly = ff.getLeftChild()  != null ? matchY.get(ff.getLeftChild())  : null;
            Double ry = ff.getRightChild() != null ? matchY.get(ff.getRightChild()) : null;
            if (ly != null && ry != null) matchY.put(ff, (ly + ry) / 2.0);
        }
        if (champMatch != null) {
            Double ly = champMatch.getLeftChild()  != null ? matchY.get(champMatch.getLeftChild())  : null;
            Double ry = champMatch.getRightChild() != null ? matchY.get(champMatch.getRightChild()) : null;
            if (ly != null && ry != null) matchY.put(champMatch, (ly + ry) / 2.0);
        }

        // ── Canvas ────────────────────────────────────────────────────
        double maxY    = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double canvasW = mainX2 + CARD_W + 60;
        double canvasH = maxY + CARD_H + 60;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, canvasH);
        pane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

        // ── Play-in WB lines (only when play-in WB exists) ───────────
        if (hasPiWb) {
            gc.setStroke(Color.web("#27ae60"));
            for (int r = 2; r <= piWbRounds; r++) {
                List<Match> curr = piWbByRound.get(r);
                if (curr == null) continue;
                double srcRX = colX[r - 2] + CARD_W, dstLX = colX[r - 1], midX = (srcRX + dstLX) / 2.0;
                for (Match m : curr) {
                    if (!matchY.containsKey(m)) continue;
                    double tgtY = matchY.get(m) + CARD_MID;
                    drawBracketLine(gc, m.getLeftChild(),  matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                    drawBracketLine(gc, m.getRightChild(), matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                }
            }
        }

        // ── Play-in LB lines (only when play-in LB exists) ───────────
        if (hasPiLb) {
            gc.setStroke(Color.web("#e74c3c"));
            for (int ri = 1; ri < piLbRounds.size(); ri++) {
                List<Match> currMs = piLbByRound.get(piLbRounds.get(ri - 1));
                List<Match> nextMs = piLbByRound.get(piLbRounds.get(ri));
                if (currMs == null || nextMs == null) continue;
                double srcRX = colX[piLbRoundToCol.get(piLbRounds.get(ri - 1))] + CARD_W;
                double dstLX = colX[piLbRoundToCol.get(piLbRounds.get(ri))];
                double midX  = (srcRX + dstLX) / 2.0;
                for (Match m : nextMs) {
                    if (!matchY.containsKey(m)) continue;
                    double tgtY = matchY.get(m) + CARD_MID;
                    Match lc = m.getLeftChild(), rc = m.getRightChild();
                    if (lc != null && matchY.containsKey(lc) && currMs.contains(lc))
                        drawBracketLine(gc, lc, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                    if (rc != null && matchY.containsKey(rc) && currMs.contains(rc))
                        drawBracketLine(gc, rc, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                }
            }
        }

        // ── Play-in GF feed lines (only when play-in GF exists) ──────
        if (hasPiGF && matchY.containsKey(piGF)) {
            gc.setStroke(Color.web("#f39c12")); gc.setLineWidth(2.5);
            double gfCY = matchY.get(piGF) + CARD_MID, mergeX = piGfX - COL_GAP / 2.0;
            if (hasPiWb && piWbByRound.containsKey(piWbRounds)) {
                List<Match> wbFinals = piWbByRound.get(piWbRounds);
                if (!wbFinals.isEmpty() && matchY.containsKey(wbFinals.get(0))) {
                    double wy = matchY.get(wbFinals.get(0)) + CARD_MID, wx = colX[piWbRounds - 1] + CARD_W;
                    gc.strokeLine(wx, wy, mergeX, wy); gc.strokeLine(mergeX, wy, mergeX, gfCY); gc.strokeLine(mergeX, gfCY, piGfX, gfCY);
                }
            }
            if (hasPiLb && !piLbRounds.isEmpty()) {
                List<Match> lbFinals = piLbByRound.get(piLbRounds.get(piLbRounds.size() - 1));
                if (lbFinals != null && !lbFinals.isEmpty() && matchY.containsKey(lbFinals.get(0))) {
                    double ly = matchY.get(lbFinals.get(0)) + CARD_MID;
                    double lx = colX[piLbRoundToCol.get(piLbRounds.get(piLbRounds.size() - 1))] + CARD_W;
                    gc.strokeLine(lx, ly, mergeX, ly); gc.strokeLine(mergeX, ly, mergeX, gfCY); gc.strokeLine(mergeX, gfCY, piGfX, gfCY);
                }
            }
        }

        // ── Dashed arrows: play-in area → main bracket SFs ───────────
        if (hasPlayIn) {
            gc.setStroke(Color.web("#3498db")); gc.setLineWidth(1.5); gc.setLineDashes(8, 5);
            for (Match sf : sfMatches) {
                if (!matchY.containsKey(sf)) continue;
                double sfMidY = matchY.get(sf) + CARD_MID;
                gc.strokeLine(piGfX + CARD_W + 10, sfMidY, mainX0, sfMidY);
            }
            gc.setLineDashes(null);
        }

        // ── Main bracket SF → FF lines (purple) ──────────────────────
        gc.setStroke(Color.web("#8e44ad")); gc.setLineWidth(2.5);
        for (Match ff : ffMatches) {
            if (!matchY.containsKey(ff)) continue;
            double tgtY = matchY.get(ff) + CARD_MID, srcRX = mainX0 + CARD_W, midX = (srcRX + mainX1) / 2.0;
            drawBracketLine(gc, ff.getLeftChild(),  matchY, srcRX, midX, mainX1, tgtY, CARD_MID);
            drawBracketLine(gc, ff.getRightChild(), matchY, srcRX, midX, mainX1, tgtY, CARD_MID);
        }

        // ── FF → Championship lines (gold) ────────────────────────────
        gc.setStroke(Color.web("#f39c12")); gc.setLineWidth(3);
        if (champMatch != null && matchY.containsKey(champMatch)) {
            double tgtY = matchY.get(champMatch) + CARD_MID, srcRX = mainX1 + CARD_W, midX = (srcRX + mainX2) / 2.0;
            drawBracketLine(gc, champMatch.getLeftChild(),  matchY, srcRX, midX, mainX2, tgtY, CARD_MID);
            drawBracketLine(gc, champMatch.getRightChild(), matchY, srcRX, midX, mainX2, tgtY, CARD_MID);
        }

        pane.getChildren().add(canvas);

        // ── Section labels (only shown when the section has content) ─
        if (hasPiWb)
            addPaneLabel(pane, "🎯 PLAY-IN — WINNERS BRACKET", colX[0], TOP_PAD - 50, "#27ae60", FontWeight.BOLD, 13);
        if (hasPiLb)
            addPaneLabel(pane, "💀 PLAY-IN — LOSERS BRACKET",  colX[0], lbOffsetY + 10, "#e74c3c", FontWeight.BOLD, 13);
        if (hasPiGF)
            addPaneLabel(pane, "🏅 PLAY-IN GRAND FINAL", piGfX, piGfY - 22, "#e67e22", FontWeight.BOLD, 12);
        addPaneLabel(pane, "🏆 MAIN BRACKET", mainX0, TOP_PAD - 50, "#8e44ad", FontWeight.BOLD, 13);

        // Play-in WB round labels
        if (hasPiWb) {
            for (Map.Entry<Integer, List<Match>> e : piWbByRound.entrySet()) {
                int r = e.getKey();
                double topY = e.getValue().stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
                if (topY == Double.MAX_VALUE) continue;
                int fromEnd = piWbRounds - r;
                String rName = fromEnd == 0 ? "WB Final" : fromEnd == 1 ? "WB Semis" : "WB R" + r;
                Label rl = styledLabel(rName, "#27ae60", FontWeight.BOLD, 11);
                rl.setLayoutX(colX[r - 1]); rl.setLayoutY(topY - 18);
                pane.getChildren().add(rl);
            }
        }
        // Play-in LB round labels
        if (hasPiLb) {
            for (int r : piLbRounds) {
                int col = piLbRoundToCol.get(r);
                double topY = piLbByRound.get(r).stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
                if (topY == Double.MAX_VALUE) continue;
                Label rl = styledLabel("LB R" + (col + 1), "#e74c3c", FontWeight.BOLD, 11);
                rl.setLayoutX(colX[col]); rl.setLayoutY(topY - 18);
                pane.getChildren().add(rl);
            }
        }

        // Bye-seed badges
        Team[] byeTeams = tournament.getByeTeams();
        int[] byePairing = {0, 3, 1, 2};
        for (int i = 0; i < sfMatches.size() && byeTeams != null && i < byePairing.length && byePairing[i] < byeTeams.length; i++) {
            Label byeLbl = new Label("⭐ SEED #" + (byePairing[i] + 1) + " — " + byeTeams[byePairing[i]].getName());
            byeLbl.setStyle(
                "-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50;" +
                "-fx-font-weight: bold; -fx-font-size: 10px;" +
                "-fx-padding: 2 6; -fx-background-radius: 3;");
            double sfY = matchY.getOrDefault(sfMatches.get(i), TOP_PAD + i * sfSlotH);
            byeLbl.setLayoutX(mainX0); byeLbl.setLayoutY(sfY - 20);
            pane.getChildren().add(byeLbl);
        }

        // ── Place match cards ─────────────────────────────────────────
        // Play-in WB cards
        if (hasPiWb) {
            for (Map.Entry<Integer, List<Match>> e : piWbByRound.entrySet()) {
                int r = e.getKey();
                for (Match m : e.getValue()) {
                    if (!matchY.containsKey(m)) continue;
                    VBox card = createDeMatchCard(m, true, false);
                    card.setLayoutX(colX[r - 1]); card.setLayoutY(matchY.get(m));
                    card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                    pane.getChildren().add(card);
                }
            }
        }
        // Play-in LB cards
        if (hasPiLb) {
            for (int r : piLbRounds) {
                int col = piLbRoundToCol.get(r);
                for (Match m : piLbByRound.get(r)) {
                    if (!matchY.containsKey(m)) continue;
                    VBox card = createDeMatchCard(m, false, false);
                    card.setLayoutX(colX[col]); card.setLayoutY(matchY.get(m));
                    card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                    pane.getChildren().add(card);
                }
            }
        }
        // Play-in GF card
        if (hasPiGF && matchY.containsKey(piGF)) {
            VBox card = createDeMatchCard(piGF, false, true);
            card.setLayoutX(piGfX); card.setLayoutY(matchY.get(piGF));
            card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
            pane.getChildren().add(card);
        }
        // Main bracket SFs
        for (Match sf : sfMatches) {
            if (!matchY.containsKey(sf)) continue;
            VBox card = createMainBracketCard(sf, "Semifinal", "#8e44ad");
            card.setLayoutX(mainX0); card.setLayoutY(matchY.get(sf));
            card.setPrefWidth(CARD_W + 20); card.setMaxWidth(CARD_W + 20);
            pane.getChildren().add(card);
        }
        // Main bracket FFs
        for (Match ff : ffMatches) {
            if (!matchY.containsKey(ff)) continue;
            VBox card = createMainBracketCard(ff, "Final Four", "#8e44ad");
            card.setLayoutX(mainX1); card.setLayoutY(matchY.get(ff));
            card.setPrefWidth(CARD_W + 20); card.setMaxWidth(CARD_W + 20);
            pane.getChildren().add(card);
        }
        // Championship
        if (champMatch != null && matchY.containsKey(champMatch)) {
            VBox card = createMainBracketCard(champMatch, "CHAMPIONSHIP", "#e67e22");
            card.setLayoutX(mainX2); card.setLayoutY(matchY.get(champMatch));
            card.setPrefWidth(CARD_W + 30); card.setMaxWidth(CARD_W + 30);
            pane.getChildren().add(card);
        }

        addScrollPane(pane, 600);
        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    // =========================================================================
    // PLAY-IN SINGLE ELIMINATION
    // =========================================================================

    private void displayPlayInSE() {
        final double CARD_W      = 180;
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;
        final double TOP_PAD     = 80;

        List<Match> piMatches = tournament.getPlayInMatches();
        Map<Integer, List<Match>> piByRound = new HashMap<>();
        for (Match m : piMatches)
            piByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        List<Integer> piRoundKeys = new ArrayList<>(piByRound.keySet());
        java.util.Collections.sort(piRoundKeys);

        List<Match> mainMatches = tournament.getMainBracketMatches();
        Map<Integer, List<Match>> mainByRound = new HashMap<>();
        for (Match m : mainMatches)
            mainByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        List<Integer> mainRoundKeys = new ArrayList<>(mainByRound.keySet());
        java.util.Collections.sort(mainRoundKeys);

        double colStride = CARD_W + COL_GAP;
        int piCols   = piRoundKeys.size();
        int mainCols = mainRoundKeys.size();

        Map<Integer, Double> piColX   = new HashMap<>();
        Map<Integer, Double> mainColX = new HashMap<>();
        for (int i = 0; i < piCols;   i++) piColX.put(piRoundKeys.get(i),   20 + i * colStride);
        double mainStartX = 20 + (piCols + 1) * colStride;
        for (int i = 0; i < mainCols; i++) mainColX.put(mainRoundKeys.get(i), mainStartX + i * colStride);

        Map<Match, Double> matchY = new HashMap<>();
        if (!piRoundKeys.isEmpty()) {
            List<Match> r1 = piByRound.get(piRoundKeys.get(0));
            double slotH = CARD_H + MATCH_V_GAP;
            for (int i = 0; i < r1.size(); i++) matchY.put(r1.get(i), TOP_PAD + i * slotH);
            for (int ri = 1; ri < piRoundKeys.size(); ri++) {
                List<Match> curr = piByRound.get(piRoundKeys.get(ri));
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                    else {
                        List<Match> prev = piByRound.get(piRoundKeys.get(ri - 1));
                        if (prev != null) {
                            int idx = curr.indexOf(m), i1 = idx * 2, i2 = idx * 2 + 1;
                            if (i2 < prev.size())
                                matchY.put(m, (matchY.getOrDefault(prev.get(i1), TOP_PAD) + matchY.getOrDefault(prev.get(i2), TOP_PAD)) / 2.0);
                            else if (i1 < prev.size())
                                matchY.put(m, matchY.getOrDefault(prev.get(i1), TOP_PAD));
                        }
                    }
                }
            }
        }

        if (!mainRoundKeys.isEmpty()) {
            List<Match> mr1 = mainByRound.get(mainRoundKeys.get(0));
            double slotH = CARD_H + MATCH_V_GAP;
            for (int i = 0; i < mr1.size(); i++) matchY.put(mr1.get(i), TOP_PAD + i * slotH);
            for (int ri = 1; ri < mainRoundKeys.size(); ri++) {
                List<Match> curr = mainByRound.get(mainRoundKeys.get(ri));
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                }
            }
        }

        double maxY    = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double canvasW = (mainCols > 0
            ? mainColX.get(mainRoundKeys.get(mainCols - 1)) + CARD_W + 60
            : mainStartX + 60);
        double canvasH = maxY + CARD_H + 60;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, canvasH);
        pane.setStyle("-fx-background-color: #f8f8f8;");
        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

        gc.setStroke(Color.web("#27ae60"));
        for (int ri = 1; ri < piRoundKeys.size(); ri++) {
            List<Match> curr = piByRound.get(piRoundKeys.get(ri));
            if (curr == null) continue;
            double srcRX = piColX.get(piRoundKeys.get(ri - 1)) + CARD_W;
            double dstLX = piColX.get(piRoundKeys.get(ri));
            double midX  = (srcRX + dstLX) / 2.0;
            for (Match m : curr) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                drawBracketLine(gc, m.getLeftChild(),  matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                drawBracketLine(gc, m.getRightChild(), matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }

        gc.setStroke(Color.web("#3498db")); gc.setLineWidth(1.5); gc.setLineDashes(8, 5);
        if (!mainRoundKeys.isEmpty()) {
            List<Match> mainR1 = mainByRound.get(mainRoundKeys.get(0));
            double mainR1X = mainColX.get(mainRoundKeys.get(0));
            double piRightX = piCols > 0
                ? piColX.get(piRoundKeys.get(piCols - 1)) + CARD_W
                : mainStartX - COL_GAP;
            for (Match m : mainR1) {
                if (!matchY.containsKey(m)) continue;
                if (m.getTeam1() == null || m.getTeam2() == null) {
                    double tgtY = matchY.get(m) + CARD_MID;
                    gc.strokeLine(piRightX, tgtY, mainR1X, tgtY);
                }
            }
        }
        gc.setLineDashes(null);

        gc.setStroke(Color.web("#8e44ad")); gc.setLineWidth(2.5);
        for (int ri = 1; ri < mainRoundKeys.size(); ri++) {
            List<Match> curr = mainByRound.get(mainRoundKeys.get(ri));
            if (curr == null) continue;
            double srcRX = mainColX.get(mainRoundKeys.get(ri - 1)) + CARD_W;
            double dstLX = mainColX.get(mainRoundKeys.get(ri));
            double midX  = (srcRX + dstLX) / 2.0;
            for (Match m : curr) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                drawBracketLine(gc, m.getLeftChild(),  matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                drawBracketLine(gc, m.getRightChild(), matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }
        pane.getChildren().add(canvas);

        if (piCols > 0) {
            double piLabelX = piColX.get(piRoundKeys.get(0));
            addPaneLabel(pane, "🎯 PLAY-IN BRACKET", piLabelX, TOP_PAD - 50, "#27ae60", FontWeight.BOLD, 13);
            for (int ri = 0; ri < piRoundKeys.size(); ri++) {
                int r = piRoundKeys.get(ri);
                List<Match> rms = piByRound.get(r);
                double topY = rms.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
                if (topY == Double.MAX_VALUE) continue;
                int fromEnd = piRoundKeys.size() - 1 - ri;
                String rName = fromEnd == 0 ? "PI Final" : fromEnd == 1 ? "PI Semis" : "PI R" + r;
                Label lbl = styledLabel(rName, "#27ae60", FontWeight.BOLD, 11);
                lbl.setLayoutX(piColX.get(r)); lbl.setLayoutY(topY - 18);
                pane.getChildren().add(lbl);
            }
        }
        if (mainCols > 0) {
            addPaneLabel(pane, "🏆 MAIN BRACKET", mainColX.get(mainRoundKeys.get(0)), TOP_PAD - 50, "#8e44ad", FontWeight.BOLD, 13);
            for (int ri = 0; ri < mainRoundKeys.size(); ri++) {
                int r = mainRoundKeys.get(ri);
                List<Match> rms = mainByRound.get(r);
                double topY = rms.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
                if (topY == Double.MAX_VALUE) continue;
                int fromEnd = mainRoundKeys.size() - 1 - ri;
                String rName = fromEnd == 0 ? "Championship" : fromEnd == 1 ? "Semifinals" : fromEnd == 2 ? "Quarterfinals" : "Round " + r;
                Label lbl = styledLabel(rName, "#8e44ad", FontWeight.BOLD, 11);
                lbl.setLayoutX(mainColX.get(r)); lbl.setLayoutY(topY - 18);
                pane.getChildren().add(lbl);
            }
        }

        Team[] byeSeeds = tournament.getByeTeams();
        if (byeSeeds != null && !mainRoundKeys.isEmpty()) {
            List<Match> mr1 = mainByRound.get(mainRoundKeys.get(0));
            double mr1X = mainColX.get(mainRoundKeys.get(0));
            for (int i = 0; i < mr1.size(); i++) {
                Match m = mr1.get(i);
                if (!matchY.containsKey(m)) continue;
                Team byeSeed = (m.getTeam1() != null) ? m.getTeam1()
                             : (m.getTeam2() != null) ? m.getTeam2() : null;
                if (byeSeed == null) continue;
                int seedNum = byeSeed.getId() + 1;
                Label lbl = new Label("⭐ SEED #" + seedNum + " — " + byeSeed.getName());
                lbl.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50;" +
                             "-fx-font-weight: bold; -fx-font-size: 10px;" +
                             "-fx-padding: 2 6; -fx-background-radius: 3;");
                lbl.setLayoutX(mr1X); lbl.setLayoutY(matchY.get(m) - 18);
                pane.getChildren().add(lbl);
            }
        }

        for (int r : piRoundKeys) {
            for (Match m : piByRound.get(r)) {
                if (!matchY.containsKey(m)) continue;
                boolean isLast = r == piRoundKeys.get(piRoundKeys.size() - 1);
                VBox card = createDeMatchCard(m, true, isLast);
                card.setLayoutX(piColX.get(r)); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }
        for (int r : mainRoundKeys) {
            for (Match m : mainByRound.get(r)) {
                if (!matchY.containsKey(m)) continue;
                boolean isChamp = r == mainRoundKeys.get(mainRoundKeys.size() - 1);
                VBox card = createMainBracketCard(m, isChamp ? "CHAMPIONSHIP" : null, isChamp ? "#e67e22" : "#8e44ad");
                card.setLayoutX(mainColX.get(r)); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W + (isChamp ? 20 : 0)); card.setMaxWidth(CARD_W + (isChamp ? 20 : 0));
                pane.getChildren().add(card);
            }
        }

        addScrollPane(pane, 600);
        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    // =========================================================================
    // PLAY-IN SE UNIFIED DISPLAY (10 and 20 teams)
    // =========================================================================

    private void displayPlayInSEUnified() {
        final double CARD_W      = 180;
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;
        final double TOP_PAD     = 70;

        List<Match> piMatches   = tournament.getPlayInMatches();
        List<Match> mainMatches = tournament.getMainBracketMatches();

        Map<Integer, List<Match>> piByRound   = new HashMap<>();
        Map<Integer, List<Match>> mainByRound = new HashMap<>();
        for (Match m : piMatches)   piByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        for (Match m : mainMatches) mainByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);

        List<Integer> piRoundKeys   = new ArrayList<>(piByRound.keySet());
        List<Integer> mainRoundKeys = new ArrayList<>(mainByRound.keySet());
        java.util.Collections.sort(piRoundKeys);
        java.util.Collections.sort(mainRoundKeys);

        List<Integer> allPiKeys   = piRoundKeys;
        List<Integer> allMainKeys = mainRoundKeys;
        int piCols   = allPiKeys.size();
        int mainCols = allMainKeys.size();
        int totalCols = piCols + mainCols;

        double colStride = CARD_W + COL_GAP;
        double[] colX = new double[totalCols];
        for (int i = 0; i < totalCols; i++) colX[i] = 20 + i * colStride;

        Map<Match, Double> matchY = new HashMap<>();
        if (!allPiKeys.isEmpty()) {
            List<Match> r1 = piByRound.get(allPiKeys.get(0));
            double slotH = CARD_H + MATCH_V_GAP;
            for (int i = 0; i < r1.size(); i++) matchY.put(r1.get(i), TOP_PAD + i * slotH);
            for (int ri = 1; ri < allPiKeys.size(); ri++) {
                List<Match> curr = piByRound.get(allPiKeys.get(ri));
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                }
            }
        }

        if (!allMainKeys.isEmpty()) {
            List<Match> mr1 = mainByRound.get(allMainKeys.get(0));
            List<Match> piLastRound = !allPiKeys.isEmpty()
                ? piByRound.get(allPiKeys.get(allPiKeys.size() - 1)) : new ArrayList<>();

            for (int i = 0; i < mr1.size(); i++) {
                Match mainM = mr1.get(i);
                Double feedY = null;
                for (Match piFinal : piLastRound) {
                    if (piFinal.getMainBracketSlot() == mainM) {
                        feedY = matchY.get(piFinal);
                        break;
                    }
                }
                if (feedY != null) {
                    matchY.put(mainM, feedY);
                } else {
                    double slotH = CARD_H + MATCH_V_GAP;
                    matchY.put(mainM, TOP_PAD + i * slotH);
                }
            }

            for (int ri = 1; ri < allMainKeys.size(); ri++) {
                List<Match> curr = mainByRound.get(allMainKeys.get(ri));
                if (curr == null) continue;
                for (Match m : curr) {
                    Double ly = m.getLeftChild()  != null ? matchY.get(m.getLeftChild())  : null;
                    Double ry = m.getRightChild() != null ? matchY.get(m.getRightChild()) : null;
                    if      (ly != null && ry != null) matchY.put(m, (ly + ry) / 2.0);
                    else if (ly != null)               matchY.put(m, ly);
                    else if (ry != null)               matchY.put(m, ry);
                }
            }
        }

        double maxY    = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double canvasW = totalCols > 0 ? colX[totalCols - 1] + CARD_W + 40 : 400;
        double canvasH = maxY + CARD_H + 80;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, canvasH);
        pane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#27ae60"));
        gc.setLineWidth(2);

        for (int ri = 1; ri < allPiKeys.size(); ri++) {
            List<Match> curr = piByRound.get(allPiKeys.get(ri));
            if (curr == null) continue;
            double srcRX = colX[ri - 1] + CARD_W, dstLX = colX[ri], midX = (srcRX + dstLX) / 2.0;
            for (Match m : curr) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                if (m.getLeftChild()  != null && matchY.containsKey(m.getLeftChild()))
                    drawBracketLine(gc, m.getLeftChild(),  matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                if (m.getRightChild() != null && matchY.containsKey(m.getRightChild()))
                    drawBracketLine(gc, m.getRightChild(), matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }

        if (piCols > 0 && mainCols > 0) {
            List<Match> piLast  = piByRound.get(allPiKeys.get(piCols - 1));
            List<Match> mainR1  = mainByRound.get(allMainKeys.get(0));
            double srcRX = colX[piCols - 1] + CARD_W;
            double dstLX = colX[piCols];
            double midX  = (srcRX + dstLX) / 2.0;
            for (Match mainM : mainR1) {
                if (!matchY.containsKey(mainM)) continue;
                double tgtY = matchY.get(mainM) + CARD_MID;
                for (Match piFinal : piLast) {
                    if (piFinal.getMainBracketSlot() == mainM && matchY.containsKey(piFinal)) {
                        drawBracketLine(gc, piFinal, matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                        break;
                    }
                }
            }
        }

        for (int ri = 1; ri < allMainKeys.size(); ri++) {
            List<Match> curr = mainByRound.get(allMainKeys.get(ri));
            if (curr == null) continue;
            double srcRX = colX[piCols + ri - 1] + CARD_W, dstLX = colX[piCols + ri], midX = (srcRX + dstLX) / 2.0;
            for (Match m : curr) {
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                if (m.getLeftChild()  != null && matchY.containsKey(m.getLeftChild()))
                    drawBracketLine(gc, m.getLeftChild(),  matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
                if (m.getRightChild() != null && matchY.containsKey(m.getRightChild()))
                    drawBracketLine(gc, m.getRightChild(), matchY, srcRX, midX, dstLX, tgtY, CARD_MID);
            }
        }

        pane.getChildren().add(canvas);

        int totalVisibleCols = piCols + mainCols;
        for (int ci = 0; ci < piCols; ci++) {
            int fromEnd = totalVisibleCols - 1 - ci;
            String rName = fromEnd == 0 ? "Final"
                         : fromEnd == 1 ? "Semifinals"
                         : fromEnd == 2 ? "Quarterfinals"
                         : "Round " + (ci + 1);
            List<Match> rMs = piByRound.get(allPiKeys.get(ci));
            if (rMs == null) continue;
            double topY = rMs.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label lbl = styledLabel(rName, "#2c3e50", FontWeight.BOLD, 11);
            lbl.setLayoutX(colX[ci]); lbl.setLayoutY(topY - 18);
            pane.getChildren().add(lbl);
        }
        for (int ci = 0; ci < mainCols; ci++) {
            int fromEnd = totalVisibleCols - 1 - (piCols + ci);
            String rName = fromEnd == 0 ? "Final"
                         : fromEnd == 1 ? "Semifinals"
                         : fromEnd == 2 ? "Quarterfinals"
                         : "Round " + (piCols + ci + 1);
            List<Match> rMs = mainByRound.get(allMainKeys.get(ci));
            if (rMs == null) continue;
            double topY = rMs.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label lbl = styledLabel(rName, "#2c3e50", FontWeight.BOLD, 11);
            lbl.setLayoutX(colX[piCols + ci]); lbl.setLayoutY(topY - 18);
            pane.getChildren().add(lbl);
        }

        for (int ri = 0; ri < allPiKeys.size(); ri++) {
            for (Match m : piByRound.get(allPiKeys.get(ri))) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, false);
                card.setLayoutX(colX[ri]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W);  card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }

        for (int ri = 0; ri < allMainKeys.size(); ri++) {
            boolean isChamp = (ri == allMainKeys.size() - 1);
            for (Match m : mainByRound.get(allMainKeys.get(ri))) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, isChamp);
                card.setLayoutX(colX[piCols + ri]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W + (isChamp ? 20 : 0));
                card.setMaxWidth(CARD_W  + (isChamp ? 20 : 0));
                pane.getChildren().add(card);
            }
        }

        addScrollPane(pane, 550);
        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    // =========================================================================
    // MATCH CARD BUILDERS
    // =========================================================================

    private VBox createDeMatchCard(Match match, boolean isWinners, boolean isGrandFinal) {
        String border  = isGrandFinal ? "#f39c12" : (isWinners ? "#27ae60" : "#e74c3c");
        String bg      = isGrandFinal ? "#fff8e1" : "white";
        String hoverBg = isGrandFinal ? "#fff0cc" : (isWinners ? "#f0fff4" : "#fff0f0");

        String t1Name = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String t2Name = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        boolean done  = match.isCompleted();
        Team winner   = match.getWinner();
        String score  = match.getScore() != null ? match.getScore() : "";

        boolean canReport = !done && match.getTeam1() != null && match.getTeam2() != null
                            && !t1Name.equals("TBD") && !t2Name.equals("TBD");

        String normalStyle = "-fx-background-color:" + bg + ";-fx-border-color:" + border + ";-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;" + (canReport ? "-fx-cursor:hand;" : "");
        String hoverStyle  = "-fx-background-color:" + hoverBg + ";-fx-border-color:" + border + ";-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";

        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 8, 4, 8));
        card.setStyle(normalStyle);
        if (canReport) {
            card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
            card.setOnMouseExited(e  -> card.setStyle(normalStyle));
            card.setOnMouseClicked(e -> showScoreDialog(match));
        }

        card.getChildren().addAll(
            makeTeamRow(t1Name, score, 0, done, winner),
            makeSeparator(),
            makeTeamRow(t2Name, score, 1, done, winner)
        );

        if (isGrandFinal && done && winner != null) {
            Label champ = new Label("🏆 " + winner.getName());
            champ.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            champ.setTextFill(Color.web("#e67e22"));
            card.getChildren().add(champ);
        }
        return card;
    }

    private VBox createMainBracketCard(Match match, String roundLabel, String accentColor) {
        boolean done  = match.isCompleted();
        Team winner   = match.getWinner();
        String t1Name = match.getTeam1() != null ? match.getTeam1().getName() : "— BYE SEED —";
        String t2Name = match.getTeam2() != null ? match.getTeam2().getName() : "Waiting for play-in…";
        String score  = match.getScore() != null ? match.getScore() : "";
        boolean canReport = !done && match.getTeam1() != null && match.getTeam2() != null;

        String normalStyle = "-fx-background-color:#fff8e1;-fx-border-color:" + accentColor + ";-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;" + (canReport ? "-fx-cursor:hand;" : "");
        String hoverStyle  = "-fx-background-color:#fff0cc;-fx-border-color:" + accentColor + ";-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";

        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 8, 4, 8));
        card.setStyle(normalStyle);
        if (canReport) {
            card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
            card.setOnMouseExited(e  -> card.setStyle(normalStyle));
            card.setOnMouseClicked(e -> showScoreDialog(match));
        }

        HBox row1 = new HBox(4);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label seedBadge = new Label();
        if (match.getTeam1() != null && tournament.getByeTeams() != null) {
            Team[] bt = tournament.getByeTeams();
            for (int i = 0; i < bt.length; i++) {
                if (bt[i] == match.getTeam1()) {
                    seedBadge.setText("#" + (i + 1));
                    seedBadge.setStyle("-fx-background-color:#f1c40f;-fx-text-fill:#2c3e50;-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:1 4;-fx-background-radius:3;");
                    break;
                }
            }
        }
        Label lbl1 = new Label(t1Name);
        lbl1.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl1.setMaxWidth(125);
        lbl1.setTextFill(done && winner != null && !winner.getName().equals(t1Name) ? Color.GRAY : Color.web("#2c3e50"));
        Label sc1 = new Label(done && !score.isEmpty() ? score.split("-")[0].trim() : "");
        sc1.setFont(Font.font("Arial", 11));
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        row1.getChildren().addAll(seedBadge, lbl1, sp1, sc1);

        HBox row2 = new HBox(4);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label(t2Name);
        lbl2.setFont(Font.font("Arial", match.getTeam2() == null ? FontWeight.NORMAL : FontWeight.BOLD, 11));
        lbl2.setMaxWidth(140);
        lbl2.setTextFill(match.getTeam2() == null ? Color.web("#aaaaaa")
            : (done && winner != null && !winner.getName().equals(t2Name)) ? Color.GRAY : Color.web("#2c3e50"));
        Label sc2 = new Label(done && !score.isEmpty() && score.split("-").length > 1 ? score.split("-")[1].trim() : "");
        sc2.setFont(Font.font("Arial", 11));
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        row2.getChildren().addAll(lbl2, sp2, sc2);

        Label roundLbl = new Label(roundLabel);
        roundLbl.setStyle("-fx-font-size:9px;-fx-text-fill:" + accentColor + ";-fx-font-weight:bold;");

        card.getChildren().addAll(row1, makeSeparator(), row2, roundLbl);

        if (done && winner != null) {
            Label wLbl = new Label("🏆 " + winner.getName());
            wLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            wLbl.setTextFill(Color.web("#e67e22"));
            card.getChildren().add(wLbl);
        }
        return card;
    }

    private HBox makeTeamRow(String teamName, String score, int teamIndex, boolean done, Team winner) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(teamName);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setMaxWidth(120);
        lbl.setWrapText(false);
        Label sc = new Label();
        sc.setFont(Font.font("Arial", 11));
        if (done && !score.isEmpty()) {
            String[] p = score.split("-");
            if (teamIndex < p.length) sc.setText(p[teamIndex].trim());
            Color c = (winner != null && winner.getName().equals(teamName)) ? Color.web("#27ae60") : Color.GRAY;
            lbl.setTextFill(c); sc.setTextFill(c);
        }
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(lbl, spacer, sc);
        return row;
    }

    private Separator makeSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #ddd;");
        return sep;
    }

    private String getWinnersRoundName(int round, int totalWinnersRounds) {
        int fromEnd = totalWinnersRounds - round;
        if (fromEnd == 0) return "Upper Final";
        if (fromEnd == 1) return "Upper Semis";
        if (fromEnd == 2) return "Upper QF";
        return "Upper R" + round;
    }

    // =========================================================================
    // ROUND ROBIN / SWISS / FREE FOR ALL
    // =========================================================================

    private void displayRoundRobin() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        Label title = new Label("ROUND ROBIN - Standings & Results");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#2c3e50"));
        container.getChildren().add(title);

        GridPane grid = makeStandingsGrid(new String[]{"Rank","Team","Wins","Losses","PD","Win %"}, "#3498db");
        List<Team> sorted = new ArrayList<>(Arrays.asList(teams));
        sorted.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        for (int i = 0; i < sorted.size(); i++) {
            Team t = sorted.get(i);
            grid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            grid.add(new Label(t.getName()), 1, i + 1);
            grid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            grid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            grid.add(new Label(String.valueOf(t.getPointDifference())), 4, i + 1);
            grid.add(new Label(String.format("%.1f%%", t.getWinPercentage())), 5, i + 1);
        }
        container.getChildren().add(grid);
        Label matchesLabel = new Label("ALL MATCHES");
        matchesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        container.getChildren().add(matchesLabel);
        VBox matchesList = new VBox(5);
        for (Match m : tournament.getAllMatches()) matchesList.getChildren().add(createMatchResultRow(m));
        container.getChildren().add(matchesList);
        ScrollPane sp = new ScrollPane(container); sp.setFitToWidth(true);
        bracketView.getChildren().add(sp);
    }

    private void displaySwissSystem() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        Label title = new Label("SWISS SYSTEM TOURNAMENT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        container.getChildren().add(title);

        GridPane grid = makeStandingsGrid(new String[]{"Rank","Team","Wins","Losses","Points","Opp Score"}, "#9b59b6");
        List<Team> sorted = new ArrayList<>(Arrays.asList(teams));
        sorted.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        for (int i = 0; i < sorted.size(); i++) {
            Team t = sorted.get(i);
            grid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            grid.add(new Label(t.getName()), 1, i + 1);
            grid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            grid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            grid.add(new Label(String.valueOf(t.getPointsScored())), 4, i + 1);
            grid.add(new Label(String.valueOf(t.getPointsAllowed())), 5, i + 1);
        }
        container.getChildren().add(grid);

        Label pLabel = new Label("CURRENT ROUND PAIRINGS");
        pLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        container.getChildren().add(pLabel);
        List<Match> pending = tournament.getPendingMatches();
        if (pending.isEmpty()) {
            Label done = new Label("All rounds complete!");
            done.setTextFill(Color.GRAY);
            container.getChildren().add(done);
        } else {
            for (Match m : pending) {
                HBox row = createSimpleMatchRow(m);
                row.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 1;");
                Button btn = new Button("Report Score");
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btn.setOnAction(e -> showScoreDialog(m));
                row.getChildren().add(btn);
                container.getChildren().add(row);
            }
        }
        ScrollPane sp = new ScrollPane(container); sp.setFitToWidth(true);
        bracketView.getChildren().add(sp);
    }

    private void displayFreeForAll() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        Label title = new Label("FREE FOR ALL - Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        container.getChildren().add(title);

        GridPane grid = makeStandingsGrid(new String[]{"Rank","Team","Wins","Losses","Pts Scored","Pts Allowed"}, "#2c3e50");
        List<Team> sorted = new ArrayList<>(Arrays.asList(teams));
        sorted.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        for (int i = 0; i < sorted.size(); i++) {
            Team t = sorted.get(i);
            grid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            grid.add(new Label(t.getName()), 1, i + 1);
            grid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            grid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            grid.add(new Label(String.valueOf(t.getPointsScored())), 4, i + 1);
            grid.add(new Label(String.valueOf(t.getPointsAllowed())), 5, i + 1);
        }
        container.getChildren().add(grid);
        ScrollPane sp = new ScrollPane(container); sp.setFitToWidth(true);
        bracketView.getChildren().add(sp);
    }

    private GridPane makeStandingsGrid(String[] headers, String headerColor) {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(5);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            h.setStyle("-fx-background-color:" + headerColor + ";-fx-text-fill:white;-fx-padding:5;");
            grid.add(h, i, 0);
        }
        return grid;
    }

    // =========================================================================
    // MATCH ROW HELPERS
    // =========================================================================

    private HBox createMatchResultRow(Match match) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5; -fx-border-color: #eee; -fx-border-width: 1;");
        String t1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String t2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        Label matchLabel = new Label(t1 + " vs " + t2);
        matchLabel.setPrefWidth(200);
        Label resultLabel = new Label();
        if (match.isCompleted() && match.getWinner() != null) {
            resultLabel.setText("WINNER: " + match.getWinner().getName() + " (" + match.getScore() + ")");
            resultLabel.setTextFill(Color.GREEN);
        } else {
            resultLabel.setText("PENDING");
            resultLabel.setTextFill(Color.RED);
        }
        Button reportBtn = new Button(match.isCompleted() ? "Done" : "Report");
        reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        reportBtn.setDisable(match.isCompleted());
        reportBtn.setOnAction(e -> showScoreDialog(match));
        row.getChildren().addAll(matchLabel, resultLabel, reportBtn);
        return row;
    }

    private HBox createSimpleMatchRow(Match match) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 3;");
        String t1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String t2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        row.getChildren().add(new Label(t1 + " vs " + t2));
        if (match.isCompleted() && match.getWinner() != null) {
            Label w = new Label("→ " + match.getWinner().getName());
            w.setTextFill(Color.GREEN);
            row.getChildren().add(w);
        }
        return row;
    }

    private void addChampionDisplay(Team champion) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f1c40f; -fx-border-radius: 8; -fx-padding: 15;");
        Label lbl = new Label("🏆 CHAMPION: " + champion.getName() + " 🏆");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web("#2c3e50"));
        box.getChildren().add(lbl);
        bracketView.getChildren().add(box);
    }

    // =========================================================================
    // SCORE DIALOG
    // =========================================================================

    private void showScoreDialog(Match match) {
        if (match.isCompleted()) { showAlert("Already Completed", "This match has already been reported."); return; }
        if (match.getTeam1() == null || match.getTeam2() == null) { showAlert("Not Ready", "Both teams are not assigned yet."); return; }

        Stage dlg = new Stage();
        dlg.setTitle("Report Match Result");
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: white;");

        Label titleLbl = new Label(match.getTeam1().getName() + " vs " + match.getTeam2().getName());
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web("#2c3e50"));

        TextField s1 = new TextField(); s1.setPromptText("Score"); s1.setPrefWidth(80);
        TextField s2 = new TextField(); s2.setPromptText("Score"); s2.setPrefWidth(80);
        ComboBox<Team> winnerCombo = new ComboBox<>();
        winnerCombo.getItems().addAll(match.getTeam1(), match.getTeam2());
        winnerCombo.setPromptText("Select winner"); winnerCombo.setPrefWidth(150);

        HBox r1b = new HBox(10, new Label(match.getTeam1().getName() + ":"), s1); r1b.setAlignment(Pos.CENTER);
        HBox r2b = new HBox(10, new Label(match.getTeam2().getName() + ":"), s2); r2b.setAlignment(Pos.CENTER);
        HBox wb  = new HBox(10, new Label("Winner:"), winnerCombo); wb.setAlignment(Pos.CENTER);

        Button submit = new Button("Submit");
        submit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        HBox btnBox = new HBox(20, submit, cancel); btnBox.setAlignment(Pos.CENTER);

        vbox.getChildren().addAll(titleLbl, r1b, r2b, wb, btnBox);
        dlg.setScene(new Scene(vbox, 350, 300));

        submit.setOnAction(e -> {
            try {
                if (s1.getText().isEmpty() || s2.getText().isEmpty()) { showAlert("Error", "Please enter both scores!"); return; }
                int score1 = Integer.parseInt(s1.getText()), score2 = Integer.parseInt(s2.getText());
                Team w = winnerCombo.getValue();
                if (w == null) { showAlert("Error", "Please select the winner!"); return; }
                tournament.recordWinner(match, w, score1, score2);
                updateBracketView(); updateProgress();
                showAlert("Success", "Match recorded!\n" + w.getName() + " wins " + score1 + "-" + score2);
                dlg.close();
            } catch (NumberFormatException ex) { showAlert("Error", "Please enter valid numbers for scores!"); }
        });
        cancel.setOnAction(e -> dlg.close());
        dlg.showAndWait();
    }

    // =========================================================================
    // SAVE / LOAD
    // =========================================================================

    private void saveBracket() {
        String name = bracketNameField.getText();
        File saveDir = new File("saved_brackets");
        if (!saveDir.exists()) saveDir.mkdir();
        File saveFile = new File(saveDir, name.replaceAll("\\s+", "_") + ".txt");
        try (PrintWriter writer = new PrintWriter(saveFile)) {
            writer.println("BRACKET NAME: " + name);
            writer.println("BRACKET TYPE: " + bracketTypeCombo.getValue());
            writer.println("SPORT/GAME: " + sportField.getText());
            writer.println("DESCRIPTION: " + descriptionArea.getText());
            writer.println("STATUS: " + statusLabel.getText());
            writer.println("TEAMS: " + teams.length);
            writer.println("----------------------------------------");
            for (Team t : teams)
                writer.println("Team: " + t.getName() + " | Wins: " + t.getWins() + " | Losses: " + t.getLosses());
            writer.println("----------------------------------------");
            writer.println("MATCH RESULTS:");
            if (tournament != null) for (Match m : tournament.getAllMatches()) {
                if (m.isCompleted()) {
                    String t1 = m.getTeam1() != null ? m.getTeam1().getName() : "TBD";
                    String t2 = m.getTeam2() != null ? m.getTeam2().getName() : "TBD";
                    writer.println(t1 + " vs " + t2 + " -> Winner: " + m.getWinner().getName() + " (" + m.getScore() + ")");
                }
            }
            showAlert("Saved", "Bracket '" + name + "' saved to:\n" + saveFile.getAbsolutePath());
        } catch (FileNotFoundException e) { showAlert("Save Error", "Could not save: " + e.getMessage()); }
    }

    private void loadBracket() {
        File saveDir = new File("saved_brackets");
        if (!saveDir.exists() || saveDir.listFiles() == null || saveDir.listFiles().length == 0) {
            showAlert("No Brackets", "No saved brackets found."); return;
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Load Bracket");
        fc.setInitialDirectory(saveDir);
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selected = fc.showOpenDialog(null);
        if (selected != null) {
            try (java.util.Scanner sc = new java.util.Scanner(selected)) {
                String bracketName = "", bracketType = "Single Elimination", sport = "";
                StringBuilder desc = new StringBuilder();
                List<String> teamNames = new ArrayList<>();
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if      (line.startsWith("BRACKET NAME: ")) bracketName = line.substring(14);
                    else if (line.startsWith("BRACKET TYPE: ")) bracketType = line.substring(14);
                    else if (line.startsWith("SPORT/GAME: "))   sport = line.substring(12);
                    else if (line.startsWith("DESCRIPTION: "))  desc.append(line.substring(13));
                    else if (line.startsWith("Team: ")) {
                        String tName = line.substring(6);
                        int idx = tName.indexOf(" |");
                        if (idx > 0) tName = tName.substring(0, idx);
                        teamNames.add(tName);
                    }
                }
                teams = new Team[teamNames.size()];
                for (int i = 0; i < teamNames.size(); i++) teams[i] = new Team(i, teamNames.get(i));
                bracketNameField.setText(bracketName);
                bracketTypeCombo.setValue(bracketType);
                sportField.setText(sport);
                descriptionArea.setText(desc.toString());
                rebuildTournament();
                updateBracketView();
                updateParticipantsList();
                updateProgress();
                showAlert("Loaded", "Successfully loaded: " + selected.getName());
            } catch (FileNotFoundException e) { showAlert("Load Error", "Could not load file: " + e.getMessage()); }
        }
    }

    // =========================================================================
    // PROGRESS & UTILITIES
    // =========================================================================

    private void updateProgress() {
        if (teams.length < 2 || tournament == null || tournament.getAllMatches().isEmpty()) {
            progressBar.setProgress(0); progressLabel.setText("0% Complete");
            statusLabel.setText("PENDING");
            statusLabel.setStyle("-fx-font-weight:bold;-fx-font-size:16px;-fx-text-fill:#e74c3c;");
            return;
        }
        int total = tournament.getAllMatches().size(), done = 0;
        for (Match m : tournament.getAllMatches()) if (m.isCompleted()) done++;
        double progress = (double) done / total;
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%d%% Complete", (int)(progress * 100)));
        if (tournament.getTournamentWinner() != null) {
            statusLabel.setText("COMPLETE");
            statusLabel.setStyle("-fx-font-weight:bold;-fx-font-size:16px;-fx-text-fill:#27ae60;");
        } else {
            statusLabel.setText("PENDING");
            statusLabel.setStyle("-fx-font-weight:bold;-fx-font-size:16px;-fx-text-fill:#e74c3c;");
        }
    }

    private void drawBracketLine(GraphicsContext gc, Match child, Map<Match, Double> matchY,
            double srcRX, double midX, double dstLX, double targetY, double cardMid) {
        if (child == null || !matchY.containsKey(child)) return;
        double y = matchY.get(child) + cardMid;
        gc.strokeLine(srcRX, y, midX, y);
        gc.strokeLine(midX, y, midX, targetY);
        gc.strokeLine(midX, targetY, dstLX, targetY);
    }

    private void addScrollPane(Pane content, double prefHeight) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(false); sp.setFitToHeight(false);
        sp.setPrefHeight(prefHeight);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background: #f8f8f8; -fx-border-color: #ddd;");
        bracketView.getChildren().clear();
        bracketView.getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
    }

    private void addPaneLabel(Pane pane, String text, double x, double y, String color, FontWeight weight, int size) {
        Label lbl = styledLabel(text, color, weight, size);
        lbl.setLayoutX(x); lbl.setLayoutY(y);
        pane.getChildren().add(lbl);
    }

    private Label styledLabel(String text, String color, FontWeight weight, int size) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", weight, size));
        lbl.setTextFill(Color.web(color));
        return lbl;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        String normal = "-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:8 15;-fx-background-radius:5;";
        String hover  = "-fx-background-color:" + adjustColor(color) + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:8 15;-fx-background-radius:5;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        return btn;
    }

    private String adjustColor(String hex) {
        if (hex.equals("#7F8EE3")) return "#5a6abf";
        if (hex.equals("#e74c3c")) return "#c0392b";
        return hex;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isPowerOfTwo(int n) { return n > 0 && (n & (n - 1)) == 0; }

    public static void main(String[] args) { launch(args); }
}