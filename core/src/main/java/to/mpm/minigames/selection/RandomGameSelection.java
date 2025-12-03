package to.mpm.minigames.selection;

import to.mpm.minigames.MinigameType;
import java.util.Random;

/**
 * Utilidad para la selección aleatoria de minijuegos.
 * <p>
 * Selecciona un minijuego aleatorio de todos los minijuegos disponibles que
 * soportan la cantidad dada de jugadores.
 */
public class RandomGameSelection {
    private static final Random random = new Random();

    /**
     * Selecciona un minijuego aleatorio adecuado para la cantidad dada de
     * jugadores.
     * <p>
     * Excluye THE_FINALE de la selección aleatoria.
     *
     * @param playerCount número de jugadores en el juego
     * @return el tipo de minijuego seleccionado
     */
    public static MinigameType selectGame(int playerCount) {
        MinigameType[] allGames = MinigameType.values();

        MinigameType[] validGames = new MinigameType[allGames.length];
        int validCount = 0;

        for (MinigameType game : allGames) {
            if (game == MinigameType.THE_FINALE) {
                continue;
            }
            if (playerCount >= game.getMinPlayers() && playerCount <= game.getMaxPlayers()) {
                validGames[validCount++] = game;
            }
        }

        if (validCount == 0) {
            return allGames[0];
        }

        int randomIndex = random.nextInt(validCount);
        return validGames[randomIndex];
    }
}
