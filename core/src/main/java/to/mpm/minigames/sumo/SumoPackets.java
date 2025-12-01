package to.mpm.minigames.sumo;

import to.mpm.network.NetworkPacket;

public class SumoPackets {
    public static class PlayerKnockback extends NetworkPacket {
        public int playerId;
        public float velocityX;
        public float velocityY;
    }

    public static class PlayerFell extends NetworkPacket {
        public int playerId;
    }
    
    // Aunque el tiempo lo maneje otro, mantenemos esto por si acaso se necesita forzar el fin
    public static class GameEnd extends NetworkPacket {
        public int winnerId;
    }

    public static class ScoreUpdate extends NetworkPacket {
        public int playerId;
        public int newScore;
    }

    public static class RoundReset extends NetworkPacket {
        // Señal para reiniciar posiciones
    }
}