package to.mpm.minigames.ballmovement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.Minigame;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;
import to.mpm.network.sync.SyncedObject;
import to.mpm.network.sync.Synchronized;

import java.util.HashMap;
import java.util.Map;

/**
 * Minijuego simple de movimiento de bolas.
 * Cada jugador controla una bola de color que puede moverse libremente.
 */
public class BallMovementMinigame implements Minigame {
    private static final float PLAYER_RADIUS = 20f;
    private static final float MOVE_SPEED = 200f;
    private static final float[][] PLAYER_COLORS = {
            { 1f, 0f, 0f }, // 0 - Red
            { 0f, 0f, 1f }, // 1 - Blue
            { 0f, 1f, 0f }, // 2 - Green
            { 1f, 1f, 0f }, // 3 - Yellow
            { 1f, 0f, 1f }, // 4 - Magenta
            { 0f, 1f, 1f }, // 5 - Cyan
    };

    private final int localPlayerId;
    private Player localPlayer;
    private final IntMap<Player> players = new IntMap<>();
    private boolean finished = false;
    private BallMovementClientHandler clientHandler;
    private BallMovementServerRelay serverRelay;

    public BallMovementMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        if (localPlayerId != -1) {
            float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
            localPlayer = new Player(true,
                    localPlayerId == 0 ? 100 : 540,
                    240,
                    color[0], color[1], color[2]);
            players.put(localPlayerId, localPlayer);
        }

        clientHandler = new BallMovementClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverRelay = new BallMovementServerRelay();
            nm.registerServerHandler(serverRelay);
        }
    }

    private void onPlayerJoined(Packets.PlayerJoined packet) {
        if (packet.playerId == localPlayerId)
            return;
        if (players.containsKey(packet.playerId))
            return;

        float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
        Player remote = new Player(false, 320, 240, color[0], color[1], color[2]);
        players.put(packet.playerId, remote);
    }

    private void onPlayerLeft(Packets.PlayerLeft packet) {
        if (packet.playerId == localPlayerId)
            return;
        Player p = players.remove(packet.playerId);
        if (p != null)
            p.dispose();
    }

    private void onPlayerPosition(Packets.PlayerPosition packet) {
        if (packet.playerId == localPlayerId)
            return;

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
        if (localPlayer != null) {
            localPlayer.update();
            sendPlayerPosition();
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            shapeRenderer.setColor(p.r, p.g, p.b, 1f);
            shapeRenderer.circle(p.x, p.y, PLAYER_RADIUS);
        }

        shapeRenderer.end();
    }

    @Override
    public void handleInput(float delta) {
        if (localPlayer == null)
            return;

        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy += MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy -= MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx -= MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx += MOVE_SPEED * delta;
        }

        localPlayer.x += dx;
        localPlayer.y += dy;

        localPlayer.x = Math.max(PLAYER_RADIUS, Math.min(640 - PLAYER_RADIUS, localPlayer.x));
        localPlayer.y = Math.max(PLAYER_RADIUS, Math.min(480 - PLAYER_RADIUS, localPlayer.y));
    }

    private void sendPlayerPosition() {
        Packets.PlayerPosition packet = new Packets.PlayerPosition();
        packet.playerId = localPlayerId;
        packet.x = localPlayer.x;
        packet.y = localPlayer.y;
        NetworkManager.getInstance().sendPacket(packet);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Map<Integer, Integer> getScores() {
        return new HashMap<>();
    }

    @Override
    public int getWinnerId() {
        return -1;
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
        for (IntMap.Entry<Player> entry : players) {
            entry.value.dispose();
        }
        players.clear();
    }

    @Override
    public void resize(int width, int height) {
    }

    private static class Player extends SyncedObject {
        @Synchronized
        public float x;
        @Synchronized
        public float y;
        public float r, g, b;

        public Player(boolean isLocallyOwned, float x, float y, float r, float g, float b) {
            super(isLocallyOwned);
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private final class BallMovementClientHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(
                    Packets.PlayerPosition.class,
                    Packets.PlayerJoined.class,
                    Packets.PlayerLeft.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerPosition position) {
                onPlayerPosition(position);
            } else if (packet instanceof Packets.PlayerJoined joined) {
                onPlayerJoined(joined);
            } else if (packet instanceof Packets.PlayerLeft left) {
                onPlayerLeft(left);
            }
        }
    }

    private static final class BallMovementServerRelay implements ServerPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(Packets.PlayerPosition.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerPosition position) {
                context.broadcastExceptSender(position);
            }
        }
    }
}
