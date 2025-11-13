package to.mpm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import to.mpm.screens.MainMenuScreen;
import to.mpm.ui.SettingsOverlayManager;
import to.mpm.ui.UISkinProvider;
import to.mpm.utils.DebugKeybinds;

/**
 * Clase principal del juego.
 */
public class Main extends Game {
    public SpriteBatch batch;
    public DebugKeybinds debugKeybinds;
    private SettingsOverlayManager settingsOverlayManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        debugKeybinds = new DebugKeybinds(this);
        settingsOverlayManager = new SettingsOverlayManager();
        
        setScreen(new MainMenuScreen(this));
        
        DebugKeybinds.printHelp();
    }

    /**
     * Alterna la pantalla de ajustes.
     */
    public void toggleSettings() {
        settingsOverlayManager.toggle();
    }

    /**
     * Llamado cuando la pantalla cambia para actualizar el manejo de entrada.
     */
    @Override
    public void setScreen(com.badlogic.gdx.Screen screen) {
        super.setScreen(screen);
        settingsOverlayManager.hide();
    }

    /**
     * Obtiene el administrador de la superposición de ajustes.
     *
     * @return instancia del administrador de overlay
     */
    public SettingsOverlayManager getSettingsOverlayManager() {
        return settingsOverlayManager;
    }

    /**
     * Renderiza el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void render() {
        debugKeybinds.update();
        
        super.render();
        
        settingsOverlayManager.renderOverlay(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
    }

    /**
     * Redimensiona la pantalla y la superposición de ajustes si está activa.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        settingsOverlayManager.resize(width, height);
    }

    /**
     * Pausa el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void pause() {
        super.pause();
    }

    /**
     * Reanuda el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void resume() {
        super.resume();
    }

    /**
     * Libera los recursos utilizados por el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void dispose() {
        batch.dispose();
        settingsOverlayManager.dispose();
        UISkinProvider.dispose();
        super.dispose();
    }
}
