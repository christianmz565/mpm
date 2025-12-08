package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.Minigame;

import java.util.HashMap;
import java.util.Map;

/**
 * Minijuego de esquivar objetos que caen del cielo.
 * <p>
 * El jugador debe evitar obstáculos que caen mientras acumula puntos
 * por sobrevivir. Diferentes tipos de obstáculos causan diferentes penalizaciones.
 */
public class DodgeRainMinigame implements Minigame {

  /** Identificador del jugador local. */
  private final int localPlayerId;
  /** Indica si el jugador es un espectador. */
  private final boolean isSpectator;

  /** Recursos del minijuego (texturas y fuentes). */
  private DodgeRainAssets assets;
  /** Lógica del juego. */
  private DodgeRainLogic logic;
  /** Renderizador del juego. */
  private DodgeRainRenderer renderer;

  /** Velocidad de movimiento del jugador. */
  private static final float PLAYER_SPEED = 300f;
  /** Multiplicador de velocidad cuando el jugador está ralentizado. */
  private static final float SLOW_SPEED_MULTIPLIER = 0.5f;

  /**
   * Construye una nueva instancia del minijuego DodgeRain.
   *
   * @param localPlayerId identificador del jugador local
   */
  public DodgeRainMinigame(int localPlayerId) {
    this.localPlayerId = localPlayerId;
    this.isSpectator = (localPlayerId == GameConstants.SPECTATOR_ID);
  }

  /**
   * Inicializa el minijuego cargando recursos y configurando la lógica.
   */
  @Override
  public void initialize() {
    assets = new DodgeRainAssets();
    assets.load();

    logic = new DodgeRainLogic(localPlayerId, isSpectator);
    renderer = new DodgeRainRenderer(assets, logic);

    if (!isSpectator) {
      Gdx.app.log("DodgeRain", "Game started as player " + localPlayerId);
    } else {
      Gdx.app.log("DodgeRain", "Game started as SPECTATOR");
    }
  }

  /**
   * Actualiza la lógica del juego.
   *
   * @param delta tiempo transcurrido desde el último frame en segundos
   */
  @Override
  public void update(float delta) {
    logic.update(delta, assets.obstacleTextures.size);
  }

  /**
   * Maneja la entrada del jugador local.
   * Los espectadores no procesan entrada.
   *
   * @param delta tiempo transcurrido desde el último frame en segundos
   */
  @Override
  public void handleInput(float delta) {
    if (isSpectator)
      return;

    if (logic.finished)
      return;

    DodgeRainPlayer localPlayer = logic.localPlayer;
    if (localPlayer == null)
      return;

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

  /**
   * Renderiza el juego usando los renderizadores proporcionados.
   *
   * @param batch renderizador de sprites para dibujar texturas
   * @param shapeRenderer renderizador de formas geométricas
   */
  @Override
  public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
    renderer.render(batch, shapeRenderer);
  }

  /**
   * Indica si el juego ha terminado.
   *
   * @return {@code true} si el juego ha finalizado, {@code false} en caso contrario
   */
  @Override
  public boolean isFinished() {
    return logic.finished;
  }

  /**
   * Obtiene las puntuaciones de todos los jugadores.
   *
   * @return mapa con los identificadores de jugadores y sus puntuaciones
   */
  @Override
  public Map<Integer, Integer> getScores() {
    Map<Integer, Integer> scores = new HashMap<>();
    if (!isSpectator) {
      scores.put(localPlayerId, logic.score);
    }
    return scores;
  }

  /**
   * Obtiene el identificador del jugador ganador.
   *
   * @return identificador del ganador o -1 si el juego no ha terminado
   */
  @Override
  public int getWinnerId() {
    return logic.finished && !isSpectator ? localPlayerId : -1;
  }

  /**
   * Libera los recursos del minijuego.
   */
  @Override
  public void dispose() {
    if (assets != null) {
      assets.dispose();
    }
  }

  /**
   * Maneja el redimensionamiento de la ventana.
   *
   * @param width nuevo ancho de la ventana en píxeles
   * @param height nuevo alto de la ventana en píxeles
   */
  @Override
  public void resize(int width, int height) {
  }
}
