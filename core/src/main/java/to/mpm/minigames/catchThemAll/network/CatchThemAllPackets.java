package to.mpm.minigames.catchThemAll.network;

import to.mpm.network.NetworkPacket;
import to.mpm.network.Transports;

/**
 * Paquetes de red específicos del minijuego Atrapa a Todos.
 * <p>
 * Cada minijuego debe tener sus propias definiciones de paquetes para mantener el desacople.
 */
public class CatchThemAllPackets {
    
    /**
     * Paquete para notificar a los clientes que un nuevo pato ha aparecido.
     * Solo enviado por el host.
     */
    public static class DuckSpawned extends NetworkPacket {
        /** ID único del pato. */
        public int duckId;
        /** Posición X inicial. */
        public float x;
        /** Posición Y inicial. */
        public float y;
        /** Tipo de pato (NEUTRAL, GOLDEN, BAD). */
        public String duckType;

        /** Constructor por defecto que prefiere UDP. */
        public DuckSpawned() {
            preferTransport(Transports.UDP);
        }
    }
    
    /**
     * Paquete para actualizar la posición de un pato.
     * Enviado periódicamente por el host para mantener sincronizados a los clientes.
     */
    public static class DuckUpdate extends NetworkPacket {
        /** ID del pato. */
        public int duckId;
        /** Nueva posición X. */
        public float x;
        /** Nueva posición Y. */
        public float y;

        /** Constructor por defecto que prefiere UDP. */
        public DuckUpdate() {
            preferTransport(Transports.UDP);
        }
    }
    
    /**
     * Paquete para notificar que un pato ha sido removido (atrapado o cayó al suelo).
     * Solo enviado por el host.
     */
    public static class DuckRemoved extends NetworkPacket {
        /** ID del pato que fue removido. */
        public int duckId;
        /** ID del jugador que lo atrapó (-1 si cayó al suelo). */
        public int caughtByPlayerId;
    }
    
    /**
     * Paquete para actualizar la puntuación de un jugador.
     * Solo enviado por el host cuando cambian las puntuaciones.
     */
    public static class ScoreUpdate extends NetworkPacket {
        /** ID del jugador. */
        public int playerId;
        /** Puntuación actual del jugador. */
        public int score;
    }
}
