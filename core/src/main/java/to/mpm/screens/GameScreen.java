package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.Main;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;
import to.mpm.network.sync.SyncedObject;
import to.mpm.network.sync.Synchronized;


/**
 * Código de prueba, solo para pruebas de red simples.
 */
public class GameScreen implements Screen {
    private static final float PLAYER_RADIUS = 20f;
    private static final float MOVE_SPEED = 200f;
    private static final float[][] PLAYER_COLORS = {
            {1f, 0f, 0f}, // 0 - Red
            {0f, 0f, 1f}, // 1 - Blue
            {0f, 1f, 0f}, // 2 - Green
            {1f, 1f, 0f}, // 3 - Yellow
            {1f, 0f, 1f}, // 4 - Magenta
            {0f, 1f, 1f}, // 5 - Cyan
    };

    private final Main game;
    private ShapeRenderer shapeRenderer;

    // Local player id and object
    private int localPlayerId;
    private Player localPlayer;

    // All players (including local) keyed by id, for rendering other remote players
    private final IntMap<Player> players = new IntMap<>();

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();

        NetworkManager nm = NetworkManager.getInstance();
        localPlayerId = nm.getMyId();

        // Create local player with a color based on id
        float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
        localPlayer = new Player(true, // locally owned
                localPlayerId == 0 ? 100 : 540, // initial x placement (simple heuristic)
                240, // initial y
                color[0], color[1], color[2]);
        players.put(localPlayerId, localPlayer);

        // Register network handlers
        nm.registerHandler(Packets.PlayerPosition.class, this::onPlayerPosition);
        nm.registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined);
        nm.registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft);
    }

    private void onPlayerJoined(Packets.PlayerJoined packet) {
        // Ignore ourselves
        if (packet.playerId == localPlayerId) return;
        if (players.containsKey(packet.playerId)) return; // already exists
        float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
        // Spawn at center until we receive a real position
        Player remote = new Player(false, 320, 240, color[0], color[1], color[2]);
        players.put(packet.playerId, remote);
    }

    private void onPlayerLeft(Packets.PlayerLeft packet) {
        if (packet.playerId == localPlayerId) return; // shouldn't happen here normally
        Player p = players.remove(packet.playerId);
        if (p != null) p.dispose();
    }

    private void onPlayerPosition(Packets.PlayerPosition packet) {
        // Ignore packets from ourselves
        if (packet.playerId == localPlayerId) return;

        // Get or create remote player representation
        Player remote = players.get(packet.playerId);
        if (remote == null) {
            float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
            // Spawn new remote player at received position
            remote = new Player(false, packet.x, packet.y, color[0], color[1], color[2]);
            players.put(packet.playerId, remote);
        } else {
            remote.setPosition(packet.x, packet.y);
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        localPlayer.update();
        sendPlayerPosition();

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw all players
        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            shapeRenderer.setColor(p.r, p.g, p.b, 1f);
            shapeRenderer.circle(p.x, p.y, PLAYER_RADIUS);
        }

        shapeRenderer.end();
    }

    private void handleInput(float delta) {
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

        // Keep inside a nominal 640x480 area (could be updated to use viewport later)
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
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        // Dispose players
        for (IntMap.Entry<Player> entry : players) {
            entry.value.dispose();
        }
        players.clear();
    }

    private static class Player extends SyncedObject {
        @Synchronized public float x;
        @Synchronized public float y;
        public float r, g, b; // Colors

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
}
