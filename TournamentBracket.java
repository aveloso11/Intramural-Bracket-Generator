import java.util.*;

public class TournamentBracket {
    private Match root;
    private Team[] teams;
    private int totalRounds;
    private List<Match> allMatches;
    private TournamentType tournamentType;
    private List<Match> losersBracketMatches;
    private Match grandFinals;
    private Map<Match, Match> winnerToLoserMatch;
    private Map<Match, Match> lbWinnerAdvanceMap;  // maps each LB match → the next LB match its winner feeds into

    // ── Play-In specific fields ───────────────────────────────────────────────
    // playInMatches    : all matches inside the DE play-in sub-bracket
    // mainBracketMatches: semifinals + final that the bye teams wait in
    // byeTeams         : top N seeds who skip the play-in
    // playInSlots      : semifinal match slots waiting for play-in winners
    private List<Match> playInMatches;
    private List<Match> mainBracketMatches;
    private Team[]      byeTeams;
    private List<Match> playInSlots;   // semifinal matches in main bracket
    private int         playInWinnersRounds; // WB rounds inside the play-in

    public TournamentBracket(Team[] teams) {
        this(teams, TournamentType.SINGLE_ELIMINATION);
    }

    public TournamentBracket(Team[] teams, TournamentType type) {
        Match.resetIdCounter();   // always start match IDs fresh for each new bracket
        this.teams                = teams;
        this.tournamentType       = type;
        this.allMatches           = new ArrayList<>();
        this.winnerToLoserMatch   = new HashMap<>();
        this.lbWinnerAdvanceMap   = new HashMap<>();
        this.losersBracketMatches = new ArrayList<>();
        this.playInMatches        = new ArrayList<>();
        this.mainBracketMatches   = new ArrayList<>();
        this.playInSlots          = new ArrayList<>();

        if      (type == TournamentType.SINGLE_ELIMINATION)         buildSingleElimination(teams);
        else if (type == TournamentType.ROUND_ROBIN)                buildRoundRobin(teams);
        else if (type == TournamentType.DOUBLE_ELIMINATION)         buildDoubleElimination(teams);
        else if (type == TournamentType.SWISS)                      buildSwissSystem(teams);
        else if (type == TournamentType.FREE_FOR_ALL)               buildFreeForAll(teams);
        else if (type == TournamentType.PLAY_IN_DOUBLE_ELIMINATION)  buildPlayInDE(teams);
        else if (type == TournamentType.PLAY_IN_SINGLE_ELIMINATION)  buildPlayInSE(teams);
        else                                                         buildSingleElimination(teams);
    }

    public TournamentBracket(Team[] teams, String type) {
        this(teams, convertToTournamentType(type));
    }

    private static TournamentType convertToTournamentType(String type) {
        if (type == null) return TournamentType.SINGLE_ELIMINATION;
        switch (type) {
            case "Single Elimination":          return TournamentType.SINGLE_ELIMINATION;
            case "Double Elimination":          return TournamentType.DOUBLE_ELIMINATION;
            case "Round Robin":                 return TournamentType.ROUND_ROBIN;
            case "Swiss System":                return TournamentType.SWISS;
            case "Free For All":                return TournamentType.FREE_FOR_ALL;
            case "Play-In Double Elimination":  return TournamentType.PLAY_IN_DOUBLE_ELIMINATION;
            case "Play-In Single Elimination":  return TournamentType.PLAY_IN_SINGLE_ELIMINATION;
            default:                            return TournamentType.SINGLE_ELIMINATION;
        }
    }

    // =========================================================================
    // SINGLE ELIMINATION
    // =========================================================================

    private void buildSingleElimination(Team[] teams) {
        int n = teams.length;
        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        playInMatches        = new ArrayList<>();
        mainBracketMatches   = new ArrayList<>();
        playInSlots          = new ArrayList<>();
        winnerToLoserMatch.clear();

        int numRounds = (int) Math.ceil(Math.log(n) / Math.log(2));
        int fullSize  = (int) Math.pow(2, numRounds);

        // For standard power-of-2 counts (4, 8, 16, 32): fill all slots, no byes
        // For bye counts (10 → pad to 16, 20 → pad to 32): top seeds get byes
        Team[] slots = seededSlots(teams, fullSize);

        List<Match> currentRound = new ArrayList<>();
        int matchId = 1;

        for (int i = 0; i < fullSize; i += 2) {
            Team t1 = slots[i];
            Team t2 = slots[i + 1];
            if (t1 == null && t2 == null) continue;
            Match m = new Match(1);
            m.setMatchId(matchId++);
            if (t1 != null) m.setTeam1(t1);
            if (t2 != null) m.setTeam2(t2);
            if (!isPowerOfTwo(n)) autoCompleteBye(m); // only auto-advance byes for non-power-of-2
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
                if (currentRound.get(i).isCompleted())
                    parent.setTeam1(currentRound.get(i).getWinner());
                if (currentRound.get(i + 1).isCompleted())
                    parent.setTeam2(currentRound.get(i + 1).getWinner());
                next.add(parent);
                allMatches.add(parent);
            }
            currentRound = next;
            round++;
        }

