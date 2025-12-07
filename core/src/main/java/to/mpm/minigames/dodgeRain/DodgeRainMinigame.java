package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.Minigame;

import java.util.HashMap;
import java.util.Map;

public class DodgeRainMinigame implements Minigame {

  private final int localPlayerId;

  private DodgeRainAssets assets;
  private DodgeRainLogic logic;
  private DodgeRainRenderer renderer;

  private static final float PLAYER_SPEED = 300f;
  private static final float SLOW_SPEED_MULTIPLIER = 0.5f;

  public DodgeRainMinigame(int localPlayerId) {
    this.localPlayerId = localPlayerId;
  }

  @Override
  public void initialize() {
    assets = new DodgeRainAssets();
    assets.load();

    logic = new DodgeRainLogic(localPlayerId);
    renderer = new DodgeRainRenderer(assets, logic);
  }

  @Override
  public void update(float delta) {
    logic.update(delta, assets.obstacleTextures.size);
  }

  @Override
  public void handleInput(float delta) {
    if (logic.finished)
      return;

    DodgeRainPlayer localPlayer = logic.localPlayer;
    float currentSpeed = PLAYER_SPEED;
    if (localPlayer.isSlowed) {
      currentSpeed *= SLOW_SPEED_MULTIPLIER;
    }

    localPlayer.isMoving = false;

    if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
      localPlayer.bounds.x -= currentSpeed * delta;
      localPlayer.facingRight = false;
      localPlayer.isMoving = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
      localPlayer.bounds.x += currentSpeed * delta;
      localPlayer.facingRight = true;
      localPlayer.isMoving = true;
    }

    if (localPlayer.bounds.x < 0)
      localPlayer.bounds.x = 0;
    if (localPlayer.bounds.x > GameConstants.Screen.WIDTH - localPlayer.bounds.width)
      localPlayer.bounds.x = GameConstants.Screen.WIDTH - localPlayer.bounds.width;
  }

  @Override
  public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
    renderer.render(batch, shapeRenderer);
  }

  @Override
  public boolean isFinished() {
    return logic.finished;
  }

  @Override
  public Map<Integer, Integer> getScores() {
    Map<Integer, Integer> scores = new HashMap<>();
    scores.put(localPlayerId, logic.score);
    return scores;
  }

  @Override
  public int getWinnerId() {
    return logic.finished ? localPlayerId : -1;
  }

  @Override
  public void dispose() {
    if (assets != null) {
      assets.dispose();
    }
  }

  @Override
  public void resize(int width, int height) {
  }
}
