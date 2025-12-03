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
 * <p>
 * Sigue la siguiente animación:
 * <p>
 * 1. SCROLLING: Desplaza la lista completa de abajo hacia arriba (2-3 segundos)
 * <p>
 * 2. PODIUM: Muestra la vista de podio (3 segundos)
 * <p>
 * 3. FADE: Se desvanece a negro (0.5 segundos)
 * <p>
 * 4. WINNER: Muestra el foco en el ganador (3 segundos)
 * <p>
 * 5. RETURN: Devuelve a todos al lobby
 */
public class ResultsScreen implements Screen {
    /**
     * Enumeración de modos de vista disponibles.
     */
    public enum ViewMode {
        VIEW_TOP3,
        VIEW_FULL_LIST,
        VIEW_WINNER_SPOTLIGHT
    }

    /**
     * Estados de animación para la secuencia automática.
     */
    private enum AnimationState {
        SCROLLING,
        PODIUM,
        FADE,
        WINNER,
        RETURN,
        COMPLETE
    }

    /** Referencia a la instancia principal del juego. */
    private final Main game;
    /** Escenario para elementos de UI. */
    private Stage stage;
    /** Skin para los componentes de UI. */
    private Skin skin;

    /** Modo de vista actual. */
    private ViewMode currentView = ViewMode.VIEW_TOP3;
    /** Contenedor del contenido que cambia según el modo. */
    private Table contentContainer;
    /** ScrollPane para la lista completa. */
    private ScrollPane fullListScrollPane;

    /** Lista de resultados de jugadores. */
    private List<to.mpm.utils.PlayerData> results;
    /** Estado actual de la animación. */
    private AnimationState animationState = AnimationState.SCROLLING;
    /** Temporizador para el estado actual. */
    private float stateTimer = 0f;
    /** Handler para volver al lobby. */
    private to.mpm.network.handlers.ClientPacketHandler returnToLobbyHandler;

