package to.mpm.minigames.eggThief.entities;

import com.badlogic.gdx.math.Rectangle;
import to.mpm.network.sync.SyncedObject;
import to.mpm.network.sync.Synchronized;

/**
 * Represents an egg that can be collected by ducks.
 * Eggs can be normal or golden, with golden eggs worth more points.
 */
public class Egg extends SyncedObject {
    // Egg dimensions
    public static final float EGG_RADIUS = 8f;
    public static final float EGG_SIZE = EGG_RADIUS * 2;

    // Point values
    public static final int NORMAL_EGG_POINTS = 10000;
    public static final int GOLDEN_EGG_POINTS = 30000;

    // Unique identifier
    public final int id;
    // Position
    @Synchronized
    public float x;
    @Synchronized
    public float y;
    // Properties
    @Synchronized
    public boolean isGolden;
    @Synchronized
    public int points;

    // Collision detection
    private final Rectangle hitbox;
    // State
    private boolean isCollected;

    /**
     * Creates a new Egg.
     *
     * @param id     unique identifier for this egg
     * @param x      x position
     * @param y      y position
     * @param golden whether this is a golden egg (worth more points)
     */
    public Egg(int id, float x, float y, boolean isGolden) {
        super(true); // Eggs are managed by the host
        this.id = id;
        this.x = x;
        this.y = y;
        this.isGolden = isGolden;
        this.points = isGolden ? GOLDEN_EGG_POINTS : NORMAL_EGG_POINTS;
        this.isCollected = false;
        this.hitbox = new Rectangle(x, y, EGG_SIZE, EGG_SIZE);
    }

    /**
     * Updates the egg state.
     *
     * @param delta time since last frame in seconds
     */
    public void update(float delta) {
        super.update();
        hitbox.setPosition(x, y);
    }

    // Marks this egg as collected.
    public void collected() {
        isCollected = true;
    }

    public boolean isCollected() {
        return isCollected;
    }

    // Getters
    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isGolden() {
        return isGolden;
    }

    public int getPoints() {
        return points;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
