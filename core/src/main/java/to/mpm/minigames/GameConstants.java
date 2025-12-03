package to.mpm.minigames;

import com.badlogic.gdx.graphics.Color;

/**
 * Constantes centralizadas del juego para todos los minijuegos.
 * <p>
 * Contiene valores de configuración de física, tiempo y jugabilidad.
 */
public final class GameConstants {

    /**
     * Constructor privado para evitar instanciación.
     */
    private GameConstants() {
    }

    /**
     * Dimensiones de pantalla (resolución virtual).
     * <p>
     * Define el espacio de coordenadas del juego.
     */
    public static final class Screen {
        /** Ancho virtual de la pantalla. */
        public static final float WIDTH = 640f;
        /** Alto virtual de la pantalla. */
        public static final float HEIGHT = 480f;
        /** Posición Y del suelo. */
        public static final float GROUND_Y = 60f;
    }

    /**
     * Configuración del jugador.
     * <p>
     * Define parámetros comunes para todos los jugadores.
     */
    public static final class Player {
        /** Radio predeterminado del jugador. */
        public static final float DEFAULT_RADIUS = 20f;
        /** Velocidad de movimiento predeterminada. */
        public static final float DEFAULT_MOVE_SPEED = 200f;
        /** Número máximo de impactos antes de eliminación. */
        public static final int DEFAULT_MAX_HITS = 3;
        /** Duración de invulnerabilidad tras recibir daño. */
        public static final float INVULNERABILITY_DURATION = 0.5f;
        
        /** Colores predeterminados para hasta 6 jugadores. */
        public static final Color[] COLORS = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN
        };
        
        /** Mismos colores como arrays de flotantes para compatibilidad. */
        public static final float[][] COLORS_FLOAT = {
            {1f, 0f, 0f},
            {0f, 0f, 1f},
            {0f, 1f, 0f},
            {1f, 1f, 0f},
            {1f, 0f, 1f},
            {0f, 1f, 1f},
        };
    }

    /**
     * Constantes de tiempo para minijuegos.
     * <p>
     * Define duraciones estándar de partidas y transiciones.
     */
    public static final class Timing {
        /** Duración predeterminada del minijuego en segundos. */
        public static final float DEFAULT_GAME_DURATION = 15f;
        /** Duración extendida para juegos estilo battle royale. */
        public static final float EXTENDED_GAME_DURATION = 180f;
        /** Duración de la pantalla de introducción antes de cada minijuego. */
        public static final float INTRO_SCREEN_DURATION = 5f;
        /** Tiempo de espera predeterminado entre disparos en segundos. */
        public static final float DEFAULT_SHOOT_COOLDOWN = 0.5f;
    }

    /**
     * Constantes específicas del minijuego Duck Shooter.
     * <p>
     * Define parámetros de jugabilidad para el modo de disparos.
     */
    public static final class DuckShooter {
        /** Radio del pato. */
        public static final float DUCK_RADIUS = 25f;
        /** Velocidad de movimiento del pato. */
        public static final float DUCK_MOVE_SPEED = 180f;
        /** Velocidad de los proyectiles quack. */
        public static final float QUACK_SPEED = 400f;
        /** Radio del proyectil quack. */
        public static final float QUACK_RADIUS = 8f;
        /** Distancia máxima del quack antes de desaparecer. */
        public static final float QUACK_MAX_DISTANCE = 800f;
        /** Número máximo de impactos que puede recibir un pato. */
        public static final int DUCK_MAX_HITS = 3;
        /** Tiempo de espera entre disparos. */
        public static final float SHOOT_COOLDOWN = 0.5f;
    }

    /**
     * Constantes específicas del minijuego Sumo.
     * <p>
     * Define parámetros del mapa y mecánicas de empuje.
     */
    public static final class Sumo {
        /** Posición X del centro del mapa. */
        public static final float MAP_CENTER_X = 320f;
        /** Posición Y del centro del mapa. */
        public static final float MAP_CENTER_Y = 240f;
        /** Radio de la plataforma circular. */
        public static final float MAP_RADIUS = 200f;
        /** Radio del jugador. */
        public static final float PLAYER_RADIUS = 25f;
        /** Fuerza de retroceso al colisionar. */
        public static final float KNOCKBACK_FORCE = 300f;
        /** Puntos otorgados por eliminar a un oponente. */
        public static final int POINTS_PER_KILL = 50000;
    }

    /**
     * Constantes específicas del minijuego Atrapa a Todos.
     * <p>
     * Define parámetros de spawn y puntuación de patos.
     */
    public static final class CatchThemAll {
        /** Ancho del jugador. */
        public static final float PLAYER_WIDTH = 64f;
        /** Alto del jugador. */
        public static final float PLAYER_HEIGHT = 64f;
        /** Ancho del pato. */
        public static final float DUCK_WIDTH = 48f;
        /** Alto del pato. */
        public static final float DUCK_HEIGHT = 48f;
        /** Intervalo entre spawn de patos. */
        public static final float SPAWN_INTERVAL = 1.5f;
        /** Velocidad de caída de los patos. */
        public static final float FALL_SPEED = 150f;
        /** Puntos por atrapar un pato bueno. */
        public static final int GOOD_DUCK_POINTS = 100;
        /** Puntos por atrapar un pato malo (negativo). */
        public static final int BAD_DUCK_POINTS = -50;
    }

    /**
     * Constantes específicas de La Final.
     * <p>
     * Usa las mecánicas del DuckShooter con duración extendida.
     */
    public static final class TheFinale {
        /** Duración del juego en segundos. */
        public static final float GAME_DURATION = 180f;
    }

    /**
     * Constantes de puntuación.
     * <p>
     * Define puntos base para diferentes acciones.
     */
    public static final class Scoring {
        /** Puntos predeterminados por eliminación. */
        public static final int DEFAULT_KILL_POINTS = 1;
        /** Puntos por eliminación en modo Sumo. */
        public static final int SUMO_KILL_POINTS = 50000;
    }

    /** Identificador para jugadores en modo espectador. */
    public static final int SPECTATOR_ID = -1;
}
