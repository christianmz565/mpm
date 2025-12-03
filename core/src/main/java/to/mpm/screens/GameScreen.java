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
import to.mpm.minigames.GameConstants;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla principal de juego que ejecuta el minijuego seleccionado.
 */
public class GameScreen implements Screen {
    /** Instancia del juego principal. */
    private final Main game;
    /** Tipo de minijuego a ejecutar. */
    private final MinigameType minigameType;
    /** Indica si este es el minijuego final. */
    private final boolean isFinale;
    /** Instancia del minijuego actual. */
    private Minigame currentMinigame;
    /** Lote de sprites para renderizado. */
    private SpriteBatch batch;
    /** Renderizador de formas. */
    private ShapeRenderer shapeRenderer;
    /** Stage para la superposición de UI. */
    private Stage uiStage;
    /** Skin para estilizar componentes de UI. */
    private Skin skin;
    /** Etiqueta que muestra la puntuación del jugador. */
    private Label scoreLabel;
    /** Contenedor para la visualización de puntuación. */
    private Table scoreContainer;
    /** Etiqueta que muestra el temporizador. */
    private Label timerLabel;
    /** Etiqueta que muestra la ronda actual. */
    private Label roundLabel;
    /** Temporizador del juego en segundos. */
    private float gameTimer;
    /** Indica si el juego ha terminado. */
    private boolean gameEnded = false;
    /** Ronda en curso (1-based). */
    private final int currentRound;
    /** Total de rondas configuradas. */
    private final int totalRounds;
    /** Puntuación anterior para detectar cambios. */
    private int previousScore = 0;
    /** Lista de popups activos. */
    private final List<ScorePopup> scorePopups = new ArrayList<>();

    /** Manejador de paquete para iniciar el juego. */
    private StartGamePacketHandler startGameHandler;
    /** Manejador de paquete para mostrar el marcador. */
    private ShowScoreboardPacketHandler showScoreboardHandler;
    /** Manejador de paquete para mostrar resultados. */
    private ShowResultsPacketHandler showResultsHandler;

    /**
     * Clase interna para representar un popup de puntuación.
     */
    private static class ScorePopup {
        Label label;
        float age;
        float fadeTime;

        ScorePopup(Label label, float fadeTime) {
            this.label = label;
            this.age = 0f;
            this.fadeTime = fadeTime;
        }

        boolean update(float delta) {
            age += delta;
            if (age >= fadeTime) {
                return true;
            }
            float alpha = 1f - (age / fadeTime);
            label.getColor().a = alpha;
            return false;
        }
    }

    /**
     * Construye una nueva pantalla de juego.
     *
     * @param game         instancia del juego principal
     * @param minigameType tipo de minijuego a ejecutar
     */
    public GameScreen(Main game, MinigameType minigameType) {
        this(game, minigameType, 0, 0);
    }

