package to.mpm.minigames.catchThemAll;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.game.GameLoop;
import to.mpm.minigames.catchThemAll.game.GameState;
import to.mpm.minigames.catchThemAll.game.PacketHandlers;
import to.mpm.minigames.catchThemAll.input.InputHandler;
import to.mpm.minigames.catchThemAll.rendering.GameRenderer;
import to.mpm.network.NetworkManager;

import java.util.Map;

/**
 * Main minigame coordinator for Catch Them All.
 * Delegates responsibilities to specialized classes in game/ folder:
 * - GameState: manages players, ducks, scores
 * - GameLoop: handles update logic for host/client
 * - PacketHandlers: processes network packets
 */
public class CatchThemAllMinigame implements Minigame {
    private static final float VIRTUAL_WIDTH = GameConstants.Screen.WIDTH;
    private static final float VIRTUAL_HEIGHT = GameConstants.Screen.HEIGHT;

    private final GameState state;
    private OrthographicCamera camera;
    private Viewport viewport;
    private PacketHandlers.ClientHandler clientHandler;
    private PacketHandlers.ServerRelay serverRelay;

    public CatchThemAllMinigame(int localPlayerId) {
        this.state = new GameState(localPlayerId);
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        // Set up camera and viewport for scaling
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        GameRenderer.initialize();
        state.createLocalPlayer();

        nm.registerAdditionalClasses(
                Duck.DuckType.class,
                to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
                to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
                to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
                to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class);

        if (nm.isHost()) {
            state.initializeDuckSpawner();
        }

        clientHandler = new PacketHandlers.ClientHandler(state);
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverRelay = new PacketHandlers.ServerRelay();
            nm.registerServerHandler(serverRelay);
        }
    }

    @Override
    public void update(float delta) {
        if (NetworkManager.getInstance().isHost()) {
            GameLoop.updateHost(delta, state);
        } else if (state.getLocalPlayer() != null) {
            GameLoop.updateClient(state);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Apply viewport and camera
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        GameRenderer.render(batch, shapeRenderer, state.getPlayers(), state.getDucks(),
                state.getScores(), GameState.PLAYER_COLORS, state.getLocalPlayerId());
    }

    @Override
    public void handleInput(float delta) {
        // Spectators (localPlayer == null) don't handle input
        if (state.getLocalPlayer() != null) {
            InputHandler.handleInput(state.getLocalPlayer(), delta);
        }
    }

    @Override
    public boolean isFinished() {
        return state.isFinished();
    }

    @Override
    public Map<Integer, Integer> getScores() {
        return state.getScores();
    }

    @Override
    public int getWinnerId() {
        return state.getWinnerId();
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

        state.reset();
        GameRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();
    }
}
