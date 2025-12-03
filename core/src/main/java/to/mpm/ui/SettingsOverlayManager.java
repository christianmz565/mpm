package to.mpm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.ui.components.SettingsOverlay;

/**
 * Controlador responsable de gestionar la superposición de ajustes.
 * <p>
 * Mantiene su propio stage y un InputMultiplexer para integrarse con la
 * pantalla activa.
 */
public class SettingsOverlayManager {
    /** Color de la capa de oscurecimiento. */
    private static final Color OVERLAY_COLOR = new Color(0f, 0f, 0f, 0.7f);
    /** Stage dedicado para el overlay. */
    private final Stage overlayStage;
    /** Renderer para la capa oscura de fondo. */
    private final ShapeRenderer dimRenderer;
    /** Componente visual de ajustes. */
    private final SettingsOverlay overlay;
    /** Multiplexer compartido con la pantalla activa. */
    private final InputMultiplexer inputMultiplexer;
    /** Stage actual de la pantalla principal. */
    private Stage activeStage;

    /**
     * Construye el administrador de la superposición utilizando el skin compartido.
     *
     * @param game referencia al juego principal
     */
    public SettingsOverlayManager(to.mpm.Main game) {
        Skin skin = UISkinProvider.obtain();
        overlayStage = new Stage(new ScreenViewport());
        dimRenderer = new ShapeRenderer();
        overlay = new SettingsOverlay(game, overlayStage, skin);
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(overlayStage);
    }

    /**
     * Adjunta el stage de la pantalla activa al multiplexer de entrada.
     *
     * @param stage stage actual que debe recibir input junto al overlay
     */
    public void attachStage(Stage stage) {
        activeStage = stage;
        rebuildInputPipeline();
    }

    /**
     * Alterna la visibilidad del overlay.
     */
    public void toggle() {
        if (overlay.isVisible()) {
            overlay.hide();
        } else {
            overlay.show();
        }
    }

    /**
     * Oculta el overlay si está visible.
     */
    public void hide() {
        if (overlay.isVisible()) {
            overlay.hide();
        }
    }

    /**
     * Renderiza la superposición de ajustes si está activa.
     *
     * @param delta tiempo transcurrido desde el último frame
     */
    public void renderOverlay(float delta) {
        if (!overlay.isVisible()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dimRenderer.setColor(OVERLAY_COLOR);
        dimRenderer.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        dimRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        overlayStage.act(delta);
        overlayStage.draw();
    }

    /**
     * Ajusta el viewport del overlay cuando cambia el tamaño de pantalla.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    public void resize(int width, int height) {
        overlayStage.getViewport().update(width, height, true);
    }

    /**
     * Libera los recursos utilizados por el overlay.
     */
    public void dispose() {
        overlayStage.dispose();
        dimRenderer.dispose();
    }

    /**
     * Obtiene el multiplexer utilizado para el input combinado.
     *
     * @return InputMultiplexer configurado
     */
    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    /**
     * Verifica si el overlay está visible actualmente.
     *
     * @return true si está visible
     */
    public boolean isVisible() {
        return overlay.isVisible();
    }

    /**
     * Reconstruye la configuración del InputMultiplexer.
     */
    private void rebuildInputPipeline() {
        inputMultiplexer.clear();
        inputMultiplexer.addProcessor(overlayStage);
        if (activeStage != null) {
            inputMultiplexer.addProcessor(activeStage);
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
    }
}
