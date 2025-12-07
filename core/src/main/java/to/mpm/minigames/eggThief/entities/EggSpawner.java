package to.mpm.minigames.eggThief.entities;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the spawning of eggs in the game world.
 * Spawns eggs at random positions and intervals.
 */
public class EggSpawner {
    // Spawn settings
    private static final float GOLDEN_EGG_CHANCE = 0.1f; // 10% chance for golden egg
    private static final int INITIAL_EGGS = 10; // Number of eggs spawned at game start

    // Spawn area (center area of the screen)
    private static final float SPAWN_MIN_X = 150f;
    private static final float SPAWN_MAX_X = 490f;
    private static final float SPAWN_MIN_Y = 100f;
    private static final float SPAWN_MAX_Y = 380f;

    private final Random random;
    private int nextEggId;
    private int currentEggCount;

    /**
     * Creates a new EggSpawner.
     */
    public EggSpawner() {
        this.random = new Random();
        this.nextEggId = 0;
        this.currentEggCount = 0;
    }

    /**
     * Spawns the initial batch of eggs at the start of the game.
     *
     * @return list of initial eggs
     */
    public List<Egg> spawnInitialEggs() {
        List<Egg> eggs = new ArrayList<>();
        for (int i = 0; i < INITIAL_EGGS; i++) {
            Egg egg = spawnEgg();
            eggs.add(egg);
            currentEggCount++;
            Gdx.app.log("EggSpawner", "Initial egg #" + egg.getId() +
                    " at (" + egg.getX() + ", " + egg.getY() + ")" +
                    " Golden: " + egg.isGolden());
        }
        return eggs;
    }

    /**
     * Updates the spawner and returns any newly spawned eggs.
     * does NOT handle collection or collisions
     * Note: After initial spawn, no more eggs are generated (game starts with 10
     * eggs).
     * 
     * @param delta time since last frame in seconds
     * @return list of newly spawned eggs (empty if none spawned)
     */
    public List<Egg> update(float delta) {
        List<Egg> newEggs = new ArrayList<>();

        // No more spawning after initial eggs - game has fixed 10 eggs
        // Players can steal from each other but eggs don't respawn

        return newEggs;
    }

    /**
     * Spawns a new egg at a random position.
     *
     * @return the newly spawned egg
     */
    private Egg spawnEgg() {
        float x = SPAWN_MIN_X + random.nextFloat() * (SPAWN_MAX_X - SPAWN_MIN_X);
        float y = SPAWN_MIN_Y + random.nextFloat() * (SPAWN_MAX_Y - SPAWN_MIN_Y);
        boolean isGolden = random.nextFloat() < GOLDEN_EGG_CHANCE;

        return new Egg(nextEggId++, x, y, isGolden);
    }

    /**
     * Notifies the spawner that an egg was collected.
     */
    public void onEggCollected() {
        currentEggCount = Math.max(0, currentEggCount - 1);
    }

    /**
     * Resets the spawner state.
     */
    public void reset() {
        nextEggId = 0;
        currentEggCount = 0;
    }
}
