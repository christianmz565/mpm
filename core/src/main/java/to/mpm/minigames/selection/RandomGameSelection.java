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
        
        // Filter games that support the player count
        MinigameType[] validGames = new MinigameType[allGames.length];
        int validCount = 0;
        
        for (MinigameType game : allGames) {
            if (playerCount >= game.getMinPlayers() && playerCount <= game.getMaxPlayers()) {
                validGames[validCount++] = game;
            }
        }
        
        if (validCount == 0) {
            // Fallback: return first game if no games support this player count
            return allGames[0];
        }
        
        // Select random game from valid games
        int randomIndex = random.nextInt(validCount);
        return validGames[randomIndex];
    }

    @Override
    public String getStrategyName() {
        return "Random Selection";
    }
}
