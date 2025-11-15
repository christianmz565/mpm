package to.mpm.minigames.eggThief.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Nest entity where ducks deliver their collected eggs.
 * Each duck has their own nest.
 */
public class Nest {
    // Nest dimensions
    public static final float NEST_SIZE = 48f;
    
    // Nest position (right side of screen)
    public static final float NEST_X = 540f;
    
    // Player identification
    private final int ownerId;
    
    // Position
    private final Vector2 position;
    
    // Visual properties
    public final float r, g, b; // Nest color (matches owner's duck color)
    
    // Collision detection
    private final Rectangle hitbox;

    /**
     * Creates a new Nest.
     *
     * @param ownerId the player ID who owns this nest
     * @param x x position
     * @param y y position
     * @param r red color component (0-1)
     * @param g green color component (0-1)
     * @param b blue color component (0-1)
     */
    public Nest(int ownerId, float x, float y, float r, float g, float b) {
        this.ownerId = ownerId;
        this.position = new Vector2(x, y);
        this.r = r;
        this.g = g;
        this.b = b;
        this.hitbox = new Rectangle(x, y, NEST_SIZE, NEST_SIZE);
    }

    // Getters

    public int getOwnerId() {
        return ownerId;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
