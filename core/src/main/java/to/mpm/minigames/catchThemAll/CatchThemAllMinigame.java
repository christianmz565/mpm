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
 * Minijuego principal de Atrapa a Todos.
 * <p>
 * Delega responsabilidades a clases especializadas en la carpeta game/:
 * <ul>
 * <li>GameState: gestiona jugadores, patos y puntuaciones</li>
 * <li>GameLoop: maneja la lógica de actualización para host/cliente</li>
 * <li>PacketHandlers: procesa paquetes de red</li>
 * </ul>
 */
public class CatchThemAllMinigame implements Minigame {
    /** Ancho virtual de la pantalla de juego */
    private static final float VIRTUAL_WIDTH = GameConstants.Screen.WIDTH;
    /** Alto virtual de la pantalla de juego */
    private static final float VIRTUAL_HEIGHT = GameConstants.Screen.HEIGHT;

    /** Estado del juego que contiene jugadores, patos y puntuaciones */
    private final GameState state;
    /** Cámara ortográfica para el renderizado 2D */
    private OrthographicCamera camera;
    /** Viewport para escalar la vista del juego */
    private Viewport viewport;
    /** Manejador de paquetes del cliente */
    private PacketHandlers.ClientHandler clientHandler;
    /** Retransmisor de paquetes del servidor */
    private PacketHandlers.ServerRelay serverRelay;

    /**
     * Constructor del minijuego Atrapa a Todos.
     *
     * @param localPlayerId identificador del jugador local
     */
    public CatchThemAllMinigame(int localPlayerId) {
        this.state = new GameState(localPlayerId);
    }

    /**
     * Inicializa el minijuego configurando la cámara, viewport,
     * renderizador, jugador local y manejadores de red.
     */
    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

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

    /**
     * Actualiza la lógica del juego según el rol (host o cliente).
     *
     * @param delta tiempo transcurrido desde la última actualización en segundos
     */
    @Override
    public void update(float delta) {
        if (NetworkManager.getInstance().isHost()) {
            GameLoop.updateHost(delta, state);
        } else if (state.getLocalPlayer() != null) {
            GameLoop.updateClient(state);
        }
    }

    /**
     * Renderiza los elementos del juego aplicando el viewport y la cámara.
     *
     * @param batch renderizador de sprites para dibujar texturas
     * @param shapeRenderer renderizador de formas geométricas
     */
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        GameRenderer.render(batch, shapeRenderer, state.getPlayers(), state.getDucks(),
                state.getScores(), GameState.PLAYER_COLORS, state.getLocalPlayerId());
    }

    /**
     * Maneja la entrada del jugador local.
     * Los espectadores no procesan entrada.
     *
     * @param delta tiempo transcurrido desde la última actualización en segundos
     */
    @Override
    public void handleInput(float delta) {
        if (state.getLocalPlayer() != null) {
            InputHandler.handleInput(state.getLocalPlayer(), delta);
        }
    }

    /**
     * Verifica si el minijuego ha terminado.
     *
     * @return true si el juego ha finalizado, false en caso contrario
     */
    @Override
    public boolean isFinished() {
        return state.isFinished();
    }

    /**
     * Obtiene las puntuaciones actuales de todos los jugadores.
     *
     * @return mapa con identificador de jugador como clave y puntuación como valor
     */
    @Override
    public Map<Integer, Integer> getScores() {
        return state.getScores();
    }

    /**
     * Obtiene el identificador del jugador ganador.
     *
     * @return identificador del ganador
     */
    @Override
    public int getWinnerId() {
        return state.getWinnerId();
    }

    /**
     * Libera los recursos utilizados por el minijuego.
     * Desregistra los manejadores de red y resetea el estado.
     */
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

    /**
     * Ajusta el viewport cuando cambia el tamaño de la ventana.
     *
     * @param width nuevo ancho de la ventana en píxeles
     * @param height nuevo alto de la ventana en píxeles
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();
    }
}
