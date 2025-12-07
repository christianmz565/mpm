package to.mpm.minigames.catchThemAll.entities;

import com.badlogic.gdx.math.MathUtils;
import to.mpm.minigames.catchThemAll.entities.Duck.DuckType;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages spawning of ducks during the game.
 * Only the host should call spawn methods to avoid duplication.
 */
public class DuckSpawner {
    private static final float SCREEN_WIDTH = 640f;
    private static final float SPAWN_Y = 480f;
    
    /** Intervalo mínimo de spawn en segundos. */
    private static final float MIN_SPAWN_INTERVAL = 0.8f;
    /** Intervalo máximo de spawn en segundos. */
    private static final float MAX_SPAWN_INTERVAL = 2.0f;
    
    /** Probabilidad del pato dorado (10%). */
    private static final int GOLDEN_PROBABILITY = 10;
    /** Probabilidad del pato malo (20%). */
    private static final int BAD_PROBABILITY = 20;
    
    private float timeSinceLastSpawn;
    private float nextSpawnInterval;
    private int nextDuckId;
    
    public DuckSpawner() {
        this.timeSinceLastSpawn = 0;
        this.nextSpawnInterval = getRandomSpawnInterval();
        this.nextDuckId = 0;
    }
    
    /**
     * Update the spawner and return newly spawned ducks.
     * Should only be called by the host.
     * 
     * @param delta time since last frame
     * @return list of newly spawned ducks (empty if no spawn)
     */
    public List<Duck> update(float delta) {
        List<Duck> newDucks = new ArrayList<>();
        
        timeSinceLastSpawn += delta;
        
        if (timeSinceLastSpawn >= nextSpawnInterval) {
            Duck newDuck = spawnDuck();
            newDucks.add(newDuck);
            
            timeSinceLastSpawn = 0;
            nextSpawnInterval = getRandomSpawnInterval();
        }
        
        return newDucks;
    }
    
    /**
     * Spawn a single duck at a random position.
     */
    private Duck spawnDuck() {
        float margin = 30f;
        float x = MathUtils.random(margin, SCREEN_WIDTH - margin - Duck.DUCK_WIDTH);
        
        DuckType type = getRandomDuckType();
        
        int id = nextDuckId++;
        Duck duck = new Duck(id, x, SPAWN_Y, type);
        
        return duck;
    }
    
    /**
     * Get random duck type based on probabilities.
     */
    private DuckType getRandomDuckType() {
        int roll = MathUtils.random(1, 100);
        
        if (roll <= GOLDEN_PROBABILITY) {
            return DuckType.GOLDEN;  // 10% - Most rare
        } else if (roll <= GOLDEN_PROBABILITY + BAD_PROBABILITY) {
            return DuckType.BAD;  // 20%
        } else {
            return DuckType.NEUTRAL;  // 70% - Most common
        }
    }
    
    /**
     * Get a random spawn interval.
     */
    private float getRandomSpawnInterval() {
        return MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL);
    }
    
    /**
     * Reset the spawner (useful when restarting the game).
     */
    public void reset() {
        timeSinceLastSpawn = 0;
        nextSpawnInterval = getRandomSpawnInterval();
        nextDuckId = 0;
    }
    
    /**
     * Force spawn a duck of specific type (useful for testing).
     */
    public Duck forceSpawn(DuckType type, float x) {
        int id = nextDuckId++;
        Duck duck = new Duck(id, x, SPAWN_Y, type);
        return duck;
    }
}
