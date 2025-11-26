package to.mpm.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lógica del lado del servidor de red.
 * <p>
 * Maneja conexiones de clientes, el envío y recepción de paquetes,
 * y el registro de manejadores de paquetes.
 */
public class NetworkServer {
    private Server server; //!< instancia del servidor KryoNet
    private final Map<Class<? extends NetworkPacket>, CopyOnWriteArrayList<ServerPacketHandler>> handlers; //!<
                                                                                                           // manejadores
                                                                                                           // por tipo
    private final ConcurrentHashMap<Integer, String> connectedPlayers; //!< mapa de jugadores conectados (ID -> nombre)
    private final ConcurrentHashMap<Integer, Integer> connectionToPlayerId; //!< mapa de conexión a ID de jugador
    private final AtomicInteger nextPlayerId; //!< contador para asignar nuevos IDs de jugador

    /**
     * Construye una nueva instancia del servidor de red.
     */
    public NetworkServer() {
        handlers = new ConcurrentHashMap<>();
        connectedPlayers = new ConcurrentHashMap<>();
        connectionToPlayerId = new ConcurrentHashMap<>();
        nextPlayerId = new AtomicInteger(0);
    }

    /**
     * Inicia el servidor en el puerto especificado.
     *
     * @param port el puerto en el que escuchar
     * @throws IOException si el servidor no puede iniciarse
     */
    public void start(int port) throws IOException {
        if (server != null) {
            Gdx.app.log("NetworkServer", "Server is already running");
            return;
        }

        server = new Server(NetworkConfig.UDP_BUFFER_SIZE, NetworkConfig.UDP_BUFFER_SIZE);
        KryoClassRegistrar.registerCoreClasses(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof NetworkPacket packet) {
                    dispatchPacket(packet, connection);
                }
            }

            @Override
            public void connected(Connection connection) {
                Gdx.app.log("NetworkServer", "Client connected: " + connection.getRemoteAddressTCP());
            }

