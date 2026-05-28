import java.util.*;

public class TournamentBracket {
    private Match root;
    private Team[] teams;
    private ScoreMatrix scoreMatrix;
    private int totalRounds;
    private List<Match> allMatches;
    private String tournamentType;
    private List<Match> losersBracketMatches;
    private Match grandFinals;

    public TournamentBracket(Team[] teams) {
        this(teams, "Single Elimination");
    }

    public TournamentBracket(Team[] teams, String type) {
        this.teams = teams;
        this.scoreMatrix = new ScoreMatrix(teams);
        this.tournamentType = type;
        this.allMatches = new ArrayList<Match>();
        
        if (type.equals("Single Elimination")) {
            buildSingleElimination(teams);
        } else if (type.equals("Round Robin")) {
            buildRoundRobin(teams);
        } else if (type.equals("Double Elimination")) {
            buildDoubleElimination(teams);
        } else if (type.equals("Swiss System")) {
            buildSwissSystem(teams);
        } else if (type.equals("Free For All")) {
            buildFreeForAll(teams);
        } else {
            buildSingleElimination(teams);
        }
    }

    private void buildSingleElimination(Team[] teams) {
        this.totalRounds = (int) Math.ceil(Math.log(teams.length) / Math.log(2));
        int bracketSize = (int) Math.pow(2, totalRounds);
        List<Match> currentRound = new ArrayList<>();

        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            if (i < teams.length) match.setTeam1(teams[i]);
            if (i + 1 < teams.length) match.setTeam2(teams[i + 1]);
            currentRound.add(match);
            allMatches.add(match);
        }

        int round = 2;
        while (currentRound.size() > 1) {
            List<Match> nextRound = new ArrayList<>();
            for (int i = 0; i < currentRound.size(); i += 2) {
                Match parentMatch = new Match(round);
                parentMatch.setLeftChild(currentRound.get(i));
                parentMatch.setRightChild(currentRound.get(i + 1));
                nextRound.add(parentMatch);
                allMatches.add(parentMatch);
            }
            currentRound = nextRound;
            round++;
        }
        
