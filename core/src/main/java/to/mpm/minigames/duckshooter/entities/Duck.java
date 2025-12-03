package to.mpm.minigames.duckshooter.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa un pato jugador en el minijuego shooter.
 * Cada pato tiene 3 puntos de vida antes de ser eliminado.
 */
public class Duck {
    private static final float DUCK_RADIUS = 25f;
    private static final float MOVE_SPEED = 180f;
    
    public final int playerId;
    public final Vector2 position;
    public final Color color;
    
    private int hits;
    private boolean alive;
    private float invulnerabilityTimer;
    
    public Duck(int playerId, float x, float y, Color color) {
        this.playerId = playerId;
        this.position = new Vector2(x, y);
        this.color = color;
        this.hits = 3;
        this.alive = true;
        this.invulnerabilityTimer = 0f;
    }
    
    public void update(float delta) {
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= delta;
        }
    }
    
    public void move(float dx, float dy, float delta) {
        position.x += dx * MOVE_SPEED * delta;
        position.y += dy * MOVE_SPEED * delta;
        
        // Mantener dentro de los límites
        position.x = Math.max(DUCK_RADIUS, Math.min(640 - DUCK_RADIUS, position.x));
        position.y = Math.max(DUCK_RADIUS, Math.min(480 - DUCK_RADIUS, position.y));
    }
    
    public void setPosition(float x, float y) {
        position.set(x, y);
    }
    
    public boolean takeDamage() {
        if (!alive || invulnerabilityTimer > 0) {
            return false;
        }
        
        hits--;
        invulnerabilityTimer = 0.5f; // Medio segundo de invulnerabilidad
        
        if (hits <= 0) {
            alive = false;
        }
        
        return true;
    }
    
    public void setHits(int hits) {
        this.hits = hits;
        this.alive = hits > 0;
    }
    
    public int getHits() {
        return hits;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public float getRadius() {
        return DUCK_RADIUS;
    }
    
    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }
}
