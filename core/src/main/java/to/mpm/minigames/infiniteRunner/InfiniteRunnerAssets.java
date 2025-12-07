package to.mpm.minigames.infiniteRunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Carga y gestiona recursos del juego.
 */
public class InfiniteRunnerAssets {
    public Texture bgImage;
    public Texture playerTexture;
    public Texture obstacleTexture;
    public BitmapFont font;
    
    public void load() {
        try {
            bgImage = new Texture(Gdx.files.internal("sprites/dodgeRain/background.png"));
        } catch (Exception e) {
            Gdx.app.log("Assets", "No background image found");
            bgImage = null;
        }
        
        try {
            playerTexture = new Texture(Gdx.files.internal("sprites/sumo/player.png"));
        } catch (Exception e) {
            Gdx.app.log("Assets", "No player texture found");
            playerTexture = null;
        }
        
        try {
            obstacleTexture = new Texture(Gdx.files.internal("sprites/dodgeRain/obstacle.png"));
        } catch (Exception e) {
            Gdx.app.log("Assets", "No obstacle texture found");
            obstacleTexture = null;
        }
        
        font = new BitmapFont();
        font.getData().setScale(2f);
    }
    
    public void dispose() {
        if (bgImage != null) bgImage.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (obstacleTexture != null) obstacleTexture.dispose();
        if (font != null) font.dispose();
    }
}
