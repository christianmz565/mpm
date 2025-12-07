package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import to.mpm.minigames.catchThemAll.entities.Duck;

/**
 * Gestiona todos los sprites para el minijuego Atrapa a Todos.
 * <p>
 * Usa el patrón Singleton para asegurar que los recursos se carguen una sola vez.
 */
public class SpriteManager {
    private static SpriteManager instance;
    
    /** Textura del fondo. */
    private Texture background;
    
    /** Textura de las nubes. */
    private Texture clouds;
    
    /** Frame 1 del jugador. */
    private Texture playerFrame1;
    /** Frame 2 del jugador. */
    private Texture playerFrame2;
    
    /** Frame 1 del pato neutral. */
    private Texture duckNeutral1;
    /** Frame 2 del pato neutral. */
    private Texture duckNeutral2;
    /** Frame 1 del pato dorado. */
    private Texture duckGold1;
    /** Frame 2 del pato dorado. */
    private Texture duckGold2;
    /** Frame 1 del pato malo. */
    private Texture duckBad1;
    /** Frame 2 del pato malo. */
    private Texture duckBad2;
    
    private boolean loaded = false;
    
    private SpriteManager() {
    }
    
    /**
     * Obtiene la instancia única del gestor de sprites.
     * 
     * @return la instancia del SpriteManager
     */
    public static SpriteManager getInstance() {
        if (instance == null) {
            instance = new SpriteManager();
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
            background = new Texture(Gdx.files.internal("sprites/catchThemAll/background.png"));
            clouds = new Texture(Gdx.files.internal("sprites/catchThemAll/clouds.png"));
            
            playerFrame1 = new Texture(Gdx.files.internal("sprites/catchThemAll/player-frame1.png"));
            playerFrame2 = new Texture(Gdx.files.internal("sprites/catchThemAll/player-frame2.png"));
            
            duckNeutral1 = new Texture(Gdx.files.internal("sprites/catchThemAll/duck-neutral1.png"));
            duckNeutral2 = new Texture(Gdx.files.internal("sprites/catchThemAll/duck-neutral2.png"));
            duckGold1 = new Texture(Gdx.files.internal("sprites/catchThemAll/duck-gold1.png"));
            duckGold2 = new Texture(Gdx.files.internal("sprites/catchThemAll/duck-gold2.png"));
            duckBad1 = new Texture(Gdx.files.internal("sprites/catchThemAll/duck-bad1.png"));
            duckBad2 = new Texture(Gdx.files.internal("sprites/catchThemAll/duck-bad2.png"));
            
            loaded = true;
            Gdx.app.log("SpriteManager", "All sprites loaded successfully");
        } catch (Exception e) {
            Gdx.app.error("SpriteManager", "Error loading sprites: " + e.getMessage());
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
        if (clouds != null) clouds.dispose();
        if (playerFrame1 != null) playerFrame1.dispose();
        if (playerFrame2 != null) playerFrame2.dispose();
        if (duckNeutral1 != null) duckNeutral1.dispose();
        if (duckNeutral2 != null) duckNeutral2.dispose();
        if (duckGold1 != null) duckGold1.dispose();
        if (duckGold2 != null) duckGold2.dispose();
        if (duckBad1 != null) duckBad1.dispose();
        if (duckBad2 != null) duckBad2.dispose();
        
        loaded = false;
        Gdx.app.log("SpriteManager", "All sprites disposed");
    }
    
    /** Obtiene el fondo. */
    public Texture getBackground() { return background; }
    
    /** Obtiene las nubes. */
    public Texture getClouds() { return clouds; }
    
    /** Obtiene el frame 1 del jugador. */
    public Texture getPlayerFrame1() { return playerFrame1; }
    /** Obtiene el frame 2 del jugador. */
    public Texture getPlayerFrame2() { return playerFrame2; }
    
    /**
     * Obtiene un frame de pato según su tipo y número de frame.
     * 
     * @param type tipo de pato
     * @param frameIndex índice del frame (0 o 1)
     * @return textura del frame solicitado
     */
    public Texture getDuckFrame(Duck.DuckType type, int frameIndex) {
        switch (type) {
            case NEUTRAL:
                return frameIndex == 0 ? duckNeutral1 : duckNeutral2;
            case GOLDEN:
                return frameIndex == 0 ? duckGold1 : duckGold2;
            case BAD:
                return frameIndex == 0 ? duckBad1 : duckBad2;
            default:
                return duckNeutral1;
        }
    }
    
    /**
     * Verifica si los sprites han sido cargados.
     * 
     * @return true si los sprites están cargados, false en caso contrario
     */
    public boolean isLoaded() {
        return loaded;
    }
}
