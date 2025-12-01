package to.mpm.minigames.sumo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class SumoPlayer {
    public int id;
    public Vector2 position;
    public Vector2 velocity;
    public Color color;
    public boolean isAlive = true;
    public static final float RADIUS = 15f;

    // Variables para saber quién me empujó
    public int lastHitterId = -1;
    public float timeSinceLastHit = 0f;

    public SumoPlayer(int id, float x, float y, Color color) {
        this.id = id;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.color = color;
    }

    public void update(float delta) {
        if (!isAlive) return;

        position.add(velocity.x * delta, velocity.y * delta);
        velocity.scl(0.95f); // Fricción

        timeSinceLastHit += delta;
        if (timeSinceLastHit > 5.0f) {
            lastHitterId = -1;
        }
    }

    public void reset(float x, float y) {
        this.position.set(x, y);
        this.velocity.set(0, 0);
        this.isAlive = true;
        this.lastHitterId = -1;
        this.timeSinceLastHit = 0;
    }
}