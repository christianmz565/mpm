package to.mpm.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.Color;
import to.mpm.Main;
import to.mpm.network.NetworkManager;
import to.mpm.screens.MainMenuScreen;
import to.mpm.ui.UIStyles;

/**
 * Capa de ajustes que puede mostrarse sobre cualquier pantalla.
 * <p>
 * Proporciona control de volumen y otras configuraciones del juego.
 */
public class SettingsOverlay {
    /** Referencia al juego principal. */
    private final Main game;
    /** Stage donde se renderiza la capa. */
    private final Stage stage;
    /** Skin para renderizar componentes UI. */
    private final Skin skin;
    /** Tabla contenedora de la capa. */
    private final Table overlay;
    /** Volumen actual (25% por defecto). */
    private float currentVolume = 0.25f;
    /** Indica si la capa está visible. */
    private boolean isVisible = false;

    /**
     * Construye una nueva capa de ajustes.
     *
     * @param game  referencia al juego principal
     * @param stage stage donde se añadirá la capa
     * @param skin  skin de UI para renderizar componentes
     */
    public SettingsOverlay(Main game, Stage stage, Skin skin) {
        this.game = game;
        this.stage = stage;
        this.skin = skin;
        this.overlay = createOverlay();
    }

    /**
     * Crea y configura la estructura visual de la capa de ajustes.
     *
     * @return tabla contenedora de la capa completa
     */
    private Table createOverlay() {
        Table background = new Table();
        background.setFillParent(true);
        background.setVisible(false);

        background.setColor(new Color(0, 0, 0, 0.7f));

        Table settingsPanel = new Table(skin);
        settingsPanel.pad(UIStyles.Spacing.LARGE);

        Label titleLabel = new Label("Ajustes", skin);
        com.badlogic.gdx.graphics.g2d.BitmapFont titleFont = skin.getFont("sixtyfour-32");
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, UIStyles.Colors.TEXT_PRIMARY);
        titleLabel.setStyle(titleStyle);
        titleLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        settingsPanel.add(titleLabel).padBottom(UIStyles.Spacing.LARGE).row();

        Table volumeControl = new VolumeSlider(skin)
                .initialValue(currentVolume)
                .onChange(this::onVolumeChanged)
                .build();

        settingsPanel.add(volumeControl).padBottom(UIStyles.Spacing.LARGE).row();

        settingsPanel.add(
                new StyledButton(skin)
                        .text("Cerrar")
                        .width(250f)
                        .height(60f)
                        .onClick(this::hide)
                        .build())
                .size(250f, 60f).padBottom(UIStyles.Spacing.MEDIUM).row();

        settingsPanel.add(
                new StyledButton(skin)
                        .text("Salir al Menú")
                        .width(250f)
                        .height(60f)
                        .onClick(this::exitToMainMenu)
                        .build())
                .size(250f, 60f);

        background.add(settingsPanel);

        stage.addActor(background);
        return background;
    }

    /**
     * Maneja los cambios de volumen aplicando el nuevo valor.
     *
     * @param newVolume nuevo valor de volumen entre 0 y 1
     */
    private void onVolumeChanged(float newVolume) {
        currentVolume = newVolume;
        Gdx.app.log("SettingsOverlay", "Volume changed to: " + (int) (newVolume * 100) + "%");
    }

    /**
     * Sale al menú principal, desconectando de la partida actual.
     */
    private void exitToMainMenu() {
        Gdx.app.log("SettingsOverlay", "Exiting to main menu");
        NetworkManager.getInstance().disconnect();
        to.mpm.minigames.manager.GameFlowManager.getInstance().reset();
        hide();
        game.setScreen(new MainMenuScreen(game));
    }

    /**
     * Muestra la capa de ajustes sobre la pantalla actual.
     */
    public void show() {
        overlay.setVisible(true);
        overlay.toFront();
        isVisible = true;
    }

    /**
     * Oculta la capa de ajustes.
     */
    public void hide() {
        overlay.setVisible(false);
        isVisible = false;
    }

    /**
     * Alterna la visibilidad de la capa de ajustes.
     */
    public void toggle() {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Verifica si la capa está actualmente visible.
     *
     * @return true si está visible, false en caso contrario
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Obtiene el valor actual de volumen.
     *
     * @return volumen actual entre 0 y 1
     */
    public float getCurrentVolume() {
        return currentVolume;
    }

    /**
     * Establece el valor de volumen.
     *
     * @param volume nuevo valor de volumen entre 0 y 1
     */
    public void setVolume(float volume) {
        this.currentVolume = volume;
    }
}
