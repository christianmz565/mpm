package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.entities.Player;

import java.util.List;

/**
 * Handles all rendering for the Catch Them All minigame.
 */
public class GameRenderer {
    private static final float SCREEN_WIDTH = 640f;
    private static final float SCREEN_HEIGHT = 480f;
    private static final float GROUND_Y = 60f;
    
    private static BitmapFont font;
    private static SpriteManager spriteManager;
    
    /**
     * Initialize the renderer (call once).
     */
    public static void initialize() {
        if (font == null) {
            try {
                Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
                font = skin.getFont("font");
                font.getData().setScale(1.5f);
            } catch (Exception e) {
                font = new BitmapFont();
                font.getData().setScale(2f);
                Gdx.app.log("GameRenderer", "Using default font (skin not found)");
            }
        }
        
        // Load sprites
        spriteManager = SpriteManager.getInstance();
        spriteManager.loadSprites();
    }
    
    /**
     * Dispose resources (call when done).
     */
    public static void dispose() {
        if (font != null) {
            font = null;
        }
        if (spriteManager != null) {
            spriteManager.dispose();
        }
    }
    
    /**
     * Render the game (players, ground, baskets, ducks, etc).
     * 
     * @param batch SpriteBatch for sprite rendering
     * @param shapeRenderer ShapeRenderer for geometric shapes
     * @param players map of all active players
     * @param ducks list of all active ducks
     * @param scores map of player IDs to scores
     * @param playerColors array of player colors
     * @param localPlayerId ID of the local player
     */
    public static void render(SpriteBatch batch, ShapeRenderer shapeRenderer, IntMap<Player> players, List<Duck> ducks, 
                             java.util.Map<Integer, Integer> scores, float[][] playerColors, int localPlayerId) {
        
        // Render background and environment with sprites
        batch.begin();
        
        if (spriteManager != null && spriteManager.isLoaded()) {
            // Draw background
            Texture bg = spriteManager.getBackground();
            if (bg != null) {
                batch.draw(bg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            }
            
            // Draw floor
            Texture floor = spriteManager.getFloor();
            if (floor != null) {
                batch.draw(floor, 0, 0, SCREEN_WIDTH, GROUND_Y);
            }
        }
        
        batch.end();
        
        // If sprites not loaded, fallback to shapes for ground
        if (spriteManager == null || !spriteManager.isLoaded()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            shapeRenderer.rect(0, 0, SCREEN_WIDTH, GROUND_Y);
            shapeRenderer.end();
        }
        
        // Render game entities with sprites
        batch.begin();
        
        for (Duck duck : ducks) {
            renderDuck(batch, duck);
        }

        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            renderPlayer(batch, p);
        }
        
        batch.end();
        
        // Render player ranking on the left side
        if (font != null && scores != null) {
            java.util.List<java.util.Map.Entry<Integer, Integer>> sortedScores = new java.util.ArrayList<>(scores.entrySet());
            sortedScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            
            renderPlayerRanking(batch, shapeRenderer, sortedScores, playerColors, localPlayerId);
        }
    }
    
    /**
     * Render the player ranking list on the left side.
     */
    private static void renderPlayerRanking(SpriteBatch batch, ShapeRenderer shapeRenderer, 
                                           java.util.List<java.util.Map.Entry<Integer, Integer>> sortedScores,
                                           float[][] playerColors, int localPlayerId) {
        float startX = 10;
        float startY = SCREEN_HEIGHT - 60;
        float boxSize = 20;
        float spacing = 30;
        float textOffsetX = 28;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < sortedScores.size(); i++) {
            java.util.Map.Entry<Integer, Integer> entry = sortedScores.get(i);
            int playerId = entry.getKey();
            
            float y = startY - (i * spacing);
            
            float[] color = playerColors[playerId % playerColors.length];
            
            shapeRenderer.setColor(color[0], color[1], color[2], 1f);
            shapeRenderer.rect(startX, y - boxSize, boxSize, boxSize);
            
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(startX, y - boxSize, boxSize, boxSize);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        }
        
        shapeRenderer.end();
        
        batch.begin();
        
        for (int i = 0; i < sortedScores.size(); i++) {
            java.util.Map.Entry<Integer, Integer> entry = sortedScores.get(i);
            int playerId = entry.getKey();
            int score = entry.getValue();
            
            float y = startY - (i * spacing);
            
            String scoreStr = String.valueOf(score);
            
            if (playerId == localPlayerId) {
                font.setColor(1f, 1f, 0.3f, 1f);
            } else {
                font.setColor(1f, 1f, 1f, 1f);
            }
            
            font.setColor(0, 0, 0, 0.7f);
            font.draw(batch, scoreStr, startX + textOffsetX + 1, y - 1);
            
            if (playerId == localPlayerId) {
                font.setColor(1f, 1f, 0.3f, 1f);
            } else {
                font.setColor(1f, 1f, 1f, 1f);
            }
            font.draw(batch, scoreStr, startX + textOffsetX, y);
        }
        
        batch.end();
    }
    
    /**
     * Render a single duck using sprites.
     */
    private static void renderDuck(SpriteBatch batch, Duck duck) {
        if (duck.isCaught()) {
            return;
        }
        
        AnimatedSprite animation = duck.getAnimation();
        if (animation != null && spriteManager != null && spriteManager.isLoaded()) {
            Texture currentFrame = animation.getCurrentFrame();
            if (currentFrame != null) {
                batch.draw(currentFrame, duck.x, duck.y, Duck.DUCK_WIDTH, Duck.DUCK_HEIGHT);
            }
        }
    }
    
    /**
     * Render a single player with their basket using sprites.
     */
    private static void renderPlayer(SpriteBatch batch, Player p) {
        AnimatedSprite animation = p.getRunAnimation();
        if (animation != null && spriteManager != null && spriteManager.isLoaded()) {
            Texture currentFrame = animation.getCurrentFrame();
            if (currentFrame != null) {
                // Apply color tint to differentiate players
                batch.setColor(p.r, p.g, p.b, 1f);
                
                // Flip sprite horizontally if facing left using texture coordinates
                if (p.isFacingRight()) {
                    // Normal draw
                    batch.draw(currentFrame, 
                        p.x, p.y,                           // position
                        Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
                } else {
                    // Draw flipped: use region parameters to flip texture coordinates
                    batch.draw(currentFrame,
                        p.x, p.y,                           // position
                        Player.PLAYER_WIDTH / 2, Player.PLAYER_HEIGHT / 2,  // origin (center for rotation)
                        Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT,          // size
                        -1f, 1f,                            // scale X negative flips horizontally
                        0f,                                 // rotation
                        0, 0,                               // source position in texture
                        currentFrame.getWidth(), currentFrame.getHeight(),  // source size
                        false, false);                      // no additional flip
                }
                
                // Reset color
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
        
        // Draw basket (could be replaced with sprite later)
        // For now, we'll skip it or you can add a basket sprite
    }
}
