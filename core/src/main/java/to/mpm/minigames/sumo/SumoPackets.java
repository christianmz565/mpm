package to.mpm.minigames.sumo;

import to.mpm.network.NetworkPacket;

public class SumoPackets {
    // El servidor avisa: "El jugador X fue empujado y ahora se mueve a esta velocidad"
    public static class PlayerKnockback extends NetworkPacket {
        public int playerId;
        public float velocityX;
        public float velocityY;
    }

    // El servidor avisa: "El jugador X se cayó del mapa"
    public static class PlayerFell extends NetworkPacket {
        public int playerId;
    }
    
    // El servidor avisa: "Juego terminado, ganó X"
    public static class GameEnd extends NetworkPacket {
        public int winnerId;
    }
}