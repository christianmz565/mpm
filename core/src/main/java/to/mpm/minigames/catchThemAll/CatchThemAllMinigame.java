package to.mpm.minigames.catchThemAll;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.entities.DuckSpawner;
import to.mpm.minigames.catchThemAll.entities.Player;
import to.mpm.minigames.catchThemAll.input.InputHandler;
import to.mpm.minigames.catchThemAll.network.NetworkHandler;
import to.mpm.minigames.catchThemAll.physics.CatchDetector;
import to.mpm.minigames.catchThemAll.physics.CollisionHandler;
import to.mpm.minigames.catchThemAll.rendering.GameRenderer;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main minigame class for Catch Them All.
 * Coordinates between input, physics, networking, and rendering modules.
 */
public class CatchThemAllMinigame implements Minigame {
    private static final float[][] PLAYER_COLORS = {
            {1f, 0.2f, 0.2f}, // 0 - Red
            {0.2f, 0.2f, 1f}, // 1 - Blue
            {0.2f, 1f, 0.2f}, // 2 - Green
            {1f, 1f, 0.2f},   // 3 - Yellow
            {1f, 0.2f, 1f},   // 4 - Magenta
            {0.2f, 1f, 1f},   // 5 - Cyan
    };

    private final int localPlayerId;
    private Player localPlayer;
    private final IntMap<Player> players = new IntMap<>();
    private final List<Duck> ducks = new ArrayList<>();
    private DuckSpawner duckSpawner;
    private boolean finished = false;
    private final Map<Integer, Integer> scores = new HashMap<>();

    public CatchThemAllMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        // Initialize renderer
        GameRenderer.initialize();

        // Initialize scores for all players
        scores.put(localPlayerId, 0);

        // Create local player with a color based on id
        float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
        float startX = 100 + (localPlayerId * 80); // Space out players
        localPlayer = new Player(
                true,
                startX,
                Player.GROUND_Y,
                color[0], color[1], color[2]
        );
        players.put(localPlayerId, localPlayer);

        // Register minigame-specific network classes
        nm.registerAdditionalClasses(
            Duck.DuckType.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class
        );

        // Initialize duck spawner (only host will use it)
        if (nm.isHost()) {
            duckSpawner = new DuckSpawner();
            Gdx.app.log("CatchThemAll", "Duck spawner initialized (host mode)");
        }

