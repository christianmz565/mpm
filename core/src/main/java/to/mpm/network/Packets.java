package to.mpm.network;

/**
 * Contenedor de tipos de paquetes de red simples usados por la capa de red.
 */
public class Packets {
    /**
     * Solicitud enviada por un cliente para unirse al juego.
     */
    public static class PlayerJoinRequest {
        public String playerName; //!< nombre para mostrar del jugador
    }

    /**
     * Respuesta del servidor asignando un ID al jugador que se une.
     */
    public static class PlayerJoinResponse {
        public int playerId; //!< id del jugador asignado por el servidor
        public String playerName; //!< nombre para mostrar del jugador
    }

    /**
     * Notificación broadcast del servidor sobre un nuevo jugador.
     */
    public static class PlayerJoined {
        public int playerId; //!< id del jugador que se unió
        public String playerName; //!< nombre para mostrar del jugador
    }

    /**
     * Notificación de que un jugador se desconectó.
     */
    public static class PlayerLeft {
        public int playerId; //!< id del jugador que se fue
    }

    /**
     * Paquete enviado por el host para indicar a los clientes que inicien la partida.
     */
    public static class StartGame {
        public String minigameType; //!< tipo de minijuego a iniciar (nombre del enum)
    }

    /**
     * Actualización genérica de sincronización para un campo de un objeto sincronizado.
     */
    public static class SyncUpdate {
        public int objectId; //!< id asignado por {@link to.mpm.network.sync.SyncedObject}
        public String fieldName; //!< nombre del campo a actualizar
        public Object value; //!< nuevo valor (debe ser serializable por Kryo)
    }

    /**
     * Actualización de posición del jugador.
     */
    public static class PlayerPosition {
        public int playerId; //!< id del jugador
        public float x; //!< nueva posición x
        public float y; //!< nueva posición y
    }

    /**
     * Marcador de posición para paquetes RPC (llamada remota) para uso futuro.
     */
    public static class RPC {
        // TODO: actually implement RPC functionality if we need it
        public String methodName; //!< nombre del método a invocar
        public Object[] args; //!< argumentos para el método
    }

    /**
     * Paquete de ping para medición de latencia.
     */
    public static class Ping {
        public long timestamp; //!< instante en que se envió el ping
    }

    /**
     * Respuesta pong a un {@link Ping}.
     */
    public static class Pong {
        public long timestamp; //!< instante en que se recibió el ping
    }
}
