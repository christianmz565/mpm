package to.mpm.minigames;

/**
 * Enumeración de todos los minijuegos disponibles.
 */
public enum MinigameType {
    BALL_MOVEMENT(
            "Ball Movement",
            "Move your ball around! Simple movement test.",
            2, 6),
    CATCH_THEM_ALL(
            "Catch Them All",
            "Catch falling ducks with your basket! Avoid bad ducks!",
            2, 6),
    THE_FINALE(
            "The Finale",
            "Final showdown! Only the best compete!",
            2, 99);

    private final String displayName; // Nombre del minijuego
    private final String description; // Descripción del minijuego
    private final int minPlayers; // Número mínimo de jugadores
    private final int maxPlayers; // Número máximo de jugadores

    /**
     * Constructor parametrizado.
     * 
     * @param displayName Nombre del minijuego
     * @param description Descripción del minijuego
     * @param minPlayers  Número mínimo de jugadores
     * @param maxPlayers  Número máximo de jugadores
     */
    MinigameType(String displayName, String description, int minPlayers, int maxPlayers) {
        this.displayName = displayName;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Obtiene el nombre del minijuego.
     * 
     * @return nombre
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene la descripción del minijuego.
     * 
     * @return descripción
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene el número mínimo de jugadores para el minijuego.
     * 
     * @return número mínimo de jugadores
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Obtiene el número máximo de jugadores para el minijuego.
     * 
     * @return número máximo de jugadores
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }
}
