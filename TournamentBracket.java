import java.util.*;

public class TournamentBracket {
    private Match root;
    private Team[] teams;
    private ScoreMatrix scoreMatrix;
    private int totalRounds;
    private List<Match> allMatches;
    private TournamentType tournamentType;
    private List<Match> losersBracketMatches;
    private Match grandFinals;
    // Maps a winners-bracket match → the losers-bracket match that receives its loser
    private Map<Match, Match> winnerToLoserMatch;

    public TournamentBracket(Team[] teams) {
        this(teams, TournamentType.SINGLE_ELIMINATION);
    }

    public TournamentBracket(Team[] teams, TournamentType type) {
        this.teams = teams;
        this.scoreMatrix = new ScoreMatrix(teams);
        this.tournamentType = type;
        this.allMatches = new ArrayList<>();
        this.winnerToLoserMatch = new HashMap<>();
        this.losersBracketMatches = new ArrayList<>();

        if      (type == TournamentType.SINGLE_ELIMINATION) buildSingleElimination(teams);
        else if (type == TournamentType.ROUND_ROBIN)        buildRoundRobin(teams);
        else if (type == TournamentType.DOUBLE_ELIMINATION) buildDoubleElimination(teams);
        else if (type == TournamentType.SWISS)              buildSwissSystem(teams);
        else if (type == TournamentType.FREE_FOR_ALL)       buildFreeForAll(teams);
        else                                                 buildSingleElimination(teams);
    }

    public TournamentBracket(Team[] teams, String type) {
        this(teams, convertToTournamentType(type));
    }

    private static TournamentType convertToTournamentType(String type) {
        if (type == null) return TournamentType.SINGLE_ELIMINATION;
        switch (type) {
            case "Single Elimination": return TournamentType.SINGLE_ELIMINATION;
            case "Double Elimination": return TournamentType.DOUBLE_ELIMINATION;
            case "Round Robin":        return TournamentType.ROUND_ROBIN;
            case "Swiss System":       return TournamentType.SWISS;
            case "Free For All":       return TournamentType.FREE_FOR_ALL;
            default:                   return TournamentType.SINGLE_ELIMINATION;
        }
    }

    // =========================================================================
    // SINGLE ELIMINATION
    // =========================================================================

    private void buildSingleElimination(Team[] teams) {
        int numRounds   = (int) Math.ceil(Math.log(teams.length) / Math.log(2));
        int bracketSize = (int) Math.pow(2, numRounds);

        List<Match> currentRound = new ArrayList<>();
        int matchId = 1;

        for (int i = 0; i < bracketSize; i += 2) {
            Match m = new Match(1);
            m.setMatchId(matchId++);
            if (i     < teams.length) m.setTeam1(teams[i]);
            if (i + 1 < teams.length) m.setTeam2(teams[i + 1]);
            autoCompleteBye(m);
            if (m.getTeam1() != null || m.getTeam2() != null) {
                currentRound.add(m);
                allMatches.add(m);
            }
        }

        int round = 2;
        while (currentRound.size() > 1) {
            List<Match> next = new ArrayList<>();
            for (int i = 0; i + 1 < currentRound.size(); i += 2) {
                Match parent = new Match(round);
                parent.setMatchId(matchId++);
                parent.setLeftChild(currentRound.get(i));
                parent.setRightChild(currentRound.get(i + 1));
                // Propagate bye winners immediately
                if (currentRound.get(i).isCompleted())
                    parent.setTeam1(currentRound.get(i).getWinner());
                if (currentRound.get(i + 1).isCompleted())
                    parent.setTeam2(currentRound.get(i + 1).getWinner());
                autoCompleteBye(parent);
                next.add(parent);
                allMatches.add(parent);
            }
            currentRound = next;
            round++;
        }

        this.totalRounds = round - 1;
        this.root = currentRound.isEmpty() ? null : currentRound.get(0);
    }

    // =========================================================================
    // DOUBLE ELIMINATION  (supports 4–32 teams)
    // =========================================================================

