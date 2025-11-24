package to.mpm.minigames.sumo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class SumoPlayer {
    public int id;
    public Vector2 position;
    public Vector2 velocity; // Velocidad actual (para el empuje)
    public Color color;
    public boolean isAlive = true;
    public static final float RADIUS = 15f;

    public SumoPlayer(int id, float x, float y, Color color) {
        this.id = id;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.color = color;
    }

    public void update(float delta) {
        if (!isAlive) return;
        position.add(velocity.x * delta, velocity.y * delta);
        velocity.scl(0.95f); 
    }
}