package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.GameConstants;

/**
 * Renderizador para el minijuego DodgeRain.
 * <p>
 * Se encarga de dibujar el fondo, jugadores con sus animaciones
 * y los obstáculos que caen.
 */
public class DodgeRainRenderer {
  /** Recursos del juego (texturas y fuentes). */
  private final DodgeRainAssets assets;
  /** Lógica del juego a renderizar. */
  private final DodgeRainLogic logic;

  /** Colores asignados a cada jugador por su índice. */
  public static final float[][] PLAYER_COLORS = {
      { 1f, 0.2f, 0.2f },
      { 0.2f, 0.2f, 1f },
      { 0.2f, 1f, 0.2f },
      { 1f, 1f, 0.2f },
      { 1f, 0.2f, 1f },
      { 0.2f, 1f, 1f },
  };

  /**
   * Construye un nuevo renderizador.
   *
   * @param assets recursos del juego
   * @param logic lógica del juego a renderizar
   */
  public DodgeRainRenderer(DodgeRainAssets assets, DodgeRainLogic logic) {
    this.assets = assets;
    this.logic = logic;
  }

  /**
   * Renderiza todos los elementos del juego.
   *
   * @param batch renderizador de sprites
   * @param shapeRenderer renderizador de formas geométricas (no utilizado)
   */
  public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
    boolean batchWasDrawing = batch.isDrawing();
    if (!batchWasDrawing) {
      batch.begin();
    }

    if (assets.bgImage != null) {
      batch.draw(assets.bgImage, 0, 0, GameConstants.Screen.WIDTH, GameConstants.Screen.HEIGHT);
    }

    for (IntMap.Entry<DodgeRainPlayer> entry : logic.players) {
      DodgeRainPlayer p = entry.value;

      Texture currentDuckTexture;
      if (p.isUpset) {
        currentDuckTexture = p.facingRight ? assets.duckRightUpset : assets.duckLeftUpset;
      } else if (p.isSlowed) {
        currentDuckTexture = p.facingRight ? assets.duckRightRalent : assets.duckLeftRalent;
      } else {
        boolean useStep = p.isMoving && (p.stateTime % 0.4f > 0.2f);
        if (p.facingRight) {
          currentDuckTexture = useStep ? assets.duckRightStep : assets.duckRight;
        } else {
          currentDuckTexture = useStep ? assets.duckLeftStep : assets.duckLeft;
        }
      }

      if (currentDuckTexture != null) {
        float[] color = PLAYER_COLORS[p.id % PLAYER_COLORS.length];
        batch.setColor(color[0], color[1], color[2], 1f);
        batch.draw(currentDuckTexture, p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
        batch.setColor(Color.WHITE);
      }
    }

    for (int i = 0; i < logic.raindrops.size; i++) {
      Rectangle raindrop = logic.raindrops.get(i);
      int type = logic.raindropTypes.get(i);
      if (type >= 0 && type < assets.obstacleTextures.size) {
        batch.draw(assets.obstacleTextures.get(type), raindrop.x, raindrop.y, raindrop.width, raindrop.height);
      }
    }

    if (logic.finished) {
      assets.font.draw(batch, "GAME OVER", GameConstants.Screen.WIDTH / 2 - 100, GameConstants.Screen.HEIGHT / 2);
    }

    if (!batchWasDrawing) {
      batch.end();
    }
  }
}
