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
    /** Ancho del jugador. */
    public static final float PLAYER_WIDTH = 45f;
    /** Alto del jugador. */
    public static final float PLAYER_HEIGHT = 60f;
    /** Ancho de la canasta. */
    public static final float BASKET_WIDTH = 60f;
    /** Alto de la canasta. */
    public static final float BASKET_HEIGHT = 22f;
    
    /** Gravedad en píxeles por segundo cuadrado. */
    public static final float GRAVITY = -800f;
    /** Posición Y del suelo. */
    public static final float GROUND_Y = 60f;
    
    /** Duración de cada frame de animación. */
    private static final float ANIMATION_FRAME_DURATION = 0.15f;
    /** Umbral mínimo de velocidad para animar. */
    private static final float MOVEMENT_THRESHOLD = 0.5f;
    
    /** Posición X del jugador. */
    public float x;
    /** Posición Y del jugador. */
    public float y;
    /** Velocidad vertical. */
    public float velocityY;
    /** Indica si está en el suelo. */
    public boolean isGrounded;
    /** Última velocidad horizontal. */
    public float lastVelocityX;
    /** Tiempo de bloqueo de input. */
    public float blockedTimer = 0f;
    
    /** Componente rojo del color. */
    public final float r;
    /** Componente verde del color. */
    public final float g;
    /** Componente azul del color. */
    public final float b;
    private AnimatedSprite runAnimation;
    private boolean facingRight = true;
    
    private final Rectangle bounds;
    private final Rectangle basketBounds;
    
    /** Indica si es propiedad local. */
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
            if (blockedTimer > 0) {
                blockedTimer -= deltaTime;
            }
            
            velocityY += GRAVITY * deltaTime;
            y += velocityY * deltaTime;
            
            if (y <= GROUND_Y) {
                y = GROUND_Y;
                velocityY = 0;
                isGrounded = true;
            }
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
        
        float absVelocity = Math.abs(lastVelocityX);
        
        if (absVelocity > MOVEMENT_THRESHOLD) {
            facingRight = lastVelocityX > 0;
            runAnimation.resume();
            runAnimation.update(deltaTime);
        } else {
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
