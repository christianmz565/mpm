package to.mpm.minigames.eggThief.physics;

import com.badlogic.gdx.math.Intersector;
import to.mpm.minigames.eggThief.entities.Duck;
import to.mpm.minigames.eggThief.entities.Egg;
import to.mpm.minigames.eggThief.entities.Nest;
import to.mpm.minigames.eggThief.network.NetworkHandler;

import java.util.List;
import java.util.Map;

/**
 * Handles collision detection between game entities.
 */
public class CollisionDetector {

    /**
     * Functional interface for handling steal events.
     */
    @FunctionalInterface
    public interface StealCallback {
        void onSteal(int thiefId, int victimId, int thiefEggs, int victimEggs);
    }

    /**
     * Checks if a duck collides with an egg.
     *
     * @param duck the duck
     * @param egg  the egg
     * @return true if collision detected
     */
    public static boolean checkEggCollection(Duck duck, Egg egg) {
        if (duck.getCarriedEgg() != null)
            return false;
        return Intersector.overlaps(duck.getHitbox(), egg.getHitbox());
    }

    /**
     * Checks if a duck has reached their nest.
     *
     * @param duck the duck
     * @param nest the nest
     * @return true if duck is at their nest
     */
    public static boolean checkNestDelivery(Duck duck, Nest nest) {
        // Only allow delivery if the duck owns the nest
        if (duck.getPlayerId() != nest.getOwnerId()) {
            return false;
        }

        return Intersector.overlaps(duck.getHitbox(), nest.getHitbox());
    }

    /**
     * Checks collisions between all players for stealing eggs.
     *
     * @param players  map of all players
     * @param callback callback to handle steal events
     */
    public static void checkPlayerCollisions(Map<Integer, Duck> players, StealCallback callback) {
        // Check all pairs of players
        for (Map.Entry<Integer, Duck> entry1 : players.entrySet()) {
            Duck duck1 = entry1.getValue();

            for (Map.Entry<Integer, Duck> entry2 : players.entrySet()) {
                Duck duck2 = entry2.getValue();

                // Don't check a duck against itself
                if (duck1.getPlayerId() == duck2.getPlayerId()) {
                    continue;
                }

                // Check if ducks are colliding
                if (Intersector.overlaps(duck1.getHitbox(), duck2.getHitbox())) {
                    // Attempt to steal from duck2
                    if (duck2.getEggsCarryingCount() > 0) {
                        boolean stolen = duck1.stealEggFrom(duck2);

                        // Notify about the steal attempt
                        callback.onSteal(
                                duck1.getPlayerId(),
                                duck2.getPlayerId(),
                                duck1.getEggsCarryingCount(),
                                duck2.getEggsCarryingCount());

                        // Only process one steal per collision
                        break;
                    }
                }
            }
        }
    }

    // /**
    // * Handles collisions between players, eggs, and nests.
    // *
    // * @param players the map of players
    // * @param eggs the list of eggs
    // * @param nests the list of nests
    // */
    // public static void handleCollisions(Map<Integer, Duck> players, List<Egg>
    // eggs, List<Nest> nests,
    // Map<Integer, Integer> scores) {
    // // Player-egg collision
    // for (Duck duck : players.values()) {
    // for (int i = eggs.size() - 1; i >= 0; i--) {
    // Egg egg = eggs.get(i);
    // if (duck.getHitbox().overlaps(egg.getHitbox())) {
    // duck.collectEgg(egg);
    // eggs.remove(i);
    // }
    // }
    // }

    // // Player-nest collision
    // for (Duck duck : players.values()) {
    // for (Nest nest : nests) {
    // if (duck.getPlayerId() == nest.getOwnerId() &&
    // duck.getHitbox().overlaps(nest.getHitbox())) {
    // int points = duck.deliverEggs();
    // scores.put(duck.getPlayerId(), scores.getOrDefault(duck.getPlayerId(), 0) +
    // points);
    // }
    // }
    // }

    // // Player-player collision (stealing)
    // Duck[] playerArray = players.values().toArray(new Duck[0]);
    // for (int i = 0; i < playerArray.length; i++) {
    // for (int j = i + 1; j < playerArray.length; j++) {
    // Duck duck1 = playerArray[i];
    // Duck duck2 = playerArray[j];
    // if (duck1.getHitbox().overlaps(duck2.getHitbox())) {
    // duck1.stealEggFrom(duck2);
    // duck2.stealEggFrom(duck1);
    // }
    // }
    // }
    // }
    public static void handleCollisions(Map<Integer, Duck> players, List<Egg> eggs,
            List<Nest> nests, Map<Integer, Integer> scores,
            StealCallback stealCallback) {

        // Egg collection (1 egg per duck) - remove from ground when picked up
        for (int i = eggs.size() - 1; i >= 0; i--) {
            Egg egg = eggs.get(i);
            for (Duck duck : players.values()) {
                if (duck.getCarriedEgg() == null && checkEggCollection(duck, egg)) {
                    duck.collectEgg(egg);
                    eggs.remove(i); // Remove from ground eggs list
                    // Notify network that egg was picked up and removed from ground
                    NetworkHandler.sendEggRemoved(egg.getId());
                    break; // only one duck can pick it
                }
            }
        }

        // Deliver eggs to nest
        for (Duck duck : players.values()) {
            for (Nest nest : nests) {
                if (checkNestDelivery(duck, nest)) {
                    int points = duck.deliverEggs();
                    if (points > 0) {
                        scores.put(duck.getPlayerId(), scores.getOrDefault(duck.getPlayerId(), 0) + points);
                        NetworkHandler.sendScoreUpdate(duck.getPlayerId(), scores.get(duck.getPlayerId()));
                    }
                }
            }
        }

        // Player-player collisions for stealing
        checkPlayerCollisions(players, stealCallback);
    }
}
