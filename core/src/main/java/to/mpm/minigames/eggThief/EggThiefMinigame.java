package to.mpm.minigames.eggThief;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.eggThief.entities.Duck;
import to.mpm.minigames.eggThief.entities.Egg;
import to.mpm.minigames.eggThief.entities.EggSpawner;
import to.mpm.minigames.eggThief.entities.Nest;
import to.mpm.minigames.eggThief.input.InputHandler;
import to.mpm.minigames.eggThief.network.NetworkHandler;
import to.mpm.minigames.eggThief.physics.CollisionDetector;
import to.mpm.minigames.eggThief.rendering.GameRenderer;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

import java.util.*;

/**
 * Main minigame class for Egg Thief.
 */
public class EggThiefMinigame implements Minigame {

    private static final float[][] PLAYER_COLORS = {
            { 1f, 0.2f, 0.2f },
            { 0.2f, 0.2f, 1f },
            { 0.2f, 1f, 0.2f },
            { 1f, 1f, 0.2f },
            { 1f, 0.2f, 1f },
            { 0.2f, 1f, 1f },
    };

    private final int localPlayerId;
    private Duck localPlayer;
    private final IntMap<Duck> players = new IntMap<>();
    private final List<Egg> eggs = new ArrayList<>();
    private EggSpawner eggSpawner;
    private final List<Nest> nests = new ArrayList<>();
    private final Map<Integer, Integer> scores = new HashMap<>();
    private float gameTimer = 180; // 3 minutes
    private boolean finished = false;

    public EggThiefMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        GameRenderer.initialize();

        scores.put(localPlayerId, 0);
        createLocalPlayer(nm.getPlayerCount());

        registerNetworkHandlers();

        if (nm.isHost()) {
            eggSpawner = new EggSpawner();
            // Spawn initial 10 eggs at game start
            List<Egg> initialEggs = eggSpawner.spawnInitialEggs();
            eggs.addAll(initialEggs);
            // Send all initial eggs to clients
            for (Egg egg : initialEggs) {
                NetworkHandler.sendEggSpawned(egg);
            }
            Gdx.app.log("EggThief",
                    "Egg spawner initialized (host mode) - " + initialEggs.size() + " initial eggs spawned");
        }

