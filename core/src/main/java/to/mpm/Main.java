package to.mpm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import to.mpm.screens.MainMenuScreen;
import to.mpm.utils.DebugKeybinds;

/**
 * Clase principal del juego.
 */
public class Main extends Game {
    public SpriteBatch batch;
    public DebugKeybinds debugKeybinds;

    @Override
    public void create() {
        batch = new SpriteBatch();
        debugKeybinds = new DebugKeybinds(this);
        setScreen(new MainMenuScreen(this));
        
        DebugKeybinds.printHelp();
    }

    @Override
    public void render() {
        debugKeybinds.update();
        
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        super.dispose();
    }
}
