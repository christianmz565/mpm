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
import java.util.Comparator;
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

    private final Main game; //!< Referencia a la instancia principal del juego
    private Stage stage; //!< Escenario para elementos de UI
    private Skin skin; //!< Skin para los componentes de UI

    private ViewMode currentView = ViewMode.VIEW_TOP3; //!< Modo de vista actual
    private Table contentContainer; //!< Contenedor del contenido que cambia según el modo

    private List<PlayerResult> results; //!< Lista de resultados de jugadores

    /**
     * Constructor de la pantalla de resultados con vista por defecto.
     *
     * @param game referencia a la instancia principal del juego
     */
    public ResultsScreen(Main game) {
        this(game, ViewMode.VIEW_TOP3);
    }

    /**
     * Constructor de la pantalla de resultados con modo de vista específico.
     *
     * @param game     referencia a la instancia principal del juego
     * @param viewMode modo de vista inicial
     */
    public ResultsScreen(Main game, ViewMode viewMode) {
        this(game, viewMode, null);
    }
    
    /**
     * Constructor de la pantalla de resultados con modo de vista específico y resultados.
     *
     * @param game     referencia a la instancia principal del juego
     * @param viewMode modo de vista inicial
     * @param scores   mapa de playerId -> score del minijuego
     */
    public ResultsScreen(Main game, ViewMode viewMode, java.util.Map<Integer, Integer> scores) {
        this.game = game;
        this.currentView = viewMode;
        this.results = new ArrayList<>();

        if (scores != null && !scores.isEmpty()) {
            // Usar resultados reales del minijuego
            to.mpm.network.NetworkManager nm = to.mpm.network.NetworkManager.getInstance();
            for (java.util.Map.Entry<Integer, Integer> entry : scores.entrySet()) {
                String playerName = nm.getConnectedPlayers().get(entry.getKey());
                if (playerName == null) playerName = "Player " + entry.getKey();
                results.add(new PlayerResult(playerName, entry.getValue(), 0));
            }
        } else {
            // Datos de prueba
            results.add(new PlayerResult("Nombre", 123000, 1));
            results.add(new PlayerResult("Nombre", 123000, 2));
            results.add(new PlayerResult("Nombre", 123000, 3));
            results.add(new PlayerResult("Nombre", 123000, 4));
            results.add(new PlayerResult("Nombre", 123000, 5));
            results.add(new PlayerResult("Nombre", 123000, 6));
        }

        results.sort(Comparator.comparingInt((PlayerResult r) -> r.score).reversed());

        for (int i = 0; i < results.size(); i++) {
            results.get(i).rank = i + 1;
        }
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
        root.add(contentContainer).expand().fill().row();
        
        // Agregar botones de navegación
        Table buttonsTable = new Table();
        buttonsTable.pad(UIStyles.Spacing.LARGE);
        
        TextButton backToMenuButton = new TextButton("Menú Principal", skin);
        backToMenuButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        buttonsTable.add(backToMenuButton).width(200).height(50).padRight(UIStyles.Spacing.MEDIUM);
        
        // Solo mostrar botón de continuar si hay conexión activa
        if (to.mpm.network.NetworkManager.getInstance().isConnected()) {
            TextButton continueButton = new TextButton("Continuar", skin);
            continueButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    boolean isHost = to.mpm.network.NetworkManager.getInstance().isHost();
                    game.setScreen(new LobbyScreen(game, isHost));
                    dispose();
                }
            });
            buttonsTable.add(continueButton).width(200).height(50);
        }
        
        root.add(buttonsTable).bottom();

        renderCurrentView();
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

        PlayerResult first = results.size() > 0 ? results.get(0) : null;
        PlayerResult second = results.size() > 1 ? results.get(1) : null;
        PlayerResult third = results.size() > 2 ? results.get(2) : null;

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
                PlayerResult pr = results.get(i);
                Table item = new ScoreItem(skin)
                        .playerName(pr.name)
                        .rank(pr.rank)
                        .score(pr.score)
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
    private Table createPodiumPosition(PlayerResult result, int position) {
        Table positionTable = new Table(skin);
        positionTable.pad(UIStyles.Spacing.MEDIUM);

        Label rankLabel = new Label(position + "° puesto", skin);
        rankLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
        positionTable.add(rankLabel).padBottom(UIStyles.Spacing.SMALL).row();

        Label nameLabel = new Label(result.name, skin);
        nameLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        positionTable.add(nameLabel).padBottom(UIStyles.Spacing.SMALL).row();

        Label scoreLabel = new Label(String.valueOf(result.score), skin);
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
        for (PlayerResult pr : results) {
            Table scoreItem = new ScoreItem(skin)
                    .rank(pr.rank)
                    .playerName(pr.name)
                    .score(pr.score)
                    .highlighted(pr.rank == 1)
                    .build();

            scoresContainer.add(scoreItem).fillX().expandX().padBottom(UIStyles.Spacing.SMALL).row();
        }

        ScrollPane scrollPane = new ScrollPane(scoresContainer, skin);
        scrollPane.setFadeScrollBars(false);
        listTable.add(scrollPane).width(UIStyles.Layout.PANEL_MAX_WIDTH)
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

        PlayerResult winner = results.get(0);

        Table spotlightTable = new Table();

        Table winnerPanel = new Table(skin);
        winnerPanel.pad(UIStyles.Spacing.XLARGE);

        Label nameLabel = new Label(winner.name, skin);
        nameLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        winnerPanel.add(nameLabel).padBottom(UIStyles.Spacing.MEDIUM).row();

        Label scoreLabel = new Label(String.valueOf(winner.score), skin);
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
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(UIStyles.Colors.BACKGROUND.r, UIStyles.Colors.BACKGROUND.g,
                UIStyles.Colors.BACKGROUND.b, UIStyles.Colors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
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
        stage.dispose();
    }

    /**
     * Clase interna para almacenar resultados de jugador.
     */
    private static class PlayerResult {
        String name; //!< Nombre del jugador
        int score; //!< Puntaje del jugador
        int rank; //!< Ranking del jugador

        /**
         * Constructor del resultado de jugador.
         *
         * @param name  nombre del jugador
         * @param score puntaje del jugador
         * @param rank  ranking del jugador
         */
        PlayerResult(String name, int score, int rank) {
            this.name = name;
            this.score = score;
            this.rank = rank;
        }
    }
}
