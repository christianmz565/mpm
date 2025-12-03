package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pantalla de marcador mostrando los puntajes acumulados después de cada ronda.
 * <p>
 * Los jugadores se ordenan por puntaje de forma descendente.
 * <p>
 * Avanza automáticamente a la siguiente ronda después de 5 segundos.
 */
public class ScoreboardScreen implements Screen {
    private final Main game; //!< referencia a la instancia principal del juego
    private final int currentRound; //!< ronda actual que acaba de terminar
    private final int totalRounds; //!< total de rondas en el juego
    private final int localPlayerId; //!< ID del jugador local

    private Stage stage; //!< stage para renderizar componentes de UI
    private Skin skin; //!< skin para estilizar componentes
    private ScrollPane scoresScroll; //!< scroll para la lista de puntajes
    private Table scoresContainer; //!< contenedor para la lista de puntajes
    private Label countdownLabel; //!< etiqueta para mostrar la cuenta regresiva
    private Label roundLabel; //!< etiqueta para mostrar la ronda actual

    private float countdownTimer = 5.0f; //!< temporizador de cuenta regresiva en segundos
    private List<PlayerData> sortedPlayers; //!< lista de jugadores ordenados por puntaje
    private StartNextRoundHandler startNextRoundHandler; //!< manejador de paquete para iniciar la siguiente ronda
    private ShowResultsHandler showResultsHandler; //!< manejador de paquete para mostrar resultados

    /**
     * Constructor de la pantalla de marcador.
     *
     * @param game          referencia a la instancia principal del juego
     * @param scores        mapa de playerId a puntaje acumulado
     * @param currentRound  ronda actual que acaba de terminar (1-based)
     * @param totalRounds   total de rondas en el juego
     * @param localPlayerId ID del jugador local
     */
    public ScoreboardScreen(Main game, Map<Integer, Integer> scores, int currentRound, int totalRounds,
            int localPlayerId) {
        this.game = game;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
        this.localPlayerId = localPlayerId;

        sortedPlayers = new ArrayList<>();
        Map<Integer, String> playerNames = NetworkManager.getInstance().getConnectedPlayers();
        Set<Integer> processedPlayers = new HashSet<>();
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();

        for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
            int playerId = entry.getKey();
            if (flowManager.isSpectator(playerId))
                continue;

            String playerName = playerNames.getOrDefault(playerId, "Player " + playerId);
            sortedPlayers.add(new PlayerData(playerId, playerName, entry.getValue()));
            processedPlayers.add(playerId);
        }

        for (Map.Entry<Integer, String> entry : playerNames.entrySet()) {
            int playerId = entry.getKey();
            if (flowManager.isSpectator(playerId))
                continue;

            if (!processedPlayers.contains(playerId)) {
                sortedPlayers.add(new PlayerData(playerId, entry.getValue(), 0));
            }
        }

        Collections.sort(sortedPlayers);
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
        root.top();
        root.pad(UIStyles.Spacing.LARGE);
        root.defaults().padBottom(UIStyles.Spacing.MEDIUM);
        stage.addActor(root);

        roundLabel = new Label("Round " + currentRound + "/" + totalRounds + " Complete!", skin);
        roundLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        roundLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        roundLabel.setAlignment(Align.center);
        root.add(roundLabel).expandX().center().row();

        Label titleLabel = new Label("Scoreboard", skin);
        titleLabel.setFontScale(UIStyles.Typography.SUBTITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        titleLabel.setAlignment(Align.center);
        root.add(titleLabel).expandX().center().padBottom(UIStyles.Spacing.LARGE).row();

        scoresContainer = new Table();
        scoresScroll = new ScrollPane(scoresContainer, skin);
        scoresScroll.setFadeScrollBars(false);

        root.add(scoresScroll)
                .width(UIStyles.Layout.PANEL_MAX_WIDTH)
                .maxHeight(UIStyles.Layout.LIST_MAX_HEIGHT)
                .expand()
                .fill()
                .padBottom(UIStyles.Spacing.LARGE)
                .row();

        updateScoresList();

        countdownLabel = new Label("Next round in: 5", skin);
        countdownLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        countdownLabel.setColor(UIStyles.Colors.ACCENT);
        countdownLabel.setAlignment(Align.center);
        root.add(countdownLabel)
                .width(UIStyles.Layout.PANEL_MAX_WIDTH)
                .center()
                .padTop(UIStyles.Spacing.LARGE);

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
                    .rank(i + 1)
                    .playerName(player.getPlayerName() + (isLocalPlayer ? " (You)" : ""))
                    .score(player.getScore())
                    .highlighted(isLocalPlayer)
                    .build();

            scoresContainer.add(scoreItem).fillX().expandX().padBottom(UIStyles.Spacing.LARGE).row();
        }

