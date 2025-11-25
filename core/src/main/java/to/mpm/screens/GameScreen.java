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
import java.util.ArrayList;
import java.util.List;

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
    private Table incrementsContainer; // !< contenedor para los incrementos de puntuación
    private Label timerLabel; // !< etiqueta que muestra el temporizador
    private Label roundLabel; // !< etiqueta que muestra la ronda actual
    private float gameTimer = 10f; // !< temporizador del juego en segundos
    private boolean gameEnded = false; // !< indica si el juego ha terminado
    private final int currentRound; // !< ronda en curso (1-based)
    private final int totalRounds; // !< total de rondas configuradas
    private int previousScore = 0; // !< puntuación anterior para detectar cambios
    private final List<ScorePopup> scorePopups = new ArrayList<>(); // !< lista de popups activos

    private StartGamePacketHandler startGameHandler;
    private ShowScoreboardPacketHandler showScoreboardHandler;
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
                return true; // Fully faded
            }
            // Update alpha based on age
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
        uiRoot.top();
        uiStage.addActor(uiRoot);

        // Timer and round info at top center
        Table topCenterContainer = new Table();
        topCenterContainer.pad(UIStyles.Spacing.MEDIUM);

        int roundToDisplay = this.currentRound;
        int totalRoundsToDisplay = this.totalRounds;
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
        if ((roundToDisplay <= 0 || totalRoundsToDisplay <= 0) && flowManager.isInitialized()) {
            roundToDisplay = flowManager.getCurrentRound();
            totalRoundsToDisplay = flowManager.getTotalRounds();
        }

        if (roundToDisplay > 0 && totalRoundsToDisplay > 0) {
            roundLabel = new Label("Round " + roundToDisplay + "/" + totalRoundsToDisplay, skin);
            roundLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            roundLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
            topCenterContainer.add(roundLabel).padBottom(UIStyles.Spacing.TINY).row();
        }

        // Check if this is the finale (no timer)
        boolean isFinale = minigameType == MinigameType.THE_FINALE;
        if (!isFinale) {
            timerLabel = new Label("Time: 60", skin);
            timerLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            timerLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
            topCenterContainer.add(timerLabel).row();
        }

        uiRoot.add(topCenterContainer).expandX().row();

        // Score display at top right
        Table scoreContainer = new Table(skin);
        scoreContainer.pad(UIStyles.Spacing.MEDIUM);

        Table scoreContent = new Table();
        scoreLabel = new Label("0 pts", skin);
        scoreLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        scoreLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        scoreContent.add(scoreLabel).row();

        // Container for score increment popups
        incrementsContainer = new Table();
        incrementsContainer.top();
        scoreContent.add(incrementsContainer).padTop(UIStyles.Spacing.TINY).right().row();

        scoreContainer.add(scoreContent);
        
        Table rightContainer = new Table();
        rightContainer.top().right();
        rightContainer.add(scoreContainer).pad(UIStyles.Spacing.MEDIUM);
        uiRoot.add(rightContainer).expand().top().right();

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
            // Prevent any updates after game has ended
            Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        currentMinigame.handleInput(delta);
        currentMinigame.update(delta);

        // Update timer (unless it's the finale)
        boolean isFinale = minigameType == MinigameType.THE_FINALE;
        if (!isFinale) {
            gameTimer -= delta;
            if (timerLabel != null) {
                int seconds = Math.max(0, (int) Math.ceil(gameTimer));
                timerLabel.setText("Time: " + seconds);
            }
        }

        // Update score display and handle score popups
        int localPlayerId = NetworkManager.getInstance().getMyId();
        int currentScore = currentMinigame.getScores().getOrDefault(localPlayerId, 0);
        scoreLabel.setText(currentScore + " pts");
        
        // Detect score changes and create popup
        if (currentScore != previousScore) {
            int increment = currentScore - previousScore;
            Label popupLabel = new Label("+" + increment, skin);
            popupLabel.setFontScale(UIStyles.Typography.SMALL_SCALE);
            popupLabel.setColor(UIStyles.Colors.SECONDARY);
            popupLabel.setAlignment(com.badlogic.gdx.utils.Align.right);
            
            ScorePopup popup = new ScorePopup(popupLabel, 2.0f); // 2 second fade
            scorePopups.add(popup);
            previousScore = currentScore;
        }
        
        // Update score popups and remove faded ones
        scorePopups.removeIf(popup -> popup.update(delta));
        
        // Rebuild increments container
        incrementsContainer.clear();
        for (ScorePopup popup : scorePopups) {
            incrementsContainer.add(popup.label).right().row();
        }

        // Check if game should end (timer expired OR minigame finished)
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
     * Ends the current game and transitions to the appropriate screen.
     * If host, updates GameFlowManager and broadcasts scoreboard.
     * All clients transition to ScoreboardScreen or ResultsScreen.
     */
    private void endGame() {
        if (gameEnded) {
            return; // Prevent multiple calls
        }
        gameEnded = true;

        java.util.Map<Integer, Integer> roundScores = currentMinigame.getScores();
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
        
        if (NetworkManager.getInstance().isHost()) {
            // Host: update game flow and broadcast results
            flowManager.endRound(roundScores);
            
            if (flowManager.isGameComplete()) {
                // All rounds complete, go to results screen
                to.mpm.minigames.manager.ManagerPackets.ShowResults resultsPacket = 
                    new to.mpm.minigames.manager.ManagerPackets.ShowResults(flowManager.getTotalScores());
                NetworkManager.getInstance().broadcastFromHost(resultsPacket);
                
                game.setScreen(new ResultsScreen(game, flowManager.getTotalScores()));
            } else {
                // More rounds to play, show scoreboard
                to.mpm.minigames.manager.ManagerPackets.ShowScoreboard scoreboardPacket = 
                    new to.mpm.minigames.manager.ManagerPackets.ShowScoreboard(
                        flowManager.getCurrentRound(),
                        flowManager.getTotalRounds(),
                        flowManager.getTotalScores()
                    );
                NetworkManager.getInstance().broadcastFromHost(scoreboardPacket);
                
                int localPlayerId = NetworkManager.getInstance().getMyId();
                game.setScreen(new ScoreboardScreen(game, flowManager.getTotalScores(), 
                    flowManager.getCurrentRound(), flowManager.getTotalRounds(), localPlayerId));
            }

            dispose();
        } else {
            // Client: just wait for host to send ShowScoreboard or ShowResults packet
            // For now, stay on this screen - handlers will transition us
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
                game.setScreen(new GameScreen(game, MinigameType.valueOf(startGame.minigameType), roundNumber, roundsTotal));
            }
        }
    }

}
