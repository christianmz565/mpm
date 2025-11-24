package to.mpm.minigames;

/**
 * Enumeración de todos los minijuegos disponibles.
 */
public enum MinigameType {
    BALL_MOVEMENT(
            "Ball Movement",
            "Move your ball around! Simple movement test.",
            2, 6
    ),
    CATCH_THEM_ALL(
            "Catch Them All",
            "Catch falling ducks with your basket! Avoid bad ducks!",
            2, 6
    ),
    THE_FINALE(
            "The Finale",
            "Final showdown! Only the best compete!",
            2, 99
    );

    private final String displayName;
    private final String description;
    private final int minPlayers;
    private final int maxPlayers;

    MinigameType(String displayName, String description, int minPlayers, int maxPlayers) {
        this.displayName = displayName;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
