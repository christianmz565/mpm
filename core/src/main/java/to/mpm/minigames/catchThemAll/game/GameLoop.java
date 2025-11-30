package to.mpm.minigames.catchThemAll.game;

import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.network.NetworkHandler;
import to.mpm.minigames.catchThemAll.physics.CatchDetector;
import to.mpm.minigames.catchThemAll.physics.CollisionHandler;
import to.mpm.minigames.catchThemAll.entities.Player;
import com.badlogic.gdx.utils.IntMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles game loop logic for host and client.
 * Host: physics, collisions, duck spawning, score calculation
 * Client: local physics, position sending
 */
public class GameLoop {
    
    public static void updateHost(float delta, GameState state) {
        // Update all player physics
        for (IntMap.Entry<Player> entry : state.getPlayers()) {
            entry.value.update();
        }
        
        // Update duck physics
        for (Duck duck : state.getDucks()) {
            duck.update();
        }
        
        // Spawn new ducks
        if (state.getDuckSpawner() != null) {
            List<Duck> newDucks = state.getDuckSpawner().update(delta);
            for (Duck duck : newDucks) {
                state.addDuck(duck);
                NetworkHandler.sendDuckSpawned(duck);
            }
        }
        
        // Calculate collisions
        CollisionHandler.handlePlayerCollisions(state.getPlayers());
        
        // Detect catches
        Map<Integer, Player> playersMap = new HashMap<>();
        for (IntMap.Entry<Player> entry : state.getPlayers()) {
            playersMap.put(entry.key, entry.value);
        }
        
        // Save list of ducks before catch detection (for network notifications)
        List<Duck> ducksBeforeCatch = new ArrayList<>(state.getDucks());
        
        Map<Integer, Integer> pointsEarned = CatchDetector.detectCatches(state.getDucks(), playersMap);
        
        // Send removal notifications for caught ducks (those that were removed)
        for (Duck duck : ducksBeforeCatch) {
            if (!state.getDucks().contains(duck)) {
                NetworkHandler.sendDuckRemoved(duck);
            }
        }
        
        // Update scores
        for (Map.Entry<Integer, Integer> entry : pointsEarned.entrySet()) {
            int playerId = entry.getKey();
            int points = entry.getValue();
            state.addScore(playerId, points);
            NetworkHandler.sendScoreUpdate(playerId, state.getScores().get(playerId));
        }
        
        // Remove grounded ducks (track before removal for notifications)
        List<Duck> groundedDucks = new ArrayList<>();
        for (Duck duck : state.getDucks()) {
            if (duck.shouldRemove()) {
                groundedDucks.add(duck);
            }
        }
        
        CatchDetector.removeGroundedDucks(state.getDucks());
        
        // Send removal notifications for grounded ducks
        for (Duck duck : groundedDucks) {
            NetworkHandler.sendDuckRemoved(duck);
        }
        
        // Broadcast updates
        NetworkHandler.sendDuckUpdates(state.getDucks());
        NetworkHandler.sendAllPlayerPositions(state.getPlayers());
    }
    
    public static void updateClient(GameState state) {
        // Update all players (local physics + animations for all)
        for (IntMap.Entry<Player> entry : state.getPlayers()) {
            entry.value.update();
        }
        
        // Send local player position to server
        NetworkHandler.sendPlayerPosition(state.getLocalPlayerId(), state.getLocalPlayer());
    }
}
