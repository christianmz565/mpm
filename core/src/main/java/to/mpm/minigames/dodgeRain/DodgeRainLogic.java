package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import to.mpm.minigames.GameConstants;
import to.mpm.network.NetworkManager;

public class DodgeRainLogic {
  public IntMap<DodgeRainPlayer> players;
  public DodgeRainPlayer localPlayer;
  public Array<Rectangle> raindrops;
  public Array<Integer> raindropTypes;

  private long lastDropTime;
  public int score;
  public float timeAlive;
  public float gameTime;
  public boolean finished;

  private static final float RAIN_SPEED = 300f;
  private static final float GAME_DURATION = 10f;
  private static final float UPSET_DURATION = 0.5f;
  private static final float SLOW_DURATION = 1.0f;

  public DodgeRainLogic(int localPlayerId) {
    players = new IntMap<>();
    // Ajusta el valor 65 para que coincida con la altura del pasto
    localPlayer = new DodgeRainPlayer(localPlayerId, GameConstants.Screen.WIDTH / 2 - 32, 65);
    players.put(localPlayerId, localPlayer);

    NetworkManager nm = NetworkManager.getInstance();
    if (nm != null) {
      for (Integer playerId : nm.getConnectedPlayers().keySet()) {
        if (playerId != localPlayerId) {
          players.put(playerId, new DodgeRainPlayer(playerId, GameConstants.Screen.WIDTH / 2 - 32, 65));
        }
      }
    }

    raindrops = new Array<>();
    raindropTypes = new Array<>();
    spawnRaindrop(3); // Default to 3 types if not specified

    score = 0;
    timeAlive = 0;
    gameTime = GAME_DURATION;
    finished = false;
  }

  private void spawnRaindrop(int maxTypes) {
    Rectangle raindrop = new Rectangle();
    raindrop.x = MathUtils.random(0, GameConstants.Screen.WIDTH - 64);
    raindrop.y = GameConstants.Screen.HEIGHT;
    raindrop.width = 48;
    raindrop.height = 48;
    raindrops.add(raindrop);
    raindropTypes.add(MathUtils.random(0, maxTypes - 1));
    lastDropTime = TimeUtils.nanoTime();
  }

  public void update(float delta, int maxObstacleTypes) {
    if (finished)
      return;

    gameTime -= delta;
    if (gameTime <= 0) {
      finished = true;
      return;
    }

    updatePlayerState(localPlayer, delta);

    float prevTimeAlive = timeAlive;
    timeAlive += delta;
    // Increase score by 10000 every second
    if ((int) timeAlive > (int) prevTimeAlive) {
      score += 10000;
    }

    if (TimeUtils.nanoTime() - lastDropTime > 300000000) {
      spawnRaindrop(maxObstacleTypes);
    }

    for (int i = 0; i < raindrops.size; i++) {
      Rectangle raindrop = raindrops.get(i);
      raindrop.y -= RAIN_SPEED * delta;

      if (raindrop.overlaps(localPlayer.bounds)) {
        // Hit logic based on type
        int type = raindropTypes.get(i);
        switch (type) {
          case 0: // Stone
            score -= 30000;
            break;
          case 1: // Branch
            score -= 20000;
            break;
          case 2: // Egg
            score -= 10000;
            break;
          default:
            score -= 10000;
            break;
        }
        if (score < 0)
          score = 0; // Prevent negative score

        localPlayer.isUpset = true;
        localPlayer.isSlowed = false;
        localPlayer.hitTimer = UPSET_DURATION;

        raindrops.removeIndex(i);
        raindropTypes.removeIndex(i);
        i--;
      } else if (raindrop.y + 64 < 0) {
        raindrops.removeIndex(i);
        raindropTypes.removeIndex(i);
        i--;
      }
    }
  }

  private void updatePlayerState(DodgeRainPlayer p, float delta) {
    if (p.hitTimer > 0) {
      p.hitTimer -= delta;
      if (p.hitTimer <= 0) {
        if (p.isUpset) {
          p.isUpset = false;
          p.isSlowed = true;
          p.hitTimer = SLOW_DURATION;
        } else if (p.isSlowed) {
          p.isSlowed = false;
        }
      }
    }
    // Update animation state time
    if (p.isMoving) {
      p.stateTime += delta;
    } else {
      p.stateTime = 0;
    }
  }
}
