package to.mpm.minigames.catchThemAll.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import to.mpm.minigames.catchThemAll.rendering.AnimatedSprite;
import to.mpm.minigames.catchThemAll.rendering.SpriteManager;

/**
 * Player entity with physics (gravity, jump, ground detection, basket).
 * Position and physics state are synchronized via PlayerPosition packets.
 */
public class Player {
    // Player dimensions
    public static final float PLAYER_WIDTH = 45f;
    public static final float PLAYER_HEIGHT = 60f;
    public static final float BASKET_WIDTH = 60f;
    public static final float BASKET_HEIGHT = 22f;
    
    // Physics constants
    public static final float GRAVITY = -800f;
    public static final float GROUND_Y = 60f;
    
    // Animation constants
    private static final float ANIMATION_FRAME_DURATION = 0.15f; // 150ms per frame
    private static final float MOVEMENT_THRESHOLD = 0.5f; // Minimum velocity (pixels per delta) to animate
    
    // Physics state (synchronized via network)
    public float x;
    public float y;
    public float velocityY;
    public boolean isGrounded;
    public float lastVelocityX;
    public float blockedTimer = 0f; // Prevents input vibration on collision
    
    // Rendering
    public final float r, g, b; // Player color
    private AnimatedSprite runAnimation;
    private boolean facingRight = true;
    
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
        
        // Initialize animation
        initializeAnimation();
    }
    
    /**
     * Initialize player running animation.
     */
    private void initializeAnimation() {
        SpriteManager spriteManager = SpriteManager.getInstance();
        if (spriteManager.isLoaded()) {
            runAnimation = new AnimatedSprite(
                new com.badlogic.gdx.graphics.Texture[] {
                    spriteManager.getPlayerFrame1(),
                    spriteManager.getPlayerFrame2()
                },
                ANIMATION_FRAME_DURATION
            );
        }
    }

    public void update() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        if (isLocallyOwned) {
            // Decrease blocked timer
            if (blockedTimer > 0) {
                blockedTimer -= deltaTime;
            }
            
            // Apply gravity
            velocityY += GRAVITY * deltaTime;
            y += velocityY * deltaTime;
            
            // Ground collision
            if (y <= GROUND_Y) {
                y = GROUND_Y;
                velocityY = 0;
                isGrounded = true;
            }
            // Note: isGrounded may be set to true by CollisionHandler when standing on another player
            // Only reset to false if we're clearly in the air (will be overridden by collision resolution)
        }
        
        updateBounds();
        updateAnimation(deltaTime);
    }
    
    /**
     * Update animation state based on movement.
     */
    private void updateAnimation(float deltaTime) {
        if (runAnimation == null) {
            initializeAnimation();
            if (runAnimation == null) return;
        }
        
        // Update facing direction and animation based on horizontal velocity
        float absVelocity = Math.abs(lastVelocityX);
        
        if (absVelocity > MOVEMENT_THRESHOLD) {
            // Update facing direction
            facingRight = lastVelocityX > 0;
            // Animate while moving
            runAnimation.resume();
            runAnimation.update(deltaTime);
        } else {
            // Pause animation when not moving and reset to first frame
            runAnimation.pause();
            runAnimation.reset();
        }
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
    
    /**
     * Get the run animation.
     */
    public AnimatedSprite getRunAnimation() {
        return runAnimation;
    }
    
    /**
     * Check if player is facing right.
     */
    public boolean isFacingRight() {
        return facingRight;
    }
}
