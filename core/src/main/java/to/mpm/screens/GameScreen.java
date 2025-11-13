package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;
import to.mpm.network.sync.SyncedObject;
import to.mpm.network.sync.Synchronized;
import to.mpm.ui.UIStyles;

/**
 * Pantalla principal del juego donde los jugadores controlan sus patos.
 * Muestra el área de juego con jugadores y un contador de puntaje en la esquina
 * superior derecha.
 */
public class GameScreen implements Screen {
    private static final float PLAYER_RADIUS = 20f; //!< Radio del círculo del jugador
    private static final float MOVE_SPEED = 200f; //!< Velocidad de movimiento del jugador
    private static final float[][] PLAYER_COLORS = { //!< Colores RGB para cada jugador
            { 1f, 0f, 0f },
            { 0f, 0f, 1f },
            { 0f, 1f, 0f },
            { 1f, 1f, 0f },
            { 1f, 0f, 1f },
            { 0f, 1f, 1f },
    };

    private final Main game; //!< Referencia a la instancia principal del juego
    private ShapeRenderer shapeRenderer; //!< Renderizador para formas geométricas
    private Stage uiStage; //!< Escenario para elementos de UI
    private Skin skin; //!< Skin para los componentes de UI
    private Label scoreLabel; //!< Etiqueta que muestra el puntaje actual
    private int currentScore = 123000; //!< Puntaje actual del jugador

    private int localPlayerId; //!< ID del jugador local
    private Player localPlayer; //!< Objeto del jugador local

    private final IntMap<Player> players = new IntMap<>(); //!< Todos los jugadores indexados por ID

    /**
     * Constructor de la pantalla de juego.
     *
     * @param game referencia a la instancia principal del juego
     */
    public GameScreen(Main game) {
        this.game = game;
    }

    /**
     * Inicializa la pantalla de juego, configurando UI y manejadores de red.
     */
    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();

        uiStage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table uiRoot = new Table();
        uiRoot.setFillParent(true);
        uiRoot.top().right();
        uiStage.addActor(uiRoot);

        Table scoreContainer = new Table(skin);
        scoreContainer.pad(UIStyles.Spacing.MEDIUM);

        Table scoreContent = new Table();
        scoreLabel = new Label(currentScore + " pts", skin);
        scoreLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        scoreLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        scoreContent.add(scoreLabel).row();

        Label incrementLabel = new Label("+1000\n+1000\n+1000\n+1000", skin);
        incrementLabel.setFontScale(UIStyles.Typography.SMALL_SCALE);
        incrementLabel.setColor(UIStyles.Colors.SECONDARY);
        incrementLabel.setAlignment(com.badlogic.gdx.utils.Align.right);
        scoreContent.add(incrementLabel).padTop(UIStyles.Spacing.TINY).right();

        scoreContainer.add(scoreContent);

        uiRoot.add(scoreContainer).pad(UIStyles.Spacing.MEDIUM);

        NetworkManager nm = NetworkManager.getInstance();
        localPlayerId = nm.getMyId();

        float[] color = PLAYER_COLORS[localPlayerId % PLAYER_COLORS.length];
        localPlayer = new Player(true,
                localPlayerId == 0 ? 100 : 540,
                240,
                color[0], color[1], color[2]);
        players.put(localPlayerId, localPlayer);

        nm.registerHandler(Packets.PlayerPosition.class, this::onPlayerPosition);
        nm.registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined);
        nm.registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft);
    }

    /**
     * Maneja el evento de unión de un jugador a la partida.
     *
     * @param packet paquete con los datos del jugador que se unió
     */
    private void onPlayerJoined(Packets.PlayerJoined packet) {
        if (packet.playerId == localPlayerId)
            return;
        if (players.containsKey(packet.playerId))
            return;
        float[] color = PLAYER_COLORS[packet.playerId % PLAYER_COLORS.length];
        Player remote = new Player(false, 320, 240, color[0], color[1], color[2]);
        players.put(packet.playerId, remote);
    }

    /**
     * Maneja el evento de salida de un jugador de la partida.
     *
     * @param packet paquete con el ID del jugador que salió
     */
    private void onPlayerLeft(Packets.PlayerLeft packet) {
        if (packet.playerId == localPlayerId)
            return;
        Player p = players.remove(packet.playerId);
        if (p != null)
            p.dispose();
    }

    /**
     * Maneja la recepción de actualizaciones de posición de otros jugadores.
     *
     * @param packet paquete con la posición actualizada del jugador
     */
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

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        handleInput(delta);

        localPlayer.update();
        sendPlayerPosition();

        Gdx.gl.glClearColor(UIStyles.Colors.BACKGROUND.r, UIStyles.Colors.BACKGROUND.g,
                UIStyles.Colors.BACKGROUND.b, UIStyles.Colors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            shapeRenderer.setColor(p.r, p.g, p.b, 1f);
            shapeRenderer.circle(p.x, p.y, PLAYER_RADIUS);
        }

        shapeRenderer.end();

        uiStage.act(delta);
        uiStage.draw();
    }

    /**
     * Actualiza el puntaje mostrado en pantalla.
     *
     * @param newScore nuevo valor del puntaje
     */
    public void updateScore(int newScore) {
        currentScore = newScore;
        scoreLabel.setText(currentScore + " pts");
    }

    /**
     * Procesa la entrada del teclado para mover al jugador local.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
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

        localPlayer.x = Math.max(PLAYER_RADIUS, Math.min(640 - PLAYER_RADIUS, localPlayer.x));
        localPlayer.y = Math.max(PLAYER_RADIUS, Math.min(480 - PLAYER_RADIUS, localPlayer.y));
    }

    /**
     * Envía la posición actual del jugador local a través de la red.
     */
    private void sendPlayerPosition() {
        Packets.PlayerPosition packet = new Packets.PlayerPosition();
        packet.playerId = localPlayerId;
        packet.x = localPlayer.x;
        packet.y = localPlayer.y;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Maneja el redimensionamiento de la ventana.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    /**
     * Método llamado cuando la aplicación es pausada.
     */
    @Override
    public void pause() {
    }

    /**
     * Método llamado cuando la aplicación es reanudada.
     */
    @Override
    public void resume() {
    }

    /**
     * Método llamado cuando esta pantalla deja de ser la pantalla actual.
     */
    @Override
    public void hide() {
    }

    /**
     * Libera los recursos utilizados por esta pantalla.
     */
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        uiStage.dispose();
        skin.dispose();
        for (IntMap.Entry<Player> entry : players) {
            entry.value.dispose();
        }
        players.clear();
    }

    /**
     * Clase interna que representa un jugador en el juego.
     * Extiende SyncedObject para sincronización de red.
     */
    private static class Player extends SyncedObject {
        @Synchronized
        public float x; //!< Coordenada X del jugador
        @Synchronized
        public float y; //!< Coordenada Y del jugador
        public float r, g, b; //!< Componentes RGB del color del jugador

        /**
         * Constructor del jugador.
         *
         * @param isLocallyOwned indica si este jugador es controlado localmente
         * @param x              posición inicial en X
         * @param y              posición inicial en Y
         * @param r              componente rojo del color
         * @param g              componente verde del color
         * @param b              componente azul del color
         */
        public Player(boolean isLocallyOwned, float x, float y, float r, float g, float b) {
            super(isLocallyOwned);
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        /**
         * Actualiza la posición del jugador.
         *
         * @param x nueva coordenada X
         * @param y nueva coordenada Y
         */
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
