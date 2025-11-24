package to.mpm.minigames.manager;

import com.badlogic.gdx.Gdx;
import to.mpm.network.NetworkManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton manager for game flow across multiple rounds.
 * Tracks round progression, accumulated scores, and determines finale eligibility.
 * Host-authoritative: only the host should call mutating methods (endRound, etc.).
 */
public class GameFlowManager {
    private static GameFlowManager instance;

    private int currentRound; //!< current round number (1-based)
    private int totalRounds; //!< total rounds configured for this game
    private final Map<Integer, Integer> accumulatedScores; //!< playerId -> total score across all rounds
    private boolean initialized; //!< whether initialize() has been called

    private GameFlowManager() {
        this.accumulatedScores = new HashMap<>();
        this.initialized = false;
    }

    /**
     * Gets the singleton instance.
     * 
     * @return the GameFlowManager instance
     */
    public static GameFlowManager getInstance() {
        if (instance == null) {
            instance = new GameFlowManager();
        }
        return instance;
    }

    /**
     * Initializes the game flow for a new session.
     * Should be called by the host when starting the game.
     * Registers necessary network packet classes.
     * 
     * @param rounds total number of rounds to play (must be >= 2)
     */
    public void initialize(int rounds) {
        if (rounds < 2) {
            throw new IllegalArgumentException("Rounds must be at least 2 (rounds - 1 normal + 1 finale)");
        }

        this.totalRounds = rounds;
        this.currentRound = 0;
        this.accumulatedScores.clear();
        this.initialized = true;

        // Register packet classes for serialization
        NetworkManager.getInstance().registerAdditionalClasses(
            ManagerPackets.RoomConfig.class,
            ManagerPackets.ShowScoreboard.class,
            ManagerPackets.StartNextRound.class,
            ManagerPackets.ShowResults.class,
            ManagerPackets.ReturnToLobby.class,
            HashMap.class,
            ArrayList.class
        );

        Gdx.app.log("GameFlowManager", "Initialized with " + rounds + " rounds");
    }

    /**
     * Starts a new round.
     * Increments the current round counter.
     */
    public void startRound() {
        if (!initialized) {
            Gdx.app.error("GameFlowManager", "Cannot start round: not initialized!");
            return;
        }
        currentRound++;
        Gdx.app.log("GameFlowManager", "Starting round " + currentRound + "/" + totalRounds);
    }

    /**
     * Ends the current round and merges scores.
     * Should be called by the host when a minigame finishes.
     * 
     * @param roundScores the scores from the minigame that just finished (playerId -> score)
     */
    public void endRound(Map<Integer, Integer> roundScores) {
        if (!initialized) {
            Gdx.app.error("GameFlowManager", "Cannot end round: not initialized!");
            return;
        }

        if (roundScores != null) {
            for (Map.Entry<Integer, Integer> entry : roundScores.entrySet()) {
                int playerId = entry.getKey();
                int score = entry.getValue();
                accumulatedScores.merge(playerId, score, Integer::sum);
            }
        }

        Gdx.app.log("GameFlowManager", "Round " + currentRound + " ended. Scores updated.");
    }

    /**
     * Checks if the finale should be played next.
     * The finale is played on the last round.
     * 
     * @return true if the next round should be the finale
     */
    public boolean shouldPlayFinale() {
        return initialized && currentRound == totalRounds - 1;
    }

    /**
     * Determines which players should participate in the finale.
     * Returns the top 30% of players (minimum 2).
     * If there's a tie at the cutoff, randomly includes one of the tied players.
     * 
     * @return list of player IDs eligible for the finale, sorted by score descending
     */
    public List<Integer> getFinalePlayerIds() {
        if (accumulatedScores.isEmpty()) {
            Gdx.app.log("GameFlowManager", "No scores available for finale filtering");
            return new ArrayList<>();
        }

        // Sort players by score descending
        List<Map.Entry<Integer, Integer>> sortedPlayers = accumulatedScores.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());

        int totalPlayers = sortedPlayers.size();
        int finaleCount = Math.max(2, (int) Math.ceil(totalPlayers * 0.3));

        // Handle edge case: if only 1 player, just return them
        if (totalPlayers == 1) {
            Gdx.app.log("GameFlowManager", "Only 1 player remaining, skipping finale");
            return sortedPlayers.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        }

        // Ensure we don't select more players than exist
        finaleCount = Math.min(finaleCount, totalPlayers);

        // Get the cutoff score
        int cutoffScore = sortedPlayers.get(finaleCount - 1).getValue();

        // Separate qualified players and tied players
        List<Integer> qualifiedPlayers = new ArrayList<>();
        List<Integer> tiedPlayers = new ArrayList<>();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Map.Entry<Integer, Integer> entry = sortedPlayers.get(i);
            if (i < finaleCount - 1) {
                // Definitely in
                qualifiedPlayers.add(entry.getKey());
            } else if (entry.getValue() > cutoffScore) {
                // Also definitely in (higher than cutoff)
                qualifiedPlayers.add(entry.getKey());
            } else if (entry.getValue() == cutoffScore) {
                // Tied at cutoff
                tiedPlayers.add(entry.getKey());
            }
        }

        // Randomly select from tied players to fill remaining spots
        int remainingSpots = finaleCount - qualifiedPlayers.size();
        Collections.shuffle(tiedPlayers, new Random());
        for (int i = 0; i < Math.min(remainingSpots, tiedPlayers.size()); i++) {
            qualifiedPlayers.add(tiedPlayers.get(i));
        }

        Gdx.app.log("GameFlowManager", "Finale participants: " + qualifiedPlayers.size() + " out of " + totalPlayers);
        return qualifiedPlayers;
    }

    /**
     * Checks if the game is complete (all rounds played).
     * 
     * @return true if all rounds have been played
     */
    public boolean isGameComplete() {
        return initialized && currentRound >= totalRounds;
    }

    /**
     * Gets the accumulated scores for all players.
     * 
     * @return map of playerId to total score (defensive copy)
     */
    public Map<Integer, Integer> getTotalScores() {
        return new HashMap<>(accumulatedScores);
    }

    /**
     * Gets the current round number (1-based).
     * 
     * @return current round number
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Gets the total number of rounds.
     * 
     * @return total rounds
     */
    public int getTotalRounds() {
        return totalRounds;
    }

    /**
     * Removes a player from the accumulated scores.
     * Should be called when a player disconnects.
     * 
     * @param playerId the player to remove
     */
    public void removePlayer(int playerId) {
        accumulatedScores.remove(playerId);
        Gdx.app.log("GameFlowManager", "Removed player " + playerId + " from game flow");
    }

    /**
     * Resets the game flow manager to initial state.
     * Should be called when returning to lobby or starting a new game.
     */
    public void reset() {
        this.currentRound = 0;
        this.totalRounds = 0;
        this.accumulatedScores.clear();
        this.initialized = false;
        Gdx.app.log("GameFlowManager", "Game flow reset");
    }

    /**
     * Checks if the manager is initialized.
     * 
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
