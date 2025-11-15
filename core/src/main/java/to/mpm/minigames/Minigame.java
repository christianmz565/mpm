package to.mpm.minigames;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Map;

/**
 * Interfaz base para todos los minijuegos.
 * Cada minijuego debe implementar esta interfaz para integrarse con el sistema.
 */
public interface Minigame {
    /**
     * Inicializa el minijuego.
     * Se llama una vez al comenzar el juego.
     */
    void initialize();

    /**
     * Actualiza la lógica del minijuego.
     *
     * @param delta tiempo transcurrido desde el último frame
     */
    void update(float delta);

    /**
     * Renderiza el minijuego.
     *
     * @param batch      SpriteBatch para dibujar sprites
     * @param shapeRenderer ShapeRenderer para formas geométricas
     */
    void render(SpriteBatch batch, ShapeRenderer shapeRenderer);

    /**
     * Procesa el input del jugador local.
     *
     * @param delta tiempo transcurrido desde el último frame
     */
    void handleInput(float delta);

    /**
     * Verifica si el minijuego ha terminado.
     *
     * @return true si el juego terminó, false en caso contrario
     */
    boolean isFinished();

    /**
     * Obtiene las puntuaciones de todos los jugadores.
     *
     * @return mapa de playerId -> score
     */
    Map<Integer, Integer> getScores();

    /**
     * Obtiene el ID del ganador.
     *
     * @return ID del jugador ganador, o -1 si no hay ganador
     */
    int getWinnerId();

    /**
     * Limpia los recursos del minijuego.
     */
    void dispose();

    /**
     * Redimensiona el viewport del minijuego.
     *
     * @param width  nuevo ancho
     * @param height nuevo alto
     */
    void resize(int width, int height);
}
