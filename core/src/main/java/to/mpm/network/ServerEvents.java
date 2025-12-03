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
     * Paquete enviado cuando un jugador se desconecta del servidor.
     */
    public static class ClientDisconnected extends NetworkPacket {
        /** ID del jugador que se desconectó. */
        public int playerId = -1;
        /** Nombre del jugador que se desconectó. */
        public String playerName;
    }
}
