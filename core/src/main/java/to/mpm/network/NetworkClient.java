package to.mpm.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lógica del lado del cliente de red.
 * <p>
 * Maneja la conexión al servidor, el envío y recepción de paquetes,
 * y el registro de manejadores de paquetes.
 */
public class NetworkClient {
    private Client client; //!< instancia del cliente KryoNet
    private final Map<Class<? extends NetworkPacket>, CopyOnWriteArrayList<ClientPacketHandler>> handlers; //!<
                                                                                                           // handlers
                                                                                                           // por
                                                                                                           // paquete
    private final ConcurrentHashMap<Integer, String> connectedPlayers; //!< mapa de jugadores conectados (ID -> nombre)
    private final ClientPacketContext clientContext; //!< contexto compartido para handlers
    private int myPlayerId = -1; //!< ID del jugador local
    private String myPlayerName; //!< nombre del jugador local
    private String pendingJoinCorrelationId; //!< correlación para detectar la asignación local

    /**
     * Construye una nueva instancia del cliente de red.
     */
    public NetworkClient() {
        handlers = new ConcurrentHashMap<>();
        connectedPlayers = new ConcurrentHashMap<>();
        clientContext = new ClientPacketContext(this);
    }

    /**
     * Conecta a un servidor como cliente.
     *
     * @param host       la dirección del servidor
     * @param port       el puerto del servidor
     * @param playerName el nombre del jugador local
     * @throws IOException si la conexión falla
     */
    public void connect(String host, int port, String playerName) throws IOException {
        if (client != null && client.isConnected()) {
            Gdx.app.log("NetworkClient", "Already connected to a server");
            return;
        }

        myPlayerName = playerName != null ? playerName : "Player";
        pendingJoinCorrelationId = UUID.randomUUID().toString();

        client = new Client(NetworkConfig.UDP_BUFFER_SIZE, NetworkConfig.UDP_BUFFER_SIZE);
        KryoClassRegistrar.registerCoreClasses(client.getKryo());

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof NetworkPacket packet) {
                    handleInternalPacket(packet);
                    Gdx.app.postRunnable(() -> dispatchPacket(packet));
                }
            }

            @Override
            public void disconnected(Connection connection) {
                Gdx.app.log("NetworkClient", "Disconnected from server");
            }
        });

        client.start();
        client.connect(NetworkConfig.TIMEOUT_MS, host, port, port);

        sendJoinRequest();

        Gdx.app.log("NetworkClient", "Connected to " + host + ":" + port);
    }

    /**
     * Desconecta del servidor y limpia los recursos.
     */
    public void disconnect() {
        if (client != null) {
            client.stop();
            client.close();
            client = null;
        }
        myPlayerId = -1;
        pendingJoinCorrelationId = null;
        connectedPlayers.clear();
        handlers.clear();
        Gdx.app.log("NetworkClient", "Client disconnected");
    }

    /**
     * Envía un paquete al servidor vía TCP.
     *
     * @param packet el paquete a enviar
     */
    public void sendTCP(NetworkPacket packet) {
        if (client != null && client.isConnected()) {
            client.sendTCP(packet);
        }
    }

    /**
     * Envía un paquete al servidor vía UDP.
     *
     * @param packet el paquete a enviar
     */
    public void sendUDP(NetworkPacket packet) {
        if (client != null && client.isConnected()) {
            client.sendUDP(packet);
        }
    }

    /**
     * Envía el paquete usando su modo preferido.
     * 
     * @param packet paquete de red a enviar
     */
    public void send(NetworkPacket packet) {
        if (packet.getTransportMode() == Transports.UDP) {
            sendUDP(packet);
        } else {
            sendTCP(packet);
        }
    }

    /**
     * Reenvía la solicitud de unión si aún no se recibió un ID.
     */
    public void resendJoinRequestIfNeeded() {
        if (client != null && client.isConnected() && !isInitialized()) {
            sendJoinRequest();
        }
    }

    /**
     * Registra un manejador basado en clases de paquete.
     * 
     * @param handler el manejador a registrar
     */
    public void registerHandler(ClientPacketHandler handler) {
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
    public void unregisterHandler(ClientPacketHandler handler) {
        for (List<ClientPacketHandler> handlerList : handlers.values()) {
            handlerList.remove(handler);
        }
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
     * Verifica si este cliente está inicializado.
     *
     * @return true si está inicializado
     */
    public boolean isInitialized() {
        return myPlayerId != -1;
    }

    /**
     * Maneja paquetes internos relacionados con la gestión de jugadores.
     *
     * @param packet el paquete de red recibido
     */
    private void handleInternalPacket(NetworkPacket packet) {
        if (packet instanceof Packets.PlayerJoined joined) {
            connectedPlayers.put(joined.playerId, joined.playerName);
            if (pendingJoinCorrelationId != null && pendingJoinCorrelationId.equals(joined.correlationId)) {
                myPlayerId = joined.playerId;
                pendingJoinCorrelationId = null;
                Gdx.app.log("NetworkClient", "Assigned player ID: " + myPlayerId + " (" + joined.playerName + ")");
            }
        } else if (packet instanceof Packets.PlayerLeft left) {
            connectedPlayers.remove(left.playerId);
            if (left.playerId == myPlayerId) {
                myPlayerId = -1;
            }
        }
    }

    /**
     * Reenvía un paquete a los manejadores registrados.
     *
     * @param packet el paquete de red a reenvíar
     */
    private void dispatchPacket(NetworkPacket packet) {
        List<ClientPacketHandler> handlerList = handlers.get(packet.getClass());
        if (handlerList == null || handlerList.isEmpty()) {
            return;
        }
        for (ClientPacketHandler handler : handlerList) {
            try {
                handler.handle(clientContext, packet);
            } catch (Exception ex) {
                Gdx.app.error("NetworkClient", "Handler error for packet " + packet.getClass().getSimpleName(), ex);
            }
        }
    }

    /**
     * Envía una solicitud de unión al servidor.
     */
    private void sendJoinRequest() {
        if (client == null || !client.isConnected()) {
            return;
        }
        pendingJoinCorrelationId = UUID.randomUUID().toString();
        Packets.PlayerJoinRequest joinRequest = new Packets.PlayerJoinRequest();
        joinRequest.playerName = myPlayerName != null ? myPlayerName : "Player";
        joinRequest.correlationId = pendingJoinCorrelationId;
        sendTCP(joinRequest);
    }
}