    /**
     * Constructor de la pantalla de resultados con scores map.
     *
     * @param game        referencia a la instancia principal del juego
     * @param finalScores mapa de playerId a puntaje final
     */
    public ResultsScreen(Main game, java.util.Map<Integer, Integer> finalScores) {
        this.game = game;
        this.results = new ArrayList<>();

        java.util.Map<Integer, String> playerNames = to.mpm.network.NetworkManager.getInstance().getConnectedPlayers();
        for (java.util.Map.Entry<Integer, Integer> entry : finalScores.entrySet()) {
            int playerId = entry.getKey();
            String playerName = playerNames.getOrDefault(playerId, "Player " + playerId);
            results.add(new to.mpm.utils.PlayerData(playerId, playerName, entry.getValue()));
        }
        java.util.Collections.sort(results);

        Gdx.app.log("ResultsScreen", "Initialized with " + results.size() + " players");
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

        if (results.size() <= 3) {
            animationState = AnimationState.WINNER;
            currentView = ViewMode.VIEW_WINNER_SPOTLIGHT;
            Gdx.app.log("ResultsScreen", "Skipping animations, going directly to winner (≤3 players)");
        } else if (results.size() == 4) {
            animationState = AnimationState.PODIUM;
            currentView = ViewMode.VIEW_TOP3;
            Gdx.app.log("ResultsScreen", "Skipping scrolling list, starting at podium (4 players)");
        } else {
            animationState = AnimationState.SCROLLING;
            currentView = ViewMode.VIEW_FULL_LIST;
            Gdx.app.log("ResultsScreen", "Starting full animation sequence (5+ players)");
        }
        renderCurrentView();

        returnToLobbyHandler = new ReturnToLobbyHandler();
        to.mpm.network.NetworkManager.getInstance().registerClientHandler(returnToLobbyHandler);

        Gdx.app.log("ResultsScreen", "Animation sequence initialized");
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
     * Renderiza la vista de podio mostrando los jugadores 2-4.
     * <p>
     * Disposición: 3° puesto | 2° puesto (más alto) | 4° puesto
     */
    private void renderPodiumView() {
        Table podiumTable = new Table();

        to.mpm.utils.PlayerData second = results.size() > 1 ? results.get(1) : null;
        to.mpm.utils.PlayerData third = results.size() > 2 ? results.get(2) : null;
        to.mpm.utils.PlayerData fourth = results.size() > 3 ? results.get(3) : null;

        Table positions = new Table();

        if (third != null) {
            Table thirdPlace = createPodiumPosition(third, 3);
            positions.add(thirdPlace).bottom().padBottom(50f).padRight(UIStyles.Spacing.MEDIUM);
        }

        if (second != null) {
            Table secondPlace = createPodiumPosition(second, 2);
            positions.add(secondPlace).bottom().padRight(UIStyles.Spacing.MEDIUM).padBottom(UIStyles.Spacing.XXLARGE);
        }

        if (fourth != null) {
            Table fourthPlace = createPodiumPosition(fourth, 4);
            positions.add(fourthPlace).bottom().padBottom(50f);
        }

        podiumTable.add(positions).row();

        if (results.size() > 4) {
            Table remainingPlayers = new Table();
            for (int i = 4; i < Math.min(results.size(), 7); i++) {
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
     * Renderiza la vista de lista completa con jugadores desde el 4° lugar.
     */
    private void renderFullListView() {
        Table listTable = new Table();

        Table scoresContainer = new Table();
        for (int i = 3; i < results.size(); i++) {
            to.mpm.utils.PlayerData pr = results.get(i);
            Table scoreItem = new ScoreItem(skin)
                    .rank(i + 1)
                    .playerName(pr.getPlayerName())
                    .score(pr.getScore())
                    .highlighted(false)
                    .build();

            scoresContainer.add(scoreItem).fillX().expandX().padBottom(UIStyles.Spacing.LARGE).row();
        }

        fullListScrollPane = new ScrollPane(scoresContainer, skin);
        fullListScrollPane.setFadeScrollBars(false);
        listTable.add(fullListScrollPane).width(UIStyles.Layout.PANEL_MAX_WIDTH)
                .height(UIStyles.Layout.LIST_MAX_HEIGHT);

        contentContainer.add(listTable);
    }

    /**
     * Renderiza la vista de foco en el ganador con sprites de patos.
     * <p>
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

        switch (animationState) {
            case SCROLLING:
                updateScrollingState();
                if (stateTimer >= 2.5f) {
                    stateTimer = 0f;
                    if (results.size() <= 3) {
                        animationState = AnimationState.FADE;
                        to.mpm.ui.transitions.ScreenTransition.fadeOut(stage, () -> {
                            animationState = AnimationState.WINNER;
                            stateTimer = 0f;
                            currentView = ViewMode.VIEW_WINNER_SPOTLIGHT;
                            renderCurrentView();
                            to.mpm.ui.transitions.ScreenTransition.fadeIn(stage, null);
                            Gdx.app.log("ResultsScreen", "Skipped podium, transition to WINNER state");
                        });
                    } else {
                        animationState = AnimationState.PODIUM;
                        currentView = ViewMode.VIEW_TOP3;
                        renderCurrentView();
                        Gdx.app.log("ResultsScreen", "Transition to PODIUM state");
                    }
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
                break;
        }

        Gdx.gl.glClearColor(UIStyles.Colors.BACKGROUND.r, UIStyles.Colors.BACKGROUND.g,
                UIStyles.Colors.BACKGROUND.b, UIStyles.Colors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Actualiza el estado de desplazamiento de la lista completa.
     */
    private void updateScrollingState() {
        if (fullListScrollPane != null) {
            float progress = Math.min(1.0f, stateTimer / 2.5f);
            float maxScroll = fullListScrollPane.getMaxY();
            fullListScrollPane.setScrollY(maxScroll * (1.0f - progress));
        }
    }

    /**
     * Devuelve a todos los jugadores al lobby.
     * <p>
     * Solo el host envía el paquete para iniciar la transición.
     */
    private void returnToLobby() {
        if (to.mpm.network.NetworkManager.getInstance().isHost()) {
            Gdx.app.log("ResultsScreen", "Host returning everyone to lobby");
            to.mpm.minigames.manager.ManagerPackets.ReturnToLobby packet = new to.mpm.minigames.manager.ManagerPackets.ReturnToLobby();
            to.mpm.network.NetworkManager.getInstance().broadcastFromHost(packet);

            to.mpm.minigames.manager.GameFlowManager.getInstance().reset();
            game.setScreen(new LobbyScreen(game, true));
            dispose();
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
     * Manejador de paquetes para volver al lobby.
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
