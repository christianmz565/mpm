package to.mpm.minigames.catchThemAll.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import to.mpm.network.sync.SyncedObject;
import to.mpm.network.sync.Synchronized;

/**
 * Player entity with physics (gravity, jump, ground detection, basket).
 */
public class Player extends SyncedObject {
    // Player dimensions
    public static final float PLAYER_WIDTH = 30f;
    public static final float PLAYER_HEIGHT = 40f;
    public static final float BASKET_WIDTH = 40f;
    public static final float BASKET_HEIGHT = 15f;
    
    // Physics constants
    public static final float GRAVITY = -800f;
    public static final float GROUND_Y = 60f;
    
    @Synchronized public float x;
    @Synchronized public float y;
    @Synchronized public float velocityY;
    @Synchronized public boolean isGrounded;
    
    public float r, g, b; // Player color
    public float lastVelocityX; // Track horizontal velocity for collision physics
    public float blockedTimer = 0f; // Timer to prevent vibration when blocked by collision
    
    private final Rectangle bounds;
    private final Rectangle basketBounds;

    public Player(boolean isLocallyOwned, float x, float y, float r, float g, float b) {
        super(isLocallyOwned);
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

    @Override
    public void update() {
        super.update();
        
        if (isLocallyOwned()) {
            // Decrease blocked timer
            if (blockedTimer > 0) {
                blockedTimer -= Gdx.graphics.getDeltaTime();
            }
            
            // Apply gravity
            velocityY += GRAVITY * Gdx.graphics.getDeltaTime();
            
            // Apply velocity
            y += velocityY * Gdx.graphics.getDeltaTime();
            
            // Ground collision
            if (y <= GROUND_Y) {
                y = GROUND_Y;
                velocityY = 0;
                isGrounded = true;
            } else {
                // If not on ground, mark as not grounded (will be corrected by server if on player)
                isGrounded = false;
            }
        }
        
        updateBounds();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateBounds();
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
