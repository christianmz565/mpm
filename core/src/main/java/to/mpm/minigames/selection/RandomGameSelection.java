package to.mpm.minigames.selection;

import to.mpm.minigames.MinigameType;
import java.util.Random;

/**
 * Random game selection strategy.
 * Selects a random minigame from all available minigames that support the given player count.
 */
public class RandomGameSelection implements GameSelectionStrategy {
    private final Random random;

    public RandomGameSelection() {
        this.random = new Random();
    }

    @Override
    public MinigameType selectGame(int playerCount) {
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

    @Override
    public String getStrategyName() {
        return "Random Selection";
    }
}