        stage.act(0);
        float localPlayerY = 0;

        for (int i = 0; i < sortedPlayers.size(); i++) {
            if (sortedPlayers.get(i).getPlayerId() == localPlayerId) {
                break;
            }
            if (i < scoresContainer.getChildren().size) {
                localPlayerY += scoresContainer.getChildren().get(i).getHeight() + UIStyles.Spacing.LARGE;
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
        countdownTimer -= delta;
        if (countdownLabel != null) {
            int seconds = Math.max(0, (int) Math.ceil(countdownTimer));
            countdownLabel.setText("Next round in: " + seconds);
        }

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
     * Avanza a la siguiente ronda o a la pantalla de resultados si es la final.
     * <p>
     * El host selecciona el siguiente minijuego y notifica a los clientes.
     */
    private void advanceToNextRound() {
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
        flowManager.startRound();

        MinigameType nextGame;
        List<Integer> participatingPlayers = null;

        if (flowManager.shouldPlayFinale()) {
            nextGame = MinigameType.THE_FINALE;
            participatingPlayers = flowManager.getFinalePlayerIds();

            if (participatingPlayers.size() == 1) {
                Gdx.app.log("ScoreboardScreen", "Only 1 player remaining, skipping finale");
                to.mpm.minigames.manager.ManagerPackets.ShowResults resultsPacket = new to.mpm.minigames.manager.ManagerPackets.ShowResults(
                        flowManager.getTotalScores());
                NetworkManager.getInstance().broadcastFromHost(resultsPacket);
                game.setScreen(new ResultsScreen(game, flowManager.getTotalScores()));
                dispose();
                return;
            }

            Gdx.app.log("ScoreboardScreen", "Starting finale with " + participatingPlayers.size() + " players");
        } else {
            int activePlayerCount = flowManager.getActivePlayerCount();
            nextGame = to.mpm.minigames.selection.RandomGameSelection.selectGame(activePlayerCount);
            Gdx.app.log("ScoreboardScreen",
                    "Selected next game: " + nextGame.getDisplayName() + " for " + activePlayerCount + " players");
        }

        to.mpm.minigames.manager.ManagerPackets.StartNextRound packet = new to.mpm.minigames.manager.ManagerPackets.StartNextRound(
                flowManager.getCurrentRound(),
                nextGame.name(),
                participatingPlayers);
        NetworkManager.getInstance().broadcastFromHost(packet);

        if (participatingPlayers == null || participatingPlayers.contains(localPlayerId)) {
            // Show intro screen before the game
            game.setScreen(new MinigameIntroScreen(game, nextGame, flowManager.getCurrentRound(), flowManager.getTotalRounds()));
        } else {
            // Show intro screen before spectating
            game.setScreen(
                    new MinigameIntroScreen(game, nextGame, flowManager.getCurrentRound(), flowManager.getTotalRounds(), true));
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

                to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager
                        .getInstance();
                if (flowManager.isSpectator(localPlayerId)) {
                    // Show intro screen before spectating
                    game.setScreen(new MinigameIntroScreen(game, minigameType, startNextRound.roundNumber, totalRounds, true));
                } else if (startNextRound.participatingPlayerIds == null ||
                        startNextRound.participatingPlayerIds.contains(localPlayerId)) {
                    // Show intro screen before the game
                    game.setScreen(new MinigameIntroScreen(game, minigameType, startNextRound.roundNumber, totalRounds));
                } else {
                    // Show intro screen before spectating
                    game.setScreen(new MinigameIntroScreen(game, minigameType, startNextRound.roundNumber, totalRounds, true));
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
