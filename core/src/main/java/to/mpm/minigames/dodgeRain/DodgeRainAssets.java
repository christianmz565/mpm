package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Gestiona los recursos del minijuego DodgeRain.
 * <p>
 * Carga y almacena todas las texturas, fuentes y otros recursos
 * necesarios para el juego.
 */
public class DodgeRainAssets implements Disposable {
  /** Textura de fondo del juego. */
  public Texture bgImage;

  /** Textura del pato mirando a la derecha. */
  public Texture duckRight;
  /** Textura del pato caminando mirando a la derecha. */
  public Texture duckRightStep;
  /** Textura del pato enfadado mirando a la derecha. */
  public Texture duckRightUpset;
  /** Textura del pato ralentizado mirando a la derecha. */
  public Texture duckRightRalent;

  /** Textura del pato mirando a la izquierda. */
  public Texture duckLeft;
  /** Textura del pato caminando mirando a la izquierda. */
  public Texture duckLeftStep;
  /** Textura del pato enfadado mirando a la izquierda. */
  public Texture duckLeftUpset;
  /** Textura del pato ralentizado mirando a la izquierda. */
  public Texture duckLeftRalent;

  /** Lista de texturas de obstáculos. */
  public Array<Texture> obstacleTextures;
  /** Fuente para renderizar texto. */
  public BitmapFont font;

  /**
   * Carga todos los recursos del juego desde el sistema de archivos.
   */
  public void load() {
    bgImage = new Texture(Gdx.files.internal("sprites/dodgeRain/bg.png"));

    duckRight = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-right.png"));
    duckRightStep = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-right-step.png"));
    duckRightUpset = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-right-upset.png"));
    duckRightRalent = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-right-ralent.png"));

    duckLeft = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-left.png"));
    duckLeftStep = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-left-step.png"));
    duckLeftUpset = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-left-upset.png"));
    duckLeftRalent = new Texture(Gdx.files.internal("sprites/dodgeRain/duck-left-ralent.png"));

    obstacleTextures = new Array<>();
    obstacleTextures.add(new Texture(Gdx.files.internal("sprites/dodgeRain/obstacle/drop-stone.png")));
    obstacleTextures.add(new Texture(Gdx.files.internal("sprites/dodgeRain/obstacle/drop-branch.png")));
    obstacleTextures.add(new Texture(Gdx.files.internal("sprites/dodgeRain/obstacle/drop-egg.png")));

    font = new BitmapFont();
    font.setColor(Color.WHITE);
    font.getData().setScale(2);
  }

  /**
   * Libera todos los recursos cargados.
   */
  @Override
  public void dispose() {
    if (bgImage != null)
      bgImage.dispose();

    if (duckRight != null)
      duckRight.dispose();
    if (duckRightStep != null)
      duckRightStep.dispose();
    if (duckRightUpset != null)
      duckRightUpset.dispose();
    if (duckRightRalent != null)
      duckRightRalent.dispose();

    if (duckLeft != null)
      duckLeft.dispose();
    if (duckLeftStep != null)
      duckLeftStep.dispose();
    if (duckLeftUpset != null)
      duckLeftUpset.dispose();
    if (duckLeftRalent != null)
      duckLeftRalent.dispose();

    if (obstacleTextures != null) {
      for (Texture t : obstacleTextures) {
        t.dispose();
      }
    }
    if (font != null)
      font.dispose();
  }
}
