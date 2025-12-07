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
 * Maneja todo el renderizado para el minijuego Atrapa a Todos.
 */
public class GameRenderer {
    private static final float SCREEN_WIDTH = 640f;
    private static final float SCREEN_HEIGHT = 480f;
    private static final float GROUND_Y = 60f;
    
    private static BitmapFont font;
    private static SpriteManager spriteManager;
    private static CloudsLayer cloudsLayer;
    
    /**
     * Inicializa el renderizador.
     * <p>
     * Debe llamarse una sola vez al inicio del minijuego.
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
        
        spriteManager = SpriteManager.getInstance();
        spriteManager.loadSprites();
        
        cloudsLayer = new CloudsLayer(SCREEN_WIDTH, SCREEN_HEIGHT);
    }
    
    /**
     * Libera los recursos del renderizador.
     * <p>
     * Debe llamarse al finalizar el minijuego.
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
     * Actualiza los elementos del renderizador.
     * <p>
     * Incluye efectos como el desplazamiento de las nubes.
     * 
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    public static void update(float delta) {
        if (cloudsLayer != null && spriteManager != null && spriteManager.isLoaded()) {
            Texture clouds = spriteManager.getClouds();
            cloudsLayer.update(delta, clouds);
        }
    }
    
    /**
     * Renderiza el juego completo.
     * <p>
     * Incluye fondo, nubes, jugadores, patos y puntuaciones.
     * 
     * @param batch el SpriteBatch para renderizar sprites
     * @param shapeRenderer el ShapeRenderer para formas geométricas
     * @param players mapa de todos los jugadores activos
     * @param ducks lista de todos los patos activos
     * @param scores mapa de identificadores de jugador a puntuaciones
     * @param playerColors arreglo de colores de jugadores
     * @param localPlayerId identificador del jugador local
     */
    public static void render(SpriteBatch batch, ShapeRenderer shapeRenderer, IntMap<Player> players, List<Duck> ducks, 
                             java.util.Map<Integer, Integer> scores, float[][] playerColors, int localPlayerId) {
        
        batch.begin();
        
        if (spriteManager != null && spriteManager.isLoaded()) {
            Texture bg = spriteManager.getBackground();
            if (bg != null) {
                batch.draw(bg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            }
            
            if (cloudsLayer != null) {
                Texture clouds = spriteManager.getClouds();
                cloudsLayer.render(batch, clouds);
            }
        }
        
        batch.end();
        
        if (spriteManager == null || !spriteManager.isLoaded()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            shapeRenderer.rect(0, 0, SCREEN_WIDTH, GROUND_Y);
            shapeRenderer.end();
        }
        
        batch.begin();
        
        for (Duck duck : ducks) {
            renderDuck(batch, duck);
        }

        for (IntMap.Entry<Player> entry : players) {
            Player p = entry.value;
            renderPlayer(batch, p);
        }
        
        batch.end();
    }
    
    /**
     * Renderiza un pato individual usando sprites.
     * 
     * @param batch el SpriteBatch para renderizar
     * @param duck el pato a renderizar
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
     * Renderiza un jugador individual con su canasta usando sprites.
     * 
     * @param batch el SpriteBatch para renderizar
     * @param p el jugador a renderizar
     */
    private static void renderPlayer(SpriteBatch batch, Player p) {
        AnimatedSprite animation = p.getRunAnimation();
        if (animation != null && spriteManager != null && spriteManager.isLoaded()) {
            Texture currentFrame = animation.getCurrentFrame();
            if (currentFrame != null) {
                batch.setColor(p.r, p.g, p.b, 1f);
                
                if (p.isFacingRight()) {
                    batch.draw(currentFrame, 
                        p.x, p.y,
                        Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
                } else {
                    batch.draw(currentFrame,
                        p.x, p.y,
                        Player.PLAYER_WIDTH / 2, Player.PLAYER_HEIGHT / 2,
                        Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT,
                        -1f, 1f,
                        0f,
                        0, 0,
                        currentFrame.getWidth(), currentFrame.getHeight(),
                        false, false);
                }
                
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
    }
}
