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
 * Clase de utilidad para transiciones de pantalla.
 * <p>
 * Proporciona efectos de fundido de entrada y salida para cambiar entre
 * pantallas.
 */
public class ScreenTransition {
    /**
     * Interfaz de callback para la finalización de la transición.
     */
    public interface TransitionCallback {
        void onComplete();
    }

    /**
     * Crea un efecto de fade-out en el stage dado.
     * <p>
     * Funde una superposición negra de transparente a opaca.
     * 
     * @param stage    el stage para hacer fade out
     * @param duration duración del fade en segundos
     * @param callback callback a ejecutar cuando el fade complete
     */
    public static void fadeOut(Stage stage, float duration, TransitionCallback callback) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image fadeImage = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        fadeImage.setFillParent(true);
        fadeImage.getColor().a = 0f;

        stage.addActor(fadeImage);

        fadeImage.addAction(
                Actions.sequence(
                        Actions.fadeIn(duration),
                        Actions.run(() -> {
                            if (callback != null) {
                                callback.onComplete();
                            }
                            texture.dispose();
                        })));
    }

    /**
     * Crea un efecto de fade-in en el stage dado.
     * <p>
     * Funde una superposición negra de opaca a transparente.
     * 
     * @param stage    el stage para hacer fade in
     * @param duration duración del fade en segundos
     * @param callback callback a ejecutar cuando el fade complete
     */
    public static void fadeIn(Stage stage, float duration, TransitionCallback callback) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image fadeImage = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        fadeImage.setFillParent(true);
        fadeImage.getColor().a = 1f;

        stage.addActor(fadeImage);

        fadeImage.addAction(
                Actions.sequence(
                        Actions.fadeOut(duration),
                        Actions.run(() -> {
                            if (callback != null) {
                                callback.onComplete();
                            }
                            fadeImage.remove();
                            texture.dispose();
                        })));
    }

    /**
     * Crea un efecto de fade-out con duración por defecto (0.5 segundos).
     * 
     * @param stage    el stage para hacer fade out
     * @param callback callback a ejecutar cuando el fade complete
     */
    public static void fadeOut(Stage stage, TransitionCallback callback) {
        fadeOut(stage, 0.5f, callback);
    }

    /**
     * Crea un efecto de fade-in con duración por defecto (0.5 segundos).
     * 
     * @param stage    el stage para hacer fade in
     * @param callback callback a ejecutar cuando el fade complete
     */
    public static void fadeIn(Stage stage, TransitionCallback callback) {
        fadeIn(stage, 0.5f, callback);
    }
}
