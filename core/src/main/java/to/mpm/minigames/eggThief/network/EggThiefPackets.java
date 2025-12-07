package to.mpm.minigames.eggThief.network;

import to.mpm.network.NetworkPacket;

// Container for all network packet types used in Egg Thief minigame (Server -> Client)
public class EggThiefPackets {

    // Sent when a new egg is spawned (generate)
    public static class EggSpawned extends NetworkPacket {
        public int eggId;
        public float x, y;
        public boolean isGolden;
    }

    // Sent when an egg is collected by a player.
    public static class EggCollected extends NetworkPacket {
        public int playerId;
        public int eggId;
        public int newEggCount;
    }

    // Sent when a player steals an egg from another player.
    public static class EggStolen extends NetworkPacket {
        public int thiefId;
        public int victimId;
        public int thiefEggCount;
        public int victimEggCount;
    }

    // Sent when a player delivers eggs to their nest.
    public static class EggsDelivered extends NetworkPacket {
        public int playerId;
        public int eggsDelivered;
        public int totalDelivered;
    }

    // Sent to update a player's score.
    public static class ScoreUpdate extends NetworkPacket {
        public int playerId;
        public int score;
    }

    // Sent to synchronize duck state.
    public static class DuckUpdate extends NetworkPacket {
        public int playerId;
        public float x;
        public float y;
        public float velocityX;
        public float velocityY;
        public int eggsCarrying;
    }

    // Sent to synchronize game timer.
    public static class GameTimerUpdate extends NetworkPacket {
        public float timeRemaining;
    }

    // Sent to update the position of an existing egg.
    public static class EggUpdate extends NetworkPacket {
        public int eggId;
        public float x, y;
    }

    // Sent to notify that an egg has been removed from the game.
    public static class EggRemoved extends NetworkPacket {
        public int eggId;
    }
}