            @Override
            public void disconnected(Connection connection) {
                handleDisconnection(connection);
            }
        });

        try {
            server.bind(port, port);
            server.start();
            Gdx.app.log("NetworkServer", "Server started on port " + port);
        } catch (IOException e) {
            server = null;
            connectedPlayers.clear();
            connectionToPlayerId.clear();
            nextPlayerId.set(0);
            throw new IOException("Failed to start server on port " + port, e);
        }
    }

    /**
     * Detiene el servidor y limpia los recursos.
     */
    public void stop() {
        if (server != null) {
            server.stop();
            server.close();
            server = null;
        }
        handlers.clear();
        connectedPlayers.clear();
        connectionToPlayerId.clear();
        nextPlayerId.set(0);
        Gdx.app.log("NetworkServer", "Server stopped");
    }

    /**
     * Registra un manejador basado en clases de paquete.
     * 
     * @param handler el manejador a registrar
     */
    public void registerHandler(ServerPacketHandler handler) {
        Collection<Class<? extends NetworkPacket>> packetClasses = handler.receivablePackets();
        if (packetClasses == null || packetClasses.isEmpty()) {
            throw new IllegalArgumentException("Handler must declare receivable packets");
        }
        for (Class<? extends NetworkPacket> packetClass : packetClasses) {
            handlers.computeIfAbsent(packetClass, key -> new CopyOnWriteArrayList<>())
                    .add(handler);
        }
    }

    /**
     * Desregistra un manejador previamente agregado.
     * 
     * @param handler el manejador a desregistrar
     */
    public void unregisterHandler(ServerPacketHandler handler) {
        for (List<ServerPacketHandler> handlerList : handlers.values()) {
            handlerList.remove(handler);
        }
    }

    /**
     * Envía un paquete a todos los clientes conectados usando su transporte
     * preferido.
     * 
     * @param packet paquete de red a enviar
     */
    public void broadcast(NetworkPacket packet) {
        if (server == null)
            return;
        if (packet.getTransportMode() == Transports.UDP) {
            server.sendToAllUDP(packet);
        } else {
            server.sendToAllTCP(packet);
        }
    }

    /**
     * Envía a todos excepto al remitente.
     * 
     * @param origin conexión del remitente
     * @param packet paquete de red a enviar
     */
    public void broadcastExcept(Connection origin, NetworkPacket packet) {
        if (server == null || origin == null)
            return;
        if (packet.getTransportMode() == Transports.UDP) {
            server.sendToAllExceptUDP(origin.getID(), packet);
        } else {
            server.sendToAllExceptTCP(origin.getID(), packet);
        }
    }

    /**
     * Envía a una conexión específica respetando el modo de transporte.
     * 
     * @param target conexión objetivo
     * @param packet paquete de red a enviar
     */
    public void send(Connection target, NetworkPacket packet) {
        if (target == null)
            return;
        if (packet.getTransportMode() == Transports.UDP) {
            target.sendUDP(packet);
        } else {
            target.sendTCP(packet);
        }
    }

    /**
     * Registra clases adicionales de Kryo para la serialización.
     *
     * @param classes las clases a registrar
     */
    public void registerAdditionalClasses(Class<?>... classes) {
        if (server != null) {
            Kryo kryo = server.getKryo();
            for (Class<?> clazz : classes) {
                kryo.register(clazz);
                Gdx.app.log("NetworkServer", "Registered class: " + clazz.getName());
            }
        }
    }

    /**
     * Obtiene el mapa de jugadores conectados.
     *
     * @return mapa de ID de jugador a nombre de jugador
     */
    public ConcurrentHashMap<Integer, String> getConnectedPlayers() {
        return connectedPlayers;
    }

    /**
     * Verifica si el servidor está en funcionamiento.
     *
     * @return true si está en funcionamiento
     */
    public boolean isRunning() {
        return server != null;
    }

    /**
     * Obtiene el número de clientes conectados.
     *
     * @return número de clientes conectados
     */
    public int getConnectionCount() {
        return server != null ? server.getConnections().length : 0;
    }

    /**
     * Genera un nuevo ID único de jugador.
     * 
     * @return nuevo ID de jugador
     */
    public int allocatePlayerId() {
        return nextPlayerId.getAndIncrement();
    }

    /**
     * Asocia una conexión con un jugador concreto.
     * 
     * @param connection conexión a asociar
     * @param playerId   ID del jugador
     * @param playerName nombre del jugador
     */
    public void bindConnectionToPlayer(Connection connection, int playerId, String playerName) {
        connectionToPlayerId.put(connection.getID(), playerId);
        if (playerName != null) {
            connectedPlayers.put(playerId, playerName);
        }
    }

    /**
     * Desasocia una conexión.
     * 
     * @param connection conexión a desasociar
     * @return ID del jugador eliminado, o null si no existía
     */
    public Integer unbindConnection(Connection connection) {
        return connectionToPlayerId.remove(connection.getID());
    }

    /**
     * Reenvía un paquete a los manejadores registrados.
     *
     * @param packet     el paquete de red a reenvíar
     * @param connection la conexión que envió el paquete
     */
    private void dispatchPacket(NetworkPacket packet, Connection connection) {
        List<ServerPacketHandler> handlerList = handlers.get(packet.getClass());
        if (handlerList == null || handlerList.isEmpty()) {
            return;
        }
        ServerPacketContext context = new ServerPacketContext(this, connection);
        for (ServerPacketHandler handler : handlerList) {
            try {
                handler.handle(context, packet);
            } catch (Exception ex) {
                Gdx.app.error("NetworkServer", "Handler error for packet " + packet.getClass().getSimpleName(), ex);
            }
        }
    }

    /**
     * Maneja la desconexión de un cliente.
     * 
     * @param connection la conexión que se ha desconectado
     */
    private void handleDisconnection(Connection connection) {
        Integer playerId = unbindConnection(connection);
        if (playerId == null) {
            return;
        }
        String playerName = connectedPlayers.remove(playerId);
        Gdx.app.log("NetworkServer", "Player disconnected: " + playerName + " (ID: " + playerId + ")");

        ServerEvents.ClientDisconnected event = new ServerEvents.ClientDisconnected();
        event.playerId = playerId;
        event.playerName = playerName;
        dispatchPacket(event, connection);
    }
}