        this.totalRounds = round - 1;
        this.root = currentRound.isEmpty() ? null : currentRound.get(0);
    }

    private boolean isPowerOfTwo(int n) { return n > 0 && (n & (n - 1)) == 0; }

    // =========================================================================
    // DOUBLE ELIMINATION  (power-of-2 only: 4, 8, 16, 32)
    // =========================================================================

    private void buildDoubleElimination(Team[] teams) {
        int n         = teams.length;
        int numRounds = (int)(Math.log(n) / Math.log(2));
        int fullSize  = n;

        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        winnerToLoserMatch.clear();
        lbWinnerAdvanceMap = new HashMap<>();

        int matchId       = 1;
        int losersMatchId = 1000;

        Team[] slots = seededSlots(teams, fullSize);

        // WB Round 1
        List<Match> wbR1 = new ArrayList<>();
        for (int i = 0; i < fullSize; i += 2) {
            Match m = new Match(1);
            m.setMatchId(matchId++);
            m.setIsWinnersBracket(true);
            m.setTeam1(slots[i]);
            m.setTeam2(slots[i + 1]);
            wbR1.add(m);
            allMatches.add(m);
        }

        List<List<Match>> wbRounds = new ArrayList<>();
        wbRounds.add(wbR1);
        List<Match> wbCurrent = new ArrayList<>(wbR1);

        for (int r = 2; r <= numRounds; r++) {
            List<Match> wbNext = new ArrayList<>();
            for (int i = 0; i + 1 < wbCurrent.size(); i += 2) {
                Match m = new Match(r);
                m.setMatchId(matchId++);
                m.setIsWinnersBracket(true);
                m.setLeftChild(wbCurrent.get(i));
                m.setRightChild(wbCurrent.get(i + 1));
                wbNext.add(m);
                allMatches.add(m);
            }
            wbRounds.add(wbNext);
            wbCurrent = wbNext;
        }
        Match wbFinal = wbCurrent.get(0);

        int lbRoundNum = numRounds;
        Map<Integer, List<Match>> lbRoundMap = new LinkedHashMap<>();

        lbRoundNum++;
        List<Match> lbElim1 = new ArrayList<>();
        List<Match> wbR1List = wbRounds.get(0);
        for (int i = 0; i + 1 < wbR1List.size(); i += 2) {
            Match lm = new Match(lbRoundNum);
            lm.setMatchId(losersMatchId++);
            winnerToLoserMatch.put(wbR1List.get(i),     lm);
            winnerToLoserMatch.put(wbR1List.get(i + 1), lm);
            lbElim1.add(lm);
            losersBracketMatches.add(lm);
            allMatches.add(lm);
        }
        lbRoundMap.put(lbRoundNum, lbElim1);

        List<Match> lbSurvivors = new ArrayList<>(lbElim1);

        for (int wr = 2; wr <= numRounds; wr++) {
            List<Match> wbThisRound = wbRounds.get(wr - 1);

            lbRoundNum++;
            List<Match> dropRound = new ArrayList<>();
            for (int i = 0; i < wbThisRound.size() && i < lbSurvivors.size(); i++) {
                Match lm = new Match(lbRoundNum);
                lm.setMatchId(losersMatchId++);
                lm.setLeftChild(lbSurvivors.get(i));
                winnerToLoserMatch.put(wbThisRound.get(i), lm);
                // Wire: winner of lbSurvivors[i] advances into lm as team2
                lbWinnerAdvanceMap.put(lbSurvivors.get(i), lm);
                dropRound.add(lm);
                losersBracketMatches.add(lm);
                allMatches.add(lm);
            }
            lbRoundMap.put(lbRoundNum, dropRound);

            if (dropRound.size() > 1) {
                lbRoundNum++;
                List<Match> elimRound = new ArrayList<>();
                for (int i = 0; i + 1 < dropRound.size(); i += 2) {
                    Match lm = new Match(lbRoundNum);
                    lm.setMatchId(losersMatchId++);
                    lm.setLeftChild(dropRound.get(i));
                    lm.setRightChild(dropRound.get(i + 1));
                    // Wire: winners of dropRound matches advance into lm
                    lbWinnerAdvanceMap.put(dropRound.get(i),     lm);
                    lbWinnerAdvanceMap.put(dropRound.get(i + 1), lm);
                    elimRound.add(lm);
                    losersBracketMatches.add(lm);
                    allMatches.add(lm);
                }
                lbRoundMap.put(lbRoundNum, elimRound);
                lbSurvivors = elimRound;
            } else {
                lbSurvivors = dropRound;
            }
        }

        Match lbFinal = lbSurvivors.isEmpty() ? null : lbSurvivors.get(0);

        lbRoundNum++;
        grandFinals = new Match(lbRoundNum);
        grandFinals.setMatchId(matchId);
        grandFinals.setLeftChild(wbFinal);
        if (lbFinal != null) grandFinals.setRightChild(lbFinal);
        allMatches.add(grandFinals);
        this.root        = grandFinals;
        this.totalRounds = lbRoundNum;

        System.out.println("DE built: n=" + n + " WBr=" + numRounds
            + " LBmatches=" + losersBracketMatches.size()
            + " total=" + allMatches.size());
    }

    private void buildPlayInDE(Team[] teams) {
        int n = teams.length;
        int numByeTeams;
        if      (n == 6)  numByeTeams = 2;
        else if (n == 10) numByeTeams = 2;
        else if (n == 12) numByeTeams = 4;
        else if (n == 20) numByeTeams = 4;
        else if (n == 24) numByeTeams = 8;
        else              numByeTeams = (n == 10) ? 2 : 4; 
        int playInSize   = n - numByeTeams;       

        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        playInMatches        = new ArrayList<>();
        mainBracketMatches   = new ArrayList<>();
        playInSlots          = new ArrayList<>();
        winnerToLoserMatch.clear();

        // Seeds 1-4 (index 0-3) get byes
        byeTeams = Arrays.copyOf(teams, numByeTeams);

        // Seeds 5-N (index 4-N-1) enter the DE play-in
        Team[] playInTeams = Arrays.copyOfRange(teams, numByeTeams, n);

        int matchId       = 1;
        int losersMatchId = 1000;

        // ── BUILD DE PLAY-IN ─────────────────────────────────────────────
        // Standard DE for playInTeams (power-of-2: 8 or 16)
        int piN      = playInTeams.length;
        int piRounds = (int)(Math.log(piN) / Math.log(2));
        Team[] piSlots = seededSlots(playInTeams, piN);

        // Play-In WB Round 1
        List<Match> piWbR1 = new ArrayList<>();
        for (int i = 0; i < piN; i += 2) {
            Match m = new Match(1);
            m.setMatchId(matchId++);
            m.setIsWinnersBracket(true);
            m.setIsPlayIn(true);
            m.setTeam1(piSlots[i]);
            m.setTeam2(piSlots[i + 1]);
            piWbR1.add(m);
            playInMatches.add(m);
            allMatches.add(m);
        }

        List<List<Match>> piWbRounds = new ArrayList<>();
        piWbRounds.add(piWbR1);
        List<Match> piWbCurrent = new ArrayList<>(piWbR1);

        for (int r = 2; r <= piRounds; r++) {
            List<Match> piWbNext = new ArrayList<>();
            for (int i = 0; i + 1 < piWbCurrent.size(); i += 2) {
                Match m = new Match(r);
                m.setMatchId(matchId++);
                m.setIsWinnersBracket(true);
                m.setIsPlayIn(true);
                m.setLeftChild(piWbCurrent.get(i));
                m.setRightChild(piWbCurrent.get(i + 1));
                piWbNext.add(m);
                playInMatches.add(m);
                allMatches.add(m);
            }
            piWbRounds.add(piWbNext);
            piWbCurrent = piWbNext;
        }
        Match piWbFinal = piWbCurrent.get(0);
        this.playInWinnersRounds = piRounds;

        // Play-In Losers Bracket
        int lbRoundNum = piRounds;

        lbRoundNum++;
        List<Match> piLbElim1 = new ArrayList<>();
        List<Match> piWbR1List = piWbRounds.get(0);
        for (int i = 0; i + 1 < piWbR1List.size(); i += 2) {
            Match lm = new Match(lbRoundNum);
            lm.setMatchId(losersMatchId++);
            lm.setIsPlayIn(true);
            winnerToLoserMatch.put(piWbR1List.get(i),     lm);
            winnerToLoserMatch.put(piWbR1List.get(i + 1), lm);
            piLbElim1.add(lm);
            losersBracketMatches.add(lm);
            playInMatches.add(lm);
            allMatches.add(lm);
        }

        List<Match> piLbSurvivors = new ArrayList<>(piLbElim1);

        for (int wr = 2; wr <= piRounds; wr++) {
            List<Match> wbThisRound = piWbRounds.get(wr - 1);

            lbRoundNum++;
            List<Match> dropRound = new ArrayList<>();
            for (int i = 0; i < wbThisRound.size() && i < piLbSurvivors.size(); i++) {
                Match lm = new Match(lbRoundNum);
                lm.setMatchId(losersMatchId++);
                lm.setIsPlayIn(true);
                lm.setLeftChild(piLbSurvivors.get(i));
                winnerToLoserMatch.put(wbThisRound.get(i), lm);
                dropRound.add(lm);
                losersBracketMatches.add(lm);
                playInMatches.add(lm);
                allMatches.add(lm);
            }

            if (dropRound.size() > 1) {
                lbRoundNum++;
                List<Match> elimRound = new ArrayList<>();
                for (int i = 0; i + 1 < dropRound.size(); i += 2) {
                    Match lm = new Match(lbRoundNum);
                    lm.setMatchId(losersMatchId++);
                    lm.setIsPlayIn(true);
                    lm.setLeftChild(dropRound.get(i));
                    lm.setRightChild(dropRound.get(i + 1));
                    elimRound.add(lm);
                    losersBracketMatches.add(lm);
                    playInMatches.add(lm);
                    allMatches.add(lm);
                }
                piLbSurvivors = elimRound;
            } else {
                piLbSurvivors = dropRound;
            }
        }
        Match piLbFinal = piLbSurvivors.isEmpty() ? null : piLbSurvivors.get(0);

        // Play-In Grand Final: WB finalist vs LB finalist
        lbRoundNum++;
        Match piGrandFinal = new Match(lbRoundNum);
        piGrandFinal.setMatchId(losersMatchId++);
        piGrandFinal.setIsPlayIn(true);
        piGrandFinal.setIsPlayInGrandFinal(true);
        piGrandFinal.setLeftChild(piWbFinal);
        if (piLbFinal != null) piGrandFinal.setRightChild(piLbFinal);
        playInMatches.add(piGrandFinal);
        allMatches.add(piGrandFinal);

        // ── BUILD MAIN BRACKET ───────────────────────────────────────────────
        // Main bracket is a standard SE with (numByeTeams * 2) slots:
        //   numByeTeams bye seeds (pre-seeded as team1) + numByeTeams play-in survivors (fill team2)
        // For 10-team (2 byes): 4-slot SE → 2 semis → 1 final
        // For 20-team (4 byes): 8-slot SE → 4 semis → 2 FF → 1 champ

        int mainRoundBase = lbRoundNum + 1;
        int mainSize      = numByeTeams * 2;   // 4 or 8

        // Build round-1 main bracket matches — each pairs 1 bye seed vs 1 PI survivor (TBD)
        // Seeding: top seed faces weakest survivor (reverse pairing)
        List<Match> mainCurrent = new ArrayList<>();
        playInSlots = new ArrayList<>();

        // byePairing maps match index → bye seed index (best seed vs worst survivor)
        // For 2 byes: match0 = seed1 vs PI-survivor, match1 = seed2 vs PI-survivor
        // For 4 byes: match0 = seed1, match1 = seed4, match2 = seed2, match3 = seed3
        // For 8 byes: match0=s1, match1=s8, match2=s4, match3=s5, match4=s2, match5=s7, match6=s3, match7=s6
        int[] byePairing;
        if (numByeTeams == 2) {
            byePairing = new int[]{0, 1};
        } else if (numByeTeams == 4) {
            byePairing = new int[]{0, 3, 1, 2};
        } else if (numByeTeams == 8) {
            byePairing = new int[]{0, 7, 3, 4, 1, 6, 2, 5};
        } else {
            byePairing = new int[numByeTeams];
            for (int i = 0; i < numByeTeams; i++) byePairing[i] = i;
        }

        for (int i = 0; i < numByeTeams; i++) {
            Match sf = new Match(mainRoundBase);
            sf.setMatchId(matchId++);
            sf.setIsMainBracket(true);
            sf.setTeam1(byeTeams[byePairing[i]]); // bye seed waits here
            // team2 filled by play-in survivor
            mainCurrent.add(sf);
            playInSlots.add(sf);
            mainBracketMatches.add(sf);
            allMatches.add(sf);
        }

        // Wire play-in survivors → main bracket round-1 slots
        // For 2-bye: piGrandFinal → slot0, piLbFinal → slot1
        // For 4-bye: piGrandFinal → slot0, piLbFinal → slot1, + 2 more from LB semis
        // For 8-bye: piGrandFinal → slot0, piLbFinal → slot1, + 6 more from LB brackets
        piGrandFinal.setMainBracketSlot(mainCurrent.get(0));
        if (piLbFinal != null && mainCurrent.size() > 1)
            piLbFinal.setMainBracketSlot(mainCurrent.get(1));

        if (numByeTeams >= 4) {
            // Wire additional LB exits → remaining slots
            wireAdditionalPlayInSurvivorSlots(piWbRounds, piLbSurvivors, mainCurrent, piGrandFinal, piLbFinal);
        }

        // Build remaining main bracket rounds (standard SE from mainCurrent)
        int mainRound = mainRoundBase + 1;
        while (mainCurrent.size() > 1) {
            List<Match> mainNext = new ArrayList<>();
            for (int i = 0; i + 1 < mainCurrent.size(); i += 2) {
                Match m = new Match(mainRound);
                m.setMatchId(matchId++);
                m.setIsMainBracket(true);
                m.setLeftChild(mainCurrent.get(i));
                m.setRightChild(mainCurrent.get(i + 1));
                if (mainCurrent.get(i).isCompleted())
                    m.setTeam1(mainCurrent.get(i).getWinner());
                if (mainCurrent.get(i + 1).isCompleted())
                    m.setTeam2(mainCurrent.get(i + 1).getWinner());
                mainNext.add(m);
                mainBracketMatches.add(m);
                allMatches.add(m);
            }
            mainCurrent = mainNext;
            mainRound++;
        }

        grandFinals = mainCurrent.isEmpty() ? null : mainCurrent.get(0);
        this.root        = grandFinals;
        this.totalRounds = mainRound - 1;

        System.out.println("Play-In DE built: n=" + n
            + " byeTeams=" + numByeTeams
            + " playInSize=" + playInSize
            + " mainSize=" + mainSize
            + " playInMatches=" + playInMatches.size()
            + " mainMatches=" + mainBracketMatches.size()
            + " total=" + allMatches.size());
    }

    /**
     * Wires the remaining play-in survivor slots (mainR1[2] and mainR1[3])
     * to the correct play-in bracket exits. Only called for the 4-bye (20-team) case.
     *
     * For a 16-team play-in we need 4 survivors:
     *   - piWbFinal winner  (slot 0) ← already wired
     *   - piLbFinal winner  (slot 1) ← already wired
     *   - LB semi exits     (slots 2, 3) ← wired here
     */
    private void wireAdditionalPlayInSurvivorSlots(
            List<List<Match>> piWbRounds,
            List<Match> finalLbSurvivors,
            List<Match> semiFinals,
            Match piGrandFinal,
            Match piLbFinal) {

        // For an 8-team play-in (3 WB rounds), there are exactly 2 exits:
        // WB final winner + LB final winner → fills slots 0 and 1.
        // Slots 2 and 3 need to come from the SEMI-final round of the play-in.

        // Collect all play-in LB matches sorted by round descending
        List<Match> lbSorted = new ArrayList<>(losersBracketMatches);
        lbSorted.removeIf(m -> !m.isPlayIn());
        lbSorted.sort((a, b) -> Integer.compare(b.getRound(), a.getRound()));

        // The LB final is the first; the LB semi-finals are the next tier
        // We need 2 more survivors for semiFinals[2] and semiFinals[3]
        // These come from the LB one round before the LB final
        if (semiFinals.size() >= 4) {
            // Find LB matches that feed into piLbFinal
            List<Match> lbSemiSlots = new ArrayList<>();
            for (Match lm : lbSorted) {
                if (lm == piLbFinal) continue;
                if (piLbFinal != null) {
                    if (lm == piLbFinal.getLeftChild() || lm == piLbFinal.getRightChild()) {
                        lbSemiSlots.add(lm);
                    }
                }
            }

            // For 16-team play-in there are 2 LB semi matches → feed slots 2 and 3
            if (lbSemiSlots.size() >= 2) {
                lbSemiSlots.get(0).setMainBracketSlot(semiFinals.get(2));
                lbSemiSlots.get(1).setMainBracketSlot(semiFinals.get(3));
            } else if (lbSemiSlots.size() == 1) {
                // 8-team play-in: only 2 true survivors; use WB semifinal exits
                List<Match> wbSemis = piWbRounds.size() >= 2
                        ? piWbRounds.get(piWbRounds.size() - 2) : new ArrayList<>();
                if (wbSemis.size() >= 2) {
                    wbSemis.get(0).setMainBracketSlot(semiFinals.get(2));
                    wbSemis.get(1).setMainBracketSlot(semiFinals.get(3));
                } else if (wbSemis.size() == 1) {
                    wbSemis.get(0).setMainBracketSlot(semiFinals.get(2));
                    lbSemiSlots.get(0).setMainBracketSlot(semiFinals.get(3));
                }
            }
        }
    }

    // =========================================================================
    // PLAY-IN SINGLE ELIMINATION
    //
    //  Odd/non-power-of-2 team counts (12, 20, 24) use a play-in stage to
    //  reduce to the nearest power-of-2, then run a standard SE main bracket.
    //
    //  12 teams → next power-of-2 below is 8.  4 teams play-in (2 matches),
    //             4 winners join 8 bye seeds for an 8-team SE main bracket.
    //             Actually: 12 = 8 + 4 extra.  We give 4 byes to top seeds,
    //             play-in the bottom 8 to produce 4 survivors → 8-team SE.
    //
    //  20 teams → 16-team main bracket.  4 extra teams need a play-in.
    //             Top 16 get byes; bottom 4 play-in to produce 2 survivors
    //             who fill the last 2 slots of a 16-team SE bracket.
    //             Simpler: top 12 bye, bottom 8 play-in → 4 survivors → 16-team SE.
    //
    //  24 teams → 16-team main bracket.  Top 8 bye, bottom 16 play-in → 8 survivors.
    //
    //  General rule:
    //    mainSize  = largest power-of-2 ≤ n that satisfies mainSize >= n/2
    //    byeCount  = mainSize - (n - mainSize)   [seeds that skip play-in]
    //    playInCount = n - byeCount              [teams that must play-in]
    //    survivors = mainSize - byeCount         [play-in winners needed]
    //    playInRounds = log2(playInCount/survivors)  [SE rounds in play-in]
    // =========================================================================

    private void buildPlayInSE(Team[] teams) {
        int n = teams.length;

        // Special cases for 10-team and 20-team play-in SE:
        //   10 teams → top 2 bye, 8 play-in → 2 survivors → 4-team main (Semis + Final)
        //   20 teams → top 4 bye, 16 play-in → 4 survivors → 8-team main (QF + Semis + Final)
        int mainSize, byeCount, playInCount, survivors;
        if (n == 10) {
            mainSize    = 4;
            byeCount    = 2;
            playInCount = 8;
            survivors   = 2;
        } else if (n == 20) {
            mainSize    = 8;
            byeCount    = 4;
            playInCount = 16;
            survivors   = 4;
        } else {
            // General formula: smallest power-of-2 >= ceil(n/2)
            mainSize = 1;
            while (mainSize < (int) Math.ceil(n / 2.0)) mainSize <<= 1;
            survivors   = mainSize - (n - mainSize); // 2*mainSize - n
            byeCount    = survivors;
            playInCount = n - byeCount;
        }

        allMatches.clear();
        losersBracketMatches = new ArrayList<>();
        playInMatches        = new ArrayList<>();
        mainBracketMatches   = new ArrayList<>();
        playInSlots          = new ArrayList<>();
        winnerToLoserMatch.clear();

        byeTeams = Arrays.copyOf(teams, byeCount);
        Team[] playInTeams = Arrays.copyOfRange(teams, byeCount, n);

        int matchId = 1;

        // ── PLAY-IN: standard single elimination ────────────────────────────
        // playInTeams.length is always a power-of-2 and == survivors * 2
        Team[] piSlots = seededSlots(playInTeams, playInTeams.length);

        List<Match> piCurrent = new ArrayList<>();
        for (int i = 0; i < playInTeams.length; i += 2) {
            Match m = new Match(1);
            m.setMatchId(matchId++);
            m.setIsWinnersBracket(true);
            m.setIsPlayIn(true);
            m.setTeam1(piSlots[i]);
            m.setTeam2(piSlots[i + 1]);
            piCurrent.add(m);
            playInMatches.add(m);
            allMatches.add(m);
        }

        int piRound = 2;
        while (piCurrent.size() > survivors) {
            List<Match> piNext = new ArrayList<>();
            for (int i = 0; i + 1 < piCurrent.size(); i += 2) {
                Match m = new Match(piRound);
                m.setMatchId(matchId++);
                m.setIsWinnersBracket(true);
                m.setIsPlayIn(true);
                m.setLeftChild(piCurrent.get(i));
                m.setRightChild(piCurrent.get(i + 1));
                piNext.add(m);
                playInMatches.add(m);
                allMatches.add(m);
            }
            piCurrent = piNext;
            piRound++;
        }
        // piCurrent now holds exactly 'survivors' play-in final matches

        int piRoundsUsed = piRound - 1;

        // ── MAIN BRACKET ────────────────────────────────────────────────────
        // Slots: bye seeds first, then play-in survivors
        // We build a seeded 'mainSize'-slot array:
        //   slots[0..byeCount-1]   = bye seeds (top seeds)
        //   slots[byeCount..]      = null (filled by play-in survivors later)
        Team[] mainSlotTeams = new Team[mainSize];
        for (int i = 0; i < byeCount; i++) mainSlotTeams[i] = byeTeams[i];
        // play-in survivor slots remain null

        // Use seeded bracket ordering for mainSize
        int[] seedOrder = buildSeedOrder(mainSize);
        // Map seed position → mainSlotTeam (null = play-in survivor TBD)
        Team[] orderedSlots = new Team[mainSize];
        for (int i = 0; i < mainSize; i++) {
            int s = seedOrder[i];
            orderedSlots[i] = (s <= mainSize && s - 1 < mainSlotTeams.length)
                               ? mainSlotTeams[s - 1] : null;
        }

        // Collect which ordered-slot indices are null (play-in survivor slots)
        List<Integer> piSlotIndices = new ArrayList<>();
        for (int i = 0; i < mainSize; i++) {
            if (orderedSlots[i] == null) piSlotIndices.add(i);
        }

        int mainRound = piRoundsUsed + 1;

        // Build main bracket round 1 matches
        List<Match> mainCurrent = new ArrayList<>();
        List<Match> mainR1 = new ArrayList<>();
        // We track which R1 matches have a play-in survivor slot
        Map<Integer, Match> piSlotIndexToMatch = new HashMap<>();

        for (int i = 0; i < mainSize; i += 2) {
            Match m = new Match(mainRound);
            m.setMatchId(matchId++);
            m.setIsMainBracket(true);
            Team t1 = orderedSlots[i];
            Team t2 = orderedSlots[i + 1];
            if (t1 != null) m.setTeam1(t1);
            if (t2 != null) m.setTeam2(t2);
            // Track null slots for wiring play-in survivors
            if (t1 == null) piSlotIndexToMatch.put(i, m);     // team1 slot
            if (t2 == null) piSlotIndexToMatch.put(i + 1, m); // team2 slot
            // Do NOT autoCompleteBye here — team2 slot is still waiting for a play-in survivor
            mainCurrent.add(m);
            mainR1.add(m);
            mainBracketMatches.add(m);
            allMatches.add(m);
        }
        mainRound++;

        // Wire play-in survivor matches → main bracket R1 slots
        // piSlotIndices[k] → piCurrent[k] (kth survivor match fills kth null slot)
        for (int k = 0; k < piSlotIndices.size() && k < piCurrent.size(); k++) {
            int slotIdx = piSlotIndices.get(k);
            Match piWinner = piCurrent.get(k);
            Match target = piSlotIndexToMatch.get(slotIdx);
            if (target != null) {
                piWinner.setMainBracketSlot(target);
                // Mark which "team" slot (1 or 2) this survivor fills
                // We store it as team1 or team2 being null
                playInSlots.add(target);
            }
        }

        // Build remaining main bracket rounds
        while (mainCurrent.size() > 1) {
            List<Match> mainNext = new ArrayList<>();
            for (int i = 0; i + 1 < mainCurrent.size(); i += 2) {
                Match m = new Match(mainRound);
                m.setMatchId(matchId++);
                m.setIsMainBracket(true);
                m.setLeftChild(mainCurrent.get(i));
                m.setRightChild(mainCurrent.get(i + 1));
                if (mainCurrent.get(i).isCompleted())
                    m.setTeam1(mainCurrent.get(i).getWinner());
                if (mainCurrent.get(i + 1).isCompleted())
                    m.setTeam2(mainCurrent.get(i + 1).getWinner());
                // Do NOT autoCompleteBye — both slots must be earned, not auto-advanced
                mainNext.add(m);
                mainBracketMatches.add(m);
                allMatches.add(m);
            }
            mainCurrent = mainNext;
            mainRound++;
        }

        grandFinals = mainCurrent.isEmpty() ? null : mainCurrent.get(0);
        this.root        = grandFinals;
        this.totalRounds = mainRound - 1;

        System.out.println("Play-In SE built: n=" + n
            + " byeCount=" + byeCount
            + " playInCount=" + playInCount
            + " survivors=" + survivors
            + " mainSize=" + mainSize
            + " playInMatches=" + playInMatches.size()
            + " mainMatches=" + mainBracketMatches.size()
            + " total=" + allMatches.size());
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
    // SEEDING HELPERS
    // =========================================================================

    private Team[] seededSlots(Team[] teams, int bracketSize) {
        int[] seedInSlot = buildSeedOrder(bracketSize);
        Team[] slots = new Team[bracketSize];
        for (int i = 0; i < bracketSize; i++) {
            int seedNum = seedInSlot[i];
            if (seedNum <= teams.length) slots[i] = teams[seedNum - 1];
        }
        return slots;
    }

    private int[] buildSeedOrder(int size) {
        List<Integer> result = buildSeedOrderList(size);
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = result.get(i);
        return arr;
    }

    private List<Integer> buildSeedOrderList(int n) {
        if (n == 1) {
            List<Integer> base = new ArrayList<>();
            base.add(1);
            return base;
        }
        List<Integer> half = buildSeedOrderList(n / 2);
        List<Integer> result = new ArrayList<>();
        for (int h : half) {
            result.add(h);
            result.add(n + 1 - h);
        }
        return result;
    }

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

    /** Returns only the WB matches inside the play-in sub-bracket for the given round. */
    public List<Match> getPlayInWinnersMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match m : playInMatches)
            if (m.getRound() == round && m.isWinnersBracket() && !m.isPlayInGrandFinal())
                result.add(m);
        result.sort(Comparator.comparingInt(Match::getMatchId));
        return result;
    }

    /** Returns LB matches inside the play-in sub-bracket for a given round. */
    public List<Match> getPlayInLosersMatchesByRound(int round) {
        List<Match> result = new ArrayList<>();
        for (Match m : playInMatches)
            if (m.getRound() == round && !m.isWinnersBracket() && !m.isPlayInGrandFinal())
                result.add(m);
        result.sort(Comparator.comparingInt(Match::getMatchId));
        return result;
    }

    public Match       getPlayInGrandFinal() {
        for (Match m : playInMatches) if (m.isPlayInGrandFinal()) return m;
        return null;
    }

    public List<Match> getAllMatches()            { return allMatches; }
    public List<Match> getLosersBracketMatches()  { return losersBracketMatches; }
    public List<Match> getPlayInMatches()         { return playInMatches; }
    public List<Match> getMainBracketMatches()    { return mainBracketMatches; }
    public Team[]      getByeTeams()              { return byeTeams; }
    public Match       getGrandFinals()           { return grandFinals; }
    public int         getTotalRounds()           { return totalRounds; }
    public Team[]      getTeams()                 { return teams; }
    public Match       getChampionship()          { return root; }
   
    public int         getPlayInWinnersRounds()   { return playInWinnersRounds; }
    public int         getWinnersRounds() {
        return (int) Math.ceil(Math.log(Math.max(teams.length, 2)) / Math.log(2));
    }

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

    public boolean isPlayInFormat() {
        return tournamentType == TournamentType.PLAY_IN_DOUBLE_ELIMINATION
            || tournamentType == TournamentType.PLAY_IN_SINGLE_ELIMINATION;
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
        if (root == null || !root.isCompleted()) return null;
        if (tournamentType == TournamentType.DOUBLE_ELIMINATION
                || tournamentType == TournamentType.PLAY_IN_DOUBLE_ELIMINATION
                || tournamentType == TournamentType.PLAY_IN_SINGLE_ELIMINATION) {
            if (grandFinals == null) return null;
            if (grandFinals.getTeam1() == null || grandFinals.getTeam2() == null) return null;
        }
        if (root.getTeam1() == null || root.getTeam2() == null) return null;
        return root.getWinner();
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

        if (tournamentType == TournamentType.SINGLE_ELIMINATION
                || tournamentType == TournamentType.DOUBLE_ELIMINATION)
            propagateWinnerUp(match, winner);

        if (tournamentType == TournamentType.PLAY_IN_DOUBLE_ELIMINATION
                || tournamentType == TournamentType.PLAY_IN_SINGLE_ELIMINATION)
            propagatePlayIn(match, winner);

        System.out.println("✓ Recorded: " + match);
    }

    private void propagateWinnerUp(Match currentMatch, Team winner) {
        Team loser = (currentMatch.getTeam1() == winner)
                     ? currentMatch.getTeam2() : currentMatch.getTeam1();

        // ── Feed loser into LB (WB matches only) ─────────────────────────────
        if (tournamentType == TournamentType.DOUBLE_ELIMINATION && loser != null
                && currentMatch.getTeam1() != null && currentMatch.getTeam2() != null) {
            Match lbSlot = winnerToLoserMatch.get(currentMatch);
            if (lbSlot != null && !lbSlot.isCompleted()) {
                if (lbSlot.getTeam1() == null) {
                    lbSlot.setTeam1(loser);
                } else if (lbSlot.getTeam2() == null && lbSlot.getTeam1() != loser) {
                    lbSlot.setTeam2(loser);
                }
            }
        }

        // ── Advance winner via lbWinnerAdvanceMap (LB drop/elim rounds) ──────
        Match nextLb = lbWinnerAdvanceMap.get(currentMatch);
        if (nextLb != null && !nextLb.isCompleted()) {
            if (nextLb.getTeam2() == null) {
                nextLb.setTeam2(winner);
                System.out.println("→ LB Winner " + winner.getName() + " → match " + nextLb.getMatchId() + " (team2)");
            } else if (nextLb.getTeam1() == null) {
                nextLb.setTeam1(winner);
                System.out.println("→ LB Winner " + winner.getName() + " → match " + nextLb.getMatchId() + " (team1)");
            }
            return;
        }

        // ── Advance winner via child links (WB rounds + GF) ───────────────────
        for (Match m : allMatches) {
            if (m.getLeftChild() == currentMatch) {
                m.setTeam1(winner);
                if (m.isCompleted()) propagateWinnerUp(m, m.getWinner());
                return;
            } else if (m.getRightChild() == currentMatch) {
                m.setTeam2(winner);
                if (m.isCompleted()) propagateWinnerUp(m, m.getWinner());
                return;
            }
        }
    }

    /**
     * Propagation for Play-In DE format.
     *
     * Three paths:
     *  1. Play-in WB/LB match → loser goes to LB slot (same as standard DE)
     *  2. Play-in match winner advances to next play-in match via child refs
     *  3. Play-in survivor (GF winner or LB final winner) → fills a main bracket semifinal slot
     */
    private void propagatePlayIn(Match currentMatch, Team winner) {
        Team loser = (currentMatch.getTeam1() == winner)
                     ? currentMatch.getTeam2() : currentMatch.getTeam1();

        // Feed loser into play-in LB if applicable
        if (loser != null && currentMatch.isPlayIn() && !currentMatch.isPlayInGrandFinal()) {
            Match lbSlot = winnerToLoserMatch.get(currentMatch);
            if (lbSlot != null && !lbSlot.isCompleted()) {
                if (lbSlot.getTeam1() == null) {
                    lbSlot.setTeam1(loser);
                    System.out.println("→ PI Loser " + loser.getName() + " → LB R" + lbSlot.getRound());
                } else if (lbSlot.getTeam2() == null && lbSlot.getTeam1() != loser) {
                    lbSlot.setTeam2(loser);
                    System.out.println("→ PI Loser " + loser.getName() + " → LB R" + lbSlot.getRound());
                }
            }
        }

        // Advance winner to next play-in match via child links
        for (Match m : playInMatches) {
            if (m.getLeftChild() == currentMatch && m.getTeam1() == null) {
                m.setTeam1(winner);
                System.out.println("→ PI Winner " + winner.getName() + " → PI match " + m.getMatchId());
                if (m.isCompleted()) propagatePlayIn(m, m.getWinner());
                return;
            } else if (m.getRightChild() == currentMatch && m.getTeam2() == null) {
                m.setTeam2(winner);
                System.out.println("→ PI Winner " + winner.getName() + " → PI match " + m.getMatchId());
                if (m.isCompleted()) propagatePlayIn(m, m.getWinner());
                return;
            }
        }

        // If this match has a main bracket slot, send winner there
        Match mainSlot = currentMatch.getMainBracketSlot();
        if (mainSlot != null) {
            if (mainSlot.getTeam1() == null) {
                mainSlot.setTeam1(winner);
                System.out.println("→ PI Survivor " + winner.getName()
                    + " → Main match " + mainSlot.getMatchId() + " (team1)");
            } else if (mainSlot.getTeam2() == null) {
                mainSlot.setTeam2(winner);
                System.out.println("→ PI Survivor " + winner.getName()
                    + " → Main match " + mainSlot.getMatchId() + " (team2)");
            }
            // Once main bracket match has both teams, propagate inside main bracket
            propagateMainBracket(mainSlot);
            return;
        }

        // Advance winner inside main bracket via child links
        for (Match m : mainBracketMatches) {
            if (m.getLeftChild() == currentMatch) {
                m.setTeam1(winner);
                propagateMainBracket(m);
                return;
            } else if (m.getRightChild() == currentMatch) {
                m.setTeam2(winner);
                propagateMainBracket(m);
                return;
            }
        }
    }

    /** Propagates a completed main-bracket match forward to the next main-bracket match. */
    private void propagateMainBracket(Match current) {
        if (!current.isCompleted()) return;
        Team w = current.getWinner();
        for (Match m : mainBracketMatches) {
            if (m.getLeftChild() == current && m.getTeam1() == null) {
                m.setTeam1(w);
                propagateMainBracket(m);
                return;
            } else if (m.getRightChild() == current && m.getTeam2() == null) {
                m.setTeam2(w);
                propagateMainBracket(m);
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
            if (grandFinals != null) System.out.println("\nGRAND FINAL:\n  " + grandFinals);
        } else if (tournamentType == TournamentType.PLAY_IN_DOUBLE_ELIMINATION
                || tournamentType == TournamentType.PLAY_IN_SINGLE_ELIMINATION) {
            String tag = tournamentType == TournamentType.PLAY_IN_SINGLE_ELIMINATION
                         ? "PLAY-IN BRACKET (SE)" : "PLAY-IN BRACKET (DE)";
            System.out.println("\n── " + tag + " ──");
            for (Match m : playInMatches) System.out.println("  R" + m.getRound() + ": " + m);
            System.out.println("\n── MAIN BRACKET ──");
            for (Match m : mainBracketMatches) System.out.println("  R" + m.getRound() + ": " + m);
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