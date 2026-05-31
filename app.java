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
    contentArea.setFillHeight(true);
    VBox.setVgrow(contentArea, Priority.ALWAYS); 
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
    panel.setFillWidth(true);  // Add this to fill width
    VBox.setVgrow(panel, Priority.ALWAYS);  // Add this to make panel grow vertically
    
    Label title = new Label("BRACKET VIEW");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    title.setTextFill(Color.web("#FFD862"));
    
    bracketView = new VBox(10);
    bracketView.setPadding(new Insets(10));
    bracketView.setStyle("-fx-background-color: #152055; -fx-border-color: #7F8EE3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
    bracketView.setFillWidth(true);  // Add this
    VBox.setVgrow(bracketView, Priority.ALWAYS);  // Make bracketView expand vertically
    
    panel.getChildren().addAll(title, bracketView);
    
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
    
    teams = remainingTeams.toArray(new Team[0]);
    participantsList.getChildren().clear();
    
    
    
    for (Team team : teams) {
        CheckBox cb = new CheckBox(team.getName());
        cb.setUserData(team);
        cb.setStyle("-fx-font-size: 12px; -fx-padding: 5;");
        participantsList.getChildren().add(cb);
    }
    
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
        bracketTypeCombo.getItems().addAll("Single Elimination", "Double Elimination", "Round Robin", "Swiss", "Free For All");
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
            separator,loadBtn, saveBracketBtn, exitBtn, tip
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
        Label msgLabel = new Label("Add at least 2 teams to start the tournament.\nClick '+ Add Team' to add participants.");
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

    private void displaySingleElimination() {
    int totalRounds = tournament.getTotalRounds();
    int totalSlots = (int) Math.pow(2, totalRounds);
    double rowHeight = 85;
    double colWidth = 220;
    double totalHeight = totalSlots * rowHeight + 100;
    double totalWidth = totalRounds * colWidth + 200;
    
    Pane bracketPane = new Pane();
    bracketPane.setPrefSize(totalWidth, totalHeight);
    bracketPane.setStyle("-fx-background-color: #f5f5f5;");
    
    Canvas canvas = new Canvas(totalWidth, totalHeight);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setStroke(Color.web("#3498db"));
    gc.setLineWidth(2);
    
    for (int round = 1; round < totalRounds; round++) {
        List<Match> currentMatches = tournament.getMatchesByRound(round);
        List<Match> nextMatches = tournament.getMatchesByRound(round + 1);
        
        if (currentMatches.isEmpty() || nextMatches.isEmpty()) continue;
        
        int spacing = totalSlots / currentMatches.size();
        int nextSpacing = totalSlots / nextMatches.size();
        
        for (int i = 0; i < currentMatches.size(); i += 2) {
            if (i + 1 >= currentMatches.size()) break;
            
            int row1 = (i * spacing) + (spacing / 2);
            int row2 = ((i + 1) * spacing) + (spacing / 2);
            int nextRow = (i / 2) * nextSpacing + (nextSpacing / 2);
            
            double x1 = (round - 1) * colWidth + 150;
            double x2 = round * colWidth;
            double centerX = x1 + 60;
            
            double y1 = row1 * rowHeight + 40;
            double y2 = row2 * rowHeight + 40;
            double targetY = nextRow * rowHeight + 40;
            
            gc.strokeLine(x1, y1, centerX, y1);
            gc.strokeLine(x1, y2, centerX, y2);
            gc.strokeLine(centerX, y1, centerX, targetY);
            gc.strokeLine(centerX, y2, centerX, targetY);
            gc.strokeLine(centerX, targetY, x2, targetY);
        }
    }
    
    bracketPane.getChildren().add(canvas);
    
    for (int round = 1; round <= totalRounds; round++) {
        List<Match> matches = tournament.getMatchesByRound(round);
        if (matches.isEmpty()) continue;
        
        int spacing = totalSlots / matches.size();
        if (spacing < 1) spacing = 1;
        
        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            int rowIndex = (i * spacing) + (spacing / 2);
            double x = (round - 1) * colWidth + 10;
            double y = rowIndex * rowHeight + 10;
            
            VBox matchCard = createBracketMatchCard(match, round, i + 1);
            matchCard.setLayoutX(x);
            matchCard.setLayoutY(y);
            bracketPane.getChildren().add(matchCard);
        }
    }
    
    ScrollPane scrollPane = new ScrollPane(bracketPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    scrollPane.setPrefHeight(550);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setStyle("-fx-background: #f5f5f5; -fx-border-color: #ddd;");
    
    bracketView.getChildren().clear();
    bracketView.getChildren().add(scrollPane);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    
    Team champion = tournament.getTournamentWinner();
    if (champion != null) {
        addChampionDisplay(champion);
    }
}

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

    private VBox createBracketMatchCard(Match match, int round, int seedNumber) {
    VBox card = new VBox(5);
    card.setAlignment(Pos.CENTER);
    card.setStyle("-fx-background-color: white; -fx-border-color: #999; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 12; -fx-min-width: 160;");
    
    String team1Name = (match.getTeam1() != null) ? match.getTeam1().getName() : "TBD";
    String team2Name = (match.getTeam2() != null) ? match.getTeam2().getName() : "TBD";
    boolean isCompleted = match.isCompleted();
    Team winner = match.getWinner();
    String score = (match.getScore() != null) ? match.getScore() : "";
    
    HBox seedBox = new HBox();
    if (round == 1) {
        Label seedLabel = new Label(String.format("%d", seedNumber));
        seedLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        seedLabel.setTextFill(Color.web("#e74c3c"));
        seedBox.getChildren().add(seedLabel);
    } else {
        seedBox.setPrefHeight(20);
    }
    
    HBox team1Row = new HBox(10);
    team1Row.setAlignment(Pos.CENTER_LEFT);
    HBox team1Box = new HBox(5);
    team1Box.setAlignment(Pos.CENTER_LEFT);
    Label team1Label = new Label(team1Name);
    team1Label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    
    if (isCompleted && winner != null && winner.getName().equals(team1Name)) {
        team1Label.setTextFill(Color.GREEN);
        Label checkLabel = new Label("✅");
        checkLabel.setTextFill(Color.GREEN);
        team1Box.getChildren().add(checkLabel);
    } else if (isCompleted) {
        team1Label.setTextFill(Color.GRAY);
    }
    team1Box.getChildren().add(team1Label);
    
    Label score1Label = new Label();
    if (isCompleted && !score.isEmpty()) {
        String[] scores = score.split("-");
        if (scores.length > 0) {
            score1Label.setText(scores[0]);
            if (winner != null && winner.getName().equals(team1Name)) {
                score1Label.setTextFill(Color.GREEN);
                score1Label.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            } else {
                score1Label.setTextFill(Color.GRAY);
            }
        }
    }
    score1Label.setPrefWidth(30);
    team1Row.getChildren().addAll(team1Box, score1Label);
    
    Label vsLabel = new Label("VS");
    vsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
    vsLabel.setTextFill(Color.RED);
    vsLabel.setAlignment(Pos.CENTER);
    
    HBox team2Row = new HBox(10);
    team2Row.setAlignment(Pos.CENTER_LEFT);
    HBox team2Box = new HBox(5);
    team2Box.setAlignment(Pos.CENTER_LEFT);
    Label team2Label = new Label(team2Name);
    team2Label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    
    if (isCompleted && winner != null && winner.getName().equals(team2Name)) {
        team2Label.setTextFill(Color.GREEN);
        Label checkLabel = new Label("✅");
        checkLabel.setTextFill(Color.GREEN);
        team2Box.getChildren().add(checkLabel);
    } else if (isCompleted) {
        team2Label.setTextFill(Color.GRAY);
    }
    team2Box.getChildren().add(team2Label);
    
    Label score2Label = new Label();
    if (isCompleted && !score.isEmpty()) {
        String[] scores = score.split("-");
        if (scores.length > 1) {
            score2Label.setText(scores[1]);
            if (winner != null && winner.getName().equals(team2Name)) {
                score2Label.setTextFill(Color.GREEN);
                score2Label.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            } else {
                score2Label.setTextFill(Color.GRAY);
            }
        }
    }
    score2Label.setPrefWidth(30);
    team2Row.getChildren().addAll(team2Box, score2Label);
    
    VBox matchContent = new VBox(5);
    matchContent.setAlignment(Pos.CENTER);
    matchContent.getChildren().addAll(team1Row, vsLabel, team2Row);
    
    if (!isCompleted && match.getTeam1() != null && match.getTeam2() != null && 
        !match.getTeam1().getName().equals("TBD") && !match.getTeam2().getName().equals("TBD")) {
        Button reportBtn = new Button("REPORT SCORE");
        reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 3; -fx-padding: 4 12; -fx-font-weight: bold;");
        reportBtn.setOnAction(e -> showScoreDialog(match));
        card.getChildren().addAll(seedBox, matchContent, reportBtn);
    } else if (isCompleted && winner != null) {
        Label winnerLabel = new Label("🏆 WINNER: " + winner.getName());
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        winnerLabel.setTextFill(Color.GREEN);
        card.getChildren().addAll(seedBox, matchContent, winnerLabel);
    } else {
        card.getChildren().addAll(seedBox, matchContent);
    }
    
    card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 12; -fx-min-width: 160;"));
    card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #999; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 12; -fx-min-width: 160;"));
    
    return card;
}

   private void displayDoubleElimination() {
    System.out.println("=== DRAWING DOUBLE ELIMINATION TREE ===");
    
    int winnersRounds = tournament.getTotalRounds() - 1;
    if (winnersRounds < 1) winnersRounds = 1;
    
    int totalTeams = teams.length;
    int totalSlots = 1;
    while (totalSlots < totalTeams) {
        totalSlots *= 2;
    }
    
    double rowHeight;
    double colWidth;
    double fontSize;
    
    if (totalTeams <= 8) {
        rowHeight = 90;
        colWidth = 230;
        fontSize = 12;
    } else if (totalTeams <= 16) {
        rowHeight = 70;
        colWidth = 200;
        fontSize = 10;
    } else if (totalTeams <= 32) {
        rowHeight = 55;
        colWidth = 180;
        fontSize = 9;
    } else {
        rowHeight = 45;
        colWidth = 160;
        fontSize = 8;
    }
    
    double winnersHeight = totalSlots * rowHeight + 80;
    double totalWidth = winnersRounds * colWidth + 200;
    
    // Get losers bracket matches
    List<Match> losersMatches = tournament.getLosersBracketMatches();
    int losersMaxRound = 0;
    if (losersMatches != null && !losersMatches.isEmpty()) {
        for (Match m : losersMatches) {
            if (m.getRound() > losersMaxRound) losersMaxRound = m.getRound();
        }
    }
    
    double losersHeight = (totalSlots / 2) * (rowHeight - 10) + 80;
    double totalHeight = winnersHeight + 100 + (losersMaxRound > 0 ? losersHeight + 80 : 0);
    
    // Single pane for BOTH brackets
    Pane bracketPane = new Pane();
    bracketPane.setPrefSize(Math.max(totalWidth, 800), Math.max(totalHeight, 500));
    bracketPane.setStyle("-fx-background-color: #f5f5f5;");
    
    Canvas canvas = new Canvas(totalWidth, totalHeight);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    
    // Draw Winners Bracket lines (green)
    gc.setStroke(Color.web("#27ae60"));
    gc.setLineWidth(2);
    
    for (int round = 1; round < winnersRounds; round++) {
        List<Match> currentMatches = tournament.getMatchesByRound(round);
        List<Match> nextMatches = tournament.getMatchesByRound(round + 1);
        
        if (currentMatches.isEmpty() || nextMatches.isEmpty()) continue;
        
        int matchesInRound = currentMatches.size();
        int nextMatchesCount = nextMatches.size();
        int spacing = totalSlots / matchesInRound;
        int nextSpacing = totalSlots / nextMatchesCount;
        
        for (int i = 0; i < matchesInRound; i += 2) {
            if (i + 1 >= matchesInRound) break;
            
            int row1 = (i * spacing) + (spacing / 2);
            int row2 = ((i + 1) * spacing) + (spacing / 2);
            int nextRow = (i / 2) * nextSpacing + (nextSpacing / 2);
            
            double x1 = (round - 1) * colWidth + 150;
            double x2 = round * colWidth;
            double connectorX = x1 + 50;
            
            double y1 = row1 * rowHeight + 35;
            double y2 = row2 * rowHeight + 35;
            double targetY = nextRow * rowHeight + 35;
            
            gc.strokeLine(x1, y1, connectorX, y1);
            gc.strokeLine(x1, y2, connectorX, y2);
            gc.strokeLine(connectorX, y1, connectorX, targetY);
            gc.strokeLine(connectorX, y2, connectorX, targetY);
            gc.strokeLine(connectorX, targetY, x2, targetY);
        }
    }
    
    // Draw Losers Bracket lines (red) if there are losers matches
    if (losersMatches != null && !losersMatches.isEmpty()) {
        gc.setStroke(Color.web("#e74c3c"));
        gc.setLineWidth(2);
        
        Map<Integer, List<Match>> losersByRound = new HashMap<>();
        for (Match m : losersMatches) {
            losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        }
        
        double yOffset = winnersHeight + 60;
        
        for (int round = 2; round < losersMaxRound; round++) {
            List<Match> currentMatches = losersByRound.get(round);
            List<Match> nextMatches = losersByRound.get(round + 1);
            
            if (currentMatches == null || nextMatches == null) continue;
            
            int matchesInRound = currentMatches.size();
            int nextMatchesCount = nextMatches.size();
            int spacing = (totalSlots / 2) / matchesInRound;
            int nextSpacing = (totalSlots / 2) / nextMatchesCount;
            
            for (int i = 0; i < matchesInRound; i += 2) {
                if (i + 1 >= matchesInRound) break;
                
                int row1 = (i * spacing) + (spacing / 2);
                int row2 = ((i + 1) * spacing) + (spacing / 2);
                int nextRow = (i / 2) * nextSpacing + (nextSpacing / 2);
                
                double x1 = (round - 2) * (colWidth - 30) + 120;
                double x2 = (round - 1) * (colWidth - 30);
                double connectorX = x1 + 40;
                
                double y1 = row1 * (rowHeight - 10) + yOffset + 10;
                double y2 = row2 * (rowHeight - 10) + yOffset + 10;
                double targetY = nextRow * (rowHeight - 10) + yOffset + 10;
                
                gc.strokeLine(x1, y1, connectorX, y1);
                gc.strokeLine(x1, y2, connectorX, y2);
                gc.strokeLine(connectorX, y1, connectorX, targetY);
                gc.strokeLine(connectorX, y2, connectorX, targetY);
                gc.strokeLine(connectorX, targetY, x2, targetY);
            }
        }
    }
    
    bracketPane.getChildren().add(canvas);
    
    // Draw Winners Bracket Label
    Label winnersLabel = new Label("🏆 WINNERS BRACKET 🏆");
    winnersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    winnersLabel.setTextFill(Color.web("#27ae60"));
    winnersLabel.setLayoutX(totalWidth / 2 - 80);
    winnersLabel.setLayoutY(5);
    bracketPane.getChildren().add(winnersLabel);
    
    // Draw Winners Bracket matches
    for (int round = 1; round <= winnersRounds; round++) {
        List<Match> matches = tournament.getMatchesByRound(round);
        if (matches.isEmpty()) continue;
        
        int matchesInRound = matches.size();
        int spacing = totalSlots / matchesInRound;
        if (spacing < 1) spacing = 1;
        
        for (int i = 0; i < matchesInRound; i++) {
            Match match = matches.get(i);
            int rowIndex = (i * spacing) + (spacing / 2);
            double x = (round - 1) * colWidth + 10;
            double y = rowIndex * rowHeight + 30;
            
            VBox matchCard = createCompactMatchCard(match, round, i + 1, fontSize);
            matchCard.setLayoutX(x);
            matchCard.setLayoutY(y);
            bracketPane.getChildren().add(matchCard);
        }
    }
    
    // Draw Losers Bracket if there are matches
    if (losersMatches != null && !losersMatches.isEmpty()) {
        double yOffset = winnersHeight + 60;
        
        // Draw Losers Bracket Label
        Label losersLabel = new Label("💀 LOSERS BRACKET 💀");
        losersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        losersLabel.setTextFill(Color.web("#e74c3c"));
        losersLabel.setLayoutX(totalWidth / 2 - 75);
        losersLabel.setLayoutY(yOffset - 30);
        bracketPane.getChildren().add(losersLabel);
        
        Map<Integer, List<Match>> losersByRound = new HashMap<>();
        for (Match m : losersMatches) {
            losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
        }
        
        for (int round = 2; round <= losersMaxRound; round++) {
            List<Match> matches = losersByRound.get(round);
            if (matches == null) continue;
            
            int matchesInRound = matches.size();
            int spacing = (totalSlots / 2) / matchesInRound;
            if (spacing < 1) spacing = 1;
            
            for (int i = 0; i < matchesInRound; i++) {
                Match match = matches.get(i);
                int rowIndex = (i * spacing) + (spacing / 2);
                double x = (round - 2) * (colWidth - 30) + 10;
                double y = rowIndex * (rowHeight - 10) + yOffset + 10;
                
                VBox matchCard = createLosersMatchCard(match, fontSize);
                matchCard.setLayoutX(x);
                matchCard.setLayoutY(y);
                bracketPane.getChildren().add(matchCard);
            }
        }
    }
    
    // ONE ScrollPane for everything
    ScrollPane scrollPane = new ScrollPane(bracketPane);
    scrollPane.setFitToWidth(false);
    scrollPane.setFitToHeight(false);
    scrollPane.setPrefViewportHeight(550);
    scrollPane.setPrefViewportWidth(780);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scrollPane.setStyle("-fx-background: #f5f5f5; -fx-border-color: #ddd;");
    
    bracketView.getChildren().clear();
    bracketView.getChildren().add(scrollPane);
    
    Team champion = tournament.getTournamentWinner();
    if (champion != null) {
        addChampionDisplay(champion);
    }
}
// ========== LOSERS BRACKET WITH BINARY TREE ==========
private void displayLosersBracket() {
    List<Match> losersMatches = tournament.getLosersBracketMatches();
    
    if (losersMatches == null || losersMatches.isEmpty()) {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(30));
        emptyBox.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #e74c3c; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label emptyLabel = new Label("💀 LOSERS BRACKET\n\nNo losers bracket matches yet.\nWhen teams lose in Winners Bracket,\nthey will appear here.");
        emptyLabel.setFont(Font.font("Arial", 13));
        emptyLabel.setTextFill(Color.GRAY);
        emptyLabel.setAlignment(Pos.CENTER);
        emptyBox.getChildren().add(emptyLabel);
        
        bracketView.getChildren().add(emptyBox);
        return;
    }
    
    Map<Integer, List<Match>> losersByRound = new HashMap<>();
    for (Match m : losersMatches) {
        losersByRound.computeIfAbsent(m.getRound(), k -> new ArrayList<>()).add(m);
    }
    
    int maxRound = 0;
    for (int r : losersByRound.keySet()) {
        if (r > maxRound) maxRound = r;
    }
    
    int totalTeams = teams.length;
    int totalSlots = 1;
    while (totalSlots < totalTeams) {
        totalSlots *= 2;
    }
    
    double rowHeight = 70;
    double colWidth = 200;
    double fontSize = 11;
    
    if (totalTeams > 16) {
        rowHeight = 50;
        colWidth = 170;
        fontSize = 9;
    }
    if (totalTeams > 32) {
        rowHeight = 40;
        colWidth = 150;
        fontSize = 8;
    }
    
    double totalHeight = totalSlots * rowHeight + 100;
    double totalWidth = maxRound * colWidth + 200;
    
    Pane losersPane = new Pane();
    losersPane.setPrefSize(Math.max(totalWidth, 600), Math.max(totalHeight, 300));
    losersPane.setStyle("-fx-background-color: #fff5f5;");
    
    // Draw losers bracket binary tree lines
    Canvas canvas = new Canvas(totalWidth, totalHeight);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setStroke(Color.web("#e74c3c"));
    gc.setLineWidth(2);
    
    for (int round = 1; round < maxRound; round++) {
        List<Match> currentMatches = losersByRound.get(round);
        List<Match> nextMatches = losersByRound.get(round + 1);
        
        if (currentMatches == null || nextMatches == null) continue;
        
        int matchesInRound = currentMatches.size();
        int nextMatchesCount = nextMatches.size();
        int spacing = totalSlots / matchesInRound;
        int nextSpacing = totalSlots / nextMatchesCount;
        
        for (int i = 0; i < matchesInRound; i += 2) {
            if (i + 1 >= matchesInRound) break;
            
            int row1 = (i * spacing) + (spacing / 2);
            int row2 = ((i + 1) * spacing) + (spacing / 2);
            int nextRow = (i / 2) * nextSpacing + (nextSpacing / 2);
            
            double x1 = (round - 1) * colWidth + 140;
            double x2 = round * colWidth;
            double connectorX = x1 + 45;
            
            double y1 = row1 * rowHeight + 30;
            double y2 = row2 * rowHeight + 30;
            double targetY = nextRow * rowHeight + 30;
            
            gc.strokeLine(x1, y1, connectorX, y1);
            gc.strokeLine(x1, y2, connectorX, y2);
            gc.strokeLine(connectorX, y1, connectorX, targetY);
            gc.strokeLine(connectorX, y2, connectorX, targetY);
            gc.strokeLine(connectorX, targetY, x2, targetY);
        }
    }
    
    losersPane.getChildren().add(canvas);
    
    for (int round = 1; round <= maxRound; round++) {
        List<Match> matches = losersByRound.get(round);
        if (matches == null) continue;
        
        int matchesInRound = matches.size();
        int spacing = totalSlots / matchesInRound;
        if (spacing < 1) spacing = 1;
        
        for (int i = 0; i < matchesInRound; i++) {
            Match match = matches.get(i);
            int rowIndex = (i * spacing) + (spacing / 2);
            double x = (round - 1) * colWidth + 10;
            double y = rowIndex * rowHeight + 5;
            
            VBox matchCard = createLosersMatchCard(match, fontSize);
            matchCard.setLayoutX(x);
            matchCard.setLayoutY(y);
            losersPane.getChildren().add(matchCard);
        }
    }
    
    ScrollPane losersScroll = new ScrollPane();
    losersScroll.setContent(losersPane);
    losersScroll.setFitToWidth(false);
    losersScroll.setFitToHeight(false);
    losersScroll.setPrefViewportHeight(350);
    losersScroll.setPrefViewportWidth(750);
    losersScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    losersScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    losersScroll.setStyle("-fx-background: #fff5f5; -fx-border-color: #e74c3c; -fx-border-width: 1;");
    
    bracketView.getChildren().add(losersScroll);
}

