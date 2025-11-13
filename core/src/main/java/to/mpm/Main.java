package to.mpm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import to.mpm.screens.MainMenuScreen;
import to.mpm.screens.SettingsScreen;
import to.mpm.utils.DebugKeybinds;

/**
 * Clase principal del juego.
 */
public class Main extends Game {
    public SpriteBatch batch;
    public DebugKeybinds debugKeybinds;
    private SettingsScreen settingsOverlay;
    private InputMultiplexer inputMultiplexer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        debugKeybinds = new DebugKeybinds(this);
        inputMultiplexer = new InputMultiplexer();
        
        setScreen(new MainMenuScreen(this));
        
        DebugKeybinds.printHelp();
    }

    /**
     * Alterna la pantalla de ajustes.
     */
    public void toggleSettings() {
        if (settingsOverlay != null) {
            settingsOverlay.dispose();
            settingsOverlay = null;
            if (getScreen() != null) {
                updateInputProcessor();
            }
        } else {
            settingsOverlay = new SettingsScreen(this, getScreen());
            settingsOverlay.show();
            updateInputProcessor();
        }
    }

    /**
     * Actualiza el InputProcessor para manejar la superposición de ajustes.
     */
    private void updateInputProcessor() {
        if (settingsOverlay != null && settingsOverlay.getStage() != null) {
            inputMultiplexer.clear();
            inputMultiplexer.addProcessor(settingsOverlay.getStage());
            Gdx.input.setInputProcessor(inputMultiplexer);
        } else {
            inputMultiplexer.clear();
        }
    }

    /**
     * Llamado cuando la pantalla cambia para actualizar el manejo de entrada.
     */
    @Override
    public void setScreen(com.badlogic.gdx.Screen screen) {
        super.setScreen(screen);
        if (settingsOverlay != null) {
            updateInputProcessor();
        }
    }

    /**
     * Revisa si los ajustes están activos actualmente.
     */
    public boolean isSettingsActive() {
        return settingsOverlay != null;
    }

    /**
     * Renderiza el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void render() {
        debugKeybinds.update();
        
        super.render();
        
        if (settingsOverlay != null) {
            settingsOverlay.renderOverlay(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
        }
    }

    /**
     * Redimensiona la pantalla y la superposición de ajustes si está activa.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (settingsOverlay != null) {
            settingsOverlay.resize(width, height);
        }
    }

    /**
     * Pausa el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void pause() {
        super.pause();
        if (settingsOverlay != null) {
            settingsOverlay.pause();
        }
    }

    /**
     * Reanuda el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void resume() {
        super.resume();
        if (settingsOverlay != null) {
            settingsOverlay.resume();
        }
    }

    /**
     * Libera los recursos utilizados por el juego y la superposición de ajustes si está activa.
     */
    @Override
    public void dispose() {
        batch.dispose();
        if (settingsOverlay != null) {
            settingsOverlay.dispose();
        }
        super.dispose();
    }
}