        Gdx.app.log("EggThief", "Game initialized for player " + localPlayerId);
    }

    // -------------------- Network handlers --------------------

    private EggThiefClientHandler clientHandler;
    private EggThiefServerHandler serverHandler;

    private void registerNetworkHandlers() {
        NetworkManager nm = NetworkManager.getInstance();

        nm.registerAdditionalClasses(
                to.mpm.minigames.eggThief.network.EggThiefPackets.EggSpawned.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.EggUpdate.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.EggRemoved.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.ScoreUpdate.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.DuckUpdate.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.EggCollected.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.EggStolen.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.EggsDelivered.class,
                to.mpm.minigames.eggThief.network.EggThiefPackets.GameTimerUpdate.class);

        clientHandler = new EggThiefClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverHandler = new EggThiefServerHandler();
            nm.registerServerHandler(serverHandler);
        }
    }

    private void onPlayerPosition(Packets.PlayerPosition packet) {
        if (packet.playerId == localPlayerId)
            return;

        Duck duck = players.get(packet.playerId);
        if (duck != null) {
            duck.setPosition(packet.x, packet.y);
        }
    }

    private void onPlayerJoined(Packets.PlayerJoined packet) {
        if (packet.playerId == localPlayerId)
            return;
        if (!scores.containsKey(packet.playerId)) {
            scores.put(packet.playerId, 0);
        }
        createRemotePlayer(packet.playerId);
        recreateNests();
    }

    private void onPlayerLeft(Packets.PlayerLeft packet) {
        players.remove(packet.playerId);
        recreateNests();
    }

    private void onEggSpawned(to.mpm.minigames.eggThief.network.EggThiefPackets.EggSpawned packet) {
        eggs.add(new Egg(packet.eggId, packet.x, packet.y, packet.isGolden));
        Gdx.app.log("EggThief", "Client: Egg spawned - ID: " + packet.eggId);
    }

    private void onEggUpdate(to.mpm.minigames.eggThief.network.EggThiefPackets.EggUpdate packet) {
        for (Egg egg : eggs) {
            if (egg.getId() == packet.eggId) {
                egg.setPosition(packet.x, packet.y);
                break;
            }
        }
    }

    private void onEggRemoved(to.mpm.minigames.eggThief.network.EggThiefPackets.EggRemoved packet) {
        eggs.removeIf(egg -> egg.getId() == packet.eggId);
        Gdx.app.log("EggThief", "Client: Egg removed - ID: " + packet.eggId);
    }

    private void onScoreUpdate(to.mpm.minigames.eggThief.network.EggThiefPackets.ScoreUpdate packet) {
        scores.put(packet.playerId, packet.score);
        Gdx.app.log("EggThief", "Client: Score update - Player " + packet.playerId + ": " + packet.score);
    }

    // -------------------- Update & game logic --------------------

    @Override
    public void update(float delta) {
        if (finished)
            return;

        gameTimer -= delta;
        if (gameTimer <= 0) {
            finished = true;
            gameTimer = 0;
        }

        // Update all players
        for (Duck duck : players.values()) {
            duck.update(delta);
        }

        eggs.forEach(e -> e.update(delta));

        if (NetworkManager.getInstance().isHost()) {
            // No more egg spawning - game starts with 10 eggs only

            Map<Integer, Duck> playerMap = new HashMap<>();
            for (IntMap.Entry<Duck> entry : players) {
                playerMap.put(entry.key, entry.value);
            }

            CollisionDetector.handleCollisions(playerMap, eggs, nests, scores,
                    (thiefId, victimId, thiefEggs, victimEggs) -> {
                        NetworkHandler.sendEggStolen(thiefId, victimId);
                    });
            NetworkHandler.sendEggUpdates(eggs);
        }

        NetworkHandler.sendPlayerPosition(localPlayerId, localPlayer);
    }

    // -------------------- Render --------------------

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        GameRenderer.render(
                batch,
                shapeRenderer,
                players,
                eggs,
                nests,
                scores,
                localPlayerId,
                gameTimer);
    }

    @Override
    public void handleInput(float delta) {
        InputHandler.handleInput(localPlayer, delta);
    }

    // -------------------- Utility methods --------------------

    private void createLocalPlayer(int playerCount) {
        float spacing = 480f / (playerCount + 1);
        float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
        float startY = spacing * (localPlayerId + 1);
        localPlayer = new Duck(true, localPlayerId, 100, startY, color[0], color[1], color[2]);
        players.put(localPlayerId, localPlayer);
        recreateNests();
    }

    private void createRemotePlayer(int playerId) {
        int playerCount = NetworkManager.getInstance().getPlayerCount();
        float spacing = 480f / (playerCount + 1);
        float[] color = PLAYER_COLORS[playerId % PLAYER_COLORS.length];
        float startY = spacing * (playerId + 1);
        Duck remote = new Duck(false, playerId, 100, startY, color[0], color[1], color[2]);
        players.put(playerId, remote);
    }

    private void recreateNests() {
        nests.clear();
        int playerCount = NetworkManager.getInstance().getPlayerCount();
        for (int i = 0; i < playerCount; i++) {
            float[] color = PLAYER_COLORS[i % PLAYER_COLORS.length];
            nests.add(new Nest(i, 640 - Nest.NEST_SIZE - 20, 480 - (i + 1) * (Nest.NEST_SIZE + 10),
                    color[0], color[1], color[2]));
        }
    }

    // -------------------- Endgame & scores --------------------

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
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    // -------------------- Cleanup --------------------

    @Override
    public void dispose() {
        players.clear();
        eggs.clear();
        nests.clear();
        if (eggSpawner != null)
            eggSpawner.reset();
        GameRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Not needed
    }

    // -------------------- Network Handler Classes --------------------

    private class EggThiefClientHandler implements ClientPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(
                    Packets.PlayerPosition.class,
                    Packets.PlayerJoined.class,
                    Packets.PlayerLeft.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggSpawned.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggUpdate.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggRemoved.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.ScoreUpdate.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.DuckUpdate.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggCollected.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggStolen.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggsDelivered.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.GameTimerUpdate.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerPosition p) {
                onPlayerPosition(p);
            } else if (packet instanceof Packets.PlayerJoined p) {
                onPlayerJoined(p);
            } else if (packet instanceof Packets.PlayerLeft p) {
                onPlayerLeft(p);
            } else if (packet instanceof to.mpm.minigames.eggThief.network.EggThiefPackets.EggSpawned p) {
                onEggSpawned(p);
            } else if (packet instanceof to.mpm.minigames.eggThief.network.EggThiefPackets.EggUpdate p) {
                onEggUpdate(p);
            } else if (packet instanceof to.mpm.minigames.eggThief.network.EggThiefPackets.EggRemoved p) {
                onEggRemoved(p);
            } else if (packet instanceof to.mpm.minigames.eggThief.network.EggThiefPackets.ScoreUpdate p) {
                onScoreUpdate(p);
            }
        }
    }

    private class EggThiefServerHandler implements ServerPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(
                    Packets.PlayerPosition.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.DuckUpdate.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggSpawned.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggUpdate.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggRemoved.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.ScoreUpdate.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggCollected.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggStolen.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.EggsDelivered.class,
                    to.mpm.minigames.eggThief.network.EggThiefPackets.GameTimerUpdate.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            // Broadcast all packets to other clients
            context.broadcastExceptSender(packet);
        }
    }
}
