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
                font.getData().setScale(1.5f);
                layout = new GlyphLayout();
            } catch (Exception e) {
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

        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(0, 0, SCREEN_WIDTH, GROUND_Y);

        for (Duck duck : ducks) {
            renderDuck(shapeRenderer, duck);
        }

        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            renderPlayer(shapeRenderer, p);
        }

        shapeRenderer.end();
        
        if (font != null && scores != null) {
            java.util.List<java.util.Map.Entry<Integer, Integer>> sortedScores = new java.util.ArrayList<>(scores.entrySet());
            sortedScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            
            batch.begin();
            
            int localScore = scores.getOrDefault(localPlayerId, 0);
            String scoreText = String.valueOf(localScore);
            layout.setText(font, scoreText);
            float textWidth = layout.width;
            float textX = (SCREEN_WIDTH - textWidth) / 2;
            float textY = SCREEN_HEIGHT - 20;
            
            font.setColor(0, 0, 0, 0.7f);
            font.draw(batch, scoreText, textX + 2, textY - 2);
            
            font.setColor(1, 1, 1, 1);
            font.draw(batch, scoreText, textX, textY);
            
            batch.end();
            
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
     * Render a single duck.
     */
    private static void renderDuck(ShapeRenderer shapeRenderer, Duck duck) {
        if (duck.isCaught()) {
            return;
        }
        
        shapeRenderer.setColor(duck.type.r, duck.type.g, duck.type.b, 1f);
        shapeRenderer.rect(duck.x, duck.y, Duck.DUCK_WIDTH, Duck.DUCK_HEIGHT);
        
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        if (duck.type == Duck.DuckType.GOLDEN) {
            shapeRenderer.setColor(1f, 1f, 0.5f, 1f);
        } else if (duck.type == Duck.DuckType.BAD) {
            shapeRenderer.setColor(1f, 0f, 0f, 1f);
        } else {
            shapeRenderer.setColor(0.4f, 0.3f, 0.1f, 1f);
        }
        
        shapeRenderer.rect(duck.x, duck.y, Duck.DUCK_WIDTH, Duck.DUCK_HEIGHT);
        
        float centerX = duck.x + Duck.DUCK_WIDTH / 2;
        float centerY = duck.y + Duck.DUCK_HEIGHT / 2;
        float symbolSize = 4f;
        
        if (duck.type == Duck.DuckType.GOLDEN) {
            shapeRenderer.line(centerX - symbolSize, centerY, centerX + symbolSize, centerY);
            shapeRenderer.line(centerX, centerY - symbolSize, centerX, centerY + symbolSize);
            shapeRenderer.line(centerX - symbolSize * 0.7f, centerY - symbolSize * 0.7f, 
                              centerX + symbolSize * 0.7f, centerY + symbolSize * 0.7f);
            shapeRenderer.line(centerX - symbolSize * 0.7f, centerY + symbolSize * 0.7f, 
                              centerX + symbolSize * 0.7f, centerY - symbolSize * 0.7f);
        } else if (duck.type == Duck.DuckType.BAD) {
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
        shapeRenderer.setColor(p.r, p.g, p.b, 1f);
        shapeRenderer.rect(p.x, p.y, Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        
        shapeRenderer.setColor(0.8f, 0.6f, 0.3f, 1f);
        Rectangle basket = p.getBasketBounds();
        shapeRenderer.rect(basket.x, basket.y, basket.width, basket.height);
        
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.4f, 0.2f, 1f);
        shapeRenderer.rect(basket.x, basket.y, basket.width, basket.height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }
}