    private void buildDoubleElimination(Team[] teams) {
        int numTeams    = teams.length;
        int numRounds   = (int) Math.ceil(Math.log(numTeams) / Math.log(2));
        int bracketSize = (int) Math.pow(2, numRounds);

        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        winnerToLoserMatch.clear();

        int matchId       = 1;
        int losersMatchId = 1000;

        // ----------------------------------------------------------------
        // WINNERS BRACKET — round numbers 1..numRounds
        // ----------------------------------------------------------------
        // Track which matches are real (2 teams) vs bye (1 team) vs empty
        Set<Match> byeWBMatches = new HashSet<>();

        List<Match> wbRound1 = new ArrayList<>();
        for (int i = 0; i < bracketSize; i += 2) {
            boolean has1 = i     < numTeams;
            boolean has2 = i + 1 < numTeams;
            if (!has1 && !has2) continue;   // empty slot – skip entirely

            Match m = new Match(1);
            m.setMatchId(matchId++);
            m.setIsWinnersBracket(true);
            if (has1) m.setTeam1(teams[i]);
            if (has2) m.setTeam2(teams[i + 1]);

            if (autoCompleteBye(m)) byeWBMatches.add(m);   // bye: record it

            wbRound1.add(m);
            allMatches.add(m);
        }

        // Build subsequent WB rounds
        List<List<Match>> wbRounds = new ArrayList<>();
        wbRounds.add(wbRound1);
        List<Match> wbCurrent = new ArrayList<>(wbRound1);

        for (int r = 2; r <= numRounds; r++) {
            List<Match> wbNext = new ArrayList<>();
            for (int i = 0; i + 1 < wbCurrent.size(); i += 2) {
                Match parent = new Match(r);
                parent.setMatchId(matchId++);
                parent.setIsWinnersBracket(true);
                parent.setLeftChild(wbCurrent.get(i));
                parent.setRightChild(wbCurrent.get(i + 1));
                // Propagate bye/pre-completed winners immediately
                if (wbCurrent.get(i).isCompleted())
                    parent.setTeam1(wbCurrent.get(i).getWinner());
                if (wbCurrent.get(i + 1).isCompleted())
                    parent.setTeam2(wbCurrent.get(i + 1).getWinner());
                if (autoCompleteBye(parent)) byeWBMatches.add(parent);
                wbNext.add(parent);
                allMatches.add(parent);
            }
            // Odd carry-over (shouldn't happen with power-of-2 bracket)
            if (wbCurrent.size() % 2 == 1)
                wbNext.add(wbCurrent.get(wbCurrent.size() - 1));
            wbRounds.add(wbNext);
            wbCurrent = wbNext;
        }

        Match wbFinal = wbCurrent.isEmpty() ? null : wbCurrent.get(0);

        // ----------------------------------------------------------------
        // LOSERS BRACKET — round numbers start at numRounds + 1
        //
        // Algorithm (correct for any N in [4,32]):
        //   Count real WB R1 matches → lb_alive losers enter LB.
        //   For each subsequent WB round wr = 2..numRounds:
        //     1. LB ELIM round: lb_alive teams play each other (floor/2 matches).
        //        Survivors = ceil(lb_alive/2).
        //     2. LB DROP round: WB-wr losers drop in.
        //        Real losers from WB round wr = count of non-bye WB matches in round wr.
        //        Each LB survivor is paired 1-to-1 with a WB loser.
        //        If counts differ, the extra LB teams get a free pass (LB bye, not a match).
        //        Matches = min(lb_survivors, wb_losers).
        //        After drop: lb_alive = matches + |lb_survivors - wb_losers| (byes carry over).
        //   After the final WB round's drop, 1 team remains → LB finalist → Grand Final.
        // ----------------------------------------------------------------

        // Count real WB matches per round
        int[] wbRealMatches = new int[numRounds + 1]; // index 1..numRounds
        for (int r = 1; r <= numRounds; r++) {
            int cnt = 0;
            for (Match m : wbRounds.get(r - 1)) {
                if (!byeWBMatches.contains(m)) cnt++;
            }
            wbRealMatches[r] = cnt;
        }

        int lb_alive = wbRealMatches[1]; // losers from WB R1
        int lbRoundNumber = numRounds;   // LB rounds start AFTER WB rounds

        // Map from "LB round number" → list of LB matches in that round
        Map<Integer, List<Match>> lbRoundMap = new LinkedHashMap<>();
        // Map from WB round → list of LB DROP matches that receive those losers
        Map<Integer, List<Match>> wbRoundToLBDrop = new HashMap<>();

        for (int wr = 2; wr <= numRounds; wr++) {

            // --- LB ELIM round ---
            lbRoundNumber++;
            List<Match> elimRound = new ArrayList<>();
            int elimMatches  = lb_alive / 2;
            int elimByes     = lb_alive % 2;  // odd team gets LB bye through elim

            for (int i = 0; i < elimMatches; i++) {
                Match lm = new Match(lbRoundNumber);
                lm.setMatchId(losersMatchId++);
                elimRound.add(lm);
                losersBracketMatches.add(lm);
                allMatches.add(lm);
            }
            if (!elimRound.isEmpty()) lbRoundMap.put(lbRoundNumber, elimRound);

            int lb_survivors = elimMatches + elimByes;

            // --- LB DROP round ---
            lbRoundNumber++;
            int wb_losers  = wbRealMatches[wr];
            int dropMatches = Math.min(lb_survivors, wb_losers);
            int lbByesInDrop = lb_survivors - dropMatches;  // LB teams with no WB opponent
            // (wb_losers >= dropMatches always if lb_survivors <= wb_losers,
            //  but we only create real match slots)

            List<Match> dropRound = new ArrayList<>();
            for (int i = 0; i < dropMatches; i++) {
                Match lm = new Match(lbRoundNumber);
                lm.setMatchId(losersMatchId++);
                dropRound.add(lm);
                losersBracketMatches.add(lm);
                allMatches.add(lm);
            }
            if (!dropRound.isEmpty()) {
                lbRoundMap.put(lbRoundNumber, dropRound);
                wbRoundToLBDrop.put(wr, dropRound);
            }

            lb_alive = dropMatches + lbByesInDrop;
        }

        // ----------------------------------------------------------------
        // Wire LB elim rounds: each elim round's matches feed pairs from prev round
        // ----------------------------------------------------------------
        List<Integer> lbRoundNums = new ArrayList<>(lbRoundMap.keySet());
        Collections.sort(lbRoundNums);

        for (int ri = 1; ri < lbRoundNums.size(); ri++) {
            int prevNum = lbRoundNums.get(ri - 1);
            int currNum = lbRoundNums.get(ri);
            List<Match> prev = lbRoundMap.get(prevNum);
            List<Match> curr = lbRoundMap.get(currNum);
            if (prev == null || curr == null) continue;

            // Is this an elim round (fewer or equal matches) or a drop round?
            // Determine by checking: same-size = drop, smaller = elim
            if (curr.size() < prev.size()) {
                // Elim: pairs from prev collapse into curr
                for (int i = 0; i < curr.size(); i++) {
                    int s1 = i * 2, s2 = i * 2 + 1;
                    if (s1 < prev.size()) curr.get(i).setLeftChild(prev.get(s1));
                    if (s2 < prev.size()) curr.get(i).setRightChild(prev.get(s2));
                }
            } else if (curr.size() == prev.size()) {
                // Drop or same-size elim: 1-to-1, left child only (WB loser fills right)
                for (int i = 0; i < curr.size() && i < prev.size(); i++) {
                    curr.get(i).setLeftChild(prev.get(i));
                }
            } else {
                // curr bigger than prev — carry prev 1-to-1, extras have no left child
                for (int i = 0; i < prev.size(); i++) {
                    curr.get(i).setLeftChild(prev.get(i));
                }
            }
        }

        // ----------------------------------------------------------------
        // Map WB matches → LB drop slots (winnerToLoserMatch)
        // ----------------------------------------------------------------
        // WB R1 losers → LB elim R1 (first LB round)
        if (!lbRoundNums.isEmpty()) {
            List<Match> lbElim1 = lbRoundMap.get(lbRoundNums.get(0));
            if (lbElim1 != null) {
                List<Match> wbR1 = wbRounds.get(0);
                int slot = 0;
                for (Match wm : wbR1) {
                    if (byeWBMatches.contains(wm)) continue;
                    // Each real WB R1 match produces one loser → fills one LB elim1 slot
                    // Two WB R1 losers share one LB elim1 match
                    int lbIdx = slot / 2;
                    if (lbIdx < lbElim1.size()) {
                        winnerToLoserMatch.put(wm, lbElim1.get(lbIdx));
                    }
                    slot++;
                }
            }
        }

        // WB R2..numRounds-1 losers → their corresponding LB DROP rounds
        for (int wr = 2; wr < numRounds; wr++) {
            List<Match> dropRound = wbRoundToLBDrop.get(wr);
            if (dropRound == null) continue;
            List<Match> wbR = wbRounds.get(wr - 1);
            int slot = 0;
            for (Match wm : wbR) {
                if (byeWBMatches.contains(wm)) continue;
                if (slot < dropRound.size()) {
                    winnerToLoserMatch.put(wm, dropRound.get(slot));
                }
                slot++;
            }
        }

        // ----------------------------------------------------------------
        // GRAND FINAL
        // ----------------------------------------------------------------
        Match lbFinal = lbRoundNums.isEmpty() ? null
                : lbRoundMap.get(lbRoundNums.get(lbRoundNums.size() - 1)) == null ? null
                : lbRoundMap.get(lbRoundNums.get(lbRoundNums.size() - 1)).isEmpty() ? null
                : lbRoundMap.get(lbRoundNums.get(lbRoundNums.size() - 1)).get(0);

        lbRoundNumber++;
        grandFinals = new Match(lbRoundNumber);
        grandFinals.setMatchId(matchId);
        if (wbFinal  != null) grandFinals.setLeftChild(wbFinal);
        if (lbFinal  != null) grandFinals.setRightChild(lbFinal);
        if (wbFinal  != null && wbFinal.isCompleted())  grandFinals.setTeam1(wbFinal.getWinner());
        if (lbFinal  != null && lbFinal.isCompleted())  grandFinals.setTeam2(lbFinal.getWinner());
        allMatches.add(grandFinals);
        this.root = grandFinals;
        this.totalRounds = lbRoundNumber;

        System.out.println("Double Elimination built: " + numTeams + " teams");
        System.out.println("  WB rounds: " + numRounds + "  |  WB matches: " + (allMatches.size() - losersBracketMatches.size() - 1));
        System.out.println("  LB rounds: " + lbRoundNums.size() + "  |  LB matches: " + losersBracketMatches.size());
        System.out.println("  Expected LB matches: " + (numTeams - 2));
        System.out.println("  Bye WB matches: " + byeWBMatches.size());
        System.out.println("  GF: round " + lbRoundNumber);
    }

