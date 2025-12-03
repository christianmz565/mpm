package to.mpm.minigames;

import com.badlogic.gdx.graphics.Color;

/**
 * Centralized game constants for all minigames.
 * Contains physics, timing, and gameplay configuration values.
 */
public final class GameConstants {

    private GameConstants() {
        // Prevent instantiation
    }

    /**
     * Screen dimensions (virtual resolution)
     */
    public static final class Screen {
        public static final float WIDTH = 640f;
        public static final float HEIGHT = 480f;
        public static final float GROUND_Y = 60f;
    }

    /**
     * Player configuration
     */
    public static final class Player {
        public static final float DEFAULT_RADIUS = 20f;
        public static final float DEFAULT_MOVE_SPEED = 200f;
        public static final int DEFAULT_MAX_HITS = 3;
        public static final float INVULNERABILITY_DURATION = 0.5f;
        
        /** Default player colors for up to 6 players */
        public static final Color[] COLORS = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN
        };
        
        /** Same colors as float arrays for compatibility */
        public static final float[][] COLORS_FLOAT = {
            {1f, 0f, 0f},    // Red
            {0f, 0f, 1f},    // Blue
            {0f, 1f, 0f},    // Green
            {1f, 1f, 0f},    // Yellow
            {1f, 0f, 1f},    // Magenta
            {0f, 1f, 1f},    // Cyan
        };
    }

    /**
     * Timing constants for minigames
     */
    public static final class Timing {
        /** Default minigame duration in seconds */
        public static final float DEFAULT_GAME_DURATION = 60f;
        /** Extended duration for battle royale style games */
        public static final float EXTENDED_GAME_DURATION = 180f;
        /** Duration of the intro screen before each minigame */
        public static final float INTRO_SCREEN_DURATION = 5f;
        /** Default shoot cooldown in seconds */
        public static final float DEFAULT_SHOOT_COOLDOWN = 0.5f;
    }

    /**
     * Duck Shooter specific constants
     */
    public static final class DuckShooter {
        public static final float DUCK_RADIUS = 25f;
        public static final float DUCK_MOVE_SPEED = 180f;
        public static final float QUACK_SPEED = 400f;
        public static final float QUACK_RADIUS = 8f;
        public static final float QUACK_MAX_DISTANCE = 800f;
        public static final int DUCK_MAX_HITS = 3;
        public static final float SHOOT_COOLDOWN = 0.5f;
    }

    /**
     * Sumo minigame specific constants
     */
    public static final class Sumo {
        public static final float MAP_CENTER_X = 320f;
        public static final float MAP_CENTER_Y = 240f;
        public static final float MAP_RADIUS = 200f;
        public static final float PLAYER_RADIUS = 25f;
        public static final float KNOCKBACK_FORCE = 300f;
        public static final int POINTS_PER_KILL = 50000;
    }

    /**
     * Catch Them All minigame specific constants
     */
    public static final class CatchThemAll {
        public static final float PLAYER_WIDTH = 64f;
        public static final float PLAYER_HEIGHT = 64f;
        public static final float DUCK_WIDTH = 48f;
        public static final float DUCK_HEIGHT = 48f;
        public static final float SPAWN_INTERVAL = 1.5f;
        public static final float FALL_SPEED = 150f;
        public static final int GOOD_DUCK_POINTS = 100;
        public static final int BAD_DUCK_POINTS = -50;
    }

    /**
     * The Finale specific constants (uses DuckShooter mechanics)
     */
    public static final class TheFinale {
        // Uses DuckShooter constants for gameplay
        public static final float GAME_DURATION = 180f;
    }

    /**
     * Scoring constants
     */
    public static final class Scoring {
        public static final int DEFAULT_KILL_POINTS = 1;
        public static final int SUMO_KILL_POINTS = 50000;
    }

    /**
     * Spectator ID constant
     */
    public static final int SPECTATOR_ID = -1;
}
