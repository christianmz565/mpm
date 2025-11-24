package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.ScoreItem;
import to.mpm.utils.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pantalla de marcador mostrando los puntajes acumulados después de cada ronda.
 * Los jugadores se ordenan por puntaje de forma descendente.
 * Avanza automáticamente a la siguiente ronda después de 5 segundos.
 */
public class ScoreboardScreen implements Screen {
    private final Main game;
    private final int currentRound;
    private final int totalRounds;
    private final int localPlayerId;
    
    private Stage stage;
    private Skin skin;
    private ScrollPane scoresScroll;
    private Table scoresContainer;
    private Label countdownLabel;
    private Label roundLabel;
    
    private float countdownTimer = 5.0f;
    private List<PlayerData> sortedPlayers;
    private StartNextRoundHandler startNextRoundHandler;
    private ShowResultsHandler showResultsHandler;

    /**
     * Constructor de la pantalla de marcador.
     *
     * @param game referencia a la instancia principal del juego
     * @param scores mapa de playerId a puntaje acumulado
     * @param currentRound ronda actual que acaba de terminar (1-based)
     * @param totalRounds total de rondas en el juego
     * @param localPlayerId ID del jugador local
     */
    public ScoreboardScreen(Main game, Map<Integer, Integer> scores, int currentRound, int totalRounds, int localPlayerId) {
        this.game = game;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
        this.localPlayerId = localPlayerId;
        
        // Convert scores to PlayerData list
        sortedPlayers = new ArrayList<>();
        Map<Integer, String> playerNames = NetworkManager.getInstance().getConnectedPlayers();
        for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
            int playerId = entry.getKey();
            String playerName = playerNames.getOrDefault(playerId, "Player " + playerId);
            sortedPlayers.add(new PlayerData(playerId, playerName, entry.getValue()));
        }
        Collections.sort(sortedPlayers); // Sort by score descending
    }

    /**
     * Inicializa la pantalla de marcador con el título y la lista de puntajes.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(stage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Round indicator
        roundLabel = new Label("Round " + currentRound + "/" + totalRounds + " Complete!", skin);
        roundLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        roundLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        root.add(roundLabel).padBottom(UIStyles.Spacing.MEDIUM).padTop(UIStyles.Spacing.LARGE).row();

        Label titleLabel = new Label("Scoreboard", skin);
        titleLabel.setFontScale(UIStyles.Typography.SUBTITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        root.add(titleLabel).padBottom(UIStyles.Spacing.LARGE).row();

        // Scores list
        scoresContainer = new Table();
        scoresScroll = new ScrollPane(scoresContainer, skin);
        scoresScroll.setFadeScrollBars(false);

        root.add(scoresScroll).width(UIStyles.Layout.PANEL_MAX_WIDTH)
                .height(UIStyles.Layout.LIST_MAX_HEIGHT)
                .expand().row();

        updateScoresList();

        // Countdown timer
        countdownLabel = new Label("Next round in: 5", skin);
        countdownLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        countdownLabel.setColor(UIStyles.Colors.ACCENT);
        root.add(countdownLabel).padTop(UIStyles.Spacing.LARGE).padBottom(UIStyles.Spacing.LARGE);

        // Register packet handlers
        startNextRoundHandler = new StartNextRoundHandler();
        NetworkManager.getInstance().registerClientHandler(startNextRoundHandler);
        
        showResultsHandler = new ShowResultsHandler();
        NetworkManager.getInstance().registerClientHandler(showResultsHandler);

        Gdx.app.log("ScoreboardScreen", "Showing scoreboard for round " + currentRound + "/" + totalRounds);
    }

    /**
     * Actualiza la lista de puntajes, resaltando al jugador local.
     */
    private void updateScoresList() {
        scoresContainer.clear();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            PlayerData player = sortedPlayers.get(i);
            boolean isLocalPlayer = player.getPlayerId() == localPlayerId;

            Table scoreItem = new ScoreItem(skin)
                    .playerName(player.getPlayerName() + (isLocalPlayer ? " (You)" : ""))
                    .score(player.getScore())
                    .build();

            // Highlight local player
            if (isLocalPlayer) {
                scoreItem.setBackground(skin.getDrawable("window"));
            }

            scoresContainer.add(scoreItem).fillX().expandX().padBottom(UIStyles.Spacing.SMALL).row();
        }

        // Scroll to local player position (center if possible)
        stage.act(0);
        float localPlayerY = 0;
        for (int i = 0; i < sortedPlayers.size(); i++) {
            if (sortedPlayers.get(i).getPlayerId() == localPlayerId) {
                localPlayerY = i * (scoresContainer.getCells().first().getPrefHeight() + UIStyles.Spacing.SMALL);
                break;
            }
        }
        
        float scrollHeight = scoresScroll.getHeight();
        float contentHeight = scoresContainer.getHeight();
        float targetScroll = localPlayerY - scrollHeight / 2;
        targetScroll = Math.max(0, Math.min(targetScroll, contentHeight - scrollHeight));
        scoresScroll.setScrollY(targetScroll);
    }

    /**
     * Renderiza la pantalla y actualiza el temporizador de cuenta regresiva.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        // Update countdown
        countdownTimer -= delta;
        if (countdownLabel != null) {
            int seconds = Math.max(0, (int) Math.ceil(countdownTimer));
            countdownLabel.setText("Next round in: " + seconds);
        }

        // Host determines next action when timer expires
        if (countdownTimer <= 0 && NetworkManager.getInstance().isHost()) {
            advanceToNextRound();
        }

        Gdx.gl.glClearColor(UIStyles.Colors.BACKGROUND.r, UIStyles.Colors.BACKGROUND.g,
                UIStyles.Colors.BACKGROUND.b, UIStyles.Colors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Host: Advances to the next round or shows results.
     * Determines the next minigame and broadcasts StartNextRound packet.
     */
    private void advanceToNextRound() {
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
        flowManager.startRound();

        MinigameType nextGame;
        List<Integer> participatingPlayers = null;

        if (flowManager.shouldPlayFinale()) {
            // Last round: play finale with top players
            nextGame = MinigameType.THE_FINALE;
            participatingPlayers = flowManager.getFinalePlayerIds();
            
            // Edge case: only 1 player remaining
            if (participatingPlayers.size() == 1) {
                Gdx.app.log("ScoreboardScreen", "Only 1 player remaining, skipping finale");
                to.mpm.minigames.manager.ManagerPackets.ShowResults resultsPacket = 
                    new to.mpm.minigames.manager.ManagerPackets.ShowResults(flowManager.getTotalScores());
                NetworkManager.getInstance().broadcastFromHost(resultsPacket);
                game.setScreen(new ResultsScreen(game, flowManager.getTotalScores()));
                dispose();
                return;
            }
            
            Gdx.app.log("ScoreboardScreen", "Starting finale with " + participatingPlayers.size() + " players");
        } else {
            // Normal round: select random game
            to.mpm.minigames.selection.GameSelectionStrategy strategy = new to.mpm.minigames.selection.RandomGameSelection();
            int playerCount = NetworkManager.getInstance().getPlayerCount();
            nextGame = strategy.selectGame(playerCount);
            Gdx.app.log("ScoreboardScreen", "Selected next game: " + nextGame.getDisplayName());
        }

        // Broadcast next round info
        to.mpm.minigames.manager.ManagerPackets.StartNextRound packet = 
            new to.mpm.minigames.manager.ManagerPackets.StartNextRound(
                flowManager.getCurrentRound(),
                nextGame.name(),
                participatingPlayers
            );
        NetworkManager.getInstance().broadcastFromHost(packet);

        // Transition to game screen or spectator screen
        if (participatingPlayers == null || participatingPlayers.contains(localPlayerId)) {
            game.setScreen(new GameScreen(game, nextGame));
        } else {
            game.setScreen(new SpectatorScreen(game));
        }
        dispose();
    }

    /**
     * Maneja el redimensionamiento de la ventana.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        NetworkManager nm = NetworkManager.getInstance();
        if (startNextRoundHandler != null) {
            nm.unregisterClientHandler(startNextRoundHandler);
            startNextRoundHandler = null;
        }
        if (showResultsHandler != null) {
            nm.unregisterClientHandler(showResultsHandler);
            showResultsHandler = null;
        }
        if (stage != null) {
            stage.dispose();
        }
    }

    /**
     * Manejador de paquetes para iniciar la siguiente ronda.
     */
    private final class StartNextRoundHandler implements ClientPacketHandler {
        @Override
        public List<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(to.mpm.minigames.manager.ManagerPackets.StartNextRound.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.StartNextRound startNextRound) {
                Gdx.app.log("ScoreboardScreen", "Received StartNextRound: " + startNextRound.minigameType);
                
                MinigameType minigameType = MinigameType.valueOf(startNextRound.minigameType);
                
                // Check if local player is participating
                if (startNextRound.participatingPlayerIds == null || 
                    startNextRound.participatingPlayerIds.contains(localPlayerId)) {
                    game.setScreen(new GameScreen(game, minigameType));
                } else {
                    // Eliminated from finale
                    game.setScreen(new SpectatorScreen(game));
                }
                dispose();
            }
        }
    }

    /**
     * Manejador de paquetes para mostrar pantalla de resultados.
     */
    private final class ShowResultsHandler implements ClientPacketHandler {
        @Override
        public List<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(to.mpm.minigames.manager.ManagerPackets.ShowResults.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.ShowResults showResults) {
                Gdx.app.log("ScoreboardScreen", "Received ShowResults, transitioning to results screen");
                game.setScreen(new ResultsScreen(game, showResults.finalScores));
                dispose();
            }
        }
    }
}
