package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.ScoreItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de resultados mostrando el ranking final del juego.
 * Soporta múltiples modos de vista:
 * - VIEW_TOP3: Muestra podio con los 3 primeros jugadores (posiciones 2°, 1°,
 * 3°)
 * - VIEW_FULL_LIST: Muestra todos los jugadores ordenados por puntaje con
 * scroll
 * - VIEW_WINNER_SPOTLIGHT: Muestra el nombre del ganador en el centro con
 * sprites de patos alrededor
 * 
 * Plays automatic animation sequence:
 * 1. SCROLLING: Scrolls full list from bottom to top (2-3 seconds)
 * 2. PODIUM: Shows podium view (3 seconds)
 * 3. FADE: Fades to black (0.5 seconds)
 * 4. WINNER: Shows winner spotlight (3 seconds)
 * 5. RETURN: Returns everyone to lobby
 */
public class ResultsScreen implements Screen {
    /**
     * Enumeración de modos de vista disponibles.
     */
    public enum ViewMode {
        VIEW_TOP3, //!< Vista de podio
        VIEW_FULL_LIST, //!< Vista de lista completa
        VIEW_WINNER_SPOTLIGHT //!< Vista de foco en el ganador
    }

    /**
     * Animation states for automatic sequence.
     */
    private enum AnimationState {
        SCROLLING,
        PODIUM,
        FADE,
        WINNER,
        RETURN,
        COMPLETE
    }

    private final Main game; //!< Referencia a la instancia principal del juego
    private Stage stage; //!< Escenario para elementos de UI
    private Skin skin; //!< Skin para los componentes de UI

    private ViewMode currentView = ViewMode.VIEW_TOP3; //!< Modo de vista actual
    private Table contentContainer; //!< Contenedor del contenido que cambia según el modo
    private ScrollPane fullListScrollPane; //!< ScrollPane para la lista completa

    private List<to.mpm.utils.PlayerData> results; //!< Lista de resultados de jugadores
    private AnimationState animationState = AnimationState.SCROLLING; //!< Estado actual de la animación
    private float stateTimer = 0f; //!< Temporizador para el estado actual
    private to.mpm.network.handlers.ClientPacketHandler returnToLobbyHandler; //!< Handler para volver al lobby

    /**
     * Constructor de la pantalla de resultados con scores map.
     *
     * @param game referencia a la instancia principal del juego
     * @param finalScores mapa de playerId a puntaje final
     */
    public ResultsScreen(Main game, java.util.Map<Integer, Integer> finalScores) {
        this.game = game;
        this.results = new ArrayList<>();

        // Convert scores to PlayerData list
        java.util.Map<Integer, String> playerNames = to.mpm.network.NetworkManager.getInstance().getConnectedPlayers();
        for (java.util.Map.Entry<Integer, Integer> entry : finalScores.entrySet()) {
            int playerId = entry.getKey();
            String playerName = playerNames.getOrDefault(playerId, "Player " + playerId);
            results.add(new to.mpm.utils.PlayerData(playerId, playerName, entry.getValue()));
        }
        java.util.Collections.sort(results); // Sort by score descending

        Gdx.app.log("ResultsScreen", "Initialized with " + results.size() + " players");
    }

    /**
     * Constructor de la pantalla de resultados con vista por defecto.
     * @deprecated Use constructor with scores map instead
     *
     * @param game referencia a la instancia principal del juego
     */
    @Deprecated
    public ResultsScreen(Main game) {
        this(game, ViewMode.VIEW_TOP3);
    }

    /**
     * Constructor de la pantalla de resultados con modo de vista específico.
     * @deprecated Use constructor with scores map instead
     *
     * @param game     referencia a la instancia principal del juego
     * @param viewMode modo de vista inicial
     */
    @Deprecated
    public ResultsScreen(Main game, ViewMode viewMode) {
        this.game = game;
        this.currentView = viewMode;
        this.results = new ArrayList<>();

        // Dummy data for testing
        results.add(new to.mpm.utils.PlayerData(1, "Nombre", 123000));
        results.add(new to.mpm.utils.PlayerData(2, "Nombre", 123000));
        results.add(new to.mpm.utils.PlayerData(3, "Nombre", 123000));
        results.add(new to.mpm.utils.PlayerData(4, "Nombre", 123000));
        results.add(new to.mpm.utils.PlayerData(5, "Nombre", 123000));
        results.add(new to.mpm.utils.PlayerData(6, "Nombre", 123000));

        java.util.Collections.sort(results);
    }

