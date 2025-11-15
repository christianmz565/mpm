package to.mpm.minigames.eggThief.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import to.mpm.minigames.eggThief.entities.Duck;

// Handles player input for controlling the duck (Client)
public class InputHandler {
    /**
     * Processes input and updates the player's duck movement.
     *
     * @param player the local player's duck
     * @param delta  time since last frame in seconds
     */
    public static void handleInput(Duck player, float delta) {
        float dx = 0;
        float dy = 0;

        // Check WASD and arrow keys for movement
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx = 1;
        }

        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
        }

        // Apply movement to player
        // player.move(dx, dy);
    }
}
