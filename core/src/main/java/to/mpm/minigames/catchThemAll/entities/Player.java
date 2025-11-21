package to.mpm.minigames.catchThemAll.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

/**
 * Player entity with physics (gravity, jump, ground detection, basket).
 * Position and physics state are synchronized via PlayerPosition packets.
 */
public class Player {
    // Player dimensions
    public static final float PLAYER_WIDTH = 30f;
    public static final float PLAYER_HEIGHT = 40f;
    public static final float BASKET_WIDTH = 40f;
    public static final float BASKET_HEIGHT = 15f;
    
    // Physics constants
    public static final float GRAVITY = -800f;
    public static final float GROUND_Y = 60f;
    
    // Physics state (synchronized via network)
    public float x;
    public float y;
    public float velocityY;
    public boolean isGrounded;
    public float lastVelocityX;
    public float blockedTimer = 0f; // Prevents input vibration on collision
    
    // Rendering
    public final float r, g, b; // Player color
    
    // Collision detection
    private final Rectangle bounds;
    private final Rectangle basketBounds;
    
    // Network ownership
    private final boolean isLocallyOwned;

    public Player(boolean isLocallyOwned, float x, float y, float r, float g, float b) {
        this.isLocallyOwned = isLocallyOwned;
        this.x = x;
        this.y = y;
        this.velocityY = 0;
        this.isGrounded = true;
        this.lastVelocityX = 0;
        this.r = r;
        this.g = g;
        this.b = b;
        
        this.bounds = new Rectangle(x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        this.basketBounds = new Rectangle(
            x - (BASKET_WIDTH - PLAYER_WIDTH) / 2,
            y + PLAYER_HEIGHT,
            BASKET_WIDTH,
            BASKET_HEIGHT
        );
    }

    public void update() {
        if (isLocallyOwned) {
            // Decrease blocked timer
            if (blockedTimer > 0) {
                blockedTimer -= Gdx.graphics.getDeltaTime();
            }
            
            // Apply gravity
            velocityY += GRAVITY * Gdx.graphics.getDeltaTime();
            y += velocityY * Gdx.graphics.getDeltaTime();
            
            // Ground collision
            if (y <= GROUND_Y) {
                y = GROUND_Y;
                velocityY = 0;
                isGrounded = true;
            } else {
                isGrounded = false;
            }
        }
        
        updateBounds();
    }
    
    public boolean isLocallyOwned() {
        return isLocallyOwned;
    }
    
    public void updateBounds() {
        bounds.setPosition(x, y);
        basketBounds.setPosition(
            x - (BASKET_WIDTH - PLAYER_WIDTH) / 2,
            y + PLAYER_HEIGHT
        );
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public Rectangle getBasketBounds() {
        return basketBounds;
    }
}