    /**
     * Inicializa la pantalla de resultados.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(stage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        contentContainer = new Table();
        root.add(contentContainer).expand().fill();

        // Start with full list view for scrolling animation
        currentView = ViewMode.VIEW_FULL_LIST;
        renderCurrentView();

        // Register ReturnToLobby packet handler
        returnToLobbyHandler = new ReturnToLobbyHandler();
        to.mpm.network.NetworkManager.getInstance().registerClientHandler(returnToLobbyHandler);

        Gdx.app.log("ResultsScreen", "Starting animation sequence");
    }

    /**
     * Renderiza la vista actual según el modo seleccionado.
     */
    private void renderCurrentView() {
        contentContainer.clear();

        switch (currentView) {
            case VIEW_TOP3:
                renderPodiumView();
                break;
            case VIEW_FULL_LIST:
                renderFullListView();
                break;
            case VIEW_WINNER_SPOTLIGHT:
                renderWinnerSpotlightView();
                break;
        }
    }

    /**
     * Renderiza la vista de podio mostrando los 3 primeros jugadores.
     * Disposición: 2° puesto | 1° puesto (más alto) | 3° puesto
     */
    private void renderPodiumView() {
        Table podiumTable = new Table();

        to.mpm.utils.PlayerData first = results.size() > 0 ? results.get(0) : null;
        to.mpm.utils.PlayerData second = results.size() > 1 ? results.get(1) : null;
        to.mpm.utils.PlayerData third = results.size() > 2 ? results.get(2) : null;

        Table positions = new Table();

        if (second != null) {
            Table secondPlace = createPodiumPosition(second, 2);
            positions.add(secondPlace).bottom().padBottom(50f).padRight(UIStyles.Spacing.MEDIUM);
        }

        if (first != null) {
            Table firstPlace = createPodiumPosition(first, 1);
            positions.add(firstPlace).bottom().padRight(UIStyles.Spacing.MEDIUM).padBottom(UIStyles.Spacing.XXLARGE);
        }

        if (third != null) {
            Table thirdPlace = createPodiumPosition(third, 3);
            positions.add(thirdPlace).bottom().padBottom(50f);
        }

        podiumTable.add(positions).row();

        if (results.size() > 3) {
            Table remainingPlayers = new Table();
            for (int i = 3; i < Math.min(results.size(), 6); i++) {
                to.mpm.utils.PlayerData pr = results.get(i);
                Table item = new ScoreItem(skin)
                        .playerName(pr.getPlayerName())
                        .rank(i + 1)
                        .score(pr.getScore())
                        .build();
                remainingPlayers.add(item).fillX().expandX().padBottom(UIStyles.Spacing.TINY).row();
            }
            podiumTable.add(remainingPlayers).width(UIStyles.Layout.PANEL_MAX_WIDTH).row();
        }

        contentContainer.add(podiumTable);
    }

    /**
     * Crea un panel de posición del podio para un jugador.
     *
     * @param result   resultado del jugador
     * @param position posición en el podio (1, 2 o 3)
     * @return tabla con el panel de posición
     */
    private Table createPodiumPosition(to.mpm.utils.PlayerData result, int position) {
        Table positionTable = new Table(skin);
        positionTable.pad(UIStyles.Spacing.MEDIUM);

        Label rankLabel = new Label(position + "° puesto", skin);
        rankLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
        positionTable.add(rankLabel).padBottom(UIStyles.Spacing.SMALL).row();

        Label nameLabel = new Label(result.getPlayerName(), skin);
        nameLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        positionTable.add(nameLabel).padBottom(UIStyles.Spacing.SMALL).row();

        Label scoreLabel = new Label(String.valueOf(result.getScore()), skin);
        scoreLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        scoreLabel.setColor(position == 1 ? UIStyles.Colors.SECONDARY : UIStyles.Colors.TEXT_PRIMARY);
        positionTable.add(scoreLabel);

        return positionTable;
    }

    /**
     * Renderiza la vista de lista completa con todos los jugadores ordenados por
     * ranking.
     * Incluye una flecha hacia arriba indicando scroll.
     */
    private void renderFullListView() {
        Table listTable = new Table();

        Label arrow = new Label("↑", skin);
        arrow.setFontScale(UIStyles.Typography.TITLE_SCALE);
        listTable.add(arrow).padBottom(UIStyles.Spacing.MEDIUM).row();

        Table scoresContainer = new Table();
        for (int i = 0; i < results.size(); i++) {
            to.mpm.utils.PlayerData pr = results.get(i);
            Table scoreItem = new ScoreItem(skin)
                    .rank(i + 1)
                    .playerName(pr.getPlayerName())
                    .score(pr.getScore())
                    .highlighted(i == 0)
                    .build();

            scoresContainer.add(scoreItem).fillX().expandX().padBottom(UIStyles.Spacing.SMALL).row();
        }

        fullListScrollPane = new ScrollPane(scoresContainer, skin);
        fullListScrollPane.setFadeScrollBars(false);
        listTable.add(fullListScrollPane).width(UIStyles.Layout.PANEL_MAX_WIDTH)
                .height(UIStyles.Layout.LIST_MAX_HEIGHT);

        contentContainer.add(listTable);
    }

