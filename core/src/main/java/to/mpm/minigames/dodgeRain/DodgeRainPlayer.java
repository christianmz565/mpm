package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.math.Rectangle;

/**
 * Representa un jugador en el minijuego DodgeRain.
 * <p>
 * Gestiona la posición, estado de animación y efectos de estado
 * del jugador (upset, slowed).
 */
public class DodgeRainPlayer {
  /** Identificador único del jugador. */
  public int id;
  /** Rectángulo de colisión del jugador. */
  public Rectangle bounds;
  /** Indica la dirección en la que mira el jugador. */
  public boolean facingRight = true;
  /** Indica si el jugador está en estado "upset" después de ser golpeado. */
  public boolean isUpset = false;
  /** Indica si el jugador está ralentizado. */
  public boolean isSlowed = false;
  /** Temporizador para gestionar la duración de los estados. */
  public float hitTimer = 0;
  /** Indica si el jugador se está moviendo. */
  public boolean isMoving = false;
  /** Tiempo acumulado para controlar la animación. */
  public float stateTime = 0;

  /**
   * Construye un nuevo jugador.
   *
   * @param id identificador del jugador
   * @param x posición inicial en el eje X
   * @param y posición inicial en el eje Y
   */
  public DodgeRainPlayer(int id, float x, float y) {
    this.id = id;
    this.bounds = new Rectangle(x, y, 64, 64);
  }
}
