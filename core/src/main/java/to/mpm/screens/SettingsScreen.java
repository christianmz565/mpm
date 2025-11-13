package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.ui.UIStyles;
import to.mpm.ui.components.VolumeSlider;

/**
 * Pantalla de ajustes que se superpone sobre la pantalla actual como un overlay.
 * No utiliza el sistema de setScreen(), sino que se renderiza sobre la pantalla activa.
 */
public class SettingsScreen {
    private final Main game; //!< instancia del juego principal
    private final Screen previousScreen; //!< pantalla sobre la que se renderiza (solo para referencia)
    private Stage stage; //!< stage para renderizar componentes de UI
    private Skin skin; //!< skin para estilizar componentes
    private ShapeRenderer shapeRenderer; //!< renderer para dibujar la capa de fondo semi-transparente
    private float currentVolume = 0.25f; //!< volumen actual (25% por defecto)

    /**
     * Construye una nueva pantalla de ajustes.
     *
     * @param game           instancia del juego principal
     * @param previousScreen pantalla anterior a la que volver al cerrar
     */
    public SettingsScreen(Main game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    public void show() {
        stage = new Stage(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table settingsPanel = new Table(skin);
        settingsPanel.pad(UIStyles.Spacing.LARGE);

        Table headerTable = new Table();
        TextButton backButton = new TextButton("<-", skin);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                goBack();
            }
        });
        headerTable.add(backButton).padRight(UIStyles.Spacing.MEDIUM);

        Label titleLabel = new Label("Ajustes", skin);
        titleLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        headerTable.add(titleLabel).left();

        settingsPanel.add(headerTable).left().padBottom(UIStyles.Spacing.LARGE).row();

        Table volumeControl = new VolumeSlider(skin)
                .initialValue(currentVolume)
                .onChange(this::onVolumeChanged)
                .build();

        settingsPanel.add(volumeControl).width(400f).row();

        root.add(settingsPanel);
    }

    /**
     * Regresa a la pantalla anterior y libera recursos.
     */
    private void goBack() {
        game.toggleSettings();
    }

    /**
     * Obtiene la pantalla anterior almacenada.
     *
     * @return pantalla anterior
     */
    public Screen getPreviousScreen() {
        return previousScreen;
    }

    /**
     * Obtiene el stage para manejo de input.
     *
     * @return stage de UI
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Maneja los cambios de volumen aplicando el nuevo valor.
     *
     * @param newVolume nuevo valor de volumen entre 0 y 1
     */
    private void onVolumeChanged(float newVolume) {
        currentVolume = newVolume;
        Gdx.app.log("SettingsScreen", "Volume changed to: " + (int) (newVolume * 100) + "%");
    }

    /**
     * Renderiza el overlay de ajustes sobre la pantalla actual.
     * Este método es llamado por Main.render() después de renderizar la pantalla actual.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    public void renderOverlay(float delta) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.7f));
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Maneja el redimensionamiento de la ventana.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Método llamado cuando la aplicación es pausada.
     */
    public void pause() {
    }

    /**
     * Método llamado cuando la aplicación es reanudada.
     */
    public void resume() {
    }

    /**
     * Libera los recursos utilizados por esta pantalla.
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
    }
}
