package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import to.mpm.minigames.GameConstants;
import to.mpm.network.NetworkManager;

/**
 * Gestiona la lógica del minijuego DodgeRain.
 * <p>
 * Maneja jugadores, obstáculos que caen, colisiones, puntuaciones
 * y el estado del juego.
 */
public class DodgeRainLogic {
  /** Mapa de todos los jugadores en el juego. */
  public IntMap<DodgeRainPlayer> players;
  /** Referencia al jugador local, puede ser {@code null} si es espectador. */
  public DodgeRainPlayer localPlayer;
  /** Lista de obstáculos que caen. */
  public Array<Rectangle> raindrops;
  /** Tipos de obstáculos correspondientes a cada elemento en raindrops. */
  public Array<Integer> raindropTypes;

  /** Timestamp de la última gota generada. */
  private long lastDropTime;
  /** Puntuación actual del jugador local. */
  public int score;
  /** Tiempo que el jugador ha sobrevivido. */
  public float timeAlive;
  /** Tiempo restante del juego. */
  public float gameTime;
  /** Indica si el juego ha terminado. */
  public boolean finished;

  /** Velocidad de caída de los obstáculos. */
  private static final float RAIN_SPEED = 300f;
  /** Duración total del juego en segundos. */
  private static final float GAME_DURATION = 10f;
  /** Duración del estado "upset" después de ser golpeado. */
  private static final float UPSET_DURATION = 0.5f;
  /** Duración del estado "slowed" después del estado "upset". */
  private static final float SLOW_DURATION = 1.0f;

  /**
   * Construye una nueva instancia de la lógica del juego.
   *
   * @param localPlayerId identificador del jugador local
   * @param isSpectator indica si el jugador local es un espectador
   */
  public DodgeRainLogic(int localPlayerId, boolean isSpectator) {
    players = new IntMap<>();

    if (!isSpectator) {
      localPlayer = new DodgeRainPlayer(localPlayerId, GameConstants.Screen.WIDTH / 2 - 32, 65);
      players.put(localPlayerId, localPlayer);
    }

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
    spawnRaindrop(3);

    score = 0;
    timeAlive = 0;
    gameTime = GAME_DURATION;
    finished = false;
  }

  /**
   * Genera un nuevo obstáculo que cae desde la parte superior de la pantalla.
   *
   * @param maxTypes número máximo de tipos de obstáculos disponibles
   */
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

  /**
   * Actualiza el estado del juego cada frame.
   *
   * @param delta tiempo transcurrido desde el último frame en segundos
   * @param maxObstacleTypes número máximo de tipos de obstáculos
   */
  public void update(float delta, int maxObstacleTypes) {
    if (finished)
      return;

    gameTime -= delta;
    if (gameTime <= 0) {
      finished = true;
      return;
    }

    if (localPlayer != null) {
      updatePlayerState(localPlayer, delta);

      float prevTimeAlive = timeAlive;
      timeAlive += delta;
      if ((int) timeAlive > (int) prevTimeAlive) {
        score += 10000;
      }
    }

    if (TimeUtils.nanoTime() - lastDropTime > 300000000) {
      spawnRaindrop(maxObstacleTypes);
    }

    for (int i = 0; i < raindrops.size; i++) {
      Rectangle raindrop = raindrops.get(i);
      raindrop.y -= RAIN_SPEED * delta;

      if (localPlayer != null && raindrop.overlaps(localPlayer.bounds)) {
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

  /**
   * Actualiza el estado de un jugador, gestionando temporizadores y animaciones.
   *
   * @param p jugador a actualizar
   * @param delta tiempo transcurrido desde el último frame en segundos
   */
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
    if (p.isMoving) {
      p.stateTime += delta;
    } else {
      p.stateTime = 0;
    }
  }
}
