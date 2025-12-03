package to.mpm.minigames.duckshooter.network;

import to.mpm.network.NetworkPacket;
import to.mpm.network.Transports;

/**
 * Paquetes de red específicos del minijuego Duck Shooter.
 */
public class DuckShooterPackets {

    /**
     * Paquete para sincronizar la posición y estado del pato.
     */
    public static class DuckState extends NetworkPacket {
        public int playerId;
        public float x;
        public float y;
        public int hits;

        public DuckState() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Paquete para notificar que un pato disparó un quack.
     */
    public static class ShootQuack extends NetworkPacket {
        public int shooterId;
        public float x;
        public float y;
        public float dirX;
        public float dirY;

        public ShootQuack() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Paquete para notificar que un quack impactó a un pato.
     */
    public static class QuackHit extends NetworkPacket {
        public int shooterId;
        public int targetId;
        public int remainingHits;
    }

    /**
     * Paquete para notificar que un pato fue eliminado.
     */
    public static class DuckEliminated extends NetworkPacket {
        public int playerId;
        public int killerId;
    }

    /**
     * Paquete para notificar que el juego ha terminado.
     */
    public static class GameEnd extends NetworkPacket {
        public int winnerId;
    }

    /**
     * Paquete para sincronizar la aparición de un botiquín de curación.
     */
    public static class HealthPackSpawned extends NetworkPacket {
        public int healthPackId;
        public float x;
        public float y;
    }

    /**
     * Paquete para notificar que un jugador recogió un botiquín.
     */
    public static class HealthPackPickup extends NetworkPacket {
        public int healthPackId;
        public int playerId;
        public int newHits; // Nuevo total de vida del jugador
    }
}
