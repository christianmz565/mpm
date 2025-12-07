package to.mpm.minigames;

import com.badlogic.gdx.Gdx;

import to.mpm.minigames.catchThemAll.CatchThemAllMinigame;
import to.mpm.minigames.dodgeRain.DodgeRainMinigame;
import to.mpm.minigames.sumo.SumoMinigame;
import to.mpm.minigames.theFinale.TheFinaleMinigame;

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
            case CATCH_THEM_ALL:
                return new CatchThemAllMinigame(localPlayerId);
            case SUMO:
                return new SumoMinigame(localPlayerId);
            case DODGE_RAIN:
                return new DodgeRainMinigame(localPlayerId);
            case THE_FINALE:
                return new TheFinaleMinigame(localPlayerId);
            default:
                Gdx.app.error("MinigameFactory", "Tipo de minijuego desconocido: " + type);
                return null;
        }
    }
}