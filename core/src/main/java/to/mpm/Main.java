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
     * Toggles the settings overlay on/off.
     */
    public void toggleSettings() {
        if (settingsOverlay != null) {
            // Close settings
            settingsOverlay.dispose();
            settingsOverlay = null;
            // Restore screen's input processor
            if (getScreen() != null) {
                updateInputProcessor();
            }
        } else {
            // Open settings
            settingsOverlay = new SettingsScreen(this, getScreen());
            settingsOverlay.show();
            // Update input multiplexer to include settings stage
            updateInputProcessor();
        }
    }

    /**
     * Updates the input processor to handle both settings and current screen.
     */
    private void updateInputProcessor() {
        if (settingsOverlay != null && settingsOverlay.getStage() != null) {
            // Settings is open - use multiplexer with settings on top
            inputMultiplexer.clear();
            inputMultiplexer.addProcessor(settingsOverlay.getStage());
            // Don't add screen processor - settings should block input to underlying screen
            Gdx.input.setInputProcessor(inputMultiplexer);
        } else {
            // Settings is closed - let the screen handle its own input
            // Screen will set its own input processor in show()
            inputMultiplexer.clear();
        }
    }

    /**
     * Called when screen changes to update input handling.
     */
    @Override
    public void setScreen(com.badlogic.gdx.Screen screen) {
        super.setScreen(screen);
        // Only update if settings is active, otherwise let screen manage input
        if (settingsOverlay != null) {
            updateInputProcessor();
        }
    }

    /**
     * Checks if settings overlay is currently active.
     */
    public boolean isSettingsActive() {
        return settingsOverlay != null;
    }

    @Override
    public void render() {
        debugKeybinds.update();
        
        // Render current screen
        super.render();
        
        // Render settings overlay on top if active
        if (settingsOverlay != null) {
            settingsOverlay.renderOverlay(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (settingsOverlay != null) {
            settingsOverlay.resize(width, height);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (settingsOverlay != null) {
            settingsOverlay.pause();
        }
    }

    @Override
    public void resume() {
        super.resume();
        if (settingsOverlay != null) {
            settingsOverlay.resume();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (settingsOverlay != null) {
            settingsOverlay.dispose();
        }
        super.dispose();
    }
}