    /**
     * Renderiza la vista de foco en el ganador con sprites de patos organizados en
     * círculo.
     * Muestra el nombre y puntaje del ganador en el centro.
     */
    private void renderWinnerSpotlightView() {
        if (results.isEmpty())
            return;

        to.mpm.utils.PlayerData winner = results.get(0);

        Table spotlightTable = new Table();

        Table winnerPanel = new Table(skin);
        winnerPanel.pad(UIStyles.Spacing.XLARGE);

        Label nameLabel = new Label(winner.getPlayerName(), skin);
        nameLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        winnerPanel.add(nameLabel).padBottom(UIStyles.Spacing.MEDIUM).row();

        Label scoreLabel = new Label(String.valueOf(winner.getScore()), skin);
        scoreLabel.setFontScale(UIStyles.Typography.TITLE_SCALE * 1.2f);
        scoreLabel.setColor(UIStyles.Colors.SECONDARY);
        winnerPanel.add(scoreLabel);

        spotlightTable.add(winnerPanel).size(400f);

        contentContainer.add(spotlightTable);
    }

    /**
     * Cambia a un modo de vista diferente.
     *
     * @param viewMode nuevo modo de vista
     */
    public void setViewMode(ViewMode viewMode) {
        this.currentView = viewMode;
        renderCurrentView();
    }

    /**
     * Renderiza la pantalla y actualiza la animación del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        stateTimer += delta;

        // Update animation state machine
        switch (animationState) {
            case SCROLLING:
                updateScrollingState();
                if (stateTimer >= 2.5f) {
                    stateTimer = 0f;
                    animationState = AnimationState.PODIUM;
                    currentView = ViewMode.VIEW_TOP3;
                    renderCurrentView();
                    Gdx.app.log("ResultsScreen", "Transition to PODIUM state");
                }
                break;

            case PODIUM:
                if (stateTimer >= 3.0f) {
                    stateTimer = 0f;
                    animationState = AnimationState.FADE;
                    to.mpm.ui.transitions.ScreenTransition.fadeOut(stage, () -> {
                        animationState = AnimationState.WINNER;
                        stateTimer = 0f;
                        currentView = ViewMode.VIEW_WINNER_SPOTLIGHT;
                        renderCurrentView();
                        to.mpm.ui.transitions.ScreenTransition.fadeIn(stage, null);
                        Gdx.app.log("ResultsScreen", "Transition to WINNER state");
                    });
                    Gdx.app.log("ResultsScreen", "Starting FADE transition");
                }
                break;

            case FADE:
                // Wait for fade transition to complete
                break;

            case WINNER:
                if (stateTimer >= 3.0f) {
                    stateTimer = 0f;
                    animationState = AnimationState.RETURN;
                    returnToLobby();
                }
                break;

            case RETURN:
            case COMPLETE:
                // Waiting for transition or already done
                break;
        }

        Gdx.gl.glClearColor(UIStyles.Colors.BACKGROUND.r, UIStyles.Colors.BACKGROUND.g,
                UIStyles.Colors.BACKGROUND.b, UIStyles.Colors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Updates the scrolling animation state.
     * Smoothly scrolls from bottom to top of the player list.
     */
    private void updateScrollingState() {
        if (fullListScrollPane != null) {
            float progress = Math.min(1.0f, stateTimer / 2.5f);
            float maxScroll = fullListScrollPane.getMaxY();
            fullListScrollPane.setScrollY(maxScroll * (1.0f - progress));
        }
    }

    /**
     * Host: Returns everyone to the lobby.
     * Broadcasts ReturnToLobby packet and resets GameFlowManager.
     */
    private void returnToLobby() {
        if (to.mpm.network.NetworkManager.getInstance().isHost()) {
            Gdx.app.log("ResultsScreen", "Host returning everyone to lobby");
            to.mpm.minigames.manager.ManagerPackets.ReturnToLobby packet = 
                new to.mpm.minigames.manager.ManagerPackets.ReturnToLobby();
            to.mpm.network.NetworkManager.getInstance().broadcastFromHost(packet);
            
            to.mpm.minigames.manager.GameFlowManager.getInstance().reset();
            game.setScreen(new LobbyScreen(game, true));
            dispose();
        }
        // Clients will transition when they receive the packet
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
        if (returnToLobbyHandler != null) {
            to.mpm.network.NetworkManager.getInstance().unregisterClientHandler(returnToLobbyHandler);
            returnToLobbyHandler = null;
        }
        if (stage != null) {
            stage.dispose();
        }
    }

    /**
     * Handler for ReturnToLobby packet.
     */
    private final class ReturnToLobbyHandler implements to.mpm.network.handlers.ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends to.mpm.network.NetworkPacket>> receivablePackets() {
            return java.util.List.of(to.mpm.minigames.manager.ManagerPackets.ReturnToLobby.class);
        }

        @Override
        public void handle(to.mpm.network.handlers.ClientPacketContext context, to.mpm.network.NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.ReturnToLobby) {
                Gdx.app.log("ResultsScreen", "Received ReturnToLobby, transitioning to lobby");
                to.mpm.minigames.manager.GameFlowManager.getInstance().reset();
                game.setScreen(new LobbyScreen(game, false));
                dispose();
            }
        }
    }

}
