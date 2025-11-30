package to.mpm.minigames.catchThemAll;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private final GameState state;
    private PacketHandlers.ClientHandler clientHandler;
    private PacketHandlers.ServerRelay serverRelay;

    public CatchThemAllMinigame(int localPlayerId) {
        this.state = new GameState(localPlayerId);
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

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
    }
}
