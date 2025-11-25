package to.mpm.network;

/**
 * Paquetes internos relacionados con eventos del servidor.
 */
public final class ServerEvents {
    /**
     * Constructor privado para evitar la instanciación.
     */
    private ServerEvents() {
    }

    /**
     * Paquete enviado cuando un jugador se une al servidor.
     */
    public static class ClientDisconnected extends NetworkPacket {
        public int playerId = -1; //!< ID del jugador que se desconectó
        public String playerName; //!< Nombre del jugador que se desconectó
    }
}
