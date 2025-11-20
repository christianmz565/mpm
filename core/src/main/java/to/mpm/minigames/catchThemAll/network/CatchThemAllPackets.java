package to.mpm.minigames.catchThemAll.network;

import to.mpm.network.NetworkPacket;
import to.mpm.network.Transports;

/**
 * Network packets specific to the Catch Them All minigame.
 * Each minigame should have its own packet definitions to keep things decoupled.
 */
public class CatchThemAllPackets {
    
    /**
     * Packet to notify clients that a new duck has spawned.
     * Only sent by the host.
     */
    public static class DuckSpawned extends NetworkPacket {
        public int duckId; //!< unique id of the duck
        public float x; //!< initial x position
        public float y; //!< initial y position
        public String duckType; //!< type of duck (NEUTRAL, GOLDEN, BAD)

        public DuckSpawned() {
            preferTransport(Transports.UDP);
        }
    }
    
    /**
     * Packet to update a duck's position.
     * Sent periodically by the host to keep clients in sync.
     */
    public static class DuckUpdate extends NetworkPacket {
        public int duckId; //!< id of the duck
        public float x; //!< new x position
        public float y; //!< new y position

        public DuckUpdate() {
            preferTransport(Transports.UDP);
        }
    }
    
    /**
     * Packet to notify that a duck has been removed (caught or hit ground).
     * Only sent by the host.
     */
    public static class DuckRemoved extends NetworkPacket {
        public int duckId; //!< id of the duck that was removed
        public int caughtByPlayerId; //!< id of player who caught it (-1 if it hit ground)
    }
    
    /**
     * Packet to update a player's score.
     * Only sent by the host when scores change.
     */
    public static class ScoreUpdate extends NetworkPacket {
        public int playerId; //!< id of the player
        public int score; //!< current score of the player
    }
}
