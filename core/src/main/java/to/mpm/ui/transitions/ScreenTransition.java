package to.mpm.ui.transitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Utility class for screen transitions.
 * Provides reusable fade effects for scene changes.
 */
public class ScreenTransition {

    /**
     * Callback interface for transition completion.
     */
    public interface TransitionCallback {
        void onComplete();
    }

    /**
     * Creates a fade-out effect on the given stage.
     * Fades a black overlay from transparent to opaque.
     * 
     * @param stage the stage to fade out
     * @param duration fade duration in seconds
     * @param callback callback to execute when fade completes
     */
    public static void fadeOut(Stage stage, float duration, TransitionCallback callback) {
        // Create a black overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image fadeImage = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        fadeImage.setFillParent(true);
        fadeImage.getColor().a = 0f; // Start transparent

        stage.addActor(fadeImage);

        // Fade to opaque, then call callback
        fadeImage.addAction(
            Actions.sequence(
                Actions.fadeIn(duration),
                Actions.run(() -> {
                    if (callback != null) {
                        callback.onComplete();
                    }
                    texture.dispose();
                })
            )
        );
    }

    /**
     * Creates a fade-in effect on the given stage.
     * Fades a black overlay from opaque to transparent.
     * 
     * @param stage the stage to fade in
     * @param duration fade duration in seconds
     * @param callback callback to execute when fade completes
     */
    public static void fadeIn(Stage stage, float duration, TransitionCallback callback) {
        // Create a black overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image fadeImage = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        fadeImage.setFillParent(true);
        fadeImage.getColor().a = 1f; // Start opaque

        stage.addActor(fadeImage);

        // Fade to transparent, then call callback
        fadeImage.addAction(
            Actions.sequence(
                Actions.fadeOut(duration),
                Actions.run(() -> {
                    if (callback != null) {
                        callback.onComplete();
                    }
                    fadeImage.remove();
                    texture.dispose();
                })
            )
        );
    }

    /**
     * Creates a fade-out effect with default duration (0.5 seconds).
     * 
     * @param stage the stage to fade out
     * @param callback callback to execute when fade completes
     */
    public static void fadeOut(Stage stage, TransitionCallback callback) {
        fadeOut(stage, 0.5f, callback);
    }

    /**
     * Creates a fade-in effect with default duration (0.5 seconds).
     * 
     * @param stage the stage to fade in
     * @param callback callback to execute when fade completes
     */
    public static void fadeIn(Stage stage, TransitionCallback callback) {
        fadeIn(stage, 0.5f, callback);
    }
}