// ========== CREATE LOSERS MATCH CARD ==========
private VBox createLosersMatchCard(Match match, double fontSize) {
    VBox card = new VBox(3);
    card.setAlignment(Pos.CENTER);
    card.setStyle("-fx-background-color: white; -fx-border-color: #e74c3c; -fx-border-width: 1.5; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 6; -fx-min-width: 130;");
    
    String team1Name = (match.getTeam1() != null) ? match.getTeam1().getName() : "TBD";
    String team2Name = (match.getTeam2() != null) ? match.getTeam2().getName() : "TBD";
    boolean isCompleted = match.isCompleted();
    Team winner = match.getWinner();
    String score = (match.getScore() != null) ? match.getScore() : "";
    
    HBox team1Box = new HBox(5);
    team1Box.setAlignment(Pos.CENTER);
    Label team1Label = new Label(team1Name);
    team1Label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
    
    Label score1Label = new Label();
    if (isCompleted && !score.isEmpty()) {
        String[] parts = score.split("-");
        if (parts.length > 0) {
            score1Label.setText(parts[0]);
            if (winner != null && winner.getName().equals(team1Name)) {
                score1Label.setTextFill(Color.GREEN);
                team1Label.setTextFill(Color.GREEN);
            }
        }
    }
    team1Box.getChildren().addAll(team1Label, score1Label);
    
    Label vsLabel = new Label(isCompleted && !score.isEmpty() ? "" : "VS");
    vsLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize - 2));
    vsLabel.setTextFill(Color.RED);
    
    HBox team2Box = new HBox(5);
    team2Box.setAlignment(Pos.CENTER);
    Label team2Label = new Label(team2Name);
    team2Label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
    
    Label score2Label = new Label();
    if (isCompleted && !score.isEmpty()) {
        String[] parts = score.split("-");
        if (parts.length > 1) {
            score2Label.setText(parts[1]);
            if (winner != null && winner.getName().equals(team2Name)) {
                score2Label.setTextFill(Color.GREEN);
                team2Label.setTextFill(Color.GREEN);
            }
        }
    }
    team2Box.getChildren().addAll(team2Label, score2Label);
    
    if (!isCompleted && match.getTeam1() != null && match.getTeam2() != null && 
        !team1Name.equals("TBD") && !team2Name.equals("TBD")) {
        Button reportBtn = new Button("REPORT");
        reportBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: " + (fontSize - 2) + "px; -fx-background-radius: 3; -fx-padding: 2 6;");
        reportBtn.setOnAction(e -> showScoreDialog(match));
        card.getChildren().addAll(team1Box, vsLabel, team2Box, reportBtn);
    } else if (isCompleted && winner != null) {
        Label winnerLabel = new Label("✓ " + winner.getName());
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize - 1));
        winnerLabel.setTextFill(Color.GREEN);
        card.getChildren().addAll(team1Box, vsLabel, team2Box, winnerLabel);
    } else {
        card.getChildren().addAll(team1Box, vsLabel, team2Box);
        if (match.getTeam1() == null && match.getTeam2() == null) {
            Label waitingLabel = new Label("WAITING");
            waitingLabel.setFont(Font.font("Arial", fontSize - 2));
            waitingLabel.setTextFill(Color.GRAY);
            card.getChildren().add(waitingLabel);
        }
    }
    
    return card;
}

    private VBox createCompactMatchCard(Match match, int round, int seedNumber, double fontSize) {
    VBox card = new VBox(3);
    card.setAlignment(Pos.CENTER);
    card.setStyle("-fx-background-color: white; -fx-border-color: #27ae60; -fx-border-width: 1.5; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-min-width: 140;");
    
    String team1Name = (match.getTeam1() != null) ? match.getTeam1().getName() : "TBD";
    String team2Name = (match.getTeam2() != null) ? match.getTeam2().getName() : "TBD";
    boolean isCompleted = match.isCompleted();
    Team winner = match.getWinner();
    String score = (match.getScore() != null) ? match.getScore() : "";
    
    HBox team1Box = new HBox(5);
    team1Box.setAlignment(Pos.CENTER);
    Label team1Label = new Label(team1Name);
    team1Label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
    
    Label score1Label = new Label();
    if (isCompleted && !score.isEmpty()) {
        String[] parts = score.split("-");
        if (parts.length > 0) {
            score1Label.setText(parts[0]);
            if (winner != null && winner.getName().equals(team1Name)) {
                score1Label.setTextFill(Color.GREEN);
                team1Label.setTextFill(Color.GREEN);
            }
        }
    }
    score1Label.setFont(Font.font("Arial", fontSize - 1));
    team1Box.getChildren().addAll(team1Label, score1Label);
    
    Label vsLabel = new Label(isCompleted && !score.isEmpty() ? "" : "VS");
    vsLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize - 2));
    vsLabel.setTextFill(Color.RED);
    
    HBox team2Box = new HBox(5);
    team2Box.setAlignment(Pos.CENTER);
    Label team2Label = new Label(team2Name);
    team2Label.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));
    
    Label score2Label = new Label();
    if (isCompleted && !score.isEmpty()) {
        String[] parts = score.split("-");
        if (parts.length > 1) {
            score2Label.setText(parts[1]);
            if (winner != null && winner.getName().equals(team2Name)) {
                score2Label.setTextFill(Color.GREEN);
                team2Label.setTextFill(Color.GREEN);
            }
        }
    }
    score2Label.setFont(Font.font("Arial", fontSize - 1));
    team2Box.getChildren().addAll(team2Label, score2Label);
    
    if (!isCompleted && match.getTeam1() != null && match.getTeam2() != null && 
        !team1Name.equals("TBD") && !team2Name.equals("TBD")) {
        Button reportBtn = new Button("REPORT");
        reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: " + (fontSize - 2) + "px; -fx-background-radius: 3; -fx-padding: 2 8;");
        reportBtn.setOnAction(e -> showScoreDialog(match));
        card.getChildren().addAll(team1Box, vsLabel, team2Box, reportBtn);
    } else if (isCompleted && winner != null) {
        Label winnerLabel = new Label("✓ " + winner.getName());
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize - 1));
        winnerLabel.setTextFill(Color.GREEN);
        card.getChildren().addAll(team1Box, vsLabel, team2Box, winnerLabel);
    } else {
        card.getChildren().addAll(team1Box, vsLabel, team2Box);
    }
    
    return card;
}

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
                    if (idIndex > 0) {
                        teamName = teamName.substring(0, idIndex);
                    }
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
    if (!saveDir.exists()) {
        saveDir.mkdir();
    }
    
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
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + adjustColor(color, -20) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;"));
        return btn;
    }
    
    private String adjustColor(String hex, int percent) {
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
