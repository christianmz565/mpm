package to.mpm.minigames.duckshooter.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa un proyectil "quack" disparado por un pato.
 */
public class Quack {
    private static final float QUACK_RADIUS = 8f;
    private static final float QUACK_SPEED = 300f;
    
    public final int shooterId;
    public final Vector2 position;
    public final Vector2 velocity;
    public final Color color;
    
    private boolean active;
    
    public Quack(int shooterId, float x, float y, float dirX, float dirY, Color color) {
        this.shooterId = shooterId;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(dirX, dirY).nor().scl(QUACK_SPEED);
        this.color = new Color(color);
        this.active = true;
    }
    
    public void update(float delta) {
        if (!active) return;
        
        position.add(velocity.x * delta, velocity.y * delta);
        
        if (position.x < 0 || position.x > 640 || position.y < 0 || position.y > 480) {
            active = false;
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void deactivate() {
        active = false;
    }
    
    public float getRadius() {
        return QUACK_RADIUS;
    }
    
    public boolean checkCollision(Duck duck) {
        if (!active || !duck.isAlive() || duck.playerId == shooterId) {
            return false;
        }
        
        float distance = position.dst(duck.position);
        return distance < (QUACK_RADIUS + duck.getRadius());
    }
}
