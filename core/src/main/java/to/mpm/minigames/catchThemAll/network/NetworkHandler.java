package to.mpm.minigames.catchThemAll.network;

import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.catchThemAll.entities.Player;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

/**
 * Handles network communication for the minigame.
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
}
