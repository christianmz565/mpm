package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
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
    private static GlyphLayout layout;
    
    /**
     * Initialize the renderer (call once).
     */
    public static void initialize() {
        if (font == null) {
            try {
                Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
                font = skin.getFont("font");
                font.getData().setScale(1.5f); // Make it bigger
                layout = new GlyphLayout();
            } catch (Exception e) {
                // Fallback to default font if skin not found
                font = new BitmapFont();
                font.getData().setScale(2f);
                layout = new GlyphLayout();
                Gdx.app.log("GameRenderer", "Using default font (skin not found)");
            }
        }
    }
    
    /**
     * Dispose resources (call when done).
     */
    public static void dispose() {
        if (font != null) {
            // Don't dispose font if it came from skin
            font = null;
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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw ground
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(0, 0, SCREEN_WIDTH, GROUND_Y);

        // Draw all ducks
        for (Duck duck : ducks) {
            renderDuck(shapeRenderer, duck);
        }

        // Draw all players
        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            renderPlayer(shapeRenderer, p);
        }

        shapeRenderer.end();
        
        // Draw HUD
        if (font != null && scores != null) {
            // Sort players by score (descending)
            java.util.List<java.util.Map.Entry<Integer, Integer>> sortedScores = new java.util.ArrayList<>(scores.entrySet());
            sortedScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            
            batch.begin();
            
            // Draw local player score at top center
            int localScore = scores.getOrDefault(localPlayerId, 0);
            String scoreText = String.valueOf(localScore);
            layout.setText(font, scoreText);
            float textWidth = layout.width;
            float textX = (SCREEN_WIDTH - textWidth) / 2;
            float textY = SCREEN_HEIGHT - 20; // 20 pixels from top
            
            // Draw shadow for better visibility
            font.setColor(0, 0, 0, 0.7f);
            font.draw(batch, scoreText, textX + 2, textY - 2);
            
            // Draw actual text
            font.setColor(1, 1, 1, 1);
            font.draw(batch, scoreText, textX, textY);
            
            batch.end();
            
            // Draw player ranking on the left side
            renderPlayerRanking(batch, shapeRenderer, sortedScores, playerColors, localPlayerId);
        }
    }
    
    /**
     * Render the player ranking list on the left side.
     */
    private static void renderPlayerRanking(SpriteBatch batch, ShapeRenderer shapeRenderer, 
                                           java.util.List<java.util.Map.Entry<Integer, Integer>> sortedScores,
                                           float[][] playerColors, int localPlayerId) {
        float startX = 10; // 10 pixels from left edge
        float startY = SCREEN_HEIGHT - 60; // Start below the center score
        float boxSize = 20; // Size of the color box
        float spacing = 30; // Vertical spacing between entries
        float textOffsetX = 28; // Text offset from box
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < sortedScores.size(); i++) {
            java.util.Map.Entry<Integer, Integer> entry = sortedScores.get(i);
            int playerId = entry.getKey();
            
            float y = startY - (i * spacing);
            
            // Get player color
            float[] color = playerColors[playerId % playerColors.length];
            
            // Draw color box
            shapeRenderer.setColor(color[0], color[1], color[2], 1f);
            shapeRenderer.rect(startX, y - boxSize, boxSize, boxSize);
            
            // Draw box outline (darker)
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(startX, y - boxSize, boxSize, boxSize);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        }
        
        shapeRenderer.end();
        
        // Draw scores text
        batch.begin();
        
        for (int i = 0; i < sortedScores.size(); i++) {
            java.util.Map.Entry<Integer, Integer> entry = sortedScores.get(i);
            int playerId = entry.getKey();
            int score = entry.getValue();
            
            float y = startY - (i * spacing);
            
            String scoreStr = String.valueOf(score);
            
            // Highlight local player
            if (playerId == localPlayerId) {
                // Draw background for local player
                font.setColor(1f, 1f, 0.3f, 1f); // Yellow highlight
            } else {
                font.setColor(1f, 1f, 1f, 1f); // White
            }
            
            // Draw shadow
            font.setColor(0, 0, 0, 0.7f);
            font.draw(batch, scoreStr, startX + textOffsetX + 1, y - 1);
            
            // Draw text
            if (playerId == localPlayerId) {
                font.setColor(1f, 1f, 0.3f, 1f); // Yellow for local player
            } else {
                font.setColor(1f, 1f, 1f, 1f); // White for others
            }
            font.draw(batch, scoreStr, startX + textOffsetX, y);
        }
        
        batch.end();
    }
    
    /**
     * Render a single duck.
     */
    private static void renderDuck(ShapeRenderer shapeRenderer, Duck duck) {
        if (duck.isCaught()) {
            return;  // Don't render caught ducks
        }
        
        // Draw duck body with color based on type
        shapeRenderer.setColor(duck.type.r, duck.type.g, duck.type.b, 1f);
        shapeRenderer.rect(duck.x, duck.y, Duck.DUCK_WIDTH, Duck.DUCK_HEIGHT);
        
        // Draw duck outline for better visibility
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // Different outline color for golden ducks
        if (duck.type == Duck.DuckType.GOLDEN) {
            shapeRenderer.setColor(1f, 1f, 0.5f, 1f);  // Bright yellow outline
        } else if (duck.type == Duck.DuckType.BAD) {
            shapeRenderer.setColor(1f, 0f, 0f, 1f);  // Red outline
        } else {
            shapeRenderer.setColor(0.4f, 0.3f, 0.1f, 1f);  // Dark brown outline
        }
        
        shapeRenderer.rect(duck.x, duck.y, Duck.DUCK_WIDTH, Duck.DUCK_HEIGHT);
        
        // Draw a symbol in the center to distinguish types better
        float centerX = duck.x + Duck.DUCK_WIDTH / 2;
        float centerY = duck.y + Duck.DUCK_HEIGHT / 2;
        float symbolSize = 4f;
        
        if (duck.type == Duck.DuckType.GOLDEN) {
            // Draw a star/cross for golden duck
            shapeRenderer.line(centerX - symbolSize, centerY, centerX + symbolSize, centerY);
            shapeRenderer.line(centerX, centerY - symbolSize, centerX, centerY + symbolSize);
            shapeRenderer.line(centerX - symbolSize * 0.7f, centerY - symbolSize * 0.7f, 
                              centerX + symbolSize * 0.7f, centerY + symbolSize * 0.7f);
            shapeRenderer.line(centerX - symbolSize * 0.7f, centerY + symbolSize * 0.7f, 
                              centerX + symbolSize * 0.7f, centerY - symbolSize * 0.7f);
        } else if (duck.type == Duck.DuckType.BAD) {
            // Draw an X for bad duck
            shapeRenderer.line(centerX - symbolSize, centerY - symbolSize, 
                              centerX + symbolSize, centerY + symbolSize);
            shapeRenderer.line(centerX - symbolSize, centerY + symbolSize, 
                              centerX + symbolSize, centerY - symbolSize);
        }
        
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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
