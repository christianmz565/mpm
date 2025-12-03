package to.mpm.minigames;

/**
 * Enumeración de todos los minijuegos disponibles.
 * <p>
 * Define los tipos de minijuegos con sus nombres, descripciones y límites de jugadores.
 */
public enum MinigameType {
    /** Minijuego de atrapar patos que caen. */
    CATCH_THEM_ALL(
            "Atrapa a Todos",
            "¡Atrapa los patos que caen con tu cesta! ¡Evita los patos malos!",
            2, 6
    ),
    /** Minijuego de empujar patos fuera de la plataforma. */
    SUMO(
            "Empujón de Estanque",
            "¡Empuja a los otros patos fuera de la plataforma! El último pato en pie gana.",
            2, 6
    ),
    /** Minijuego final del torneo. */
    THE_FINALE(
            "La Final",
            "¡Enfrentamiento final! ¡Solo compiten los mejores!",
            2, 99
    );

    /** Nombre del minijuego para mostrar. */
    private final String displayName;
    /** Descripción del minijuego. */
    private final String description;
    /** Número mínimo de jugadores requeridos. */
    private final int minPlayers;
    /** Número máximo de jugadores permitidos. */
    private final int maxPlayers;

    /**
     * Constructor parametrizado para crear un tipo de minijuego.
     * 
     * @param displayName nombre del minijuego para mostrar en la UI
     * @param description descripción breve del minijuego
     * @param minPlayers  número mínimo de jugadores requeridos
     * @param maxPlayers  número máximo de jugadores permitidos
     */
    MinigameType(String displayName, String description, int minPlayers, int maxPlayers) {
        this.displayName = displayName;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Obtiene el nombre del minijuego para mostrar en la interfaz.
     * 
     * @return nombre del minijuego
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene la descripción del minijuego.
     * 
     * @return descripción del minijuego
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene el número mínimo de jugadores requeridos para el minijuego.
     * 
     * @return número mínimo de jugadores
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Obtiene el número máximo de jugadores permitidos en el minijuego.
     * 
     * @return número máximo de jugadores
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }
}