    // =========================================================================
    // ROUND ROBIN
    // =========================================================================

    private void buildRoundRobin(Team[] teams) {
        this.totalRounds = 1;
        allMatches.clear();
        for (int i = 0; i < teams.length; i++)
            for (int j = i + 1; j < teams.length; j++) {
                Match m = new Match(1);
                m.setTeam1(teams[i]);
                m.setTeam2(teams[j]);
                allMatches.add(m);
            }
        this.root = null;
    }

    // =========================================================================
    // SWISS SYSTEM
    // =========================================================================

    private void buildSwissSystem(Team[] teams) {
        this.totalRounds = Math.min(5, teams.length / 2);
        allMatches.clear();
        List<Team> shuffled = new ArrayList<>(Arrays.asList(teams));
        Collections.shuffle(shuffled);
        for (int r = 1; r <= totalRounds; r++) {
            for (int i = 0; i + 1 < shuffled.size(); i += 2) {
                Match m = new Match(r);
                m.setTeam1(shuffled.get(i));
                m.setTeam2(shuffled.get(i + 1));
                allMatches.add(m);
            }
            Collections.shuffle(shuffled);
        }
        this.root = null;
    }

    // =========================================================================
    // FREE FOR ALL
    // =========================================================================

    private void buildFreeForAll(Team[] teams) {
        this.totalRounds = 2;
        allMatches.clear();
        for (int i = 0; i < teams.length; i++)
            for (int j = i + 1; j < teams.length; j++) {
                Match m = new Match(1);
                m.setTeam1(teams[i]);
                m.setTeam2(teams[j]);
                allMatches.add(m);
            }
        this.root = null;
    }