        this.totalRounds = round - 1;
        this.root = currentRound.get(0);
    }

    private void buildRoundRobin(Team[] teams) {
        this.totalRounds = 1;
        this.allMatches.clear();
        for (int i = 0; i < teams.length; i++) {
            for (int j = i + 1; j < teams.length; j++) {
                Match match = new Match(1);
                match.setTeam1(teams[i]);
                match.setTeam2(teams[j]);
                allMatches.add(match);
            }
        }
        this.root = null;
    }

    private void buildDoubleElimination(Team[] teams) {
        int numTeams = teams.length;
        int numRounds = (int) Math.ceil(Math.log(numTeams) / Math.log(2));
        int bracketSize = (int) Math.pow(2, numRounds);
        
        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        this.totalRounds = numRounds + 1;
        
        // Winners Bracket
        List<Match> winnersRound = new ArrayList<>();
        for (int i = 0; i < bracketSize; i += 2) {
            Match match = new Match(1);
            if (i < numTeams) match.setTeam1(teams[i]);
            if (i + 1 < numTeams) match.setTeam2(teams[i + 1]);
            if (match.getTeam1() != null || match.getTeam2() != null) {
                winnersRound.add(match);
                allMatches.add(match);
            }
        }
        
        int round = 2;
        while (winnersRound.size() > 1) {
            List<Match> nextRound = new ArrayList<>();
            for (int i = 0; i < winnersRound.size(); i += 2) {
                if (i + 1 < winnersRound.size()) {
                    Match parentMatch = new Match(round);
                    parentMatch.setLeftChild(winnersRound.get(i));
                    parentMatch.setRightChild(winnersRound.get(i + 1));
                    nextRound.add(parentMatch);
                    allMatches.add(parentMatch);
                } else {
                    nextRound.add(winnersRound.get(i));
                }
            }
            winnersRound = nextRound;
            round++;
        }
        
        Match winnersFinal = winnersRound.isEmpty() ? null : winnersRound.get(0);
        
        // Grand Finals
        if (winnersFinal != null) {
            grandFinals = new Match(numRounds + 1);
            grandFinals.setLeftChild(winnersFinal);
            allMatches.add(grandFinals);
            this.root = grandFinals;
        } else {
            this.root = null;
        }
    }

    private void buildSwissSystem(Team[] teams) {
        this.totalRounds = Math.min(5, teams.length / 2);
        this.allMatches.clear();
        List<Team> shuffled = new ArrayList<>(Arrays.asList(teams));
        Collections.shuffle(shuffled);
        for (int round = 1; round <= totalRounds; round++) {
            for (int i = 0; i < shuffled.size(); i += 2) {
                if (i + 1 < shuffled.size()) {
                    Match match = new Match(round);
                    match.setTeam1(shuffled.get(i));
                    match.setTeam2(shuffled.get(i + 1));
                    allMatches.add(match);
                }
            }
            Collections.shuffle(shuffled);
        }
        this.root = null;
    }

    private void buildFreeForAll(Team[] teams) {
        this.totalRounds = 2;
        this.allMatches.clear();
        for (int i = 0; i < teams.length; i++) {
            for (int j = i + 1; j < teams.length; j++) {
                Match match = new Match(1);
                match.setTeam1(teams[i]);
                match.setTeam2(teams[j]);
                allMatches.add(match);
            }
        }
        this.root = null;
    }

    public List<Match> getMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match match : allMatches) {
            if (match.getRound() == round) {
                result.add(match);
            }
        }
        return result;
    }

    public List<Match> getAllMatches() {
        return allMatches;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public Team[] getTeams() {
        return teams;
    }

    public Match getChampionship() {
        return root;
    }

    public ScoreMatrix getScoreMatrix() {
        return scoreMatrix;
    }

    public void recordWinner(Match match, Team winner, int score1, int score2) {
        if (match.getTeam1() == null || match.getTeam2() == null) {
            System.out.println("Error: Match does not have both teams assigned!");
            return;
        }

        match.setWinner(winner, score1, score2);

        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();
        scoreMatrix.recordMatch(team1.getId(), team2.getId(), score1, score2);

        if (tournamentType.equals("Single Elimination") || tournamentType.equals("Double Elimination")) {
            propagateWinnerUp(match, winner);
        }

        System.out.println("✓ Recorded:" + match);
    }

    private void propagateWinnerUp(Match currentMatch, Team winner) {
        for (Match match : allMatches) {
            if (match.getLeftChild() == currentMatch) {
                match.setTeam1(winner);
                checkAndCompleteMatch(match);
                propagateWinnerUp(match, winner);
                return;
            } else if (match.getRightChild() == currentMatch) {
                match.setTeam2(winner);
                checkAndCompleteMatch(match);
                propagateWinnerUp(match, winner);
                return;
            }
        }
    }

    private void checkAndCompleteMatch(Match match) {
        if (match.getTeam1() != null && match.getTeam2() != null && !match.isCompleted()) {
            System.out.println("→ Match is ready: " + match);
        }
    }

    public List<Match> getPendingMatches() {
        List<Match> pending = new ArrayList<>();
        for (Match match : allMatches) {
            if (!match.isCompleted() && match.getTeam1() != null && match.getTeam2() != null) {
                pending.add(match);
            }
        }
        return pending;
    }

    public Match getCurrentMatch() {
        List<Match> pending = getPendingMatches();
        if (pending.isEmpty()) return null;
        return pending.get(0);
    }

    public String getProgress() {
        int total = allMatches.size();
        int completed = 0;
        for (Match m : allMatches) {
            if (m.isCompleted()) completed++;
        }
        return completed + "/" + total + " matches completed";
    }

    public Team getTournamentWinner() {
        if (tournamentType.equals("Round Robin") || tournamentType.equals("Swiss System") || tournamentType.equals("Free For All")) {
            Team champion = null;
            int mostWins = -1;
            for (Team team : teams) {
                if (team.getWins() > mostWins) {
                    mostWins = team.getWins();
                    champion = team;
                } else if (team.getWins() == mostWins && champion != null) {
                    if (team.getPointDifference() > champion.getPointDifference()) {
                        champion = team;
                    }
                }
            }
            return champion;
        }
        
        if (root != null && root.isCompleted()) {
            return root.getWinner();
        }
        return null;
    }

    public List<Match> getLosersBracketMatches() {
        return losersBracketMatches;
    }

    public Match getGrandFinals() {
        return grandFinals;
    }

    public void printBracket() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏆 TOURNAMENT BRACKET - " + tournamentType + " 🏆");
        System.out.println("=".repeat(60));
        for (int round = 1; round <= totalRounds; round++) {
            System.out.println("\n📍 ROUND " + round + ":");
            System.out.println("-".repeat(40));
            for (Match match : getMatchesByRound(round)) {
                System.out.println(" " + match);
            }
        }
        System.out.println("\n" + "=".repeat(60));
    }

    public void printStandings() {
        System.out.println("\n📊 TEAM STANDINGS:");
        System.out.println("-".repeat(50));
        List<Team> sortedTeams = Arrays.asList(teams);
        sortedTeams.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team t = sortedTeams.get(i);
            System.out.printf("%d. %-12s | Wins: %d | Losses: %d | PD: %d | Win%%: %.1f%%\n",  
                i+1, t.getName(), t.getWins(), t.getLosses(), t.getPointDifference(), t.getWinPercentage());
        }
        System.out.println("-".repeat(50));
    }
}