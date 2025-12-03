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
        for (IntMap.Entry<Player> entry : state.getPlayers()) {
            entry.value.update();
        }
        
        for (Duck duck : state.getDucks()) {
            duck.update();
        }
        
        if (state.getDuckSpawner() != null) {
            List<Duck> newDucks = state.getDuckSpawner().update(delta);
            for (Duck duck : newDucks) {
                state.addDuck(duck);
                NetworkHandler.sendDuckSpawned(duck);
            }
        }
        
        CollisionHandler.handlePlayerCollisions(state.getPlayers());
        
        Map<Integer, Player> playersMap = new HashMap<>();
        for (IntMap.Entry<Player> entry : state.getPlayers()) {
            playersMap.put(entry.key, entry.value);
        }
        
        List<Duck> ducksBeforeCatch = new ArrayList<>(state.getDucks());
        
        Map<Integer, Integer> pointsEarned = CatchDetector.detectCatches(state.getDucks(), playersMap);
        
        for (Duck duck : ducksBeforeCatch) {
            if (!state.getDucks().contains(duck)) {
                NetworkHandler.sendDuckRemoved(duck);
            }
        }
        
        for (Map.Entry<Integer, Integer> entry : pointsEarned.entrySet()) {
            int playerId = entry.getKey();
            int points = entry.getValue();
            state.addScore(playerId, points);
            NetworkHandler.sendScoreUpdate(playerId, state.getScores().get(playerId));
        }
        
        List<Duck> groundedDucks = new ArrayList<>();
        for (Duck duck : state.getDucks()) {
            if (duck.shouldRemove()) {
                groundedDucks.add(duck);
            }
        }
        
        CatchDetector.removeGroundedDucks(state.getDucks());
        
        for (Duck duck : groundedDucks) {
            NetworkHandler.sendDuckRemoved(duck);
        }
        
        NetworkHandler.sendDuckUpdates(state.getDucks());
        NetworkHandler.sendAllPlayerPositions(state.getPlayers());
    }
    
    public static void updateClient(GameState state) {
        for (IntMap.Entry<Player> entry : state.getPlayers()) {
            entry.value.update();
        }
        
        NetworkHandler.sendPlayerPosition(state.getLocalPlayerId(), state.getLocalPlayer());
    }
}