    // =========================================================================
    // HELPER: auto-complete a bye match (one team present, other null)
    // Returns true if it was a bye.
    // =========================================================================

    private boolean autoCompleteBye(Match m) {
        if (m.getTeam1() != null && m.getTeam2() == null) {
            m.setWinner(m.getTeam1(), 1, 0);
            return true;
        }
        if (m.getTeam1() == null && m.getTeam2() != null) {
            m.setWinner(m.getTeam2(), 0, 1);
            return true;
        }
        return false;
    }

    // =========================================================================
    // QUERY
    // =========================================================================

    public List<Match> getMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match m : allMatches)
            if (m.getRound() == round) result.add(m);
        result.sort(Comparator.comparingInt(Match::getMatchId));
        return result;
    }

    public List<Match> getWinnersMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match m : allMatches)
            if (m.getRound() == round && m != grandFinals && m.isWinnersBracket())
                result.add(m);
        result.sort(Comparator.comparingInt(Match::getMatchId));
        return result;
    }

    public List<Match> getAllMatches()            { return allMatches; }
    public List<Match> getLosersBracketMatches()  { return losersBracketMatches; }
    public Match       getGrandFinals()           { return grandFinals; }
    public int         getTotalRounds()           { return totalRounds; }
    public Team[]      getTeams()                 { return teams; }
    public Match       getChampionship()          { return root; }
    public ScoreMatrix getScoreMatrix()           { return scoreMatrix; }

    public List<Match> getWinnersBracketMatches() {
        List<Match> r = new ArrayList<>();
        for (Match m : allMatches)
            if (m.isWinnersBracket() && m != grandFinals) r.add(m);
        return r;
    }

    public List<Match> getPendingMatches() {
        List<Match> r = new ArrayList<>();
        for (Match m : allMatches)
            if (!m.isCompleted() && m.getTeam1() != null && m.getTeam2() != null)
                r.add(m);
        return r;
    }

    public Match getCurrentMatch() {
        List<Match> p = getPendingMatches();
        return p.isEmpty() ? null : p.get(0);
    }

    public String getProgress() {
        int total = allMatches.size(), done = 0;
        for (Match m : allMatches) if (m.isCompleted()) done++;
        return done + "/" + total + " matches completed";
    }

    public Team getTournamentWinner() {
        if (tournamentType == TournamentType.ROUND_ROBIN
                || tournamentType == TournamentType.SWISS
                || tournamentType == TournamentType.FREE_FOR_ALL) {
            Team champ = null; int best = -1;
            for (Team t : teams) {
                if (t.getWins() > best) { best = t.getWins(); champ = t; }
                else if (t.getWins() == best && champ != null
                         && t.getPointDifference() > champ.getPointDifference())
                    champ = t;
            }
            return champ;
        }
        return (root != null && root.isCompleted()) ? root.getWinner() : null;
    }

    // =========================================================================
    // RECORD WINNER / PROPAGATION
    // =========================================================================

    public void recordWinner(Match match, Team winner, int score1, int score2) {
        if (match.getTeam1() == null || match.getTeam2() == null) {
            System.out.println("Error: Match missing a team!");
            return;
        }
        match.setWinner(winner, score1, score2);
        scoreMatrix.recordMatch(match.getTeam1().getId(), match.getTeam2().getId(), score1, score2);

        if (tournamentType == TournamentType.SINGLE_ELIMINATION
                || tournamentType == TournamentType.DOUBLE_ELIMINATION)
            propagateWinnerUp(match, winner);

        System.out.println("✓ Recorded: " + match);
    }

    private void propagateWinnerUp(Match currentMatch, Team winner) {
        Team loser = (currentMatch.getTeam1() == winner)
                     ? currentMatch.getTeam2() : currentMatch.getTeam1();

        // Feed loser into LB (double elim only, and only for real 2-team matches)
        if (tournamentType == TournamentType.DOUBLE_ELIMINATION && loser != null
                && currentMatch.getTeam1() != null && currentMatch.getTeam2() != null) {
            Match lbSlot = winnerToLoserMatch.get(currentMatch);
            if (lbSlot != null && !lbSlot.isCompleted()) {
                if (lbSlot.getTeam1() == null) {
                    lbSlot.setTeam1(loser);
                    System.out.println("→ Loser " + loser.getName() + " → LB R" + lbSlot.getRound());
                } else if (lbSlot.getTeam2() == null && lbSlot.getTeam1() != loser) {
                    lbSlot.setTeam2(loser);
                    System.out.println("→ Loser " + loser.getName() + " → LB R" + lbSlot.getRound());
                }
            }
        }

        // Advance winner to next match
        for (Match m : allMatches) {
            if (m.getLeftChild() == currentMatch) {
                m.setTeam1(winner);
                System.out.println("→ Winner " + winner.getName() + " → match " + m.getMatchId());
                if (m.isCompleted()) propagateWinnerUp(m, m.getWinner());
                return;
            } else if (m.getRightChild() == currentMatch) {
                m.setTeam2(winner);
                System.out.println("→ Winner " + winner.getName() + " → match " + m.getMatchId());
                if (m.isCompleted()) propagateWinnerUp(m, m.getWinner());
                return;
            }
        }
    }

    // =========================================================================
    // DEBUG
    // =========================================================================

    public void printBracket() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏆 TOURNAMENT - " + tournamentType.getDisplayName());
        System.out.println("=".repeat(60));
        if (tournamentType == TournamentType.DOUBLE_ELIMINATION) {
            int numRounds = (int) Math.ceil(Math.log(teams.length) / Math.log(2));
            System.out.println("\nWINNERS BRACKET:");
            for (int r = 1; r <= numRounds; r++) {
                List<Match> ms = getWinnersMatchesByRound(r);
                if (!ms.isEmpty()) { System.out.println("  R" + r + ":"); for (Match m : ms) System.out.println("    " + m); }
            }
            System.out.println("\nLOSERS BRACKET:");
            for (Match m : losersBracketMatches) System.out.println("  R" + m.getRound() + ": " + m);
            if (grandFinals != null) { System.out.println("\nGRAND FINAL:\n  " + grandFinals); }
        } else {
            for (int r = 1; r <= totalRounds; r++) {
                List<Match> ms = getMatchesByRound(r);
                if (!ms.isEmpty()) { System.out.println("  R" + r + ":"); for (Match m : ms) System.out.println("    " + m); }
            }
        }
    }

    public void printStandings() {
        System.out.println("\n📊 STANDINGS:");
        List<Team> sorted = new ArrayList<>(Arrays.asList(teams));
        sorted.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        for (int i = 0; i < sorted.size(); i++) {
            Team t = sorted.get(i);
            System.out.printf("%d. %-12s W:%d L:%d PD:%d Win%%:%.1f%%%n",
                    i+1, t.getName(), t.getWins(), t.getLosses(),
                    t.getPointDifference(), t.getWinPercentage());
        }
    }
}