package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.ui.UIStyles;
import to.mpm.ui.components.StyledButton;

/**
 * Pantalla de espectador para observar el juego sin participar.
 * Soporta dos modos ver todos los jugadores a la vez o ver un grupo específico con flechas de navegación
 */
public class SpectatorScreen implements Screen {
    private final Main game; //!< instancia del juego principal
    private Stage stage; //!< stage para renderizar componentes de UI
    private Skin skin; //!< skin para estilizar componentes

    private ViewMode currentMode = ViewMode.ALL_PLAYERS; //!< modo de visualización actual
    private int currentGroupIndex = 0; //!< índice del grupo actual en modo SINGLE_GROUP
    private Table contentContainer; //!< contenedor del área de contenido del juego
    private Table controlsContainer; //!< contenedor de controles de navegación

    /**
     * Enumeración de modos de visualización disponibles.
     */
    public enum ViewMode {
        ALL_PLAYERS, //!< espectando a todos los jugadores
        SINGLE_GROUP //!< espectando a un grupo específico con navegación
    }

    /**
     * Construye una nueva pantalla de espectador.
     *
     * @param game instancia del juego principal
     */
    public SpectatorScreen(Main game) {
        this.game = game;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table modeSelector = new Table();
        modeSelector.pad(UIStyles.Spacing.MEDIUM);

        Label modeLabel = new Label("Espectando a:", skin);
        modeLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        modeSelector.add(modeLabel).padRight(UIStyles.Spacing.MEDIUM);

        ButtonGroup<TextButton> modeGroup = new ButtonGroup<>();
        modeGroup.setMinCheckCount(1);
        modeGroup.setMaxCheckCount(1);

        TextButton todosButton = new TextButton("todos", skin, "toggle");
        TextButton grupoButton = new TextButton("un grupo", skin, "toggle");

        todosButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (todosButton.isChecked()) {
                    setViewMode(ViewMode.ALL_PLAYERS);
                }
            }
        });

        grupoButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (grupoButton.isChecked()) {
                    setViewMode(ViewMode.SINGLE_GROUP);
                }
            }
        });

        modeSelector.add(todosButton).padRight(UIStyles.Spacing.SMALL);
        modeSelector.add(grupoButton);

        root.add(modeSelector).top().expandX().row();

        contentContainer = new Table();
        root.add(contentContainer).expand().fill().pad(UIStyles.Spacing.MEDIUM).row();

        controlsContainer = new Table();
        root.add(controlsContainer).bottom().expandX().row();

        modeGroup.add(todosButton);
        modeGroup.add(grupoButton);

        todosButton.setChecked(true);
    }

    /**
     * Establece el modo de visualización actual.
     *
     * @param mode nuevo modo de visualización
     */
    private void setViewMode(ViewMode mode) {
        this.currentMode = mode;
        renderCurrentView();
    }

    /**
     * Renderiza la vista actual según el modo seleccionado.
     */
    private void renderCurrentView() {
        contentContainer.clear();
        controlsContainer.clear();

        switch (currentMode) {
            case ALL_PLAYERS:
                renderAllPlayersView();
                break;
            case SINGLE_GROUP:
                renderSingleGroupView();
                renderNavigationControls();
                break;
        }
    }

    /**
     * Renderiza la vista mostrando todos los jugadores a la vez.
     * Esto mostraría una cuadrícula o pantalla dividida de todas las vistas de
     * jugadores.
     */
    private void renderAllPlayersView() {
        Table playersGrid = new Table();

        Label placeholder = new Label("Vista de todos los jugadores\n(Grid de 2x2 o 3x2 dependiendo del número)", skin);
        placeholder.setWrap(true);
        placeholder.setAlignment(com.badlogic.gdx.utils.Align.center);
        playersGrid.add(placeholder).center().pad(UIStyles.Spacing.XLARGE);

        contentContainer.add(playersGrid).expand().fill();
    }

    /**
     * Renderiza la vista mostrando un solo grupo/jugador.
     * Esto proporciona una vista enfocada en un jugador o grupo pequeño.
     */
    private void renderSingleGroupView() {
        Table groupView = new Table();

        Label placeholder = new Label(
                "Vista del grupo " + (currentGroupIndex + 1) + "\n(Vista enfocada en jugador/grupo específico)", skin);
        placeholder.setWrap(true);
        placeholder.setAlignment(com.badlogic.gdx.utils.Align.center);
        groupView.add(placeholder).center().pad(UIStyles.Spacing.XLARGE);

        contentContainer.add(groupView).expand().fill();
    }

    /**
     * Renderiza las flechas de navegación para cambiar entre grupos.
     */
    private void renderNavigationControls() {
        Table navTable = new Table();
        navTable.pad(UIStyles.Spacing.MEDIUM);

        navTable.add(
                new StyledButton(skin)
                        .text("<-")
                        .width(100f)
                        .onClick(this::previousGroup)
                        .build())
                .padRight(UIStyles.Spacing.XLARGE);

        Label groupLabel = new Label("Grupo " + (currentGroupIndex + 1), skin);
        groupLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        navTable.add(groupLabel).padRight(UIStyles.Spacing.XLARGE);

        navTable.add(
                new StyledButton(skin)
                        .text("->")
                        .width(100f)
                        .onClick(this::nextGroup)
                        .build());

        controlsContainer.add(navTable);
    }

    /**
     * Navega al grupo anterior en modo de vista de grupo único.
     */
    private void previousGroup() {
        int maxGroups = 4;
        currentGroupIndex = (currentGroupIndex - 1 + maxGroups) % maxGroups;
        renderCurrentView();
    }

    /**
     * Navega al siguiente grupo en modo de vista de grupo único.
     */
    private void nextGroup() {
        int maxGroups = 4;
        currentGroupIndex = (currentGroupIndex + 1) % maxGroups;
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
        skin.dispose();
    }
}
