package to.mpm.minigames;

import to.mpm.minigames.ballmovement.BallMovementMinigame;
import to.mpm.minigames.catchThemAll.CatchThemAllMinigame;

/**
 * Factory para crear instancias de minijuegos.
 */
public class MinigameFactory {
    /**
     * Crea una instancia del minijuego especificado.
     *
     * @param type         tipo de minijuego a crear
     * @param localPlayerId ID del jugador local
     * @return instancia del minijuego
     */
    public static Minigame createMinigame(MinigameType type, int localPlayerId) {
        switch (type) {
            case BALL_MOVEMENT:
                return new BallMovementMinigame(localPlayerId);
            case CATCH_THEM_ALL:
                return new CatchThemAllMinigame(localPlayerId);
            default:
                throw new IllegalArgumentException("Unknown minigame type: " + type);
        }
    }
}
