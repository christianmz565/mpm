package to.mpm.minigames.eggThief.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import to.mpm.network.sync.SyncedObject;
import to.mpm.network.sync.Synchronized;

import java.util.ArrayList;
import java.util.List;

/**
 * Duck entity representing a player in the Egg Thief minigame.
 * Ducks can move around, collect eggs, steal from other ducks, and deliver eggs
 * to their nest.
 */
public class Duck extends SyncedObject {

    // Duck dimensions
    public static final float DUCK_SIZE = 32f;

    // Spawn positions
    // public static final float SPAWN_X = 100f; // Left side of screen

    // Movement constants
    public static final float BASE_SPEED = 200f;
    public static final float SCREEN_WIDTH = 640f;
    public static final float SCREEN_HEIGHT = 480f;

    // Stealing constants
    public static final int MAX_STEAL_HITS = 3; // Number of hits before egg
    // breaks

    // Player identification
    private final int playerId;

    // Position and movement
    @Synchronized
    public float x;
    @Synchronized
    public float y;
    private final Vector2 velocity;
    private float speed;

    // Stealing mechanic
    private int stealHitCount;
    // Visual properties
    public final float r, g, b; // Duck color

    // Egg carrying (just one egg at a time)
    private Egg carriedEgg;
    @Synchronized
    private int deliveredEggCount;
    private final Rectangle hitbox;

    @Synchronized
    public int points;

    /**
     * Creates a new Duck.
     * 
     * @param isLocallyOwned whether this duck is controlled by the local player
     * @param playerId       unique identifier for this player
     * @param x              starting x position
     * @param y              starting y position
     * @param r              red color component (0-1)
     * @param g              green color component (0-1)
     * @param b              blue color component (0-1)
     */
    public Duck(boolean isLocallyOwned, int playerId, float x, float y, float r, float g, float b) {
        super(isLocallyOwned);
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.velocity = new Vector2(0, 0);
        this.speed = BASE_SPEED;
        this.deliveredEggCount = 0;
        this.stealHitCount = 0;
        this.r = r;
        this.g = g;
        this.b = b;
        this.hitbox = new Rectangle(x, y, DUCK_SIZE, DUCK_SIZE);
        this.points = 0;
    }

    /**
     * Updates the duck's position and physics.
     * For locally owned ducks, applies velocity and movement.
     * For remote ducks, just updates the hitbox based on synced position.
     * 
     * @param delta time since last frame in seconds
     */
    public void update(float delta) {
        super.update();
        if (isLocallyOwned()) {
            x += velocity.x * delta;
            y += velocity.y * delta;
        }
        hitbox.setPosition(x, y);
    }

    /**
     * Moves the duck in the specified direction.
     *
     * @param dx horizontal movement (-1 to 1)
     * @param dy vertical movement (-1 to 1)
     */
    public void move(float dx, float dy) {
        velocity.set(dx * speed, dy * speed);
    }

    /**
     * Sets the duck's position (for network synchronization).
     *
     * @param x new x position
     * @param y new y position
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the duck's velocity (for network synchronization).
     *
     * @param vx horizontal velocity
     * @param vy vertical velocity
     */
    public void setVelocity(float vx, float vy) {
        velocity.set(vx, vy);
    }

    // Collecting eggs
    public boolean collectEgg(Egg egg) {
        if (carriedEgg != null)
            return false;
        carriedEgg = egg;
        return true;
    }

    /**
     * Attempts to steal an egg from another duck.
     *
     * @param other the duck to steal from
     * @return true if an egg was successfully stolen, false otherwise
     */
    public boolean stealEggFrom(Duck other) {
        if (other.carriedEgg != null) {
            stealHitCount++;
            if (stealHitCount >= MAX_STEAL_HITS) {
                // Successful steal
                this.carriedEgg = other.carriedEgg;
                other.carriedEgg = null;
                other.stealHitCount = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Delivers all carried eggs to the nest.
     *
     * @return number of points scored
     */
    public int deliverEggs() {
        if (carriedEgg == null)
            return 0;
        int points = carriedEgg.getPoints();
        addPoints(points);
        carriedEgg = null;
        stealHitCount = 0;

        Gdx.app.log("DUCK", "Count egg delivered: " + deliveredEggCount);
        Gdx.app.log("DUCK", "Total score: " + points);
        Gdx.app.log("DUCK", "This delivery scored (current egg): " + points);
        return points;
    }

    // Getters
    public int getPlayerId() {
        return playerId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public int getEggsCarryingCount() {
        return carriedEgg != null ? 1 : 0;
    }

    public List<Egg> getCarriedEggs() {
        List<Egg> eggs = new ArrayList<>();
        if (carriedEgg != null) {
            eggs.add(carriedEgg);
        }
        return eggs;
    }

    public Egg getCarriedEgg() {
        return carriedEgg;
    }

    public int getDeliveredEggs() {
        return deliveredEggCount;
    }

    public void setDeliveredEggs(int eggs) {
        this.deliveredEggCount = eggs;
    }

    public int getStealHitCount() {
        return stealHitCount;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public int getPoints() {
        return points;
    }
}
