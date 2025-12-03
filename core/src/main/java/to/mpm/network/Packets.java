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
        /** Nombre para mostrar del jugador. */
        public String playerName;
        /** Identificador para reconocer la respuesta local. */
        public String correlationId;
    }

    /**
     * Notificación broadcast del servidor sobre un nuevo jugador.
     */
    public static class PlayerJoined extends NetworkPacket {
        /** ID del jugador que se unió. */
        public int playerId;
        /** Nombre para mostrar del jugador. */
        public String playerName;
        /** True si ya estaba en la sala. */
        public boolean existingPlayer;
        /** Correlación opcional para el jugador local. */
        public String correlationId;
    }

    /**
     * Notificación de que un jugador se desconectó.
     */
    public static class PlayerLeft extends NetworkPacket {
        /** ID del jugador que se fue. */
        public int playerId;
    }

    /**
     * Paquete enviado por el host para indicar a los clientes que inicien la
     * partida.
     */
    public static class StartGame extends NetworkPacket {
        /** Tipo de minijuego a iniciar (nombre del enum). */
        public String minigameType;
        /** Ronda que se va a jugar. */
        public int currentRound;
        /** Total de rondas configuradas. */
        public int totalRounds;
    }

    /**
     * Actualización genérica de sincronización para un campo de un objeto
     * sincronizado.
     */
    public static class SyncUpdate extends NetworkPacket {
        /** ID asignado por {@link to.mpm.network.sync.SyncedObject}. */
        public UUID objectId;
        /** Nombre del campo a actualizar. */
        public String fieldName;
        /** Nuevo valor (debe ser serializable por Kryo). */
        public Object value;

        /** Constructor por defecto que prefiere UDP. */
        public SyncUpdate() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Paquete para anunciar la creación de un objeto sincronizado.
     */
    public static class SyncedObjectCreated extends NetworkPacket {
        /** ID único del objeto. */
        public UUID objectId;
        /** Etiqueta opcional para el tipo. */
        public String objectType;
    }

    /**
     * Actualización de posición del jugador.
     */
    public static class PlayerPosition extends NetworkPacket {
        /** ID del jugador. */
        public int playerId;
        /** Nueva posición X. */
        public float x;
        /** Nueva posición Y. */
        public float y;
        /** Velocidad vertical (para sincronizar gravedad). */
        public float velocityY;
        /** Velocidad horizontal (para sincronizar colisiones). */
        public float lastVelocityX;
        /** Si está en el suelo o sobre otro jugador. */
        public boolean isGrounded;

        /** Constructor por defecto que prefiere UDP. */
        public PlayerPosition() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Marcador para paquetes RPC si se necesitan.
     */
    public static class RPC extends NetworkPacket {
        /** Nombre del método a invocar. */
        public String methodName;
        /** Argumentos para el método. */
        public Object[] args;
    }

    /**
     * Paquete de ping para medición de latencia.
     */
    public static class Ping extends NetworkPacket {
        /** Instante en que se envió el ping. */
        public long timestamp;

        /** Constructor por defecto que prefiere UDP. */
        public Ping() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Respuesta pong a un {@link Ping}.
     */
    public static class Pong extends NetworkPacket {
        /** Instante en que se recibió el ping. */
        public long timestamp;

        /** Constructor por defecto que prefiere UDP. */
        public Pong() {
            preferTransport(Transports.UDP);
        }
    }

    /**
     * Paquete para actualizar el estado de espectador de un jugador.
     */
    public static class SpectatorStatus extends NetworkPacket {
        /** ID del jugador. */
        public int playerId;
        /** True si es espectador, false si es jugador. */
        public boolean isSpectator;
    }
}
