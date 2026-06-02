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
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    // Drag-and-drop state for participant reordering
    private int dragSourceIndex = -1;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Intramural Sports Bracket Generator");

        teams = new Team[0];
        tournament = null;

        VBox leftPanel   = createParticipantsPanel();
        VBox centerPanel = createBracketViewPanel();
        VBox rightPanel  = createInformationPanel();
        HBox bottomPanel = createBottomProgressPanel();

        HBox contentArea = new HBox(15, leftPanel, centerPanel, rightPanel);
        contentArea.setPadding(new Insets(15));
        contentArea.setFillHeight(true);
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        leftPanel.setPrefWidth(260);
        centerPanel.setPrefWidth(500);
        rightPanel.setPrefWidth(260);
        leftPanel.setMaxWidth(260);
        rightPanel.setMaxWidth(260);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(contentArea);
        mainLayout.setBottom(bottomPanel);
        mainLayout.setTop(createHeaderBar());
        mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to right, #040D43 0%, #040D43 50%, #FFBA09 100%);");

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.setFill(Color.web("#040D43"));
        primaryStage.setScene(scene);
        primaryStage.show();

        updateBracketView();

        updateProgress();
    }

    // =========================================================================
    // HEADER
    // =========================================================================

    private HBox createHeaderBar() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: linear-gradient(to right, #040D43 0%, #040D43 50%, #FFBA09 100%); -fx-border-width: 1 0 0 0;");

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
        for (int idx = 0; idx < teams.length; idx++) {
            final int i = idx;
            Team team = teams[i];

            Label handle = new Label("\u283f");
            handle.setStyle("-fx-text-fill: #7F8EE3; -fx-font-size: 14px; -fx-padding: 0 6 0 2; -fx-cursor: open-hand;");
            Tooltip handleTip = new Tooltip("Drag to reorder");
            Tooltip.install(handle, handleTip);

            CheckBox cb = new CheckBox(team.getName());
            cb.setUserData(team);
            cb.setStyle("-fx-font-size: 12px;");
            HBox.setHgrow(cb, Priority.ALWAYS);

            HBox row = new HBox(2, handle, cb);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(3, 4, 3, 4));
            row.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;");

            String normalRowStyle = "-fx-background-color: transparent; -fx-background-radius: 4;";
            String activeRowStyle = "-fx-background-color: #1e3060; -fx-background-radius: 4;";
            String hoverRowStyle  = "-fx-background-color: #2a4080; -fx-background-radius: 4; -fx-border-color: #7F8EE3; -fx-border-width: 0 0 2 0;";
            String handleNormal   = "-fx-text-fill: #7F8EE3; -fx-font-size: 14px; -fx-padding: 0 6 0 2; -fx-cursor: open-hand;";
            String handleActive   = "-fx-text-fill: #FFD862; -fx-font-size: 14px; -fx-padding: 0 6 0 2; -fx-cursor: closed-hand;";

            handle.setOnMousePressed(e -> {
            dragSourceIndex = i;
            handle.setStyle(handleActive);
            row.setStyle(activeRowStyle);
            e.consume();
        });
        handle.setOnMouseReleased(e -> {
            // Reset visuals only — do NOT touch dragSourceIndex here
            handle.setStyle(handleNormal);
            row.setStyle(normalRowStyle);
        });
        handle.setOnDragDetected(e -> {
            if (dragSourceIndex >= 0) {
                handle.startFullDrag();
            }
            e.consume();
        });
            row.setOnMouseDragOver(e -> {
                if (dragSourceIndex >= 0 && dragSourceIndex != i)
                    row.setStyle(hoverRowStyle);
                e.consume();
            });
            row.setOnMouseDragExited(e -> {
                if (dragSourceIndex != i) row.setStyle(normalRowStyle);
            });
            row.setOnMouseDragReleased(e -> {
                if (dragSourceIndex >= 0 && dragSourceIndex != i) {
                    int src = dragSourceIndex;
                    dragSourceIndex = -1;
                    // Insert src before i (shift elements between)
                    String srcName = teams[src].getName();
                    List<String> names = new ArrayList<>();
                    for (Team t : teams) names.add(t.getName());
                    names.remove(src);
                    names.add(i < src ? i : i - 1 + 1, srcName);
                    // Rebuild teams array with new order
                    teams = new Team[names.size()];
                    for (int k = 0; k < names.size(); k++) teams[k] = new Team(k, names.get(k));
                    rebuildTournament();
                    updateParticipantsList();
                    updateBracketView();
                    updateProgress();
                }
                e.consume();
            });

            participantsList.getChildren().add(row);
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
        scrollPane.setPannable(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #152055; -fx-border-color: #7F8EE3; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        scrollPane.lookupAll(".scroll-bar").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        scrollPane.lookupAll(".thumb").forEach(node ->
            node.setStyle("-fx-background-color: #7F8EE3; -fx-background-radius: 4;"));
        scrollPane.lookupAll(".track").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        scrollPane.lookupAll(".increment-button, .decrement-button").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        scrollPane.lookupAll(".increment-arrow, .decrement-arrow").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
});

        Button addTeamBtn    = createStyledButton("ADD",     "#7F8EE3");
        Button removeTeamBtn = createStyledButton("REMOVE",  "#e74c3c");
        Button shuffleBtn    = createStyledButton("SHUFFLE", "#7F8EE3");

        String addNormal = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String addHover  = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String remNormal = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String remHover  = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String shuNormal = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String shuHover  = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

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

        addTeamBtn.setPrefWidth(100);
        removeTeamBtn.setPrefWidth(100);
        shuffleBtn.setPrefWidth(200);
        HBox buttonBox = new HBox(10, addTeamBtn, removeTeamBtn);
        buttonBox.setAlignment(Pos.CENTER);
        HBox shuffleBox = new HBox(shuffleBtn);
        shuffleBox.setAlignment(Pos.CENTER);
        
        Label tip = new Label("Quick Tip: You can mass remove participants by checking the box above");
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

        bracketView = new VBox(0);
        bracketView.setPadding(new Insets(0));
        bracketView.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

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
                return isPowerOfTwo(n) && n >= 4 && n <= 32;
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
            CheckBox cb = null;
            if (node instanceof HBox) {
                for (javafx.scene.Node child : ((HBox) node).getChildren())
                    if (child instanceof CheckBox) { cb = (CheckBox) child; break; }
            } else if (node instanceof CheckBox) {
                cb = (CheckBox) node;
            }
            if (cb != null && cb.isSelected() && cb.getUserData() instanceof Team)
                teamsToRemove.add((Team) cb.getUserData());
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
        bracketNameField.textProperty().addListener((obs, oldText, newText) -> {
        String[] lines = newText.split("\n", -1);
        int maxLines = 1;
        int maxCharsPerLine = 45;
        boolean tooManyLines = lines.length > maxLines;
        boolean lineTooLong = false;
        for (String line : lines) {
            if (line.length() > maxCharsPerLine) { lineTooLong = true; break; }
        }
        if (tooManyLines || lineTooLong) {
            bracketNameField.setText(oldText);
            bracketNameField.positionCaret(oldText.length());
        }
    });


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
        sportField.textProperty().addListener((obs, oldText, newText) -> {
        String[] lines = newText.split("\n", -1);
        int maxLines = 1;
        int maxCharsPerLine = 45;
        boolean tooManyLines = lines.length > maxLines;
        boolean lineTooLong = false;
        for (String line : lines) {
            if (line.length() > maxCharsPerLine) { lineTooLong = true; break; }
        }
        if (tooManyLines || lineTooLong) {
            sportField.setText(oldText);
            sportField.positionCaret(oldText.length());
        }
    });

       Label descLabel = new Label("Bracket Description:");
        descLabel.setTextFill(Color.web("#FFFFFF"));
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter tournament description...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle(
            "-fx-control-inner-background: #152055; -fx-background-color: transparent;" +
            "-fx-border-color: #7F8EE3; -fx-border-radius: 3;" +
            "-fx-prompt-text-fill: #e0e6edc2; -fx-text-fill: #FFFFFF;");
        descriptionArea.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            ScrollPane sp = (ScrollPane) descriptionArea.lookup(".scroll-pane");
            if (sp != null) {
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        });
        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
        String[] lines = newText.split("\n", -1);
        int maxLines = 4;
        int maxCharsPerLine = 28;
        boolean tooManyLines = lines.length > maxLines;
        boolean lineTooLong = false;
        for (String line : lines) {
            if (line.length() > maxCharsPerLine) { lineTooLong = true; break; }
        }
        if (tooManyLines || lineTooLong) {
            descriptionArea.setText(oldText);
            descriptionArea.positionCaret(oldText.length());
        }
    });
        descriptionArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                int caret = descriptionArea.getCaretPosition();
                String current = descriptionArea.getText();
                if (current.length() < 220) {
                    descriptionArea.setText(current.substring(0, caret) + "\n" + current.substring(caret));
                    descriptionArea.positionCaret(caret + 1);
                }
                e.consume();
            }
        });

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
        tip.setTextFill(Color.web("#E0E6ED"));
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
        panel.setStyle("-fx-background-color: linear-gradient(to right, #040D43 0%, #040D43 50%, #FFBA09 100%); -fx-border-width: 1 0 0 0;");
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
        if (n == 0) return "Single Elimination (4, 8, 10, 16, 20, 32) \nDouble Elimination (4, 8, 16, 32) \nRound Robin, Swiss System, and Free For All require at least 3 teams.";
        if ("Single Elimination".equals(type)) {
            if (n < 4) return "Need at least 4 Participants for Single Elimination.";
            int[] valid = {4, 8, 10, 16, 20, 32};
            for (int v : valid) {
                if (n == v) return "";
                if (v > n)  return "Add " + (v - n) + " more team(s) to reach " + v + " teams.\n(Valid: 4, 8, 10, 16, 20, 32)";
            }
            return "Single Elimination supports 4, 8, 10, 16, 20, or 32 teams.\nCurrent: " + n;
        }
        if ("Double Elimination".equals(type)) {
            if (n < 4)  return "Need at least 4 teams for Double Elimination.";
            if (isPowerOfTwo(n)) return "";
            int[] valid = {4, 8, 16, 32};
            for (int v : valid) if (v > n) return "Add " + (v - n) + " more team(s) for " + v + "-team Double Elimination.\n(Valid: 4, 8, 16, 32)";
            return "Double Elimination supports 4, 8, 16, or 32 teams.\nCurrent: " + n;
        }
        // Round Robin, Swiss, Free For All: only 3+ required
        if (n < 3) return "Need at least 3 teams for " + type + ".";
        return "";
    }

    private void updateBracketView() {
        bracketView.getChildren().clear();
        String currentType = bracketTypeCombo.getValue();

        if (tournament == null || !isValidTeamCount(teams.length, currentType)) {
    String fullMsg = getNotReadyMessage(teams.length, currentType);
    if (teams.length == 0 || fullMsg.startsWith("Supported")) {
        Label title = new Label("SUPPORTED FORMATS:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#FFD862"));
        title.setPadding(new Insets(10, 10, 0, 10));
        Label msg = new Label(fullMsg.replace("Supported Formats:\n \n", ""));
        msg.setFont(Font.font("Arial", 13));
        msg.setTextFill(Color.web("#E0E6ED"));
        msg.setWrapText(true);
        msg.setPadding(new Insets(5, 10, 10, 10));
        bracketView.getChildren().addAll(title, msg);
    } else {
        Label msg = new Label(fullMsg);
        msg.setFont(Font.font("Arial", 13));
        msg.setTextFill(Color.web("#E0E6ED"));
        msg.setWrapText(true);
        msg.setPadding(new Insets(10));
        bracketView.getChildren().add(msg);
    }
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
        pane.setStyle("-fx-background-color: #040D43;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#FFBA09"));
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
            String rName = fromEnd == 0 ? "Finals"
                         : fromEnd == 1 ? "Semifinals"
                         : fromEnd == 2 ? "Quarterfinals"
                         : "Round " + (ri + 1);
            List<Match> rMs = visibleByRound.get(visRoundKeys.get(ri));
            if (rMs == null) continue;
            double topY = rMs.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label lbl = styledLabel(rName, "#FFD862", FontWeight.BOLD, 11);
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

        int n         = teams.length;
        int numRounds = (int)(Math.log(n) / Math.log(2));   // WB rounds: 3 for 8t, 4 for 16t
        int winnersRounds = numRounds;

        // ── Collect WB matches by round (simple: filter allMatches) ──────────
        Map<Integer, List<Match>> winnersByRound = new HashMap<>();
        for (int r = 1; r <= winnersRounds; r++) {
            List<Match> ms = tournament.getWinnersMatchesByRound(r);
            if (!ms.isEmpty()) winnersByRound.put(r, ms);
        }

        // ── Collect LB matches by round ──────────────────────────────────────
        List<Match> losersAll = tournament.getLosersBracketMatches();
        Map<Integer, List<Match>> losersByRound = new LinkedHashMap<>();
        for (Match m : losersAll) {
            losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        }
        List<Integer> lRounds = new ArrayList<>(losersByRound.keySet());
        Collections.sort(lRounds);
        // re-index LB rounds to columns 0,1,2,...
        Map<Integer, Integer> lRoundToCol = new HashMap<>();
        for (int i = 0; i < lRounds.size(); i++) lRoundToCol.put(lRounds.get(i), i);

        Match grandFinal = tournament.getGrandFinals();

        int wCols = winnersRounds;
        int lCols = lRounds.size();
        double colStride = CARD_W + COL_GAP;

        double[] wColX = new double[Math.max(wCols, 1)];
        for (int c = 0; c < wCols; c++) wColX[c] = 20 + c * colStride;
        double[] lColX = new double[Math.max(lCols, 1)];
        for (int c = 0; c < lCols; c++) lColX[c] = 20 + c * colStride;

        // ── POSITIONAL Y layout (no child-link traversal) ────────────────────
        // WB: assign Y by slot index, doubling the gap each round
        Map<Match, Double> matchY = new HashMap<>();

        // WB round 1: evenly spaced
        if (winnersByRound.containsKey(1)) {
            List<Match> r1 = winnersByRound.get(1);
            double slotH = CARD_H + MATCH_V_GAP;
            for (int i = 0; i < r1.size(); i++) {
                matchY.put(r1.get(i), W_TOP_PAD + i * slotH);
            }
        }

        // WB rounds 2+: midpoint of the two children from the previous round
        for (int r = 2; r <= winnersRounds; r++) {
            List<Match> prev = winnersByRound.get(r - 1);
            List<Match> curr = winnersByRound.get(r);
            if (prev == null || curr == null) continue;
            for (int i = 0; i < curr.size(); i++) {
                int c1 = i * 2, c2 = i * 2 + 1;
                double y1 = c1 < prev.size() && matchY.containsKey(prev.get(c1))
                            ? matchY.get(prev.get(c1)) : W_TOP_PAD;
                double y2 = c2 < prev.size() && matchY.containsKey(prev.get(c2))
                            ? matchY.get(prev.get(c2)) : y1;
                matchY.put(curr.get(i), (y1 + y2) / 2.0);
            }
        }

        double winnersMaxY = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(W_TOP_PAD);
        double lbOffsetY   = winnersMaxY + CARD_H + SECTION_GAP;

        // LB: assign Y by slot index per column, scaling the gap to match WB spread
        if (!lRounds.isEmpty()) {
            // LB col 0 has the most matches; scale slot height to fill same vertical spread as WB
            List<Match> lbC0 = losersByRound.get(lRounds.get(0));
            double lbSlotH = CARD_H + MATCH_V_GAP;
            if (lbC0 != null) {
                for (int i = 0; i < lbC0.size(); i++) {
                    matchY.put(lbC0.get(i), lbOffsetY + 60 + i * lbSlotH);
                }
            }
            // LB later columns: midpoint of previous column's matches
            for (int ci = 1; ci < lRounds.size(); ci++) {
                List<Match> prev = losersByRound.get(lRounds.get(ci - 1));
                List<Match> curr = losersByRound.get(lRounds.get(ci));
                if (prev == null || curr == null) continue;
                int prevSize = prev.size();
                int currSize = curr.size();
                if (currSize == prevSize) {
                    // 1:1 mapping (drop round): same Y as previous
                    for (int i = 0; i < currSize; i++) {
                        if (matchY.containsKey(prev.get(i)))
                            matchY.put(curr.get(i), matchY.get(prev.get(i)));
                    }
                } else {
                    // elim round: pair up previous matches
                    for (int i = 0; i < currSize; i++) {
                        int c1 = i * 2, c2 = i * 2 + 1;
                        double y1 = c1 < prevSize && matchY.containsKey(prev.get(c1))
                                    ? matchY.get(prev.get(c1)) : lbOffsetY + 60;
                        double y2 = c2 < prevSize && matchY.containsKey(prev.get(c2))
                                    ? matchY.get(prev.get(c2)) : y1;
                        matchY.put(curr.get(i), (y1 + y2) / 2.0);
                    }
                }
            }
        }

        // Grand Final: vertically centered between WB and LB
        double wbTopY  = W_TOP_PAD;
        double lbBotY  = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(lbOffsetY) + CARD_H;
        double gfCenterY = (wbTopY + lbBotY) / 2.0;

        double lastWbRightEdge = (wCols > 0) ? wColX[wCols - 1] + CARD_W : 0;
        double lastLbRightEdge = (lCols > 0) ? lColX[lCols - 1] + CARD_W : 0;
        double gfX = Math.max(lastWbRightEdge, lastLbRightEdge) + COL_GAP;
        double gfY = gfCenterY - CARD_MID;
        if (grandFinal != null) matchY.put(grandFinal, gfY);

        // ── Canvas sizing ────────────────────────────────────────────────────
        double maxY    = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(400);
        double canvasW = gfX + CARD_W + 60;
        double canvasH = maxY + CARD_H + 80;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, canvasH);
        pane.setStyle("-fx-background-color: #040D43;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#FFBA09"));
        gc.setLineWidth(2);

        // ── WB connector lines ───────────────────────────────────────────────
        for (int r = 2; r <= winnersRounds; r++) {
            List<Match> prev = winnersByRound.get(r - 1);
            List<Match> curr = winnersByRound.get(r);
            if (prev == null || curr == null) continue;
            double srcRX = wColX[r - 2] + CARD_W;
            double dstLX = wColX[r - 1];
            double midX  = (srcRX + dstLX) / 2.0;
            for (int i = 0; i < curr.size(); i++) {
                Match m = curr.get(i);
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                int c1 = i * 2, c2 = i * 2 + 1;
                if (c1 < prev.size() && matchY.containsKey(prev.get(c1))) {
                    double y = matchY.get(prev.get(c1)) + CARD_MID;
                    gc.strokeLine(srcRX, y, midX, y);
                    gc.strokeLine(midX, y, midX, tgtY);
                    gc.strokeLine(midX, tgtY, dstLX, tgtY);
                }
                if (c2 < prev.size() && matchY.containsKey(prev.get(c2))) {
                    double y = matchY.get(prev.get(c2)) + CARD_MID;
                    gc.strokeLine(srcRX, y, midX, y);
                    gc.strokeLine(midX, y, midX, tgtY);
                    gc.strokeLine(midX, tgtY, dstLX, tgtY);
                }
            }
        }

        // ── LB connector lines ───────────────────────────────────────────────
        for (int ci = 1; ci < lRounds.size(); ci++) {
            List<Match> prev = losersByRound.get(lRounds.get(ci - 1));
            List<Match> curr = losersByRound.get(lRounds.get(ci));
            if (prev == null || curr == null) continue;
            int prevCol = lRoundToCol.get(lRounds.get(ci - 1));
            int currCol = lRoundToCol.get(lRounds.get(ci));
            double srcRX = lColX[prevCol] + CARD_W;
            double dstLX = lColX[currCol];
            double midX  = (srcRX + dstLX) / 2.0;
            int prevSize = prev.size(), currSize = curr.size();
            for (int i = 0; i < currSize; i++) {
                Match m = curr.get(i);
                if (!matchY.containsKey(m)) continue;
                double tgtY = matchY.get(m) + CARD_MID;
                if (currSize == prevSize) {
                    // 1:1 drop round
                    if (matchY.containsKey(prev.get(i))) {
                        double y = matchY.get(prev.get(i)) + CARD_MID;
                        gc.strokeLine(srcRX, y, midX, y);
                        gc.strokeLine(midX, y, midX, tgtY);
                        gc.strokeLine(midX, tgtY, dstLX, tgtY);
                    }
                } else {
                    // elim round: two feeders
                    int c1 = i * 2, c2 = i * 2 + 1;
                    if (c1 < prevSize && matchY.containsKey(prev.get(c1))) {
                        double y = matchY.get(prev.get(c1)) + CARD_MID;
                        gc.strokeLine(srcRX, y, midX, y);
                        gc.strokeLine(midX, y, midX, tgtY);
                        gc.strokeLine(midX, tgtY, dstLX, tgtY);
                    }
                    if (c2 < prevSize && matchY.containsKey(prev.get(c2))) {
                        double y = matchY.get(prev.get(c2)) + CARD_MID;
                        gc.strokeLine(srcRX, y, midX, y);
                        gc.strokeLine(midX, y, midX, tgtY);
                        gc.strokeLine(midX, tgtY, dstLX, tgtY);
                    }
                }
            }
        }

        // ── Grand Final feed lines ───────────────────────────────────────────
        if (grandFinal != null && matchY.containsKey(grandFinal)) {
            gc.setStroke(Color.web("#FFBA09")); gc.setLineWidth(2.5);
            double gfCY  = matchY.get(grandFinal) + CARD_MID;
            double mergeX = gfX - COL_GAP / 2.0;
            // WB final → GF
            if (winnersByRound.containsKey(winnersRounds)) {
                List<Match> lastW = winnersByRound.get(winnersRounds);
                if (!lastW.isEmpty() && matchY.containsKey(lastW.get(0))) {
                    double wy = matchY.get(lastW.get(0)) + CARD_MID;
                    double wx = wColX[winnersRounds - 1] + CARD_W;
                    gc.strokeLine(wx, wy, mergeX, wy);
                    gc.strokeLine(mergeX, wy, mergeX, gfCY);
                    gc.strokeLine(mergeX, gfCY, gfX, gfCY);
                }
            }
            // LB final → GF
            if (!lRounds.isEmpty()) {
                List<Match> lastL = losersByRound.get(lRounds.get(lRounds.size() - 1));
                if (lastL != null && !lastL.isEmpty() && matchY.containsKey(lastL.get(0))) {
                    double ly = matchY.get(lastL.get(0)) + CARD_MID;
                    double lx = lColX[lRoundToCol.get(lRounds.get(lRounds.size() - 1))] + CARD_W;
                    gc.strokeLine(lx, ly, mergeX, ly);
                    gc.strokeLine(mergeX, ly, mergeX, gfCY);
                    gc.strokeLine(mergeX, gfCY, gfX, gfCY);
                }
            }
        }

        pane.getChildren().add(canvas);

        // ── Section labels ───────────────────────────────────────────────────
        addPaneLabel(pane, "WINNERS BRACKET", wCols > 0 ? wColX[0] : 20, W_TOP_PAD - 42, "#FFBA09", FontWeight.BOLD, 13);
        addPaneLabel(pane, "LOSERS BRACKET",  lCols > 0 ? lColX[0] : 20, lbOffsetY + 8,  "#FFBA09", FontWeight.BOLD, 13);
        if (grandFinal != null)
            addPaneLabel(pane, "Grand Finals", gfX, gfY - 18, "#FFD862", FontWeight.BOLD, 12);

        // ── WB round labels ──────────────────────────────────────────────────
        for (int r = 1; r <= winnersRounds; r++) {
            if (!winnersByRound.containsKey(r)) continue;
            String rName = getWinnersRoundName(r, winnersRounds);
            Label rl = styledLabel(rName, "#FFD862", FontWeight.BOLD, 11);
            rl.setLayoutX(wColX[r - 1]);
            double topY = winnersByRound.get(r).stream().filter(matchY::containsKey)
                          .mapToDouble(matchY::get).min().orElse(W_TOP_PAD);
            rl.setLayoutY(topY - 18);
            pane.getChildren().add(rl);
        }

        // ── LB round labels ──────────────────────────────────────────────────
        for (int ci = 0; ci < lRounds.size(); ci++) {
            int r = lRounds.get(ci);
            List<Match> rms = losersByRound.get(r);
            double topY = rms.stream().filter(matchY::containsKey)
                          .mapToDouble(matchY::get).min().orElse(lbOffsetY + 60);
            Label rl = styledLabel("Losers Round " + (ci + 1), "#FFD862", FontWeight.BOLD, 11);
            rl.setLayoutX(lColX[ci]); rl.setLayoutY(topY - 18);
            pane.getChildren().add(rl);
        }

        // ── WB match cards ───────────────────────────────────────────────────
        for (int r = 1; r <= winnersRounds; r++) {
            if (!winnersByRound.containsKey(r)) continue;
            for (Match m : winnersByRound.get(r)) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, false);
                card.setLayoutX(wColX[r - 1]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }

        // ── LB match cards ───────────────────────────────────────────────────
        for (int ci = 0; ci < lRounds.size(); ci++) {
            int r = lRounds.get(ci);
            for (Match m : losersByRound.get(r)) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, false, false);
                card.setLayoutX(lColX[ci]); card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W); card.setMaxWidth(CARD_W);
                pane.getChildren().add(card);
            }
        }

        // ── Grand Final card ─────────────────────────────────────────────────
        if (grandFinal != null && matchY.containsKey(grandFinal)) {
            VBox gfCard = createDeMatchCard(grandFinal, false, true);
            gfCard.setLayoutX(gfX); gfCard.setLayoutY(matchY.get(grandFinal));
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

        // ── Canvas ────────────────────────────────────────────────────
        double maxY    = matchY.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double canvasW = piGfX + CARD_W + 60;
        double canvasH = maxY + CARD_H + 60;

        Pane pane = new Pane();
        pane.setPrefSize(canvasW, canvasH);
        pane.setStyle("-fx-background-color: #040D43;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

        // ── Play-in WB lines (only when play-in WB exists) ───────────
        if (hasPiWb) {
            gc.setStroke(Color.web("#FFBA09"));
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
            gc.setStroke(Color.web("#FFBA09"));
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

        pane.getChildren().add(canvas);

        // ── Section labels (only shown when the section has content) ─
        if (hasPiWb)
            addPaneLabel(pane, "WINNERS BRACKET", colX[0], TOP_PAD - 50, "#FFBA09", FontWeight.BOLD, 13);
        if (hasPiLb)
            addPaneLabel(pane, "LOSERS BRACKET",  colX[0], lbOffsetY + 10, "#FFBA09", FontWeight.BOLD, 13);
        if (hasPiGF)
            addPaneLabel(pane, "Grand Finals", piGfX, piGfY - 22, "#FFD862", FontWeight.BOLD, 12);
        // Play-in WB round labels
        if (hasPiWb) {
            for (Map.Entry<Integer, List<Match>> e : piWbByRound.entrySet()) {
                int r = e.getKey();
                double topY = e.getValue().stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
                if (topY == Double.MAX_VALUE) continue;
                int fromEnd = piWbRounds - r;
                String rName = fromEnd == 0 ? "Upper Final" : fromEnd == 1 ? "Upper Semifinals" : "Upper Round " + r;
                Label rl = styledLabel(rName, "#FFD862", FontWeight.BOLD, 11);
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
                Label rl = styledLabel("Losers Round " + (col + 1), "#FFD862", FontWeight.BOLD, 11);
                rl.setLayoutX(colX[col]); rl.setLayoutY(topY - 18);
                pane.getChildren().add(rl);
            }
        }

        addScrollPane(pane, 600);
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
        pane.setStyle("-fx-background-color: #040D43;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#FFBA09"));
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
            String rName = fromEnd == 0 ? "Finals"
                         : fromEnd == 1 ? "Semifinals"
                         : fromEnd == 2 ? "Quarterfinals"
                         : "Round " + (ci + 1);
            List<Match> rMs = piByRound.get(allPiKeys.get(ci));
            if (rMs == null) continue;
            double topY = rMs.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label lbl = styledLabel(rName, "#FFD862", FontWeight.BOLD, 11);
            lbl.setLayoutX(colX[ci]); lbl.setLayoutY(topY - 18);
            pane.getChildren().add(lbl);
        }
        for (int ci = 0; ci < mainCols; ci++) {
            int fromEnd = totalVisibleCols - 1 - (piCols + ci);
            String rName = fromEnd == 0 ? "Finals"
                         : fromEnd == 1 ? "Semifinals"
                         : fromEnd == 2 ? "Quarterfinals"
                         : "Round " + (piCols + ci + 1);
            List<Match> rMs = mainByRound.get(allMainKeys.get(ci));
            if (rMs == null) continue;
            double topY = rMs.stream().filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topY == Double.MAX_VALUE) continue;
            Label lbl = styledLabel(rName, "#FFD862", FontWeight.BOLD, 11);
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
        String border  = isGrandFinal ? "#FFBA09" : (isWinners ? "#FFBA09" : "#FFBA09");

        String t1Name = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String t2Name = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        boolean done  = match.isCompleted();
        Team winner   = match.getWinner();
        String score  = match.getScore() != null ? match.getScore() : "";

        boolean canReport = !done && match.getTeam1() != null && match.getTeam2() != null
                            && !t1Name.equals("TBD") && !t2Name.equals("TBD");

        String normalStyle = "-fx-background-color:#152055;-fx-border-color:" + border + ";-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;" + (canReport ? "-fx-cursor:hand;" : "");
        String hoverStyle  = "-fx-background-color:#152055;-fx-border-color:#FFBA09;-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";

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

        return card;
    }

    private HBox makeTeamRow(String teamName, String score, int teamIndex, boolean done, Team winner) {
    HBox row = new HBox(4);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(1, 2, 1, 2));
    Label lbl = new Label(teamName);
    lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
    lbl.setMaxWidth(120);
    lbl.setWrapText(false);
    lbl.setStyle("-fx-text-fill: #E0E6ED;");
    Label sc = new Label();
    sc.setFont(Font.font("Arial", 11));
    sc.setStyle("-fx-text-fill: #FFD862;");
    if (done && !score.isEmpty()) {
        String[] p = score.split("-");
        if (teamIndex < p.length) sc.setText(p[teamIndex].trim());
        boolean isWinner = winner != null && winner.getName().equals(teamName);
        if (isWinner) {
            lbl.setStyle("-fx-text-fill: #FFC107 ; -fx-font-weight: bold;");
            sc.setStyle("-fx-text-fill: #FFC107 ;");
        } else {
            lbl.setStyle("-fx-text-fill: #FCEB92 ; -fx-font-weight: normal;");
            sc.setStyle("-fx-text-fill: #FCEB92 ;");
            row.setStyle("-fx-background-color: #eeeeee80; -fx-background-radius: 3;");
        }
    }
    Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
    row.getChildren().addAll(lbl, spacer, sc);
    return row;
}
    private Separator makeSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #FFBA09;");
        return sep;
    }

    private String getWinnersRoundName(int round, int totalWinnersRounds) {
        int fromEnd = totalWinnersRounds - round;
        if (fromEnd == 0) return "Upper Finals";
        if (fromEnd == 1) return "Upper Semifinals";
        if (fromEnd == 2) return "Quarterfinals";
        return "Upper Round " + round;
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
        box.setStyle("-fx-background-color: #152055;-fx-border-color: #7F8EE3  09; -fx-border-radius: 8; -fx-padding: 15;");
        Label lbl = new Label("CHAMPION: " + champion.getName() + "");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web("#FFD862"));
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
        vbox.setStyle("-fx-background-color: #040D43;");

        Label titleLbl = new Label(match.getTeam1().getName() + " vs " + match.getTeam2().getName());
        titleLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web("#FFD862"));

        TextField s1 = new TextField(); s1.setPromptText("Score"); s1.setPrefWidth(80);
        TextField s2 = new TextField(); s2.setPromptText("Score"); s2.setPrefWidth(80); 
        s1.setStyle("-fx-background-color: #152055; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #ffffffa2; -fx-border-color: #7F8EE3; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;");
        s2.setStyle("-fx-background-color: #152055; -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #ffffffa2; -fx-border-color: #7F8EE3; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;");

        // Dynamic winner preview label (no dropdown — winner is always highest scorer)
        Label winnerPreview = new Label("Enter the scores");
        winnerPreview.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        winnerPreview.setTextFill(Color.web("#FFD862"));
        

        HBox r1b = new HBox(10, new Label(match.getTeam1().getName() + ":") {{ setTextFill(Color.WHITE); setFont(Font.font("Arial", FontWeight.BOLD, 13)); }}, s1); r1b.setAlignment(Pos.CENTER);
        HBox r2b = new HBox(10, new Label(match.getTeam2().getName() + ":") {{ setTextFill(Color.WHITE); setFont(Font.font("Arial", FontWeight.BOLD, 13)); }}, s2); r2b.setAlignment(Pos.CENTER);

        Button submit = new Button("Submit");
        submit.setStyle("-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        submit.setDisable(true);
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        HBox btnBox = new HBox(20, submit, cancel); btnBox.setAlignment(Pos.CENTER);

        vbox.getChildren().addAll(titleLbl, r1b, r2b, winnerPreview, btnBox);
        dlg.setScene(new Scene(vbox, 350, 270));

        // Auto-determine winner by score as user types — no dropdown needed
            javafx.beans.value.ChangeListener<String> scoreListener = (obs, oldVal, newVal) -> {
            try {
                String t1 = s1.getText().trim(), t2 = s2.getText().trim();
                if (!t1.isEmpty() && !t2.isEmpty()) {
                    int sc1 = Integer.parseInt(t1), sc2 = Integer.parseInt(t2);
                    if (sc1 > sc2) {
                        winnerPreview.setText("Winner: " + match.getTeam1().getName());
                        winnerPreview.setStyle("-fx-text-fill: #FFBA09;");
                        submit.setDisable(false);
                    } else if (sc2 > sc1) {
                        winnerPreview.setText("Winner: " + match.getTeam2().getName());
                        winnerPreview.setStyle("-fx-text-fill: #FFBA09;");
                        submit.setDisable(false);
                    } else {
                        winnerPreview.setText("No ties Allowed");
                        winnerPreview.setStyle("-fx-text-fill: #e74c3c;");
                        submit.setDisable(true);
                    }
                } else {
                    winnerPreview.setText("Enter the scores");
                    winnerPreview.setStyle("-fx-text-fill: #FFD862;");
                    submit.setDisable(true);
                }
            } catch (NumberFormatException ignored) {
                winnerPreview.setText("Enter valid numbers");
                winnerPreview.setStyle("-fx-text-fill: #e74c3c;");
                submit.setDisable(true);
            }
};
        s1.textProperty().addListener(scoreListener);
        s2.textProperty().addListener(scoreListener);

        submit.setOnAction(e -> {
            try {
                if (s1.getText().isEmpty() || s2.getText().isEmpty()) { showAlert("Error", "Please enter both scores!"); return; }
                int score1 = Integer.parseInt(s1.getText()), score2 = Integer.parseInt(s2.getText());
                if (score1 == score2) { showAlert("Error", "Scores cannot be tied — one team must win!"); return; }
                // Winner is always determined by higher score — no dropdown needed
                Team w = score1 > score2 ? match.getTeam1() : match.getTeam2();
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
            statusLabel.setStyle("-fx-font-weight:bold;-fx-font-size:16px;-fx-text-fill:#FFBA09;");
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
    sp.setStyle(
        "-fx-background: #040D43;" +
        "-fx-background-color: #040D43;" +
        "-fx-border-color: transparent;" +
        "-fx-border-width: 0;" +
        "-fx-focus-color: transparent;" +
        "-fx-faint-focus-color: transparent;"
    );
    sp.getStylesheets().clear();
    sp.skinProperty().addListener((obs, oldSkin, newSkin) -> {
        sp.lookupAll(".scroll-bar").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        sp.lookupAll(".thumb").forEach(node ->
            node.setStyle("-fx-background-color: #7F8EE3; -fx-background-radius: 0;"));
        sp.lookupAll(".track").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        sp.lookupAll(".increment-button, .decrement-button").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        sp.lookupAll(".increment-arrow, .decrement-arrow").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
    });
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