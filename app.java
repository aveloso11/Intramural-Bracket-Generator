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
        
        Team[] tempTeams = new Team[]{new Team(0, "Loading...")};
        tournament = new TournamentBracket(tempTeams);
        
        VBox leftPanel = createParticipantsPanel();
        VBox centerPanel = createBracketViewPanel();
        VBox rightPanel = createInformationPanel();
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
        
        Button addTeamBtn = createStyledButton("ADD", "#7F8EE3");
        Button removeTeamBtn = createStyledButton("REMOVE", "#e74c3c");

        String addNormal  = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String addHover   = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        String remNormal  = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String remHover   = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        addTeamBtn.setStyle(addNormal);
        removeTeamBtn.setStyle(remNormal);
        addTeamBtn.setOnMouseEntered(e -> addTeamBtn.setStyle(addHover));
        addTeamBtn.setOnMouseExited(e -> addTeamBtn.setStyle(addNormal));
        removeTeamBtn.setOnMouseEntered(e -> removeTeamBtn.setStyle(remHover));
        removeTeamBtn.setOnMouseExited(e -> removeTeamBtn.setStyle(remNormal));
        addTeamBtn.setOnAction(e -> addTeam());
        removeTeamBtn.setOnAction(e -> removeSelectedTeams());

        HBox buttonBox = new HBox(10, addTeamBtn, removeTeamBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        Label tip = new Label("Quick Tip: Add participants by checking the box above");
        tip.setFont(Font.font("Arial", 10));
        tip.setTextFill(Color.web("#E0E6ED"));
        tip.setWrapText(true);
        
        panel.getChildren().addAll(title, scrollPane, buttonBox, tip);
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
    
    private void addTeam() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Team");
        dialog.setHeaderText("Enter team name:");
        dialog.setContentText("Team name:");
        
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Team[] newTeams = new Team[teams.length + 1];
                System.arraycopy(teams, 0, newTeams, 0, teams.length);
                newTeams[teams.length] = new Team(teams.length, name.trim());
                teams = newTeams;
                
                System.out.println("Total teams now: " + teams.length);
                
                if (teams.length >= 2) {
                    tournament = new TournamentBracket(teams, bracketTypeCombo.getValue());
                    updateBracketView();
                    System.out.println("Tournament recreated with " + teams.length + " teams");
                } else {
                    bracketView.getChildren().clear();
                    Label msgLabel = new Label("Add at least 2 teams to start the tournament.\nCurrent teams: " + teams.length);
                    msgLabel.setFont(Font.font("Arial", 14));
                    msgLabel.setTextFill(Color.web("#FFFFFF"));
                    msgLabel.setAlignment(Pos.CENTER);
                    bracketView.getChildren().add(msgLabel);
                }
                
                updateParticipantsList();
                updateProgress();
            }
        });
    }
    
    private void removeSelectedTeams() {
        List<Team> remainingTeams = new ArrayList<>();
        List<Team> teamsToRemove = new ArrayList<>();
        
        for (javafx.scene.Node node : participantsList.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Team) {
                    Team selectedTeam = (Team) cb.getUserData();
                    teamsToRemove.add(selectedTeam);
                }
            }
        }
        
        if (teamsToRemove.isEmpty()) {
            showAlert("No Selection", "Please check the box next to the team(s) you want to remove.");
            return;
        }
        
        for (Team team : teams) {
            boolean shouldRemove = false;
            for (Team removeTeam : teamsToRemove) {
                if (team.getId() == removeTeam.getId()) {
                    shouldRemove = true;
                    break;
                }
            }
            if (!shouldRemove) {
                remainingTeams.add(team);
            }
        }
        
        List<Team> reindexed = new ArrayList<>();
        for (int i = 0; i < remainingTeams.size(); i++) {
            reindexed.add(new Team(i, remainingTeams.get(i).getName()));
        }
        teams = reindexed.toArray(new Team[0]);

        if (teams.length >= 2) {
            tournament = new TournamentBracket(teams, bracketTypeCombo.getValue());
        }

        updateParticipantsList();
        updateBracketView();
        updateProgress();
        showAlert("Teams Removed", teamsToRemove.size() + " team(s) have been removed.\nRemaining teams: " + teams.length);
    }
    
    private VBox createInformationPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label title = new Label("BRACKET INFORMATION");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#FFD862"));
        
        Label nameLabel = new Label("Bracket Name:");
        nameLabel.setTextFill(Color.web("#FFFFFF"));
        nameLabel.setStyle("fx-font-weight: bold; -fx-font-size: 12px;");
        bracketNameField = new TextField("");
        bracketNameField.setPromptText("Enter bracket name...");
        bracketNameField.setStyle("-fx-background-color: #152055;-fx-text-fill: #E0E6ED; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");
        
        Label typeLabel = new Label("Bracket Type:");
        typeLabel.setTextFill(Color.web("#FFFFFF"));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        bracketTypeCombo = new ComboBox<>();
        bracketTypeCombo.getItems().addAll("Single Elimination", "Double Elimination", "Round Robin", "Swiss System", "Free For All");
        bracketTypeCombo.setValue("Single Elimination");
        bracketTypeCombo.setStyle("-fx-background-color: #152055; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");
        bracketTypeCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #E0E6ED; -fx-background-color: #152055;");
                }
            }
        });

        bracketTypeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #E0E6ED;");
                }
            }
        });

        bracketTypeCombo.setOnAction(e -> {
            String selected = bracketTypeCombo.getValue();
            System.out.println("Bracket type changed to: " + selected);
            if (teams.length >= 2) {
                tournament = new TournamentBracket(teams, selected);
                updateBracketView();
                updateProgress();
            }
        });
        
        Label sportLabel = new Label("Sport/Game:");
        sportLabel.setTextFill(Color.web("#FFFFFF"));
        sportLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        sportField = new TextField("");
        sportField.setPromptText("e.g. Basketball, Valorant...");
        sportField.setStyle("-fx-background-color: #152055;-fx-text-fill: #E0E6ED; -fx-border-color: #7F8EE3; -fx-border-radius: 3;");
        
        Label descLabel = new Label("Bracket Description:");
        descLabel.setTextFill(Color.web("#FFFFFF"));
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter tournament description...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setStyle(
            "-fx-control-inner-background: #152055; " +
            "-fx-background-color: transparent; " + 
            "-fx-border-color: #7F8EE3; " +
            "-fx-border-radius: 3; " +
            "-fx-prompt-text-fill: #e0e6edc2; " +
            "-fx-text-fill: #FFFFFF;" 
        );
        
        Label statusTitle = new Label("BRACKET STATUS:");
        statusTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #FFD862;");
        statusLabel = new Label("PENDING");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e74c3c;");
        
        Separator separator = new Separator();
        
        Button saveBracketBtn = createStyledButton("SAVE BRACKET", "#7F8EE3");
        saveBracketBtn.setPrefWidth(Double.MAX_VALUE);
        saveBracketBtn.setOnAction(e -> saveBracket());

        Button loadBtn = createStyledButton("LOAD BRACKET", "#7F8EE3");
        loadBtn.setPrefWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadBracket());

        Button exitBtn = createStyledButton("EXIT", "#e74c3c");
        exitBtn.setPrefWidth(Double.MAX_VALUE);
        exitBtn.setOnAction(e -> System.exit(0));

        String blueNormal = "-fx-background-color: #7F8EE3; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String blueHover  = "-fx-background-color: #5a6abf; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String redNormal  = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";
        String redHover   = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: transparent; -fx-font-weight: bold;";

        saveBracketBtn.setStyle(blueNormal);
        loadBtn.setStyle(blueNormal);
        exitBtn.setStyle(redNormal);
        saveBracketBtn.setOnMouseEntered(e -> saveBracketBtn.setStyle(blueHover));
        saveBracketBtn.setOnMouseExited(e -> saveBracketBtn.setStyle(blueNormal));

        loadBtn.setOnMouseEntered(e -> loadBtn.setStyle(blueHover));
        loadBtn.setOnMouseExited(e -> loadBtn.setStyle(blueNormal));

        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(redHover));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(redNormal));

        Label tip = new Label("To save your bracket, click the 'Save Bracket' button. You will then be able to create an account where you can manage your bracket and start the tournament.");
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
    
    private void updateBracketView() {
        bracketView.getChildren().clear();
        
        if (teams.length < 2) {
            Label msgLabel = new Label("Add at least 2 teams to start the tournament.\nClick 'ADD' to add participants.");
            msgLabel.setFont(Font.font("Arial", 14));
            msgLabel.setTextFill(Color.web("#7f8c8d"));
            msgLabel.setAlignment(Pos.CENTER);
            bracketView.getChildren().add(msgLabel);
            return;
        }
        
        String bracketType = bracketTypeCombo.getValue();
        System.out.println("Updating bracket view for: " + bracketType);
        
        if (bracketType.equals("Single Elimination")) {
            displaySingleElimination();
        } else if (bracketType.equals("Double Elimination")) {
            displayDoubleElimination();
        } else if (bracketType.equals("Round Robin")) {
            displayRoundRobin();
        } else if (bracketType.equals("Swiss System")) {
            displaySwissSystem();
        } else if (bracketType.equals("Free For All")) {
            displayFreeForAll();
        } else {
            displaySingleElimination();
        }
    }

    // ========== SINGLE ELIMINATION ==========

    private void displaySingleElimination() {
        final double CARD_W      = 180;
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;

        int totalRounds = tournament.getTotalRounds();

        Map<Integer, List<Match>> byRound = new HashMap<>();
        for (int r = 1; r <= totalRounds; r++) {
            List<Match> ms = tournament.getMatchesByRound(r);
            if (!ms.isEmpty()) byRound.put(r, ms);
        }

        final double TOP_PAD = 70;
        Map<Match, Double> matchY = new HashMap<>();
        List<Match> r1 = byRound.get(1);
        if (r1 != null) {
            double slotH = CARD_H + MATCH_V_GAP;
            for (int i = 0; i < r1.size(); i++) {
                matchY.put(r1.get(i), TOP_PAD + i * slotH);
            }
            for (int r = 2; r <= totalRounds; r++) {
                List<Match> prev = byRound.get(r - 1);
                List<Match> curr = byRound.get(r);
                if (prev == null || curr == null) continue;
                for (int i = 0; i < curr.size(); i++) {
                    int idx1 = i * 2, idx2 = i * 2 + 1;
                    if (idx2 < prev.size()) {
                        double y1 = matchY.get(prev.get(idx1));
                        double y2 = matchY.get(prev.get(idx2));
                        matchY.put(curr.get(i), (y1 + y2) / 2.0);
                    } else if (idx1 < prev.size()) {
                        matchY.put(curr.get(i), matchY.get(prev.get(idx1)));
                    }
                }
            }
        }

        double colStride = CARD_W + COL_GAP;
        double[] colX = new double[totalRounds];
        for (int r = 0; r < totalRounds; r++) colX[r] = 20 + r * colStride;

        double maxY = 0;
        for (double y : matchY.values()) maxY = Math.max(maxY, y);
        double canvasW = colX[totalRounds - 1] + CARD_W + 40;
        double canvasH = maxY + CARD_H + 60;

        Pane bracketPane = new Pane();
        bracketPane.setPrefSize(canvasW, canvasH);
        bracketPane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#27ae60"));
        gc.setLineWidth(2);

        for (int r = 1; r < totalRounds; r++) {
            List<Match> currMs = byRound.get(r);
            List<Match> nextMs = byRound.get(r + 1);
            if (currMs == null || nextMs == null) continue;
            double srcRightX = colX[r - 1] + CARD_W;
            double dstLeftX  = colX[r];
            double midX      = (srcRightX + dstLeftX) / 2.0;
            for (int i = 0; i < nextMs.size(); i++) {
                Match next = nextMs.get(i);
                int src1 = i * 2, src2 = i * 2 + 1;
                double targetY = matchY.get(next) + CARD_MID;
                if (src1 < currMs.size()) {
                    double y1 = matchY.get(currMs.get(src1)) + CARD_MID;
                    gc.strokeLine(srcRightX, y1, midX, y1);
                    gc.strokeLine(midX, y1, midX, targetY);
                    gc.strokeLine(midX, targetY, dstLeftX, targetY);
                }
                if (src2 < currMs.size()) {
                    double y2 = matchY.get(currMs.get(src2)) + CARD_MID;
                    gc.strokeLine(srcRightX, y2, midX, y2);
                    gc.strokeLine(midX, y2, midX, targetY);
                }
            }
        }
        bracketPane.getChildren().add(canvas);

        for (int r = 1; r <= totalRounds; r++) {
            int fromEnd = totalRounds - r;
            String rName;
            if (fromEnd == 0)      rName = "Final";
            else if (fromEnd == 1) rName = "Semifinals";
            else if (fromEnd == 2) rName = "Quarterfinals";
            else                   rName = "Round " + r;
            List<Match> rMs = byRound.get(r);
            if (rMs == null) continue;
            double topCardY = Double.MAX_VALUE;
            for (Match m : rMs) {
                if (matchY.containsKey(m)) topCardY = Math.min(topCardY, matchY.get(m));
            }
            if (topCardY == Double.MAX_VALUE) continue;
            Label lbl = new Label(rName);
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            lbl.setTextFill(Color.web("#2c3e50"));
            lbl.setLayoutX(colX[r - 1]);
            lbl.setLayoutY(topCardY - 18);
            bracketPane.getChildren().add(lbl);
        }

        for (int r = 1; r <= totalRounds; r++) {
            List<Match> ms = byRound.get(r);
            if (ms == null) continue;
            for (Match m : ms) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, r == totalRounds);
                card.setLayoutX(colX[r - 1]);
                card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W);
                card.setMaxWidth(CARD_W);
                bracketPane.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(bracketPane);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(550);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #f8f8f8; -fx-border-color: #ddd;");

        bracketView.getChildren().clear();
        bracketView.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    // ========== DOUBLE ELIMINATION ==========
    // Fixed: centered connector lines, separate column grids for winners/losers,
    //        explicit round→column map for non-power-of-2 team counts (6,8,10...32)

    private void displayDoubleElimination() {
        // --- Layout constants ---
        final double CARD_W      = 180;   // wider cards
        final double CARD_H      = 68;
        final double CARD_MID    = CARD_H / 2.0;   // FIX 1: true vertical center
        final double COL_GAP     = 70;
        final double MATCH_V_GAP = 24;
        final double W_TOP_PAD   = 80;
        final double SECTION_GAP = 100;

        // --- Collect rounds ---
        int totalRounds   = tournament.getTotalRounds();
        int winnersRounds = Math.max(1, totalRounds / 2);

        Map<Integer, List<Match>> winnersByRound = new HashMap<>();
        for (int r = 1; r <= winnersRounds; r++) {
            List<Match> ms = tournament.getWinnersMatchesByRound(r);
            if (!ms.isEmpty()) winnersByRound.put(r, ms);
        }

        List<Match> losersAll = tournament.getLosersBracketMatches();
        Map<Integer, List<Match>> losersByRound = new HashMap<>();
        int maxLosersRound = 0;
        for (Match m : losersAll) {
            losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
            if (m.getRound() > maxLosersRound) maxLosersRound = m.getRound();
        }

        Match grandFinal = tournament.getGrandFinals();

        // FIX 3: explicit sorted round list + 0-based column index map
        List<Integer> lRounds = new ArrayList<>(losersByRound.keySet());
        java.util.Collections.sort(lRounds);
        Map<Integer, Integer> lRoundToCol = new HashMap<>();
        for (int i = 0; i < lRounds.size(); i++) lRoundToCol.put(lRounds.get(i), i);

        int wCols = winnersByRound.isEmpty() ? 0 : winnersRounds;
        int lCols = lRounds.size();

        // FIX 4: separate colX arrays — each bracket has its own independent grid
        double colStride = CARD_W + COL_GAP;
        double[] wColX = new double[Math.max(wCols, 1)];
        for (int c = 0; c < wCols; c++) wColX[c] = 20 + c * colStride;

        double[] lColX = new double[Math.max(lCols, 1)];
        for (int c = 0; c < lCols; c++) lColX[c] = 20 + c * colStride;

        // FIX 4: GF column X = one stride after the rightmost column in either bracket
        double gfX = 20;
        if (wCols > 0) gfX = Math.max(gfX, wColX[wCols - 1] + CARD_W + COL_GAP);
        if (lCols > 0) gfX = Math.max(gfX, lColX[lCols - 1] + CARD_W + COL_GAP);

        // --- Compute Y positions ---
        Map<Match, Double> matchY = new HashMap<>();

        // Winners bracket Y
        if (!winnersByRound.isEmpty()) {
            List<Match> r1 = winnersByRound.get(1);
            if (r1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                for (int i = 0; i < r1.size(); i++) matchY.put(r1.get(i), W_TOP_PAD + i * slotH);
                for (int r = 2; r <= winnersRounds; r++) {
                    List<Match> prev = winnersByRound.get(r - 1);
                    List<Match> curr = winnersByRound.get(r);
                    if (prev == null || curr == null) continue;
                    for (int i = 0; i < curr.size(); i++) {
                        int i1 = i * 2, i2 = i * 2 + 1;
                        if (i2 < prev.size()) {
                            matchY.put(curr.get(i), (matchY.get(prev.get(i1)) + matchY.get(prev.get(i2))) / 2.0);
                        } else if (i1 < prev.size()) {
                            matchY.put(curr.get(i), matchY.get(prev.get(i1)));
                        }
                    }
                }
            }
        }

        // Losers bracket Y — positioned below winners section
        double winnersHeight = 0;
        for (double y : matchY.values()) winnersHeight = Math.max(winnersHeight, y);
        double lOffsetY = winnersHeight + CARD_H + SECTION_GAP;

        if (!lRounds.isEmpty()) {
            List<Match> lr1 = losersByRound.get(lRounds.get(0));
            if (lr1 != null) {
                double slotH = CARD_H + MATCH_V_GAP;
                double lTopPad = 60;
                for (int i = 0; i < lr1.size(); i++) matchY.put(lr1.get(i), lOffsetY + lTopPad + i * slotH);
                for (int ri = 1; ri < lRounds.size(); ri++) {
                    List<Match> prev = losersByRound.get(lRounds.get(ri - 1));
                    List<Match> curr = losersByRound.get(lRounds.get(ri));
                    if (prev == null || curr == null) continue;
                    if (curr.size() == prev.size()) {
                        // Drop-in round: WB loser joins → keep same Y
                        for (int i = 0; i < curr.size() && i < prev.size(); i++)
                            matchY.put(curr.get(i), matchY.get(prev.get(i)));
                    } else {
                        // Elimination round: center between pairs
                        for (int i = 0; i < curr.size(); i++) {
                            int i1 = i * 2, i2 = i * 2 + 1;
                            if (i2 < prev.size()) {
                                matchY.put(curr.get(i), (matchY.get(prev.get(i1)) + matchY.get(prev.get(i2))) / 2.0);
                            } else if (i1 < prev.size()) {
                                matchY.put(curr.get(i), matchY.get(prev.get(i1)));
                            }
                        }
                    }
                }
            }
        }

        // GF Y: midpoint between last winners match and last losers match
        double gfY = lOffsetY / 2.0;
        {
            List<Match> lastW = winnersByRound.get(winnersRounds);
            if (lastW == null) for (int r = winnersRounds; r >= 1; r--)
                if (winnersByRound.containsKey(r)) { lastW = winnersByRound.get(r); break; }
            List<Match> lastL = lRounds.isEmpty() ? null
                : losersByRound.get(lRounds.get(lRounds.size() - 1));
            double wY = (lastW != null && !lastW.isEmpty() && matchY.containsKey(lastW.get(0)))
                        ? matchY.get(lastW.get(0)) : 0;
            double lY = (lastL != null && !lastL.isEmpty() && matchY.containsKey(lastL.get(0)))
                        ? matchY.get(lastL.get(0)) : lOffsetY;
            gfY = (wY + lY) / 2.0;
        }

        // --- Canvas ---
        double maxY = 0;
        for (double y : matchY.values()) maxY = Math.max(maxY, y);
        maxY = Math.max(maxY, gfY) + CARD_H + 60;
        double canvasW = gfX + CARD_W + 60;   // FIX 6

        Pane bracketPane = new Pane();
        bracketPane.setPrefSize(canvasW, maxY);
        bracketPane.setStyle("-fx-background-color: #f8f8f8;");

        Canvas canvas = new Canvas(canvasW, maxY);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);

        // ===== WINNERS BRACKET CONNECTOR LINES =====
        gc.setStroke(Color.web("#27ae60"));
        for (int r = 1; r < winnersRounds; r++) {
            List<Match> currMs = winnersByRound.get(r);
            List<Match> nextMs = winnersByRound.get(r + 1);
            if (currMs == null || nextMs == null) continue;
            double srcRightX = wColX[r - 1] + CARD_W;
            double dstLeftX  = wColX[r];
            double midX      = (srcRightX + dstLeftX) / 2.0;   // FIX 2: true midpoint
            for (int i = 0; i < nextMs.size(); i++) {
                double targetY = matchY.get(nextMs.get(i)) + CARD_MID;
                int s1 = i * 2, s2 = i * 2 + 1;
                if (s1 < currMs.size()) {
                    double y1 = matchY.get(currMs.get(s1)) + CARD_MID;
                    gc.strokeLine(srcRightX, y1, midX, y1);
                    gc.strokeLine(midX, y1, midX, targetY);
                    gc.strokeLine(midX, targetY, dstLeftX, targetY);
                }
                if (s2 < currMs.size()) {
                    double y2 = matchY.get(currMs.get(s2)) + CARD_MID;
                    gc.strokeLine(srcRightX, y2, midX, y2);
                    gc.strokeLine(midX, y2, midX, targetY);
                }
            }
        }

        // ===== LOSERS BRACKET CONNECTOR LINES =====
        gc.setStroke(Color.web("#e74c3c"));
        for (int ri = 0; ri < lRounds.size() - 1; ri++) {
            int rCurr = lRounds.get(ri);
            int rNext = lRounds.get(ri + 1);
            List<Match> currMs = losersByRound.get(rCurr);
            List<Match> nextMs = losersByRound.get(rNext);
            if (currMs == null || nextMs == null) continue;
            int cCol = lRoundToCol.get(rCurr);   // FIX 3
            int nCol = lRoundToCol.get(rNext);
            double srcRightX = lColX[cCol] + CARD_W;
            double dstLeftX  = lColX[nCol];
            double midX      = (srcRightX + dstLeftX) / 2.0;   // FIX 2

            if (nextMs.size() < currMs.size()) {
                // Elimination: pairs merge into one
                for (int i = 0; i < nextMs.size(); i++) {
                    double targetY = matchY.get(nextMs.get(i)) + CARD_MID;
                    int s1 = i * 2, s2 = i * 2 + 1;
                    if (s1 < currMs.size()) {
                        double y1 = matchY.get(currMs.get(s1)) + CARD_MID;
                        gc.strokeLine(srcRightX, y1, midX, y1);
                        gc.strokeLine(midX, y1, midX, targetY);
                        gc.strokeLine(midX, targetY, dstLeftX, targetY);
                    }
                    if (s2 < currMs.size()) {
                        double y2 = matchY.get(currMs.get(s2)) + CARD_MID;
                        gc.strokeLine(srcRightX, y2, midX, y2);
                        gc.strokeLine(midX, y2, midX, targetY);
                    }
                }
            } else {
                // Drop-in: same count, straight elbow per match
                for (int i = 0; i < nextMs.size() && i < currMs.size(); i++) {
                    double y1 = matchY.get(currMs.get(i)) + CARD_MID;
                    double y2 = matchY.get(nextMs.get(i)) + CARD_MID;
                    gc.strokeLine(srcRightX, y1, midX, y1);
                    gc.strokeLine(midX, y1, midX, y2);
                    gc.strokeLine(midX, y2, dstLeftX, y2);
                }
            }
        }

        // ===== FEED LINES INTO GRAND FINAL — FIX 2: midX = halfway between source and gfX =====
        gc.setStroke(Color.web("#f39c12"));
        gc.setLineWidth(2.5);
        double gfCenterY = gfY + CARD_MID;

        // From last winners match
        if (!winnersByRound.isEmpty()) {
            List<Match> lastW = winnersByRound.get(winnersRounds);
            if (lastW == null) for (int r = winnersRounds; r >= 1; r--)
                if (winnersByRound.containsKey(r)) { lastW = winnersByRound.get(r); break; }
            if (lastW != null && !lastW.isEmpty() && matchY.containsKey(lastW.get(0))) {
                double wx   = wColX[winnersRounds - 1] + CARD_W;
                double wy   = matchY.get(lastW.get(0)) + CARD_MID;
                double midX = (wx + gfX) / 2.0;   // FIX 2
                gc.strokeLine(wx, wy, midX, wy);
                gc.strokeLine(midX, wy, midX, gfCenterY);
                gc.strokeLine(midX, gfCenterY, gfX, gfCenterY);
            }
        }
        // From last losers match
        if (!lRounds.isEmpty()) {
            List<Match> lastL = losersByRound.get(lRounds.get(lRounds.size() - 1));
            if (lastL != null && !lastL.isEmpty() && matchY.containsKey(lastL.get(0))) {
                int lLastCol = lRoundToCol.get(lRounds.get(lRounds.size() - 1));
                double lx   = lColX[lLastCol] + CARD_W;
                double ly   = matchY.get(lastL.get(0)) + CARD_MID;
                double midX = (lx + gfX) / 2.0;   // FIX 2
                gc.strokeLine(lx, ly, midX, ly);
                gc.strokeLine(midX, ly, midX, gfCenterY);
                gc.strokeLine(midX, gfCenterY, gfX, gfCenterY);
            }
        }

        bracketPane.getChildren().add(canvas);

        // ===== SECTION LABELS =====
        Label wLabel = new Label("🏆 WINNERS BRACKET");
        wLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        wLabel.setTextFill(Color.web("#27ae60"));
        wLabel.setLayoutX(wCols > 0 ? wColX[0] : 20);
        wLabel.setLayoutY(W_TOP_PAD - 42);
        bracketPane.getChildren().add(wLabel);

        Label lLabel = new Label("💀 LOSERS BRACKET");
        lLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lLabel.setTextFill(Color.web("#e74c3c"));
        lLabel.setLayoutX(lCols > 0 ? lColX[0] : 20);
        lLabel.setLayoutY(lOffsetY + 8);
        bracketPane.getChildren().add(lLabel);

        // ===== ROUND LABELS =====
        // Winners
        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int r = e.getKey();
            double topCardY = e.getValue().stream()
                .filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topCardY == Double.MAX_VALUE) continue;
            Label rl = new Label(getWinnersRoundName(r, winnersRounds));
            rl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            rl.setTextFill(Color.web("#2c3e50"));
            rl.setLayoutX(wColX[r - 1]);
            rl.setLayoutY(topCardY - 18);
            bracketPane.getChildren().add(rl);
        }
        // Losers
        for (int r : lRounds) {
            int col = lRoundToCol.get(r);
            List<Match> rMs = losersByRound.get(r);
            if (rMs == null) continue;
            double topCardY = rMs.stream()
                .filter(matchY::containsKey).mapToDouble(matchY::get).min().orElse(Double.MAX_VALUE);
            if (topCardY == Double.MAX_VALUE) continue;
            Label rl = new Label("Lower R" + (col + 1));
            rl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            rl.setTextFill(Color.web("#c0392b"));
            rl.setLayoutX(lColX[col]);
            rl.setLayoutY(topCardY - 18);
            bracketPane.getChildren().add(rl);
        }
        // GF
        Label gfLabel = new Label("GRAND FINAL");
        gfLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gfLabel.setTextFill(Color.web("#e67e22"));
        gfLabel.setLayoutX(gfX);
        gfLabel.setLayoutY(gfY - 18);
        bracketPane.getChildren().add(gfLabel);

        // ===== PLACE MATCH CARDS — FIX 5: use per-bracket colX arrays =====
        // Winners
        for (Map.Entry<Integer, List<Match>> e : winnersByRound.entrySet()) {
            int r = e.getKey();
            for (Match m : e.getValue()) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, true, false);
                card.setLayoutX(wColX[r - 1]);
                card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W);
                card.setMaxWidth(CARD_W);
                bracketPane.getChildren().add(card);
            }
        }
        // Losers
        for (int r : lRounds) {
            int col = lRoundToCol.get(r);
            for (Match m : losersByRound.get(r)) {
                if (!matchY.containsKey(m)) continue;
                VBox card = createDeMatchCard(m, false, false);
                card.setLayoutX(lColX[col]);
                card.setLayoutY(matchY.get(m));
                card.setPrefWidth(CARD_W);
                card.setMaxWidth(CARD_W);
                bracketPane.getChildren().add(card);
            }
        }
        // Grand Final
        if (grandFinal != null) {
            VBox gfCard = createDeMatchCard(grandFinal, false, true);
            gfCard.setLayoutX(gfX);
            gfCard.setLayoutY(gfY);
            gfCard.setPrefWidth(CARD_W + 20);
            gfCard.setMaxWidth(CARD_W + 20);
            bracketPane.getChildren().add(gfCard);
        }

        ScrollPane scrollPane = new ScrollPane(bracketPane);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(600);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #f8f8f8; -fx-border-color: #ddd;");

        bracketView.getChildren().clear();
        bracketView.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Team champion = tournament.getTournamentWinner();
        if (champion != null) addChampionDisplay(champion);
    }

    /**
     * Creates a compact match card for the bracket.
     * isWinners controls the border accent color; isGrandFinal gives a gold style.
     */
    private VBox createDeMatchCard(Match match, boolean isWinners, boolean isGrandFinal) {
        String border  = isGrandFinal ? "#f39c12" : (isWinners ? "#27ae60" : "#e74c3c");
        String bg      = isGrandFinal ? "#fff8e1" : "white";
        String hoverBg = isGrandFinal ? "#fff0cc" : (isWinners ? "#f0fff4" : "#fff0f0");

        String t1Name = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String t2Name = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        boolean done  = match.isCompleted();
        Team winner   = match.getWinner();
        String score  = match.getScore() != null ? match.getScore() : "";

        boolean canReport = !done
            && match.getTeam1() != null && match.getTeam2() != null
            && !t1Name.equals("TBD") && !t2Name.equals("TBD");

        String normalStyle =
            "-fx-background-color: " + bg + ";" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            (canReport ? "-fx-cursor: hand;" : "");

        String hoverStyle =
            "-fx-background-color: " + hoverBg + ";" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;";

        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 8, 4, 8));
        card.setStyle(normalStyle);

        if (canReport) {
            card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
            card.setOnMouseExited(e -> card.setStyle(normalStyle));
            card.setOnMouseClicked(e -> showScoreDialog(match));
        }

        HBox row1 = new HBox(4);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label lbl1 = new Label(t1Name);
        lbl1.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl1.setMaxWidth(120);
        lbl1.setWrapText(false);
        Label sc1 = new Label();
        sc1.setFont(Font.font("Arial", 11));
        if (done && !score.isEmpty()) {
            String[] p = score.split("-");
            if (p.length > 0) sc1.setText(p[0].trim());
            if (winner != null && winner.getName().equals(t1Name)) {
                lbl1.setTextFill(Color.web("#27ae60"));
                sc1.setTextFill(Color.web("#27ae60"));
            } else {
                lbl1.setTextFill(Color.GRAY);
                sc1.setTextFill(Color.GRAY);
            }
        }
        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        row1.getChildren().addAll(lbl1, spacer1, sc1);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #ddd;");

        HBox row2 = new HBox(4);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label lbl2 = new Label(t2Name);
        lbl2.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl2.setMaxWidth(120);
        lbl2.setWrapText(false);
        Label sc2 = new Label();
        sc2.setFont(Font.font("Arial", 11));
        if (done && !score.isEmpty()) {
            String[] p = score.split("-");
            if (p.length > 1) sc2.setText(p[1].trim());
            if (winner != null && winner.getName().equals(t2Name)) {
                lbl2.setTextFill(Color.web("#27ae60"));
                sc2.setTextFill(Color.web("#27ae60"));
            } else {
                lbl2.setTextFill(Color.GRAY);
                sc2.setTextFill(Color.GRAY);
            }
        }
        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
        row2.getChildren().addAll(lbl2, spacer2, sc2);

        card.getChildren().addAll(row1, sep, row2);

        if (isGrandFinal && done && winner != null) {
            Label champ = new Label("🏆 " + winner.getName());
            champ.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            champ.setTextFill(Color.web("#e67e22"));
            card.getChildren().add(champ);
        }

        return card;
    }

    private String getWinnersRoundName(int round, int totalWinnersRounds) {
        int fromEnd = totalWinnersRounds - round;
        if (fromEnd == 0) return "Upper Final";
        if (fromEnd == 1) return "Upper Semis";
        if (fromEnd == 2) return "Upper QF";
        return "Upper R" + round;
    }

    // ========== ROUND ROBIN ==========

    private void displayRoundRobin() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("ROUND ROBIN - Standings & Results");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#2c3e50"));
        container.getChildren().add(title);
        
        GridPane standingsGrid = new GridPane();
        standingsGrid.setHgap(10);
        standingsGrid.setVgap(5);
        standingsGrid.setPadding(new Insets(10));
        standingsGrid.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        String[] headers = {"Rank", "Team", "Wins", "Losses", "PD", "Win %"};
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            header.setTextFill(Color.WHITE);
            header.setStyle("-fx-background-color: #3498db; -fx-padding: 8;");
            standingsGrid.add(header, i, 0);
        }
        
        List<Team> sortedTeams = new ArrayList<>(Arrays.asList(teams));
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            standingsGrid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            standingsGrid.add(new Label(t.getName()), 1, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getPointDifference())), 4, i + 1);
            standingsGrid.add(new Label(String.format("%.1f%%", t.getWinPercentage())), 5, i + 1);
        }
        
        container.getChildren().add(standingsGrid);
        
        Label matchesLabel = new Label("ALL MATCHES");
        matchesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        container.getChildren().add(matchesLabel);
        
        VBox matchesList = new VBox(5);
        for (Match match : tournament.getAllMatches()) {
            HBox matchRow = createMatchResultRow(match);
            matchesList.getChildren().add(matchRow);
        }
        container.getChildren().add(matchesList);
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        bracketView.getChildren().add(scrollPane);
    }

    // ========== SWISS SYSTEM ==========

    private void displaySwissSystem() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("SWISS SYSTEM TOURNAMENT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        container.getChildren().add(title);
        
        GridPane standingsGrid = new GridPane();
        standingsGrid.setHgap(10);
        standingsGrid.setVgap(5);
        standingsGrid.setPadding(new Insets(10));
        standingsGrid.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        String[] headers = {"Rank", "Team", "Wins", "Losses", "Points", "Opponent Score"};
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            header.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5;");
            standingsGrid.add(header, i, 0);
        }
        
        List<Team> sortedTeams = new ArrayList<>(Arrays.asList(teams));
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            standingsGrid.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            standingsGrid.add(new Label(t.getName()), 1, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getPointsScored())), 4, i + 1);
            standingsGrid.add(new Label(String.valueOf(t.getPointsAllowed())), 5, i + 1);
        }
        
        container.getChildren().add(standingsGrid);
        
        Label pairingsLabel = new Label("CURRENT ROUND PAIRINGS");
        pairingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        pairingsLabel.setStyle("-fx-padding: 10 0 5 0;");
        container.getChildren().add(pairingsLabel);
        
        List<Match> pendingMatches = tournament.getPendingMatches();
        if (pendingMatches.isEmpty()) {
            Label noMatches = new Label("All rounds complete! Check standings for winner.");
            noMatches.setFont(Font.font("Arial", 12));
            noMatches.setTextFill(Color.GRAY);
            container.getChildren().add(noMatches);
        } else {
            for (Match match : pendingMatches) {
                HBox matchRow = createSimpleMatchRow(match);
                matchRow.setStyle("-fx-padding: 5; -fx-border-color: #ddd; -fx-border-width: 1;");
                
                Button reportBtn = new Button("Report Score");
                reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                reportBtn.setOnAction(e -> showScoreDialog(match));
                matchRow.getChildren().add(reportBtn);
                
                container.getChildren().add(matchRow);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        bracketView.getChildren().add(scrollPane);
    }

    // ========== FREE FOR ALL ==========

    private void displayFreeForAll() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f5f5;");
        
        Label title = new Label("FREE FOR ALL - Leaderboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        container.getChildren().add(title);
        
        GridPane leaderboard = new GridPane();
        leaderboard.setHgap(10);
        leaderboard.setVgap(5);
        leaderboard.setPadding(new Insets(10));
        leaderboard.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");
        
        String[] headers = {"Rank", "Team", "Wins", "Losses", "Points Scored", "Points Allowed"};
        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            header.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-padding: 5;");
            leaderboard.add(header, i, 0);
        }
        
        List<Team> sortedTeams = new ArrayList<>(Arrays.asList(teams));
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            leaderboard.add(new Label(String.valueOf(i + 1)), 0, i + 1);
            leaderboard.add(new Label(t.getName()), 1, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getWins())), 2, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getLosses())), 3, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getPointsScored())), 4, i + 1);
            leaderboard.add(new Label(String.valueOf(t.getPointsAllowed())), 5, i + 1);
        }
        
        container.getChildren().add(leaderboard);
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        bracketView.getChildren().add(scrollPane);
    }

    // ========== MATCH CARD HELPERS ==========

    private HBox createMatchResultRow(Match match) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5; -fx-border-color: #eee; -fx-border-width: 1;");
        
        String team1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String team2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        
        Label matchLabel = new Label(team1 + " vs " + team2);
        matchLabel.setPrefWidth(200);
        
        Label resultLabel = new Label();
        if (match.isCompleted() && match.getWinner() != null) {
            resultLabel.setText("WINNER: " + match.getWinner().getName() + " (" + match.getScore() + ")");
            resultLabel.setTextFill(Color.GREEN);
        } else {
            resultLabel.setText("PENDING");
            resultLabel.setTextFill(Color.RED);
        }
        
        Button reportBtn = new Button("Report");
        reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        reportBtn.setOnAction(e -> showScoreDialog(match));
        
        if (match.isCompleted()) {
            reportBtn.setDisable(true);
            reportBtn.setText("Done");
        }
        
        row.getChildren().addAll(matchLabel, resultLabel, reportBtn);
        return row;
    }

    private HBox createSimpleMatchRow(Match match) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 3;");
        
        String team1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
        String team2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
        
        row.getChildren().add(new Label(team1 + " vs " + team2));
        
        if (match.isCompleted() && match.getWinner() != null) {
            Label winnerLabel = new Label("→ " + match.getWinner().getName());
            winnerLabel.setTextFill(Color.GREEN);
            row.getChildren().add(winnerLabel);
        }
        
        return row;
    }

    private void addChampionDisplay(Team champion) {
        HBox championBox = new HBox();
        championBox.setAlignment(Pos.CENTER);
        championBox.setStyle("-fx-background-color: #f1c40f; -fx-border-radius: 8; -fx-padding: 15;");
        Label championLabel = new Label("🏆 CHAMPION: " + champion.getName() + " 🏆");
        championLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        championLabel.setTextFill(Color.web("#2c3e50"));
        championBox.getChildren().add(championLabel);
        bracketView.getChildren().add(championBox);
    }

    // ========== SCORE DIALOG ==========
    
    private void showScoreDialog(Match match) {
        System.out.println("Opening dialog for match: " + match.getMatchId());
        
        if (match.isCompleted()) {
            showAlert("Match Already Completed", "This match has already been reported.");
            return;
        }
        
        if (match.getTeam1() == null || match.getTeam2() == null) {
            showAlert("Match Not Ready", "Both teams are not assigned yet.");
            return;
        }
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Report Match Result");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        VBox dialogVBox = new VBox(15);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label(match.getTeam1().getName() + " vs " + match.getTeam2().getName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        HBox team1Box = new HBox(10);
        team1Box.setAlignment(Pos.CENTER);
        Label team1Label = new Label(match.getTeam1().getName() + ":");
        team1Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField score1Field = new TextField();
        score1Field.setPromptText("Score");
        score1Field.setPrefWidth(80);
        team1Box.getChildren().addAll(team1Label, score1Field);
        
        HBox team2Box = new HBox(10);
        team2Box.setAlignment(Pos.CENTER);
        Label team2Label = new Label(match.getTeam2().getName() + ":");
        team2Label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField score2Field = new TextField();
        score2Field.setPromptText("Score");
        score2Field.setPrefWidth(80);
        team2Box.getChildren().addAll(team2Label, score2Field);
        
        HBox winnerBox = new HBox(10);
        winnerBox.setAlignment(Pos.CENTER);
        Label winnerLabel = new Label("Winner:");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ComboBox<Team> winnerCombo = new ComboBox<>();
        winnerCombo.getItems().addAll(match.getTeam1(), match.getTeam2());
        winnerCombo.setPromptText("Select winner");
        winnerCombo.setPrefWidth(150);
        winnerBox.getChildren().addAll(winnerLabel, winnerCombo);
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        Button submitBtn = new Button("Submit");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5;");
        buttonBox.getChildren().addAll(submitBtn, cancelBtn);
        
        dialogVBox.getChildren().addAll(titleLabel, team1Box, team2Box, winnerBox, buttonBox);
        
        Scene dialogScene = new Scene(dialogVBox, 350, 300);
        dialogStage.setScene(dialogScene);
        
        submitBtn.setOnAction(e -> {
            try {
                String score1Text = score1Field.getText();
                String score2Text = score2Field.getText();
                
                if (score1Text.isEmpty() || score2Text.isEmpty()) {
                    showAlert("Error", "Please enter both scores!");
                    return;
                }
                
                int score1 = Integer.parseInt(score1Text);
                int score2 = Integer.parseInt(score2Text);
                Team winner = winnerCombo.getValue();
                
                if (winner == null) {
                    showAlert("Error", "Please select the winner!");
                    return;
                }
                
                tournament.recordWinner(match, winner, score1, score2);
                updateBracketView();
                updateProgress();
                
                showAlert("Success", "Match result recorded!\n" + winner.getName() + " wins " + score1 + "-" + score2);
                dialogStage.close();
                
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter valid numbers for scores!");
            }
        });
        
        cancelBtn.setOnAction(e -> dialogStage.close());
        
        dialogStage.showAndWait();
    }

    // ========== SAVE / LOAD ==========
    
    private void loadBracket() {
        File saveDir = new File("saved_brackets");
        if (!saveDir.exists()) {
            showAlert("No Brackets", "No saved brackets found.");
            return;
        }
        
        File[] files = saveDir.listFiles();
        if (files == null || files.length == 0) {
            showAlert("No Brackets", "No saved brackets found.");
            return;
        }
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Bracket");
        fileChooser.setInitialDirectory(saveDir);
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try (java.util.Scanner scanner = new java.util.Scanner(selectedFile)) {
                String bracketName = "";
                String bracketType = "Single Elimination";
                String sport = "";
                StringBuilder description = new StringBuilder();
                List<String> teamNames = new ArrayList<>();
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("BRACKET NAME: ")) {
                        bracketName = line.substring(14);
                    } else if (line.startsWith("BRACKET TYPE: ")) {
                        bracketType = line.substring(14);
                    } else if (line.startsWith("SPORT/GAME: ")) {
                        sport = line.substring(12);
                    } else if (line.startsWith("DESCRIPTION: ")) {
                        description.append(line.substring(13));
                    } else if (line.startsWith("Team: ")) {
                        String teamName = line.substring(6);
                        int idIndex = teamName.indexOf(" |");
                        if (idIndex > 0) teamName = teamName.substring(0, idIndex);
                        teamNames.add(teamName);
                    }
                }
                
                teams = new Team[teamNames.size()];
                for (int i = 0; i < teamNames.size(); i++) {
                    teams[i] = new Team(i, teamNames.get(i));
                }
                
                bracketNameField.setText(bracketName);
                bracketTypeCombo.setValue(bracketType);
                sportField.setText(sport);
                descriptionArea.setText(description.toString());
                
                if (teams.length >= 2) {
                    tournament = new TournamentBracket(teams, bracketType);
                    updateBracketView();
                } else {
                    bracketView.getChildren().clear();
                    Label msgLabel = new Label("Not enough teams to create bracket.\nNeed at least 2 teams.");
                    msgLabel.setFont(Font.font("Arial", 14));
                    msgLabel.setTextFill(Color.web("#7f8c8d"));
                    msgLabel.setAlignment(Pos.CENTER);
                    bracketView.getChildren().add(msgLabel);
                }
                
                updateParticipantsList();
                updateProgress();
                showAlert("Bracket Loaded", "Successfully loaded: " + selectedFile.getName());
                
            } catch (FileNotFoundException e) {
                showAlert("Load Error", "Could not load file: " + e.getMessage());
            }
        }
    }

    private void saveBracket() {
        String name = bracketNameField.getText();
        String type = bracketTypeCombo.getValue();
        String sport = sportField.getText();
        String description = descriptionArea.getText();
        
        File saveDir = new File("saved_brackets");
        if (!saveDir.exists()) saveDir.mkdir();
        
        String filename = name.replaceAll("\\s+", "_") + ".txt";
        File saveFile = new File(saveDir, filename);
        
        try (PrintWriter writer = new PrintWriter(saveFile)) {
            writer.println("BRACKET NAME: " + name);
            writer.println("BRACKET TYPE: " + type);
            writer.println("SPORT/GAME: " + sport);
            writer.println("DESCRIPTION: " + description);
            writer.println("STATUS: " + statusLabel.getText());
            writer.println("TEAMS: " + teams.length);
            writer.println("----------------------------------------");
            
            for (Team team : teams) {
                writer.println("Team: " + team.getName() + " | Wins: " + team.getWins() + " | Losses: " + team.getLosses());
            }
            
            writer.println("----------------------------------------");
            writer.println("MATCH RESULTS:");
            
            for (Match match : tournament.getAllMatches()) {
                if (match.isCompleted()) {
                    String t1 = match.getTeam1() != null ? match.getTeam1().getName() : "TBD";
                    String t2 = match.getTeam2() != null ? match.getTeam2().getName() : "TBD";
                    writer.println(t1 + " vs " + t2 + " -> Winner: " + match.getWinner().getName() + " (" + match.getScore() + ")");
                }
            }
            
            showAlert("Bracket Saved", "Bracket '" + name + "' saved to:\n" + saveFile.getAbsolutePath());
            
        } catch (FileNotFoundException e) {
            showAlert("Save Error", "Could not save bracket: " + e.getMessage());
        }
    }
    
    // ========== PROGRESS & UTILITY ==========
    
    private void updateProgress() {
        if (teams.length < 2 || tournament.getAllMatches().isEmpty()) {
            progressBar.setProgress(0);
            progressLabel.setText("0% Complete");
            statusLabel.setText("PENDING");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            return;
        }
        
        int totalMatches = tournament.getAllMatches().size();
        int completedMatches = 0;
        for (Match m : tournament.getAllMatches()) {
            if (m.isCompleted()) completedMatches++;
        }
        
        double progress = totalMatches > 0 ? (double) completedMatches / totalMatches : 0;
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%d%% Complete", (int)(progress * 100)));
        
        if (tournament.getTournamentWinner() != null) {
            statusLabel.setText("COMPLETE");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #27ae60;");
        } else {
            statusLabel.setText("PENDING");
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");
        }
    }
    
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        String normalStyle = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;";
        String hoverStyle  = "-fx-background-color: " + adjustColor(color) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;";
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    private String adjustColor(String hex) {
        if (hex.equals("#7F8EE3")) return "#5a6abf";
        if (hex.equals("#e74c3c")) return "#c0392b";
        return hex;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}