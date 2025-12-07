package to.mpm.minigames.theFinale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Gestiona todos los sprites para el minijuego The Finale.
 * <p>
 * Usa el patrón Singleton para asegurar que los recursos se carguen una sola vez.
 */
public class FinaleSpriteManager {
    private static FinaleSpriteManager instance;

    private Texture background;
    private Texture player;
    private Texture heal;
    private Texture crosshair;
    private Texture particles;

    private boolean loaded = false;

    private FinaleSpriteManager() {
    }

    /**
     * Obtiene la instancia única del gestor de sprites.
     *
     * @return la instancia del FinaleSpriteManager
     */
    public static FinaleSpriteManager getInstance() {
        if (instance == null) {
            instance = new FinaleSpriteManager();
        }
        return instance;
    }

    /**
     * Carga todos los sprites desde los recursos.
     */
    public void loadSprites() {
        if (loaded) {
            return;
        }

        try {
            background = new Texture(Gdx.files.internal("sprites/finale/bg-final.png"));
            player = new Texture(Gdx.files.internal("sprites/finale/player-final.png"));
            heal = new Texture(Gdx.files.internal("sprites/finale/heal-final.png"));
            crosshair = new Texture(Gdx.files.internal("sprites/finale/crosshair-final.png"));
            particles = new Texture(Gdx.files.internal("sprites/finale/particles-final.png"));

            loaded = true;
            Gdx.app.log("FinaleSpriteManager", "All sprites loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("FinaleSpriteManager", "Error loading sprites: " + e.getMessage());
        }
    }

    /**
     * Libera todas las texturas cargadas.
     */
    public void dispose() {
        if (!loaded) {
            return;
        }

        if (background != null) background.dispose();
        if (player != null) player.dispose();
        if (heal != null) heal.dispose();
        if (crosshair != null) crosshair.dispose();
        if (particles != null) particles.dispose();

        loaded = false;
        Gdx.app.log("FinaleSpriteManager", "All sprites disposed");
    }

    public Texture getBackground() {
        return background;
    }

    public Texture getPlayer() {
        return player;
    }

    public Texture getHeal() {
        return heal;
    }

    public Texture getCrosshair() {
        return crosshair;
    }

    public Texture getParticles() {
        return particles;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
