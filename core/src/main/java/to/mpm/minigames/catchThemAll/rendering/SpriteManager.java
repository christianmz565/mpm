package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import to.mpm.minigames.catchThemAll.entities.Duck;

/**
 * Manages all sprites for the Catch Them All minigame.
 * Singleton pattern to ensure resources are loaded once.
 */
public class SpriteManager {
    private static SpriteManager instance;
    
    // Environment sprites
    private Texture background;
    private Texture floor;
    
    // Player animation frames
    private Texture playerFrame1;
    private Texture playerFrame2;
    
    // Duck animation frames by type
    private Texture duckNeutral1;
    private Texture duckNeutral2;
    private Texture duckGold1;
    private Texture duckGold2;
    private Texture duckBad1;
    private Texture duckBad2;
    
    private boolean loaded = false;
    
    private SpriteManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static SpriteManager getInstance() {
        if (instance == null) {
            instance = new SpriteManager();
        }
        return instance;
    }
    
    /**
     * Load all sprites from assets.
     */
    public void loadSprites() {
        if (loaded) {
            return;
        }
        
        try {
            // Load environment
            background = new Texture(Gdx.files.internal("sprites/catchThemAll/background.png"));
            floor = new Texture(Gdx.files.internal("sprites/catchThemAll/floor.png"));
            
            // Load player frames
            playerFrame1 = new Texture(Gdx.files.internal("sprites/catchThemAll/player-frame1.png"));
            playerFrame2 = new Texture(Gdx.files.internal("sprites/catchThemAll/player-frame2.png"));
            
            // Load duck frames
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
     * Dispose all textures.
     */
    public void dispose() {
        if (!loaded) {
            return;
        }
        
        if (background != null) background.dispose();
        if (floor != null) floor.dispose();
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
    
    // Getters for environment
    public Texture getBackground() { return background; }
    public Texture getFloor() { return floor; }
    
    // Getters for player frames
    public Texture getPlayerFrame1() { return playerFrame1; }
    public Texture getPlayerFrame2() { return playerFrame2; }
    
    /**
     * Get duck frame by type and frame number.
     * @param type Duck type
     * @param frameIndex 0 or 1
     * @return Texture for the requested frame
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
    
    public boolean isLoaded() {
        return loaded;
    }
}
