package to.mpm.minigames.selection;

import to.mpm.minigames.MinigameType;

/**
 * Strategy interface for selecting a minigame.
 * Implementations can provide different selection methods (random, voting, etc.)
 */
public interface GameSelectionStrategy {
    /**
     * Selects a minigame based on the strategy's logic.
     *
     * @param playerCount number of players in the game
     * @return the selected minigame type
     */
    MinigameType selectGame(int playerCount);

    /**
     * Gets the name of this selection strategy.
     *
     * @return strategy name
     */
    String getStrategyName();
}
