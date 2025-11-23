package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.MinigameFactory;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;

/**
 * Pantalla principal de juego que ejecuta el minijuego seleccionado.
 */
public class GameScreen implements Screen {
    private final Main game; // !< instancia del juego principal
    private final MinigameType minigameType; // !< tipo de minijuego a ejecutar
    private Minigame currentMinigame; // !< instancia del minijuego actual
    private SpriteBatch batch; // !< lote de sprites para renderizado
    private ShapeRenderer shapeRenderer; // !< renderizador de formas
    private Stage uiStage; // !< stage para la superposición de UI
    private Skin skin; // !< skin para estilizar componentes de UI
    private Label scoreLabel; // !< etiqueta que muestra la puntuación del jugador

    private StartGamePacketHandler startGameHandler;

    /**
     * Construye una nueva pantalla de juego.
     *
     * @param game         instancia del juego principal
     * @param minigameType tipo de minijuego a ejecutar
     */
    public GameScreen(Main game, MinigameType minigameType) {
        this.game = game;
        this.minigameType = minigameType;
    }

    /**
     * Inicializa la pantalla de juego, configurando UI y manejadores de red.
     */
    @Override
    public void show() {
        batch = game.batch;
        shapeRenderer = new ShapeRenderer();

        uiStage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(uiStage);

        Table uiRoot = new Table();
        uiRoot.setFillParent(true);
        uiRoot.top().right();
        uiStage.addActor(uiRoot);

        Table scoreContainer = new Table(skin);
        scoreContainer.pad(UIStyles.Spacing.MEDIUM);

        Table scoreContent = new Table();
        scoreLabel = new Label("0 pts", skin);
        scoreLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        scoreLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        scoreContent.add(scoreLabel).row();

        Label incrementLabel = new Label("+0\n+0\n+0\n+0", skin);
        incrementLabel.setFontScale(UIStyles.Typography.SMALL_SCALE);
        incrementLabel.setColor(UIStyles.Colors.SECONDARY);
        incrementLabel.setAlignment(com.badlogic.gdx.utils.Align.right);
        scoreContent.add(incrementLabel).padTop(UIStyles.Spacing.TINY).right();

        scoreContainer.add(scoreContent);
        uiRoot.add(scoreContainer).pad(UIStyles.Spacing.MEDIUM);

        int localPlayerId = NetworkManager.getInstance().getMyId();
        currentMinigame = MinigameFactory.createMinigame(minigameType, localPlayerId);
        currentMinigame.initialize();

        startGameHandler = new StartGamePacketHandler();
        NetworkManager.getInstance().registerClientHandler(startGameHandler);

        Gdx.app.log("GameScreen", "Started minigame: " + minigameType.getDisplayName());
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        currentMinigame.handleInput(delta);

        currentMinigame.update(delta);

        int localPlayerId = NetworkManager.getInstance().getMyId();
        int currentScore = currentMinigame.getScores().getOrDefault(localPlayerId, 0);
        scoreLabel.setText(currentScore + " pts");

        if (currentMinigame.isFinished()) {
            // Ir a la pantalla de resultados con los scores del minijuego
            game.setScreen(new ResultsScreen(game, ResultsScreen.ViewMode.VIEW_TOP3, currentMinigame.getScores()));
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        currentMinigame.render(batch, shapeRenderer);

        uiStage.act(delta);
        uiStage.draw();
    }

    /**
     * Maneja el redimensionamiento de la ventana.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    @Override
    public void resize(int width, int height) {
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
        if (currentMinigame != null) {
            currentMinigame.resize(width, height);
        }
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
        if (currentMinigame != null) {
            currentMinigame.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (startGameHandler != null) {
            NetworkManager.getInstance().unregisterClientHandler(startGameHandler);
            startGameHandler = null;
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
    }

    /**
     * Manejador de paquetes para iniciar el juego.
     */
    private final class StartGamePacketHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(Packets.StartGame.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.StartGame startGame) {
                game.setScreen(new GameScreen(game, MinigameType.valueOf(startGame.minigameType)));
            }
        }
    }

}
