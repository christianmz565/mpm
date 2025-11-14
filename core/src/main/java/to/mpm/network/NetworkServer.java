package to.mpm.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Lógica del lado del servidor de red.
 * Maneja conexiones de clientes, el envío y recepción de paquetes,
 * y el registro de manejadores de paquetes.
 */
public class NetworkServer {
    private Server server; //!< instancia del servidor KryoNet
    private final ConcurrentHashMap<Integer, String> connectedPlayers; //!< mapa de jugadores conectados (ID -> nombre)
    private final ConcurrentHashMap<Integer, Integer> connectionToPlayerId; //!< mapa de conexión a ID de jugador
    private final AtomicInteger nextPlayerId; //!< contador para asignar nuevos IDs de jugador
    private int hostPlayerId; //!< ID del jugador host
    private BiConsumer<Object, Connection> packetReceivedCallback; //!< callback para notificar recepción de paquetes
    
    /**
     * Construye una nueva instancia del servidor de red.
     */
    public NetworkServer() {
        connectedPlayers = new ConcurrentHashMap<>();
        connectionToPlayerId = new ConcurrentHashMap<>();
        nextPlayerId = new AtomicInteger(0);
    }

    /**
     * Configura un callback para notificar la recepción de paquetes.
     * 
     * @param callback el callback a configurar
     */
    public void setPacketReceivedCallback(BiConsumer<Object, Connection> callback) {
        this.packetReceivedCallback = callback;
    }

    /**
     * Inicia el servidor en el puerto especificado.
     * 
     * @param port el puerto en el que escuchar
     * @param hostPlayerName el nombre del jugador host
     * @throws IOException si el servidor no puede iniciarse
     */
    public void start(int port, String hostPlayerName) throws IOException {
        if (server != null) {
            Gdx.app.log("NetworkServer", "Server is already running");
            return;
        }

        hostPlayerId = nextPlayerId.getAndIncrement();
        connectedPlayers.put(hostPlayerId, hostPlayerName);

        server = new Server(NetworkConfig.UDP_BUFFER_SIZE, NetworkConfig.UDP_BUFFER_SIZE);
        registerClasses(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packets.PlayerJoinRequest) {
                    handlePlayerJoinRequest(connection, (Packets.PlayerJoinRequest) object);
                } else {
                    if (object instanceof Packets.PlayerPosition || object instanceof Packets.SyncUpdate) {
                        server.sendToAllExceptUDP(connection.getID(), object);
                    } else {
                        server.sendToAllExceptTCP(connection.getID(), object);
                    }
                    
                    if (packetReceivedCallback != null) {
                        packetReceivedCallback.accept(object, connection);
                    }
                }
            }

            @Override
            public void connected(Connection connection) {
                Gdx.app.log("NetworkServer", "Client connected: " + connection.getRemoteAddressTCP());
            }

            @Override
            public void disconnected(Connection connection) {
                Integer playerId = connectionToPlayerId.remove(connection.getID());
                if (playerId != null) {
                    String playerName = connectedPlayers.remove(playerId);
                    Gdx.app.log("NetworkServer", "Player disconnected: " + playerName + " (ID: " + playerId + ")");

                    Packets.PlayerLeft leftPacket = new Packets.PlayerLeft();
                    leftPacket.playerId = playerId;
                    server.sendToAllUDP(leftPacket);

                    if (packetReceivedCallback != null) {
                        packetReceivedCallback.accept(leftPacket, connection);
                    }
                }
            }
        });

        try {
            server.bind(port, port);
            server.start();
            Gdx.app.log("NetworkServer", "Server started on port " + port);
        } catch (IOException e) {
            server = null;
            connectedPlayers.remove(hostPlayerId);
            nextPlayerId.decrementAndGet();
            throw new IOException("Failed to start server on port " + port, e);
        }
    }

    /**
     * Detiene el servidor y limpia los recursos.
     */
    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
        connectedPlayers.clear();
        connectionToPlayerId.clear();
        nextPlayerId.set(0);
        Gdx.app.log("NetworkServer", "Server stopped");
    }

    /**
     * Envía un paquete a todos los clientes conectados.
     *
     * @param packet el paquete a enviar
     */
    public void sendToAllTCP(Object packet) {
        if (server != null) {
            server.sendToAllTCP(packet);
        }
    }

    /**
     * Envía un paquete a todos los clientes conectados vía UDP.
     *
     * @param packet el paquete a enviar
     */
    public void sendToAllUDP(Object packet) {
        if (server != null) {
            server.sendToAllUDP(packet);
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
     * Obtiene el ID del jugador host.
     *
     * @return el ID del jugador host
     */
    public int getHostPlayerId() {
        return hostPlayerId;
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
     * Obtiene el número de clientes conectados (excluyendo al host).
     *
     * @return número de clientes conectados
     */
    public int getConnectionCount() {
        return server != null ? server.getConnections().length : 0;
    }

    /**
     * Registra las clases de paquetes principales con Kryo.
     *
     * @param kryo la instancia de Kryo
     */
    private void registerClasses(Kryo kryo) {
        kryo.register(Packets.PlayerJoinRequest.class);
        kryo.register(Packets.PlayerJoinResponse.class);
        kryo.register(Packets.PlayerJoined.class);
        kryo.register(Packets.PlayerLeft.class);
        kryo.register(Packets.StartGame.class);
        kryo.register(Packets.SyncUpdate.class);
        kryo.register(Packets.PlayerPosition.class);
        kryo.register(Packets.RPC.class);
        kryo.register(Packets.Ping.class);
        kryo.register(Packets.Pong.class);
        kryo.register(Object[].class);
    }

    /**
     * Maneja una solicitud de unión de jugador.
     * Asigna un ID de jugador único y notifica a todos los clientes.
     *
     * @param connection la conexión del jugador que se une
     * @param request la solicitud de unión
     */
    private void handlePlayerJoinRequest(Connection connection, Packets.PlayerJoinRequest request) {
        int newPlayerId = nextPlayerId.getAndIncrement();
        String playerName = request.playerName;

        connectedPlayers.put(newPlayerId, playerName);
        connectionToPlayerId.put(connection.getID(), newPlayerId);

        Gdx.app.log("NetworkServer", "Player joined: " + playerName + " (ID: " + newPlayerId + ")");

        Packets.PlayerJoinResponse response = new Packets.PlayerJoinResponse();
        response.playerId = newPlayerId;
        response.playerName = playerName;
        connection.sendTCP(response);

        Packets.PlayerJoined joinedPacket = new Packets.PlayerJoined();
        joinedPacket.playerId = newPlayerId;
        joinedPacket.playerName = playerName;
        server.sendToAllExceptTCP(connection.getID(), joinedPacket);
        
        if (packetReceivedCallback != null) {
            packetReceivedCallback.accept(joinedPacket, connection);
        }

        for (var entry : connectedPlayers.entrySet()) {
            if (entry.getKey() != newPlayerId) {
                Packets.PlayerJoined existingPlayer = new Packets.PlayerJoined();
                existingPlayer.playerId = entry.getKey();
                existingPlayer.playerName = entry.getValue();
                connection.sendTCP(existingPlayer);
            }
        }
    }
}
