package to.mpm.minigames.eggThief.network;

import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.eggThief.entities.Duck;
import to.mpm.minigames.eggThief.entities.Egg;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

import java.util.List;

// Uses EggThiefPackets for minigame-specific network messages.
// NOTE: Methods marked "Only the host should call this" must be executed by the authoritative server/host instance.

public class NetworkHandler {
    /**
     * Send a single duck update (position, velocity, eggs carrying).
     * Should be called by the host to broadcast authoritative state.
     *
     * @param duck the duck to send update for
     */
    public static void sendDuckUpdate(Duck duck) {
        EggThiefPackets.DuckUpdate packet = new EggThiefPackets.DuckUpdate();
        packet.playerId = duck.getPlayerId();
        packet.x = duck.getX();
        packet.y = duck.getY();
        packet.velocityX = duck.getVelocity().x;
        packet.velocityY = duck.getVelocity().y;
        packet.eggsCarrying = duck.getEggsCarryingCount();
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Send all duck positions / states (used by host after simulation / collision
     * resolution).
     * Only the host should call this.
     *
     * @param ducks map of all ducks keyed by playerId
     */
    public static void sendAllDuckUpdates(IntMap<Duck> ducks) {
        for (IntMap.Entry<Duck> entry : ducks) {
            sendDuckUpdate(entry.value);
        }
    }

    /**
     * Send a notification that a new egg has spawned.
     * Only the host should call this.
     *
     * @param egg the egg that spawned
     */
    public static void sendEggSpawned(Egg egg) {
        EggThiefPackets.EggSpawned packet = new EggThiefPackets.EggSpawned();
        packet.eggId = egg.getId();
        packet.x = egg.getX();
        packet.y = egg.getY();
        packet.isGolden = egg.isGolden();
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Send position updates for eggs if necessary.
     * Only call this for eggs that actually moved or when correction is required.
     *
     * @param eggs list of eggs to update
     */
    public static void sendEggUpdates(List<Egg> eggs) {
        for (Egg egg : eggs) {
            EggThiefPackets.EggUpdate packet = new EggThiefPackets.EggUpdate();
            packet.eggId = egg.getId();
            packet.x = egg.getX();
            packet.y = egg.getY();
            NetworkManager.getInstance().sendPacket(packet);
        }
    }

    /**
     * Notify clients that an egg was collected by a player.
     * Only the host should call this after validating the collect.
     *
     * @param playerId ID of player who collected the egg
     * @param eggId    ID of the collected egg
     */
    public static void sendEggCollected(int playerId, int eggId) {
        EggThiefPackets.EggCollected packet = new EggThiefPackets.EggCollected();
        packet.playerId = playerId;
        packet.eggId = eggId;
        // newEggCount is not strictly needed when each duck can carry only 1 egg,
        // but keep it for compatibility / UI: 0 or 1.
        packet.newEggCount = 1;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Notify clients that an egg should be removed from the world (collected /
     * destroyed).
     * Only the host should call this.
     *
     * @param eggId ID of the egg to remove
     */
    public static void sendEggRemoved(int eggId) {
        EggThiefPackets.EggRemoved packet = new EggThiefPackets.EggRemoved();
        packet.eggId = eggId;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Notify clients that a steal happened (thief took victim's egg).
     * Only the host should call this after resolving collision and rules.
     *
     * @param thiefId  ID of player who stole the egg
     * @param victimId ID of player who lost the egg
     */
    public static void sendEggStolen(int thiefId, int victimId) {
        EggThiefPackets.EggStolen packet = new EggThiefPackets.EggStolen();
        packet.thiefId = thiefId;
        packet.victimId = victimId;
        // Because each duck can carry at most 1 egg, counts are either 0 or 1.
        packet.thiefEggCount = 1;
        packet.victimEggCount = 0;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Notify clients that a player delivered an egg to their nest.
     * Only the host should call this.
     *
     * @param playerId       ID of player who delivered
     * @param eggsDelivered  number of eggs delivered in this action (usually 1 or
     *                       0)
     * @param totalDelivered player's total delivered count after this action
     */
    public static void sendEggsDelivered(int playerId, int eggsDelivered, int totalDelivered) {
        EggThiefPackets.EggsDelivered packet = new EggThiefPackets.EggsDelivered();
        packet.playerId = playerId;
        packet.eggsDelivered = eggsDelivered;
        packet.totalDelivered = totalDelivered;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Send a score update for a player (host authoritative).
     *
     * @param playerId ID of the player
     * @param score    new total score
     */
    public static void sendScoreUpdate(int playerId, int score) {
        EggThiefPackets.ScoreUpdate packet = new EggThiefPackets.ScoreUpdate();
        packet.playerId = playerId;
        packet.score = score;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Send a periodic game timer update (host authoritative).
     *
     * @param timeRemaining time remaining in seconds
     */
    public static void sendGameTimerUpdate(float timeRemaining) {
        EggThiefPackets.GameTimerUpdate packet = new EggThiefPackets.GameTimerUpdate();
        packet.timeRemaining = timeRemaining;
        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Convenience helper sending a simple PlayerPosition packet (uses the generic
     * Packets class).
     * Clients may use this to send their local predicted position to the host if
     * you want client-side prediction,
     * but prefer sending only input packets from client -> host to remain
     * server-authoritative.
     *
     * @param localPlayerId local player id
     * @param duck          local duck instance
     */
    public static void sendPlayerPosition(int localPlayerId, Duck duck) {
        Packets.PlayerPosition packet = new Packets.PlayerPosition();
        packet.playerId = localPlayerId;
        packet.x = duck.getX();
        packet.y = duck.getY();
        NetworkManager.getInstance().sendPacket(packet);
    }
}
