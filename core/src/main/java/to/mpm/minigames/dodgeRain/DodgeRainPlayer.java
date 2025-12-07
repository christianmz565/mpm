package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.math.Rectangle;

public class DodgeRainPlayer {
  public int id;
  public Rectangle bounds;
  public boolean facingRight = true;
  public boolean isUpset = false;
  public boolean isSlowed = false;
  public float hitTimer = 0;
  public boolean isMoving = false;
  public float stateTime = 0;

  public DodgeRainPlayer(int id, float x, float y) {
    this.id = id;
    this.bounds = new Rectangle(x, y, 64, 64);
  }
}
