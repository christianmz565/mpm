package to.mpm.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lógica del lado del cliente de red.
 * Maneja la conexión al servidor, el envío y recepción de paquetes,
 * y el registro de manejadores de paquetes.
 */
public class NetworkClient {
    private Client client; //!< instancia del cliente KryoNet
    private final PacketHandlerRegistry handlerRegistry; //!< registro de manejadores de paquetes
    private final ConcurrentHashMap<Integer, String> connectedPlayers; //!< mapa de jugadores conectados (ID -> nombre)
    private int myPlayerId = -1; //!< ID del jugador local
    private String myPlayerName; //!< nombre del jugador local

    /**
     * Construye una nueva instancia del cliente de red.
     */
    public NetworkClient() {
        handlerRegistry = new PacketHandlerRegistry();
        connectedPlayers = new ConcurrentHashMap<>();
    }

    /**
     * Conecta a un servidor como cliente.
     *
     * @param host la dirección del servidor
     * @param port el puerto del servidor
     * @param playerName el nombre del jugador local
     * @throws IOException si la conexión falla
     */
    public void connect(String host, int port, String playerName) throws IOException {
        if (client != null && client.isConnected()) {
            Gdx.app.log("NetworkClient", "Already connected to a server");
            return;
        }

        myPlayerName = playerName != null ? playerName : "Player";

        client = new Client(NetworkConfig.UDP_BUFFER_SIZE, NetworkConfig.UDP_BUFFER_SIZE);
        registerClasses(client.getKryo());

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packets.PlayerJoinResponse) {
                    handlePlayerJoinResponse((Packets.PlayerJoinResponse) object);
                } else {
                    // Dispatch packet to all registered handlers on the GDX thread
                    Gdx.app.postRunnable(() -> handlerRegistry.invokeHandlers(object));
                }
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.log("NetworkClient", "Disconnected from server");
            }
        });

        client.start();
        client.connect(NetworkConfig.TIMEOUT_MS, host, port, port);

        // Send join request
        Packets.PlayerJoinRequest joinRequest = new Packets.PlayerJoinRequest();
        joinRequest.playerName = myPlayerName;
        client.sendTCP(joinRequest);

        Gdx.app.log("NetworkClient", "Connected to " + host + ":" + port);
    }

    /**
     * Inicializa el cliente como el host local.
     * 
     * @param playerId el ID asignado al jugador host
     * @param playerName el nombre del jugador host
     */
    public void connectAsLocalHost(int playerId, String playerName) {
        myPlayerId = playerId;
        myPlayerName = playerName;
        connectedPlayers.put(playerId, playerName);
        Gdx.app.log("NetworkClient", "Initialized as local host client with ID: " + playerId);
    }

    /**
     * Desconecta del servidor y limpia los recursos.
     */
    public void disconnect() {
        if (client != null) {
            client.stop();
            client = null;
        }
        myPlayerId = -1;
        connectedPlayers.clear();
        Gdx.app.log("NetworkClient", "Client disconnected");
    }

    /**
     * Envía un paquete al servidor vía TCP.
     *
     * @param packet el paquete a enviar
     */
    public void sendTCP(Object packet) {
        if (client != null && client.isConnected()) {
            client.sendTCP(packet);
        }
    }

    /**
     * Envía un paquete al servidor vía UDP.
     *
     * @param packet el paquete a enviar
     */
    public void sendUDP(Object packet) {
        if (client != null && client.isConnected()) {
            client.sendUDP(packet);
        }
    }

    /**
     * Registra un manejador para un tipo específico de paquete.
     * 
     * @param <T>        el tipo de paquete
     * @param packetClass la clase del paquete
     * @param handler    el consumidor que maneja el paquete
     * @return el ID del manejador registrado
     */
    public <T> long registerHandler(Class<T> packetClass, Consumer<T> handler) {
        return handlerRegistry.registerHandler(packetClass, handler);
    }

    /**
     * Desregistra un manejador por su ID.
     *
     * @param handlerId el ID del manejador retornado por registerHandler
     * @return true si el manejador fue removido
     */
    public boolean unregisterHandler(long handlerId) {
        return handlerRegistry.unregisterHandler(handlerId);
    }

    /**
     * Desregistra todos los manejadores para un tipo específico de paquete.
     * 
     * @param packetClass la clase del paquete
     * @return el número de manejadores removidos
     */
    public int unregisterAllHandlers(Class<?> packetClass) {
        return handlerRegistry.unregisterAllHandlers(packetClass);
    }

    /**
     * Limpia todos los manejadores registrados.
     */
    public void clearAllHandlers() {
        handlerRegistry.clear();
    }

    /**
     * Procesa un paquete recibido del servidor (usado para el cliente host local).
     * Esto permite que el host reciba paquetes desde el lado del servidor.
     *
     * @param packet el paquete a procesar
     */
    public void processPacket(Object packet) {
        Gdx.app.postRunnable(() -> handlerRegistry.invokeHandlers(packet));
    }

    /**
     * Registra clases adicionales de Kryo para la serialización.
     *
     * @param classes las clases a registrar
     */
    public void registerAdditionalClasses(Class<?>... classes) {
        Kryo kryo = client != null ? client.getKryo() : null;
        if (kryo != null) {
            for (Class<?> clazz : classes) {
                kryo.register(clazz);
                Gdx.app.log("NetworkClient", "Registered class: " + clazz.getName());
            }
        }
    }

    /**
     * Obtiene el ID del jugador local.
     *
     * @return el ID del jugador local
     */
    public int getMyPlayerId() {
        return myPlayerId;
    }

    /**
     * Obtiene el nombre del jugador local.
     *
     * @return el nombre del jugador local
     */
    public String getMyPlayerName() {
        return myPlayerName;
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
     * Verifica si el cliente está conectado a un servidor.
     *
     * @return true si está conectado
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Verifica si este cliente está inicializado (ya sea conectado o host local).
     *
     * @return true si está inicializado
     */
    public boolean isInitialized() {
        return myPlayerId != -1;
    }

    /**
     * Registra las clases principales de paquetes con Kryo.
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
     * Maneja la respuesta de unión del jugador desde el servidor.
     * Asigna el ID del jugador local.
     *
     * @param response la respuesta de unión
     */
    private void handlePlayerJoinResponse(Packets.PlayerJoinResponse response) {
        myPlayerId = response.playerId;
        connectedPlayers.put(myPlayerId, response.playerName);
        Gdx.app.log("NetworkClient", "Assigned player ID: " + myPlayerId + " (" + response.playerName + ")");
    }
}
