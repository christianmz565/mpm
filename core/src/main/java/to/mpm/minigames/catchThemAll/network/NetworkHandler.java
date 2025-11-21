package to.mpm.minigames.catchThemAll.network;

import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.entities.Player;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

import java.util.List;

/**
 * Handles network communication for the Catch Them All minigame.
 * Uses CatchThemAllPackets for minigame-specific network messages.
 */
public class NetworkHandler {
    
    /**
     * Send the position of a single player.
     * 
     * @param playerId ID of the player
     * @param player player instance
     */
    public static void sendPlayerPosition(int playerId, Player player) {
        Packets.PlayerPosition packet = new Packets.PlayerPosition();
        packet.playerId = playerId;
        packet.x = player.x;
        packet.y = player.y;
        packet.velocityY = player.velocityY;
        packet.lastVelocityX = player.lastVelocityX;
        packet.isGrounded = player.isGrounded;
        NetworkManager.getInstance().sendPacket(packet);
    }
    
    /**
     * Send all player positions (used by host after collision resolution).
     * 
     * @param players map of all players
     */
    public static void sendAllPlayerPositions(IntMap<Player> players) {
        for (IntMap.Entry<Player> entry : players) {
            sendPlayerPosition(entry.key, entry.value);
        }
    }
    
    /**
     * Send a notification that a new duck has spawned.
     * Only the host should call this.
     * 
     * @param duck the duck that spawned
     */
    public static void sendDuckSpawned(Duck duck) {
        CatchThemAllPackets.DuckSpawned packet = new CatchThemAllPackets.DuckSpawned();
        packet.duckId = duck.id;
        packet.x = duck.x;
        packet.y = duck.y;
        packet.duckType = duck.type.name();
        NetworkManager.getInstance().sendPacket(packet);
    }
    
    /**
     * Send duck position updates.
     * Only the host should call this periodically.
     * 
     * @param ducks list of all active ducks
     */
    public static void sendDuckUpdates(List<Duck> ducks) {
        for (Duck duck : ducks) {
            if (!duck.isCaught()) {
                CatchThemAllPackets.DuckUpdate packet = new CatchThemAllPackets.DuckUpdate();
                packet.duckId = duck.id;
                packet.x = duck.x;
                packet.y = duck.y;
                NetworkManager.getInstance().sendPacket(packet);
            }
        }
    }
    
    /**
     * Send notification that a duck was removed (caught or grounded).
     * Only the host should call this.
     * 
     * @param duck the duck that was removed
     */
    public static void sendDuckRemoved(Duck duck) {
        CatchThemAllPackets.DuckRemoved packet = new CatchThemAllPackets.DuckRemoved();
        packet.duckId = duck.id;
        packet.caughtByPlayerId = duck.caughtByPlayerId;
        NetworkManager.getInstance().sendPacket(packet);
    }
    
    /**
     * Send score update for a player.
     * Only the host should call this.
     * 
     * @param playerId ID of the player
     * @param score new score
     */
    public static void sendScoreUpdate(int playerId, int score) {
        CatchThemAllPackets.ScoreUpdate packet = new CatchThemAllPackets.ScoreUpdate();
        packet.playerId = playerId;
        packet.score = score;
        NetworkManager.getInstance().sendPacket(packet);
    }
}
