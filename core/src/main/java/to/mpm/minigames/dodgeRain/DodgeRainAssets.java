package to.mpm.minigames.dodgeRain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class DodgeRainAssets implements Disposable {
  public Texture bgImage;

  public Texture duckRight;
  public Texture duckRightStep;
  public Texture duckRightUpset;
  public Texture duckRightRalent;

  public Texture duckLeft;
  public Texture duckLeftStep;
  public Texture duckLeftUpset;
  public Texture duckLeftRalent;

  public Array<Texture> obstacleTextures;
  public BitmapFont font;

  // Load all assets
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
