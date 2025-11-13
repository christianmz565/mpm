package to.mpm.minigames.catchThemAll.physics;

import com.badlogic.gdx.math.Rectangle;
import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.entities.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles detection and resolution of duck catches by players.
 * Only the host should call these methods to avoid conflicts.
 */
public class CatchDetector {
    
    /**
     * Check all ducks against all player baskets and handle catches.
     * Returns a map of player IDs to points earned this frame.
     * 
     * @param ducks list of active ducks
     * @param players map of player ID to player
     * @return map of player ID to points earned (can be negative for bad ducks)
     */
    public static Map<Integer, Integer> detectCatches(List<Duck> ducks, Map<Integer, Player> players) {
        Map<Integer, Integer> pointsEarned = new HashMap<>();
        List<Duck> ducksToRemove = new ArrayList<>();
        
        for (Duck duck : ducks) {
            if (duck.isCaught()) {
                continue;  // Skip already caught ducks
            }
            
            Rectangle duckBounds = duck.getBounds();
            
            // Check against each player's basket
            for (Map.Entry<Integer, Player> entry : players.entrySet()) {
                int playerId = entry.getKey();
                Player player = entry.getValue();
                Rectangle basketBounds = player.getBasketBounds();
                
                // Check if duck intersects with basket
                if (duckBounds.overlaps(basketBounds)) {
                    // Duck caught!
                    duck.setCaught(playerId);
                    ducksToRemove.add(duck);
                    
                    // Add points (or subtract for bad ducks)
                    int points = duck.type.points;
                    pointsEarned.put(playerId, pointsEarned.getOrDefault(playerId, 0) + points);
                    
                    break;  // Each duck can only be caught by one player
                }
            }
        }
        
        // Remove caught ducks from the list
        ducks.removeAll(ducksToRemove);
        
        return pointsEarned;
    }
    
    /**
     * Remove ducks that have reached the ground without being caught.
     * 
     * @param ducks list of active ducks
     * @return number of ducks removed
     */
    public static int removeGroundedDucks(List<Duck> ducks) {
        int removed = 0;
        List<Duck> toRemove = new ArrayList<>();
        
        for (Duck duck : ducks) {
            if (duck.shouldRemove()) {
                toRemove.add(duck);
                removed++;
            }
        }
        
        ducks.removeAll(toRemove);
        return removed;
    }
}
