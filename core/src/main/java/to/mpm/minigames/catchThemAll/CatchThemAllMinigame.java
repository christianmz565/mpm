package to.mpm.minigames.catchThemAll;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.catchThemAll.entities.Player;
import to.mpm.minigames.catchThemAll.input.InputHandler;
import to.mpm.minigames.catchThemAll.network.NetworkHandler;
import to.mpm.minigames.catchThemAll.physics.CollisionHandler;
import to.mpm.minigames.catchThemAll.rendering.GameRenderer;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

import java.util.HashMap;
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
    private boolean finished = false;
    private final Map<Integer, Integer> scores = new HashMap<>();

    public CatchThemAllMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

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

        // Register network handlers
        nm.registerHandler(Packets.PlayerPosition.class, this::onPlayerPosition);
        nm.registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined);
        nm.registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft);
        
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

    @Override
    public void update(float delta) {
        localPlayer.update();
        
        // Only the host calculates collisions to avoid conflicts
        if (NetworkManager.getInstance().isHost()) {
            CollisionHandler.handlePlayerCollisions(players);
        }
        
        NetworkHandler.sendPlayerPosition(localPlayerId, localPlayer);
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        GameRenderer.render(batch, shapeRenderer, players);
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
        return -1; // No winner in this game
    }

    @Override
    public void dispose() {
        players.clear();
    }

    @Override
    public void resize(int width, int height) {
        // Not needed for this simple game
    }
}
