package to.mpm.minigames.selection;

import to.mpm.minigames.MinigameType;
import java.util.Random;

/**
 * Random game selection utility.
 * Selects a random minigame from all available minigames that support the given player count.
 */
public class RandomGameSelection {
    private static final Random random = new Random();

    /**
     * Selects a random minigame suitable for the given player count.
     * Excludes THE_FINALE from random selection.
     *
     * @param playerCount number of players in the game
     * @return the selected minigame type
     */
    public static MinigameType selectGame(int playerCount) {
        MinigameType[] allGames = MinigameType.values();
        
        MinigameType[] validGames = new MinigameType[allGames.length];
        int validCount = 0;
        
        for (MinigameType game : allGames) {
            // Exclude THE_FINALE from random selection (only selected explicitly)
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
