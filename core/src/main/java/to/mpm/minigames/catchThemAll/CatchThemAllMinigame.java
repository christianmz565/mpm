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
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

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
            {1f, 0.2f, 0.2f},
            {0.2f, 0.2f, 1f},
            {0.2f, 1f, 0.2f},
            {1f, 1f, 0.2f},
            {1f, 0.2f, 1f},
            {0.2f, 1f, 1f},
    };

    private final int localPlayerId;
    private Player localPlayer;
    private final IntMap<Player> players = new IntMap<>();
    private final List<Duck> ducks = new ArrayList<>();
    private DuckSpawner duckSpawner;
    private boolean finished = false;
    private final Map<Integer, Integer> scores = new HashMap<>();
    
    private CatchThemAllClientHandler clientHandler;
    private CatchThemAllServerRelay serverRelay;

    public CatchThemAllMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        GameRenderer.initialize();

        scores.put(localPlayerId, 0);

        float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
        float startX = 100 + (localPlayerId * 80);
        localPlayer = new Player(
                true,
                startX,
                Player.GROUND_Y,
                color[0], color[1], color[2]
        );
        players.put(localPlayerId, localPlayer);

        nm.registerAdditionalClasses(
            Duck.DuckType.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
            to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class
        );

        if (nm.isHost()) {
            duckSpawner = new DuckSpawner();
            Gdx.app.log("CatchThemAll", "Duck spawner initialized (host mode)");
        }

        clientHandler = new CatchThemAllClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverRelay = new CatchThemAllServerRelay();
            nm.registerServerHandler(serverRelay);
        }
        
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
        NetworkManager nm = NetworkManager.getInstance();
        if (nm.isHost() && packet.playerId == localPlayerId) return;

        Player player = players.get(packet.playerId);
        if (player == null) {
            float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
            player = new Player(false, packet.x, packet.y, color[0], color[1], color[2]);
            players.put(packet.playerId, player);
        } else {
            player.setPosition(packet.x, packet.y);
        }
    }

    private void onDuckSpawned(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned packet) {
        Duck.DuckType type = Duck.DuckType.valueOf(packet.duckType);
        Duck duck = new Duck(packet.duckId, packet.x, packet.y, type);
        ducks.add(duck);
        Gdx.app.log("CatchThemAll", "Client: Duck spawned - ID: " + packet.duckId + ", Type: " + packet.duckType);
    }

    private void onDuckUpdate(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate packet) {
        for (Duck duck : ducks) {
            if (duck.id == packet.duckId) {
                duck.setPosition(packet.x, packet.y);
                break;
            }
        }
    }

    private void onDuckRemoved(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved packet) {
        ducks.removeIf(duck -> duck.id == packet.duckId);
        Gdx.app.log("CatchThemAll", "Client: Duck removed - ID: " + packet.duckId);
    }

    private void onScoreUpdate(to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate packet) {
        scores.put(packet.playerId, packet.score);
        Gdx.app.log("CatchThemAll", "Client: Score update - Player " + packet.playerId + ": " + packet.score);
    }

    @Override
    public void update(float delta) {
        for (IntMap.Entry<Player> entry : players) {
            entry.value.update();
        }
        
        for (Duck duck : ducks) {
            duck.update();
        }
        
        if (NetworkManager.getInstance().isHost()) {
            if (duckSpawner != null) {
                List<Duck> newDucks = duckSpawner.update(delta);
                for (Duck duck : newDucks) {
                    ducks.add(duck);
                    NetworkHandler.sendDuckSpawned(duck);
                    Gdx.app.log("CatchThemAll", "Host: Duck spawned - ID: " + duck.id + ", Type: " + duck.type);
                }
            }
            
            CollisionHandler.handlePlayerCollisions(players);
            
            Map<Integer, Player> playersMap = new HashMap<>();
            for (IntMap.Entry<Player> entry : players) {
                playersMap.put(entry.key, entry.value);
            }
            
            List<Duck> ducksBeforeCatch = new ArrayList<>(ducks);
            Map<Integer, Integer> pointsEarned = CatchDetector.detectCatches(ducks, playersMap);
            
            for (Duck duck : ducksBeforeCatch) {
                if (duck.isCaught() && !ducks.contains(duck)) {
                    NetworkHandler.sendDuckRemoved(duck);
                    Gdx.app.log("CatchThemAll", "Host: Duck caught - ID: " + duck.id);
                }
            }
            
            for (Map.Entry<Integer, Integer> entry : pointsEarned.entrySet()) {
                int playerId = entry.getKey();
                int points = entry.getValue();
                int newScore = scores.getOrDefault(playerId, 0) + points;
                scores.put(playerId, newScore);
                
                NetworkHandler.sendScoreUpdate(playerId, newScore);
                
                Gdx.app.log("CatchThemAll", "Player " + playerId + " earned " + points + " points! Total: " + newScore);
            }
            
            List<Duck> ducksBeforeGroundRemoval = new ArrayList<>(ducks);
            int removed = CatchDetector.removeGroundedDucks(ducks);
            
            if (removed > 0) {
                for (Duck duck : ducksBeforeGroundRemoval) {
                    if (!ducks.contains(duck)) {
                        NetworkHandler.sendDuckRemoved(duck);
                    }
                }
                Gdx.app.log("CatchThemAll", "Removed " + removed + " grounded ducks");
            }
            
            NetworkHandler.sendDuckUpdates(ducks);
            
            NetworkHandler.sendAllPlayerPositions(players);
        } else {
            NetworkHandler.sendPlayerPosition(localPlayerId, localPlayer);
        }
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
        NetworkManager nm = NetworkManager.getInstance();
        if (clientHandler != null) {
            nm.unregisterClientHandler(clientHandler);
            clientHandler = null;
        }
        if (serverRelay != null) {
            nm.unregisterServerHandler(serverRelay);
            serverRelay = null;
        }
        
        players.clear();
        ducks.clear();
        if (duckSpawner != null) {
            duckSpawner.reset();
        }
        GameRenderer.dispose();
        
        Gdx.app.log("CatchThemAll", "Game disposed, handlers cleaned up");
    }

    @Override
    public void resize(int width, int height) {
    }

    private final class CatchThemAllClientHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(
                    Packets.PlayerPosition.class,
                    Packets.PlayerJoined.class,
                    Packets.PlayerLeft.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class
            );
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerPosition position) {
                onPlayerPosition(position);
            } else if (packet instanceof Packets.PlayerJoined joined) {
                onPlayerJoined(joined);
            } else if (packet instanceof Packets.PlayerLeft left) {
                onPlayerLeft(left);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned spawned) {
                onDuckSpawned(spawned);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate update) {
                onDuckUpdate(update);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved removed) {
                onDuckRemoved(removed);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate scoreUpdate) {
                onScoreUpdate(scoreUpdate);
            }
        }
    }

    private static final class CatchThemAllServerRelay implements ServerPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(
                    Packets.PlayerPosition.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class
            );
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            context.broadcastExceptSender(packet);
        }
    }
}
