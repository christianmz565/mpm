package to.mpm.minigames.catchThemAll.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import to.mpm.minigames.catchThemAll.rendering.AnimatedSprite;
import to.mpm.minigames.catchThemAll.rendering.SpriteManager;

/**
 * Duck entity that falls from the sky.
 * Can be NEUTRAL, GOLDEN, or BAD.
 * Note: Ducks are NOT synced objects - only the host spawns and controls them.
 * Clients will receive duck information through dedicated network packets.
 */
public class Duck {
    public enum DuckType {
        NEUTRAL(10000, 0.7f, 0.5f, 0.2f),    // Brown - 10k points
        GOLDEN(50000, 1.0f, 0.84f, 0.0f),    // Gold - 50k points
        BAD(-20000, 0.9f, 0.1f, 0.1f);       // Red - -20k points
        
        public final int points;
        public final float r, g, b;
        
        DuckType(int points, float r, float g, float b) {
            this.points = points;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    
    // Duck dimensions
    public static final float DUCK_WIDTH = 30f;
    public static final float DUCK_HEIGHT = 30f;
    
    // Physics constants
    private static final float FALL_SPEED = 120f;  // Pixels per second
    private static final float GROUND_Y = 60f;
    
    // Animation constants
    private static final float DUCK_ANIMATION_FRAME_DURATION = 0.2f; // 200ms per frame
    
    public int id;  // Unique duck ID for network sync
    public float x;
    public float y;
    public DuckType type;
    public boolean caught;
    public int caughtByPlayerId;
    
    private final Rectangle bounds;
    private boolean reachedGround;
    private AnimatedSprite animation;

    public Duck(int id, float x, float y, DuckType type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.caught = false;
        this.caughtByPlayerId = -1;
        this.reachedGround = false;
        
        this.bounds = new Rectangle(x, y, DUCK_WIDTH, DUCK_HEIGHT);
        
        // Initialize animation
        initializeAnimation();
    }
    
    /**
     * Initialize duck animation based on type.
     */
    private void initializeAnimation() {
        SpriteManager spriteManager = SpriteManager.getInstance();
        if (spriteManager.isLoaded()) {
            animation = new AnimatedSprite(
                new com.badlogic.gdx.graphics.Texture[] {
                    spriteManager.getDuckFrame(type, 0),
                    spriteManager.getDuckFrame(type, 1)
                },
                DUCK_ANIMATION_FRAME_DURATION
            );
        }
    }

    public void update() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Update animation always (even when caught or grounded)
        if (animation != null) {
            animation.update(deltaTime);
        } else {
            initializeAnimation();
        }
        
        // Don't update physics if already caught or reached ground
        if (caught || reachedGround) {
            return;
        }
        
        // Fall down
        y -= FALL_SPEED * deltaTime;
        
        // Check if reached ground
        if (y <= GROUND_Y) {
            y = GROUND_Y;
            reachedGround = true;
        }
        
        // Update bounds
        updateBounds();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateBounds();
    }
    
    public void updateBounds() {
        bounds.setPosition(x, y);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isReachedGround() {
        return reachedGround;
    }
    
    public boolean isCaught() {
        return caught;
    }
    
    public void setCaught(int playerId) {
        this.caught = true;
        this.caughtByPlayerId = playerId;
    }
    
    /**
     * Check if this duck should be removed from the game.
     * @return true if caught or reached ground
     */
    public boolean shouldRemove() {
        return caught || reachedGround;
    }
    
    /**
     * Get the duck animation.
     */
    public AnimatedSprite getAnimation() {
        return animation;
    }
}
