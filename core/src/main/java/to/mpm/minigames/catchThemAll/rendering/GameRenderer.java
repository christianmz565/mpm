package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.catchThemAll.entities.Player;

/**
 * Handles all rendering for the Catch Them All minigame.
 */
public class GameRenderer {
    private static final float SCREEN_WIDTH = 640f;
    private static final float GROUND_Y = 60f;
    
    /**
     * Render the game (players, ground, baskets, etc).
     * 
     * @param batch SpriteBatch for sprite rendering
     * @param shapeRenderer ShapeRenderer for geometric shapes
     * @param players map of all active players
     */
    public static void render(SpriteBatch batch, ShapeRenderer shapeRenderer, IntMap<Player> players) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw ground
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(0, 0, SCREEN_WIDTH, GROUND_Y);

        // Draw all players
        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            renderPlayer(shapeRenderer, p);
        }

        shapeRenderer.end();
        
        // Draw HUD with scores
        batch.begin();
        // TODO: Add BitmapFont and draw scores here
        batch.end();
    }
    
    /**
     * Render a single player with their basket.
     */
    private static void renderPlayer(ShapeRenderer shapeRenderer, Player p) {
        // Draw player body
        shapeRenderer.setColor(p.r, p.g, p.b, 1f);
        shapeRenderer.rect(p.x, p.y, Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        
        // Draw basket on top of player (brown/tan color)
        shapeRenderer.setColor(0.8f, 0.6f, 0.3f, 1f);
        Rectangle basket = p.getBasketBounds();
        shapeRenderer.rect(basket.x, basket.y, basket.width, basket.height);
        
        // Draw basket outline for visibility
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.4f, 0.2f, 1f);
        shapeRenderer.rect(basket.x, basket.y, basket.width, basket.height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }
}
