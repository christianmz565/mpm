package to.mpm.utils;

/**
 * Normalized player data structure used across the game.
 * Contains player ID, name, and score.
 */
public class PlayerData implements Comparable<PlayerData> {
    private final int playerId;
    private String playerName;
    private int score;

    /**
     * Creates a new PlayerData instance.
     * 
     * @param playerId the unique player ID
     * @param playerName the player's display name
     * @param score the player's current score
     */
    public PlayerData(int playerId, String playerName, int score) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.score = score;
    }

    /**
     * Creates a new PlayerData instance with zero score.
     * 
     * @param playerId the unique player ID
     * @param playerName the player's display name
     */
    public PlayerData(int playerId, String playerName) {
        this(playerId, playerName, 0);
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    /**
     * Compares players by score (descending order).
     * Higher scores come first. If scores are equal, uses player ID for consistent ordering.
     */
    @Override
    public int compareTo(PlayerData other) {
        int scoreCompare = Integer.compare(other.score, this.score); // Descending
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Integer.compare(this.playerId, other.playerId); // Ascending for ties
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
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
