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
    
    /**
     * Paquete para forzar el fin del juego.
     * Aunque el tiempo lo maneje otro módulo, se mantiene por compatibilidad.
     */
    public static class GameEnd extends NetworkPacket {
        public int winnerId;
    }

    public static class ScoreUpdate extends NetworkPacket {
        public int playerId;
        public int newScore;
    }

    /**
     * Paquete para señalar el reinicio de ronda.
     * Indica que se deben reiniciar las posiciones de los jugadores.
     */
    public static class RoundReset extends NetworkPacket {
    }
}