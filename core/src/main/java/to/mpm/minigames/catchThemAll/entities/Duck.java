package to.mpm.minigames.catchThemAll.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

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
    public static final float DUCK_WIDTH = 20f;
    public static final float DUCK_HEIGHT = 20f;
    
    // Physics constants
    private static final float FALL_SPEED = 120f;  // Pixels per second
    private static final float GROUND_Y = 60f;
    
    public int id;  // Unique duck ID for network sync
    public float x;
    public float y;
    public DuckType type;
    public boolean caught;
    public int caughtByPlayerId;
    
    private final Rectangle bounds;
    private boolean reachedGround;

    public Duck(int id, float x, float y, DuckType type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.caught = false;
        this.caughtByPlayerId = -1;
        this.reachedGround = false;
        
        this.bounds = new Rectangle(x, y, DUCK_WIDTH, DUCK_HEIGHT);
    }

    public void update() {
        
        // Don't update if already caught or reached ground
        if (caught || reachedGround) {
            return;
        }
        
        // Fall down
        y -= FALL_SPEED * Gdx.graphics.getDeltaTime();
        
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
}
