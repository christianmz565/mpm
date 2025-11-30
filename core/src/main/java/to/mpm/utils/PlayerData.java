package to.mpm.utils;

/**
 * Estructura de datos para almacenar información del jugador.
 */
public class PlayerData implements Comparable<PlayerData> {
    private final int playerId;
    private String playerName;
    private int score;

    /**
     * Crea una nueva instancia de PlayerData.
     *
     * @param playerId   el ID único del jugador
     * @param playerName el nombre para mostrar del jugador
     * @param score      la puntuación actual del jugador
     */
    public PlayerData(int playerId, String playerName, int score) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.score = score;
    }

    /**
     * Crea una nueva instancia de PlayerData con puntuación cero.
     *
     * @param playerId   el ID único del jugador
     * @param playerName el nombre para mostrar del jugador
     */
    public PlayerData(int playerId, String playerName) {
        this(playerId, playerName, 0);
    }

    /**
     * Obtiene el ID del jugador.
     *
     * @return el ID del jugador
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Obtiene el nombre del jugador.
     *
     * @return el nombre del jugador
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Establece el nombre del jugador.
     *
     * @param playerName el nuevo nombre del jugador
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Obtiene la puntuación del jugador.
     *
     * @return la puntuación del jugador
     */
    public int getScore() {
        return score;
    }

    /**
     * Establece la puntuación del jugador.
     *
     * @param score la nueva puntuación del jugador
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Añade puntos a la puntuación del jugador.
     *
     * @param points los puntos a añadir
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * Compara jugadores por puntuación (orden descendente).
     * <p>
     * Las puntuaciones más altas aparecen primero. Si las puntuaciones son iguales,
     * se utiliza el ID del jugador.
     * 
     * @param other el otro PlayerData a comparar
     * @return resultado de la comparación
     */
    @Override
    public int compareTo(PlayerData other) {
        int scoreCompare = Integer.compare(other.score, this.score);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Integer.compare(this.playerId, other.playerId);
    }

    /**
     * Compara dos objetos PlayerData por su ID de jugador.
     * <p>
     * Dos objetos son iguales si tienen el mismo ID de jugador.
     * 
     * @param obj el objeto a comparar
     * @return true si los objetos son iguales
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        PlayerData other = (PlayerData) obj;
        return playerId == other.playerId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(playerId);
    }

    @Override
    public String toString() {
        return String.format("PlayerData{id=%d, name='%s', score=%d}", playerId, playerName, score);
    }
}
