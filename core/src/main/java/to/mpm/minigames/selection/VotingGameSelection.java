package to.mpm.minigames.selection;

import to.mpm.minigames.MinigameType;
import java.util.HashMap;
import java.util.Map;

/**
 * Voting-based game selection strategy.
 * Players vote for their preferred minigame, and the one with most votes is selected.
 * 
 * NOTE: This is a placeholder implementation for future voting functionality.
 * Currently returns the first available game. 
 * Full implementation requires networking support for vote collection.
 */
public class VotingGameSelection implements GameSelectionStrategy {
    private final Map<MinigameType, Integer> votes;

    public VotingGameSelection() {
        this.votes = new HashMap<>();
    }

    /**
     * Registers a vote for a specific minigame.
     * 
     * @param game the minigame to vote for
     */
    public void vote(MinigameType game) {
        votes.put(game, votes.getOrDefault(game, 0) + 1);
    }

    /**
     * Clears all votes.
     */
    public void clearVotes() {
        votes.clear();
    }

    @Override
    public MinigameType selectGame(int playerCount) {
        // If no votes, return first valid game
        if (votes.isEmpty()) {
            MinigameType[] allGames = MinigameType.values();
            for (MinigameType game : allGames) {
                if (playerCount >= game.getMinPlayers() && playerCount <= game.getMaxPlayers()) {
                    return game;
                }
            }
            return allGames[0];
        }

        // Find game with most votes that supports player count
        MinigameType winner = null;
        int maxVotes = 0;
        
        for (Map.Entry<MinigameType, Integer> entry : votes.entrySet()) {
            MinigameType game = entry.getKey();
            int voteCount = entry.getValue();
            
            if (playerCount >= game.getMinPlayers() && playerCount <= game.getMaxPlayers()) {
                if (voteCount > maxVotes) {
                    maxVotes = voteCount;
                    winner = game;
                }
            }
        }
        
        // If no valid winner found, fall back to first game
        if (winner == null) {
            return MinigameType.values()[0];
        }
        
        return winner;
    }

    @Override
    public String getStrategyName() {
        return "Voting Selection";
    }
}
