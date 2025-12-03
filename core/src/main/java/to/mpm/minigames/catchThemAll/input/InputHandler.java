package to.mpm.minigames.catchThemAll.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import to.mpm.minigames.catchThemAll.entities.Player;

/**
 * Handles player input for movement and jumping.
 */
public class InputHandler {
    private static final float MOVE_SPEED = 250f;
    private static final float JUMP_FORCE = 450f;
    private static final float SCREEN_WIDTH = 640f;
    
    /**
     * Process input and update local player.
     * 
     * @param localPlayer the local player to control
     * @param delta time since last frame
     */
    public static void handleInput(Player localPlayer, float delta) {
        float inputVelocity = 0;
        
        if (localPlayer.blockedTimer <= 0) {
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                inputVelocity -= MOVE_SPEED * delta;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                inputVelocity += MOVE_SPEED * delta;
            }

            localPlayer.x += inputVelocity;
        }
        
        localPlayer.lastVelocityX = inputVelocity;

        if ((Gdx.input.isKeyJustPressed(Input.Keys.W) || 
             Gdx.input.isKeyJustPressed(Input.Keys.UP) || 
             Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && 
            localPlayer.isGrounded) {
            localPlayer.velocityY = JUMP_FORCE;
            localPlayer.isGrounded = false;
        }

        localPlayer.x = Math.max(0, Math.min(SCREEN_WIDTH - Player.PLAYER_WIDTH, localPlayer.x));
    }
}
