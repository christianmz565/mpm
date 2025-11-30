package to.mpm.minigames;

import com.badlogic.gdx.Gdx;

import to.mpm.minigames.ballmovement.BallMovementMinigame;
import to.mpm.minigames.catchThemAll.CatchThemAllMinigame;
import to.mpm.minigames.theFinale.TheFinaleMinigame;
import to.mpm.minigames.duckshooter.DuckShooterMinigame;

/**
 * Factory para crear instancias de minijuegos.
 */
public class MinigameFactory {
    /**
     * Crea una instancia del minijuego especificado.
     *
     * @param type          tipo de minijuego a crear
     * @param localPlayerId ID del jugador local
     * @return instancia del minijuego
     */
    public static Minigame createMinigame(MinigameType type, int localPlayerId) {
        switch (type) {
            case BALL_MOVEMENT:
                return new BallMovementMinigame(localPlayerId);
            case CATCH_THEM_ALL:
                return new CatchThemAllMinigame(localPlayerId);
            case THE_FINALE:
                return new TheFinaleMinigame(localPlayerId);
            case DUCK_SHOOTER:
                return new DuckShooterMinigame(localPlayerId);
            default:
                Gdx.app.error("MinigameFactory", "Tipo de minijuego desconocido: " + type);
                return null;
        }
    }
}
