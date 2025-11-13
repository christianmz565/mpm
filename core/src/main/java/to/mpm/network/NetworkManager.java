package to.mpm.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Gestor de red singleton que soporta un servidor host y múltiples clientes.
 */
public class NetworkManager {
    private static NetworkManager instance; //!< instancia singleton
    private final ConcurrentHashMap<Class<?>, Consumer<Object>> packetHandlers; //!< manejadores de paquetes registrados
    private final ConcurrentHashMap<Integer, String> connectedPlayers; //!< mapa de jugadores conectados (id -> nombre)
    private final ConcurrentHashMap<Integer, Integer> connectionToPlayerId; //!< mapa de conexión KryoNet ID a player ID
    private final AtomicInteger nextPlayerId; //!< contador para asignar IDs únicos de jugador
    private Server server; //!< instancia de KryoNet Server
    private Client client; //!< instancia de KryoNet Client
    private boolean isHost; //!< true si está hospedando una partida
    private int myId; //!< id del jugador local asignado

    private NetworkManager() {
        packetHandlers = new ConcurrentHashMap<>();
        connectedPlayers = new ConcurrentHashMap<>();
        connectionToPlayerId = new ConcurrentHashMap<>();
        nextPlayerId = new AtomicInteger(0);
    }

    /**
     * Devuelve la instancia singleton del NetworkManager.
     *
     * @return instancia singleton
     */
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    /**
     * Registra las clases de Kryo para la serialización usadas por la capa de red.
     *
     * @param kryo instancia de Kryo
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
     * Inicia el servidor hospedando una partida en el puerto configurado.
     * Esto enlazará un {@link Server} de KryoNet y escuchará conexiones entrantes.
     *
     * @throws IOException si el servidor falla al iniciarse
     */
    public void hostGame() throws IOException {
        if (server != null) {
            Gdx.app.log("NetworkManager", "El servidor ya está en ejecución");
            return;
        }

        isHost = true;
        myId = nextPlayerId.getAndIncrement();
        connectedPlayers.put(myId, "Host");

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
                    }
                    handlePacket(object, connection);
                }
            }

            @Override
            public void connected(Connection connection) {
                Gdx.app.log("NetworkManager", "Cliente conectado: " + connection.getRemoteAddressTCP());
            }

            @Override
            public void disconnected(Connection connection) {
                Integer playerId = connectionToPlayerId.remove(connection.getID());
                if (playerId != null) {
                    String playerName = connectedPlayers.remove(playerId);
                    Gdx.app.log("NetworkManager", "Jugador desconectado: " + playerName + " (ID: " + playerId + ")");

                    Packets.PlayerLeft leftPacket = new Packets.PlayerLeft();
                    leftPacket.playerId = playerId;
                    server.sendToAllUDP(leftPacket);

                    handlePacket(leftPacket, connection);
                }
            }
        });

        try {
            server.bind(NetworkConfig.DEFAULT_PORT, NetworkConfig.DEFAULT_PORT);
            server.start();
            Gdx.app.log("NetworkManager", "Servidor iniciado en el puerto " + NetworkConfig.DEFAULT_PORT);
        } catch (IOException e) {
            server = null;
            isHost = false;
            connectedPlayers.remove(myId);
            nextPlayerId.decrementAndGet();
            throw new IOException("No se pudo iniciar el servidor en el puerto " + NetworkConfig.DEFAULT_PORT + 
                ". El puerto puede estar ya en uso. " + e.getMessage(), e);
        }
    }

    /**
     * Conecta a un host como cliente.
     * Envía un {@link Packets.PlayerJoinRequest} inicial después de conectarse.
     *
     * @param host nombre de host o IP del host
     * @param port número de puerto al que conectar
     * @param playerName nombre del jugador para mostrar
     * @throws IOException si el intento de conexión falla
     */
    public void joinGame(String host, int port, String playerName) throws IOException {
        isHost = false;

        client = new Client(NetworkConfig.UDP_BUFFER_SIZE, NetworkConfig.UDP_BUFFER_SIZE);
        registerClasses(client.getKryo());

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packets.PlayerJoinResponse) {
                    handlePlayerJoinResponse((Packets.PlayerJoinResponse) object);
                } else {
                    handlePacket(object, connection);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.log("NetworkManager", "Desconectado del servidor");
            }
        });

        client.start();
        client.connect(NetworkConfig.TIMEOUT_MS, host, port, port);

        Packets.PlayerJoinRequest joinRequest = new Packets.PlayerJoinRequest();
        joinRequest.playerName = playerName != null ? playerName : "Player";
        client.sendTCP(joinRequest);

        Gdx.app.log("NetworkManager", "Conectado a " + host + ":" + port);
    }

    /**
     * Envía un paquete a los pares remotos. Si es host, envía a todos los clientes conectados. Si es cliente, envía al host
     *
     * @param packet un objeto que representa el paquete (una de las clases en {@link Packets})
     */
    public void sendPacket(Object packet) {
        if (isHost && server != null) {
            server.sendToAllUDP(packet);
        } else if (client != null && client.isConnected()) {
            client.sendUDP(packet);
        }
    }

    /**
     * Registra un manejador que será llamado cuando se reciba un paquete de la clase {@code packetClass}.
     * El manejador se ejecuta en el hilo de la aplicación de libGDX.
     *
     * @param packetClass clase del paquete
     * @param handler     consumidor que acepta la instancia del paquete
     * @param <T>         tipo de paquete
     */
    @SuppressWarnings("unchecked")
    public <T> void registerHandler(Class<T> packetClass, Consumer<T> handler) {
        packetHandlers.put(packetClass, (Consumer<Object>) handler);
    }

    /**
     * Maneja un paquete entrante despachándolo al manejador registrado en el hilo GDX.
     *
     * @param packet paquete recibido
     * @param connection conexión desde la que se recibió el paquete
     */
    private void handlePacket(Object packet, Connection connection) {
        Consumer<Object> handler = packetHandlers.get(packet.getClass());
        if (handler != null) {
            Gdx.app.postRunnable(() -> handler.accept(packet));
        }
    }

    /**
     * Maneja una solicitud de unión de jugador en el servidor.
     * Asigna un ID único al jugador y notifica a todos los clientes.
     *
     * @param connection conexión del jugador que se une
     * @param request solicitud de unión
     */
    private void handlePlayerJoinRequest(Connection connection, Packets.PlayerJoinRequest request) {
        int newPlayerId = nextPlayerId.getAndIncrement();
        String playerName = request.playerName;

        connectedPlayers.put(newPlayerId, playerName);
        connectionToPlayerId.put(connection.getID(), newPlayerId);

        Gdx.app.log("NetworkManager", "Jugador unido: " + playerName + " (ID: " + newPlayerId + ")");

        Packets.PlayerJoinResponse response = new Packets.PlayerJoinResponse();
        response.playerId = newPlayerId;
        response.playerName = playerName;
        connection.sendTCP(response);

        Packets.PlayerJoined joinedPacket = new Packets.PlayerJoined();
        joinedPacket.playerId = newPlayerId;
        joinedPacket.playerName = playerName;

        server.sendToAllExceptTCP(connection.getID(), joinedPacket);
        handlePacket(joinedPacket, connection);

        for (var entry : connectedPlayers.entrySet()) {
            if (entry.getKey() != newPlayerId) {
                Packets.PlayerJoined existingPlayer = new Packets.PlayerJoined();
                existingPlayer.playerId = entry.getKey();
                existingPlayer.playerName = entry.getValue();
                connection.sendTCP(existingPlayer);
            }
        }
    }

    /**
     * Maneja la respuesta de unión de jugador en el cliente.
     * Asigna el ID del jugador local.
     *
     * @param response respuesta del servidor
     */
    private void handlePlayerJoinResponse(Packets.PlayerJoinResponse response) {
        myId = response.playerId;
        connectedPlayers.put(myId, response.playerName);
        Gdx.app.log("NetworkManager", "ID asignado: " + myId + " (" + response.playerName + ")");
    }

    /**
     * Desconecta y limpia recursos de red.
     */
    public void disconnect() {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (client != null) {
            client.stop();
            client = null;
        }
        isHost = false;
        connectedPlayers.clear();
        connectionToPlayerId.clear();
        nextPlayerId.set(0);
        Gdx.app.log("NetworkManager", "Desconectado");
    }

    /**
     * Comprueba si esta instancia está hospedando la partida.
     *
     * @return true si es host, false si es cliente
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Obtiene el ID del jugador local asignado por esta instancia.
     *
     * @return ID del jugador local
     */
    public int getMyId() {
        return myId;
    }

    /**
     * Comprueba si hay una conexión activa.
     * Para el host: true si hay clientes conectados. Para el cliente: true si está conectado al host.
     *
     * @return true si está conectado
     */
    public boolean isConnected() {
        if (isHost) {
            return server != null && server.getConnections().length > 0;
        } else {
            return client != null && client.isConnected();
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
     * Obtiene el número de jugadores conectados (incluyendo el jugador local).
     *
     * @return número de jugadores
     */
    public int getPlayerCount() {
        return connectedPlayers.size();
    }
}