        // Register network handlers
        nm.registerHandler(Packets.PlayerPosition.class, this::onPlayerPosition);
        nm.registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined);
        nm.registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft);
        nm.registerHandler(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class, this::onDuckSpawned);
        nm.registerHandler(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class, this::onDuckUpdate);
        nm.registerHandler(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class, this::onDuckRemoved);
        nm.registerHandler(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class, this::onScoreUpdate);
        
        Gdx.app.log("CatchThemAll", "Game initialized for player " + localPlayerId);
    }

    private void onPlayerJoined(Packets.PlayerJoined packet) {
        if (packet.playerId == localPlayerId) return;
        if (players.containsKey(packet.playerId)) return;
        
        scores.put(packet.playerId, 0);
        
        float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
        float startX = 100 + (packet.playerId * 80);
        Player remote = new Player(false, startX, Player.GROUND_Y, color[0], color[1], color[2]);
        players.put(packet.playerId, remote);
    }

    private void onPlayerLeft(Packets.PlayerLeft packet) {
        if (packet.playerId == localPlayerId) return;
        players.remove(packet.playerId);
    }

    private void onPlayerPosition(Packets.PlayerPosition packet) {
        if (packet.playerId == localPlayerId) return;

        Player remote = players.get(packet.playerId);
        if (remote == null) {
            float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
            remote = new Player(false, packet.x, packet.y, color[0], color[1], color[2]);
            players.put(packet.playerId, remote);
        } else {
            remote.setPosition(packet.x, packet.y);
        }
    }

    private void onDuckSpawned(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned packet) {
        // Clients create the duck when notified by host
        Duck.DuckType type = Duck.DuckType.valueOf(packet.duckType);
        Duck duck = new Duck(packet.duckId, packet.x, packet.y, type);
        ducks.add(duck);
        Gdx.app.log("CatchThemAll", "Client: Duck spawned - ID: " + packet.duckId + ", Type: " + packet.duckType);
    }

    private void onDuckUpdate(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate packet) {
        // Update duck position on clients
        for (Duck duck : ducks) {
            if (duck.id == packet.duckId) {
                duck.setPosition(packet.x, packet.y);
                break;
            }
        }
    }

    private void onDuckRemoved(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved packet) {
        // Remove duck from clients
        ducks.removeIf(duck -> duck.id == packet.duckId);
        Gdx.app.log("CatchThemAll", "Client: Duck removed - ID: " + packet.duckId);
    }

    private void onScoreUpdate(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate packet) {
        // Update score on clients
        scores.put(packet.playerId, packet.score);
        Gdx.app.log("CatchThemAll", "Client: Score update - Player " + packet.playerId + ": " + packet.score);
    }

    @Override
    public void update(float delta) {
        localPlayer.update();
        
        // Update all ducks (falling physics)
        for (Duck duck : ducks) {
            duck.update();
        }
        
        // Host-only logic
        if (NetworkManager.getInstance().isHost()) {
            // Spawn new ducks
            if (duckSpawner != null) {
                List<Duck> newDucks = duckSpawner.update(delta);
                for (Duck duck : newDucks) {
                    ducks.add(duck);
                    // Notify clients about new duck
                    NetworkHandler.sendDuckSpawned(duck);
                    Gdx.app.log("CatchThemAll", "Host: Duck spawned - ID: " + duck.id + ", Type: " + duck.type);
                }
            }
            
            // Handle player collisions
            CollisionHandler.handlePlayerCollisions(players);
            
            // Detect duck catches and track removed ducks
            Map<Integer, Player> playersMap = new HashMap<>();
            for (IntMap.Entry<Player> entry : players) {
                playersMap.put(entry.key, entry.value);
            }
            
            // Store ducks before detection to see which were removed
            List<Duck> ducksBeforeCatch = new ArrayList<>(ducks);
            Map<Integer, Integer> pointsEarned = CatchDetector.detectCatches(ducks, playersMap);
            
            // Find and notify about caught ducks
            for (Duck duck : ducksBeforeCatch) {
                if (duck.isCaught() && !ducks.contains(duck)) {
                    NetworkHandler.sendDuckRemoved(duck);
                    Gdx.app.log("CatchThemAll", "Host: Duck caught - ID: " + duck.id);
                }
            }
            
            // Update scores and notify clients
            for (Map.Entry<Integer, Integer> entry : pointsEarned.entrySet()) {
                int playerId = entry.getKey();
                int points = entry.getValue();
                int newScore = scores.getOrDefault(playerId, 0) + points;
                scores.put(playerId, newScore);
                
                // Send score update to all clients
                NetworkHandler.sendScoreUpdate(playerId, newScore);
                
                Gdx.app.log("CatchThemAll", "Player " + playerId + " earned " + points + " points! Total: " + newScore);
            }
            
            // Track ducks before removing grounded ones
            List<Duck> ducksBeforeGroundRemoval = new ArrayList<>(ducks);
            int removed = CatchDetector.removeGroundedDucks(ducks);
            
            // Notify about grounded ducks
            if (removed > 0) {
                for (Duck duck : ducksBeforeGroundRemoval) {
                    if (!ducks.contains(duck)) {
                        NetworkHandler.sendDuckRemoved(duck);
                    }
                }
                Gdx.app.log("CatchThemAll", "Removed " + removed + " grounded ducks");
            }
            
            // Send periodic duck position updates (every frame for smooth movement)
            NetworkHandler.sendDuckUpdates(ducks);
        }
        
        NetworkHandler.sendPlayerPosition(localPlayerId, localPlayer);
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        GameRenderer.render(batch, shapeRenderer, players, ducks, scores, PLAYER_COLORS, localPlayerId);
    }

    @Override
    public void handleInput(float delta) {
        InputHandler.handleInput(localPlayer, delta);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Map<Integer, Integer> getScores() {
        return scores;
    }

    @Override
    public int getWinnerId() {
        // Return the player with the highest score
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

    @Override
    public void dispose() {
        players.clear();
        ducks.clear();
        if (duckSpawner != null) {
            duckSpawner.reset();
        }
        GameRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Not needed for this simple game
    }
}
