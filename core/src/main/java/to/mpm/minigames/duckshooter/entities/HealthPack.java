package to.mpm.minigames.duckshooter.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa un botiquín de curación que restaura 1 vida a un pato.
 * Aparece como un punto verde en el mapa.
 */
public class HealthPack {
    private static final float HEALTH_PACK_RADIUS = 12f;
    private static final float PICKUP_RADIUS = 35f; // Radio de colisión para recoger
    private static final float LIFESPAN = 15f; // Duración en el mapa antes de desaparecer

    public final int id;
    public final Vector2 position;
    public final Color color = Color.GREEN;

    private boolean active;
    private float lifespanTimer;

    public HealthPack(int id, float x, float y) {
        this.id = id;
        this.position = new Vector2(x, y);
        this.active = true;
        this.lifespanTimer = LIFESPAN;
    }

    public void update(float delta) {
        if (!active)
            return;

        lifespanTimer -= delta;
        if (lifespanTimer <= 0) {
            active = false;
        }
    }

    /**
     * Verifica si el pato está lo suficientemente cerca para recoger el botiquín.
     */
    public boolean checkCollision(Duck duck) {
        if (!active || !duck.isAlive())
            return false;

        float distance = position.dst(duck.position);
        return distance < (PICKUP_RADIUS + duck.getRadius());
    }

    public void pickup() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public float getRadius() {
        return HEALTH_PACK_RADIUS;
    }

    public float getRemainingTime() {
        return lifespanTimer;
    }

    /**
     * Obtiene el color del botiquín con parpadeo si está por expirar.
     */
    public Color getRenderColor() {
        if (lifespanTimer < 3f && (System.currentTimeMillis() / 200) % 2 == 0) {
            return new Color(0, 0.5f, 0, 1); // Verde oscuro
        }
        return color;
    }
}
