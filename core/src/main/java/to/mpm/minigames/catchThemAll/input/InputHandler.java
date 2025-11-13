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
        // Horizontal movement
        float dx = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx -= MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx += MOVE_SPEED * delta;
        }

        // Track velocity for collision detection
        localPlayer.lastVelocityX = dx;

        // Jump (both players can always jump)
        if ((Gdx.input.isKeyJustPressed(Input.Keys.W) || 
             Gdx.input.isKeyJustPressed(Input.Keys.UP) || 
             Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && 
            localPlayer.isGrounded) {
            localPlayer.velocityY = JUMP_FORCE;
            localPlayer.isGrounded = false;
        }

        // Apply horizontal movement
        localPlayer.x += dx;

        // Keep inside horizontal bounds
        localPlayer.x = Math.max(0, Math.min(SCREEN_WIDTH - Player.PLAYER_WIDTH, localPlayer.x));
    }
}
