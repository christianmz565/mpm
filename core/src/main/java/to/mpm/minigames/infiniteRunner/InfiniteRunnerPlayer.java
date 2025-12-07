package to.mpm.minigames.infiniteRunner;

import com.badlogic.gdx.math.Rectangle;

/**
 * Representa a un jugador en la carrera.
 */
public class InfiniteRunnerPlayer {
    public int id;
    public float x, y;
    public float width, height;
    
    public float velocityY = 0f;
    public boolean isGrounded = true;
    
    public boolean isSlowed = false;
    public float slowTimer = 0f;
    public boolean facingRight = true;
    public boolean isMoving = false;
    
    public float stateTime = 0f;
    
    public InfiniteRunnerPlayer(int id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
