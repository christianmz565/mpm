package to.mpm.minigames.infiniteRunner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.GameConstants;

/**
 * Renderiza el juego de carreras.
 */
public class InfiniteRunnerRenderer {
    private static final float[][] PLAYER_COLORS = {
        { 1f, 0.2f, 0.2f },
        { 0.2f, 0.2f, 1f },
        { 0.2f, 1f, 0.2f },
        { 1f, 1f, 0.2f },
        { 1f, 0.2f, 1f },
        { 0.2f, 1f, 1f },
    };
    
    private final InfiniteRunnerAssets assets;
    private final InfiniteRunnerLogic logic;
    
    public InfiniteRunnerRenderer(InfiniteRunnerAssets assets, InfiniteRunnerLogic logic) {
        this.assets = assets;
        this.logic = logic;
    }
    
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        boolean batchWasDrawing = batch.isDrawing();
        if (!batchWasDrawing) {
            batch.begin();
        }
        
        if (assets.bgImage != null) {
            float bgY = logic.cameraY % GameConstants.Screen.HEIGHT;
            batch.draw(assets.bgImage, 0, bgY, GameConstants.Screen.WIDTH, GameConstants.Screen.HEIGHT);
            batch.draw(assets.bgImage, 0, bgY - GameConstants.Screen.HEIGHT, GameConstants.Screen.WIDTH, GameConstants.Screen.HEIGHT);
        }
        
        for (IntMap.Entry<InfiniteRunnerPlayer> entry : logic.players) {
            InfiniteRunnerPlayer player = entry.value;
            float screenY = player.y - logic.cameraY;
            
            if (screenY > -player.height && screenY < GameConstants.Screen.HEIGHT) {
                float[] color = PLAYER_COLORS[player.id % PLAYER_COLORS.length];
                batch.setColor(color[0], color[1], color[2], 1f);
                
                if (assets.playerTexture != null) {
                    batch.draw(assets.playerTexture, player.x, screenY, player.width, player.height);
                }
                
                batch.setColor(Color.WHITE);
            }
        }
        
        for (InfiniteRunnerLogic.Obstacle obs : logic.obstacles) {
            float screenY = obs.y - logic.cameraY;
            
            if (screenY > -obs.height && screenY < GameConstants.Screen.HEIGHT) {
                if (assets.obstacleTexture != null) {
                    batch.draw(assets.obstacleTexture, obs.x, screenY, obs.width, obs.height);
                }
            }
        }
        
        if (logic.countdownActive) {
            int countValue = (int)Math.ceil(logic.countdownTime);
            if (countValue > 0) {
                assets.font.draw(batch, String.valueOf(countValue), 
                    GameConstants.Screen.WIDTH / 2 - 20, GameConstants.Screen.HEIGHT / 2 + 50);
            } else {
                assets.font.draw(batch, "¡VAMOS!", 
                    GameConstants.Screen.WIDTH / 2 - 80, GameConstants.Screen.HEIGHT / 2 + 50);
            }
        }
        
        if (!batchWasDrawing) {
            batch.end();
        }
    }
}
