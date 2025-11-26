package to.mpm.minigames.catchThemAll.game;

import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.entities.DuckSpawner;
import to.mpm.minigames.catchThemAll.entities.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages game state for Catch Them All minigame.
 * Holds all players, ducks, scores, and provides access methods.
 */
public class GameState {
    public static final float[][] PLAYER_COLORS = {
            {1f, 0.2f, 0.2f},  // Red
            {0.2f, 0.2f, 1f},  // Blue
            {0.2f, 1f, 0.2f},  // Green
            {1f, 1f, 0.2f},    // Yellow
            {1f, 0.2f, 1f},    // Magenta
            {0.2f, 1f, 1f},    // Cyan
    };
    
    private final int localPlayerId;
    private Player localPlayer;
    private final IntMap<Player> players = new IntMap<>();
    private final List<Duck> ducks = new ArrayList<>();
    private final Map<Integer, Integer> scores = new HashMap<>();
    private DuckSpawner duckSpawner;
    private boolean finished = false;

    public GameState(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    public void createLocalPlayer() {
        float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
        float startX = 100 + (localPlayerId * 80);
        localPlayer = new Player(true, startX, Player.GROUND_Y, color[0], color[1], color[2]);
        players.put(localPlayerId, localPlayer);
        scores.put(localPlayerId, 0);
    }

    public void createRemotePlayer(int playerId) {
        if (players.containsKey(playerId)) return;
        
        float[] color = PLAYER_COLORS[playerId % PLAYER_COLORS.length];
        float startX = 100 + (playerId * 80);
        Player remote = new Player(false, startX, Player.GROUND_Y, color[0], color[1], color[2]);
        players.put(playerId, remote);
        scores.put(playerId, 0);
    }

    public void removePlayer(int playerId) {
        players.remove(playerId);
        scores.remove(playerId);
    }

    public void addDuck(Duck duck) {
        ducks.add(duck);
    }

    public void removeDuck(int duckId) {
        ducks.removeIf(duck -> duck.id == duckId);
    }

    public void updateScore(int playerId, int score) {
        scores.put(playerId, score);
    }

    public void addScore(int playerId, int points) {
        int newScore = scores.getOrDefault(playerId, 0) + points;
        scores.put(playerId, newScore);
    }

    public void initializeDuckSpawner() {
        duckSpawner = new DuckSpawner();
    }

    public void reset() {
        players.clear();
        ducks.clear();
        scores.clear();
        localPlayer = null;
        if (duckSpawner != null) {
            duckSpawner.reset();
        }
    }

    // Getters
    public int getLocalPlayerId() { return localPlayerId; }
    public Player getLocalPlayer() { return localPlayer; }
    public IntMap<Player> getPlayers() { return players; }
    public List<Duck> getDucks() { return ducks; }
    public Map<Integer, Integer> getScores() { return scores; }
    public DuckSpawner getDuckSpawner() { return duckSpawner; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public int getWinnerId() {
        int winnerId = -1;
        int maxScore = Integer.MIN_VALUE;
        
        for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                winnerId = entry.getKey();
            }
        }
        
        return winnerId;
    }
}
