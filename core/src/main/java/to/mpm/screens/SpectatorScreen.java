package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.MinigameFactory;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;

/**
 * Pantalla de espectador para observar el juego sin participar.
 * <p>
 * Muestra el minijuego en curso sin permitir interacción.
 */
public class SpectatorScreen implements Screen {
    /** Instancia del juego principal. */
    private final Main game;
    /** Tipo de minijuego que se está jugando. */
    private final MinigameType minigameType;
    /** Ronda actual. */
    private final int currentRound;
    /** Total de rondas. */
    private final int totalRounds;
    /** Instancia del minijuego en curso. */
    private Minigame currentMinigame;
    /** Sprite batch para renderizado. */
    private SpriteBatch batch;
    /** Shape renderer para renderizado. */
    private ShapeRenderer shapeRenderer;
    /** Stage para la UI overlay. */
    private Stage uiStage;
    /** Skin para estilizar componentes de UI. */
    private Skin skin;
    /** Etiqueta para mostrar el temporizador. */
    private Label timerLabel;
    /** Etiqueta para mostrar la ronda actual. */
    private Label roundLabel;
    /** Temporizador del juego. */
    private float gameTimer = 10f;
    /** Manejador de paquete para mostrar el marcador. */
    private ClientPacketHandler showScoreboardHandler;
    /** Manejador de paquete para mostrar resultados. */
    private ClientPacketHandler showResultsHandler;
    /** Manejador de paquete para iniciar la siguiente ronda. */
    private ClientPacketHandler startNextRoundHandler;

    /**
     * Construye una nueva pantalla de espectador.
     *
     * @param game         instancia del juego principal
     * @param minigameType tipo de minijuego que se está jugando
     * @param currentRound ronda actual
     * @param totalRounds  total de rondas
     */
    public SpectatorScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds) {
        this.game = game;
        this.minigameType = minigameType;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
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
        uiStage.addActor(uiRoot);

        Table topBar = new Table();
        topBar.setBackground(UIStyles.createSemiTransparentBackground(0f, 0f, 0f, 0.4f));
        topBar.pad(UIStyles.Spacing.SMALL);

        Table topLeftContainer = new Table();
        topLeftContainer.left();
        
        Table spectatorBadge = new Table();
        spectatorBadge.setBackground(UIStyles.createSemiTransparentBackground(0.9f, 0.3f, 0.3f, 0.3f));
        spectatorBadge.pad(UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM, UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM);
        
        Label spectatorLabel = new Label("ESPECTANDO", skin);
        spectatorLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        spectatorLabel.setColor(UIStyles.Colors.ACCENT);
        spectatorBadge.add(spectatorLabel);
        
        topLeftContainer.add(spectatorBadge);
        topBar.add(topLeftContainer).width(150).left();

        Table topCenterContainer = new Table();
        topCenterContainer.setBackground(UIStyles.createSemiTransparentBackground(0f, 0f, 0f, 0.3f));
        topCenterContainer.pad(UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM, UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM);

        boolean isFinale = minigameType == MinigameType.THE_FINALE;
        
        if (!isFinale) {
            if (currentRound > 0 && totalRounds > 0) {
                roundLabel = new Label("Ronda " + currentRound + "/" + totalRounds, skin);
                roundLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
                roundLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
                topCenterContainer.add(roundLabel).padRight(UIStyles.Spacing.MEDIUM);
            }

            timerLabel = new Label("Tiempo: 60", skin);
            timerLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            timerLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
            topCenterContainer.add(timerLabel);
        } else {
            Label finaleLabel = new Label("LA FINAL", skin);
            finaleLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            finaleLabel.setColor(UIStyles.Colors.SECONDARY);
            topCenterContainer.add(finaleLabel);
        }

        topBar.add(topCenterContainer).expandX().center();

        Table topRightContainer = new Table();
        topRightContainer.right();
        topBar.add(topRightContainer).width(150).right();

        uiRoot.add(topBar).expandX().fillX().top().row();
        uiRoot.add().expand();

        currentMinigame = MinigameFactory.createMinigame(minigameType, -1);
        currentMinigame.initialize();

        NetworkManager networkManager = NetworkManager.getInstance();

        showScoreboardHandler = new ShowScoreboardPacketHandler();
        networkManager.registerClientHandler(showScoreboardHandler);

        showResultsHandler = new ShowResultsPacketHandler();
        networkManager.registerClientHandler(showResultsHandler);

        startNextRoundHandler = new StartNextRoundPacketHandler();
        networkManager.registerClientHandler(startNextRoundHandler);

        Gdx.app.log("SpectatorScreen", "Spectating minigame: " + minigameType.getDisplayName());
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        currentMinigame.update(delta);

        boolean isFinale = minigameType == MinigameType.THE_FINALE;
        if (!isFinale && timerLabel != null) {
            gameTimer -= delta;
            int seconds = Math.max(0, (int) Math.ceil(gameTimer));
            timerLabel.setText("Tiempo: " + seconds);
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
        uiStage.getViewport().update(width, height, true);
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
        if (showScoreboardHandler != null) {
            NetworkManager.getInstance().unregisterClientHandler(showScoreboardHandler);
        }
        if (showResultsHandler != null) {
            NetworkManager.getInstance().unregisterClientHandler(showResultsHandler);
        }
        if (startNextRoundHandler != null) {
            NetworkManager.getInstance().unregisterClientHandler(startNextRoundHandler);
        }

        if (currentMinigame != null) {
            currentMinigame.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
    }

    /**
     * Manejador para paquetes ShowScoreboard, transiciona a los espectadores a la
     * pantalla de marcador.
     */
    private final class ShowScoreboardPacketHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(to.mpm.minigames.manager.ManagerPackets.ShowScoreboard.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.ShowScoreboard showScoreboard) {
                Gdx.app.log("SpectatorScreen", "Received ShowScoreboard for round " + showScoreboard.currentRound);
                int localPlayerId = NetworkManager.getInstance().getMyId();
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new ScoreboardScreen(game, showScoreboard.allPlayerScores,
                            showScoreboard.currentRound, showScoreboard.totalRounds, localPlayerId));
                });
            }
        }
    }

    /**
     * Manejador para paquetes ShowResults, transiciona a los espectadores a la
     * pantalla de resultados.
     */
    private final class ShowResultsPacketHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(to.mpm.minigames.manager.ManagerPackets.ShowResults.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.ShowResults showResults) {
                Gdx.app.log("SpectatorScreen", "Received ShowResults packet");
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new ResultsScreen(game, showResults.finalScores));
                });
            }
        }
    }

    /**
     * Manejador para paquetes StartNextRound, transiciona a los espectadores a la
     * siguiente ronda o minijuego.
     */
    private final class StartNextRoundPacketHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(to.mpm.minigames.manager.ManagerPackets.StartNextRound.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.StartNextRound startNextRound) {
                Gdx.app.log("SpectatorScreen", "Received StartNextRound packet");
                Gdx.app.postRunnable(() -> {
                    MinigameType nextMinigameType = MinigameType.valueOf(startNextRound.minigameType);
                    int myId = NetworkManager.getInstance().getMyId();

                    boolean shouldParticipate = startNextRound.participatingPlayerIds != null &&
                            startNextRound.participatingPlayerIds.contains(myId);

                    if (shouldParticipate) {
                        game.setScreen(new MinigameIntroScreen(game, nextMinigameType,
                                startNextRound.roundNumber, totalRounds));
                    } else {
                        game.setScreen(new MinigameIntroScreen(game, nextMinigameType,
                                startNextRound.roundNumber, totalRounds, true));
                    }
                    dispose();
                });
            }
        }
    }
}
