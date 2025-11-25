package to.mpm.network;

import java.util.UUID;

/**
 * Contenedor de tipos de paquetes de red simples usados por la capa de red.
 */
public class Packets {
    /**
     * Solicitud enviada por un cliente para unirse al juego.
     */
    public static class PlayerJoinRequest extends NetworkPacket {
        public String playerName; // !< nombre para mostrar del jugador
        public String correlationId; // !< identificador para reconocer la respuesta local
    }

    /**
     * Notificación broadcast del servidor sobre un nuevo jugador.
     */
    public static class PlayerJoined extends NetworkPacket {
        public int playerId; // !< id del jugador que se unió
        public String playerName; // !< nombre para mostrar del jugador
        public boolean existingPlayer; // !< true si ya estaba en la sala
        public String correlationId; // !< correlación opcional para el jugador local
    }

    /**
     * Notificación de que un jugador se desconectó.
     */
    public static class PlayerLeft extends NetworkPacket {
        public int playerId; // !< id del jugador que se fue
    }

    /**
     * Paquete enviado por el host para indicar a los clientes que inicien la
     * partida.
     */
    public static class StartGame extends NetworkPacket {
        public String minigameType; // !< tipo de minijuego a iniciar (nombre del enum)
        public int currentRound; // !< ronda que se va a jugar
        public int totalRounds; // !< total de rondas configuradas
    }

    /**
     * Actualización genérica de sincronización para un campo de un objeto
     * sincronizado.
     */
    public static class SyncUpdate extends NetworkPacket {
        public UUID objectId; // !< id asignado por {@link to.mpm.network.sync.SyncedObject}
        public String fieldName; // !< nombre del campo a actualizar
        public Object value; // !< nuevo valor (debe ser serializable por Kryo)

        public SyncUpdate() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Paquete para anunciar la creación de un objeto sincronizado.
     */
    public static class SyncedObjectCreated extends NetworkPacket {
        public UUID objectId; // !< id único del objeto
        public String objectType; // !< etiqueta opcional para el tipo
    }

    /**
     * Actualización de posición del jugador.
     */
    public static class PlayerPosition extends NetworkPacket {
        public int playerId; // !< id del jugador
        public float x; // !< nueva posición x
        public float y; // !< nueva posición y

        public PlayerPosition() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Marcador para paquetes RPC si se necesitan.
     */
    public static class RPC extends NetworkPacket {
        // TODO: actually implement RPC functionality if we need it
        public String methodName; // !< nombre del método a invocar
        public Object[] args; // !< argumentos para el método
    }

    /**
     * Paquete de ping para medición de latencia.
     */
    public static class Ping extends NetworkPacket {
        public long timestamp; // !< instante en que se envió el ping

        public Ping() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Respuesta pong a un {@link Ping}.
     */
    public static class Pong extends NetworkPacket {
        public long timestamp; // !< instante en que se recibió el ping

        public Pong() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Paquete para actualizar el estado de espectador de un jugador.
     */
    public static class SpectatorStatus extends NetworkPacket {
        public int playerId; // !< id del jugador
        public boolean isSpectator; // !< true si es espectador, false si es jugador
    }
}