    /**
     * Construye una nueva pantalla de juego con contexto de ronda.
     */
    public GameScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds) {
        this.game = game;
        this.minigameType = minigameType;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
        this.isFinale = (minigameType == MinigameType.THE_FINALE);
        this.gameTimer = isFinale ? GameConstants.TheFinale.GAME_DURATION : GameConstants.Timing.DEFAULT_GAME_DURATION;
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
        uiStage.addActor(uiRoot);

        Table topBar = new Table();
        topBar.setBackground(UIStyles.createSemiTransparentBackground(0f, 0f, 0f, 0.4f));
        topBar.pad(UIStyles.Spacing.SMALL);

        Table topLeftContainer = new Table();
        topLeftContainer.left();
        topBar.add(topLeftContainer).width(150).left();

        Table topCenterContainer = new Table();
        topCenterContainer.setBackground(UIStyles.createSemiTransparentBackground(0f, 0f, 0f, 0.3f));
        topCenterContainer.pad(UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM, UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM);

        int roundToDisplay = this.currentRound;
        int totalRoundsToDisplay = this.totalRounds;
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
        if ((roundToDisplay <= 0 || totalRoundsToDisplay <= 0) && flowManager.isInitialized()) {
            roundToDisplay = flowManager.getCurrentRound();
            totalRoundsToDisplay = flowManager.getTotalRounds();
        }

        if (!isFinale) {
            if (roundToDisplay > 0 && totalRoundsToDisplay > 0) {
                roundLabel = new Label("Ronda " + roundToDisplay + "/" + totalRoundsToDisplay, skin);
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

        if (!isFinale) {
            scoreContainer = new Table(skin);
            scoreContainer.setBackground(UIStyles.createSemiTransparentBackground(0f, 0f, 0f, 0.3f));
            scoreContainer.pad(UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM, UIStyles.Spacing.SMALL, UIStyles.Spacing.MEDIUM);

            scoreLabel = new Label("0 pts", skin);
            scoreLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            scoreLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
            scoreContainer.add(scoreLabel);
            topRightContainer.add(scoreContainer);
        }

        topBar.add(topRightContainer).width(150).right();

        uiRoot.add(topBar).expandX().fillX().top().row();
        uiRoot.add().expand();

        int localPlayerId = NetworkManager.getInstance().getMyId();
        currentMinigame = MinigameFactory.createMinigame(minigameType, localPlayerId);
        currentMinigame.initialize();

        NetworkManager networkManager = NetworkManager.getInstance();
        startGameHandler = new StartGamePacketHandler();
        networkManager.registerClientHandler(startGameHandler);

        showScoreboardHandler = new ShowScoreboardPacketHandler();
        networkManager.registerClientHandler(showScoreboardHandler);

        showResultsHandler = new ShowResultsPacketHandler();
        networkManager.registerClientHandler(showResultsHandler);

        Gdx.app.log("GameScreen", "Started minigame: " + minigameType.getDisplayName());
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        if (gameEnded) {
            Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        currentMinigame.handleInput(delta);
        currentMinigame.update(delta);

        if (!isFinale) {
            gameTimer -= delta;
            if (timerLabel != null) {
                int seconds = Math.max(0, (int) Math.ceil(gameTimer));
                timerLabel.setText("Tiempo: " + seconds);
            }
        }

        if (!isFinale && scoreLabel != null) {
            int localPlayerId = NetworkManager.getInstance().getMyId();
            int currentScore = currentMinigame.getScores().getOrDefault(localPlayerId, 0);
            
            if (currentScore != previousScore) {
                int increment = currentScore - previousScore;
                String incrementText = increment >= 0 ? "+" + increment : String.valueOf(increment);
                scoreLabel.setText(currentScore + " pts (" + incrementText + ")");
                scoreLabel.setColor(increment >= 0 ? UIStyles.Colors.SECONDARY : UIStyles.Colors.ERROR);
                
                ScorePopup popup = new ScorePopup(scoreLabel, 1.5f);
                scorePopups.clear();
                scorePopups.add(popup);
                previousScore = currentScore;
            }

            scorePopups.removeIf(popup -> {
                boolean finished = popup.update(delta);
                if (finished) {
                    scoreLabel.setText(currentScore + " pts");
                    scoreLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
                }
                return finished;
            });
        }

        boolean timeUp = !isFinale && gameTimer <= 0;
        boolean minigameFinished = currentMinigame.isFinished();

        if (timeUp || minigameFinished) {
            endGame();
            return;
        }

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        currentMinigame.render(batch, shapeRenderer);

        uiStage.act(delta);
        uiStage.draw();
    }

    /**
     * Termina el juego actual y maneja la transición a la siguiente pantalla.
     * <p>
     * Solo el host maneja la lógica de flujo del juego y envía los paquetes
     * correspondientes.
     */
    private void endGame() {
        if (gameEnded) {
            return;
        }
        gameEnded = true;

        java.util.Map<Integer, Integer> roundScores = currentMinigame.getScores();
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();

        if (NetworkManager.getInstance().isHost()) {
            flowManager.endRound(roundScores);

            if (flowManager.isGameComplete()) {
                to.mpm.minigames.manager.ManagerPackets.ShowResults resultsPacket = new to.mpm.minigames.manager.ManagerPackets.ShowResults(
                        flowManager.getTotalScores());
                NetworkManager.getInstance().broadcastFromHost(resultsPacket);

                game.setScreen(new ResultsScreen(game, flowManager.getTotalScores()));
            } else {
                to.mpm.minigames.manager.ManagerPackets.ShowScoreboard scoreboardPacket = new to.mpm.minigames.manager.ManagerPackets.ShowScoreboard(
                        flowManager.getCurrentRound(),
                        flowManager.getTotalRounds(),
                        flowManager.getTotalScores());
                NetworkManager.getInstance().broadcastFromHost(scoreboardPacket);

                int localPlayerId = NetworkManager.getInstance().getMyId();
                game.setScreen(new ScoreboardScreen(game, flowManager.getTotalScores(),
                        flowManager.getCurrentRound(), flowManager.getTotalRounds(), localPlayerId));
            }

            dispose();
        } else {
            Gdx.app.log("GameScreen", "Game ended, waiting for host instructions");
        }
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
        if (showScoreboardHandler != null) {
            NetworkManager.getInstance().unregisterClientHandler(showScoreboardHandler);
            showScoreboardHandler = null;
        }
        if (showResultsHandler != null) {
            NetworkManager.getInstance().unregisterClientHandler(showResultsHandler);
            showResultsHandler = null;
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
    }

    /**
     * Cambia a la pantalla de marcador cuando el host lo solicita.
     */
    private final class ShowScoreboardPacketHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(to.mpm.minigames.manager.ManagerPackets.ShowScoreboard.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.ShowScoreboard showScoreboard) {
                Gdx.app.log("GameScreen", "Received ShowScoreboard for round " + showScoreboard.currentRound);
                int localPlayerId = NetworkManager.getInstance().getMyId();
                game.setScreen(new ScoreboardScreen(game, showScoreboard.allPlayerScores,
                        showScoreboard.currentRound, showScoreboard.totalRounds, localPlayerId));
                dispose();
            }
        }
    }

    /**
     * Cambia a la pantalla de resultados cuando finaliza la partida.
     */
    private final class ShowResultsPacketHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(to.mpm.minigames.manager.ManagerPackets.ShowResults.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.ShowResults showResults) {
                Gdx.app.log("GameScreen", "Received ShowResults packet");
                game.setScreen(new ResultsScreen(game, showResults.finalScores));
                dispose();
            }
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
                int roundNumber = startGame.currentRound > 0 ? startGame.currentRound : 1;
                int roundsTotal = startGame.totalRounds > 0 ? startGame.totalRounds : 1;
                game.setScreen(
                        new MinigameIntroScreen(game, MinigameType.valueOf(startGame.minigameType), roundNumber, roundsTotal));
            }
        }
    }

}
