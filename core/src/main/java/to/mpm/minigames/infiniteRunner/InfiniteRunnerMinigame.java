package to.mpm.minigames.infiniteRunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.Minigame;
import to.mpm.network.NetworkManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Minijuego de carreras infinitas con obstáculos.
 * Los jugadores corren hacia adelante automáticamente,
 * evitando obstáculos lateralmente.
 */
public class InfiniteRunnerMinigame implements Minigame {
    private final int localPlayerId;
    
    private InfiniteRunnerAssets assets;
    private InfiniteRunnerLogic logic;
    private InfiniteRunnerRenderer renderer;
    
    private static final float PLAYER_LATERAL_SPEED = 400f;
    private static final float SLOW_SPEED_MULTIPLIER = 0.5f;
    private static final float JUMP_POWER = 500f;
    
    public InfiniteRunnerMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }
    
    @Override
    public void initialize() {
        assets = new InfiniteRunnerAssets();
        assets.load();
        
        logic = new InfiniteRunnerLogic(localPlayerId);
        renderer = new InfiniteRunnerRenderer(assets, logic);
        
        Gdx.app.log("InfiniteRunner", "Game initialized");
    }
    
    @Override
    public void update(float delta) {
        logic.update(delta);
    }
    
    @Override
    public void handleInput(float delta) {
        if (logic.finished || logic.countdownActive)
            return;
        
        InfiniteRunnerPlayer localPlayer = logic.localPlayer;
        
        float lateralSpeed = PLAYER_LATERAL_SPEED;
        if (localPlayer.isSlowed) {
            lateralSpeed *= SLOW_SPEED_MULTIPLIER;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || 
            Gdx.input.isKeyPressed(Input.Keys.A)) {
            localPlayer.x -= lateralSpeed * delta;
            localPlayer.facingRight = false;
            localPlayer.isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || 
            Gdx.input.isKeyPressed(Input.Keys.D)) {
            localPlayer.x += lateralSpeed * delta;
            localPlayer.facingRight = true;
            localPlayer.isMoving = true;
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (localPlayer.isGrounded) {
                localPlayer.velocityY = JUMP_POWER;
                localPlayer.isGrounded = false;
            }
        }
        
        if (localPlayer.x < 0)
            localPlayer.x = 0;
        if (localPlayer.x > GameConstants.Screen.WIDTH - localPlayer.width)
            localPlayer.x = GameConstants.Screen.WIDTH - localPlayer.width;
    }
    
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        renderer.render(batch, shapeRenderer);
    }
    
    @Override
    public boolean isFinished() {
        return logic.finished;
    }
    
    @Override
    public Map<Integer, Integer> getScores() {
        Map<Integer, Integer> scores = new HashMap<>();
        scores.put(localPlayerId, logic.getLocalScore());
        return scores;
    }
    
    @Override
    public int getWinnerId() {
        return logic.finished ? localPlayerId : -1;
    }
    
    @Override
    public void dispose() {
        if (assets != null) {
            assets.dispose();
        }
    }
    
    @Override
    public void resize(int width, int height) {
    }
}
