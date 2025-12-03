package to.mpm.network;

import com.badlogic.gdx.Gdx;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinador principal de la red.
 * Maneja la lógica tanto del servidor como del cliente,
 * y proporciona una interfaz unificada para enviar paquetes.
 */
public class NetworkManager {
    /** Instancia singleton. */
    private static NetworkManager instance;
    /** Instancia del servidor de red. */
    private NetworkServer server;
    /** Instancia del cliente de red. */
    private NetworkClient client;
    /** Indica si este es el host. */
    private boolean isHost;

    /**
     * Constructor privado para el singleton.
     */
    private NetworkManager() {
    }

    /**
     * Devuelve la instancia singleton de NetworkManager.
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
     * Inicia un juego como host con un nombre de jugador personalizado.
     * <p>
     * Crea tanto un NetworkServer como un NetworkClient (para el host).
     *
     * @param hostPlayerName el nombre del jugador host
     * @param port           el puerto en el que escuchar
     * @throws IOException si el servidor no puede iniciarse
     */
    public void hostGame(String hostPlayerName, int port) throws IOException {
        if (server != null) {
            Gdx.app.log("NetworkManager", "Server is already running");
            return;
        }

        isHost = true;

        server = new NetworkServer();
        server.start(port);

        client = new NetworkClient();
        client.connect("127.0.0.1", port, hostPlayerName);

        Gdx.app.log("NetworkManager", "Hosting game on port " + port);
    }

    /**
     * Se une a un juego como cliente.
     *
     * @param host       la dirección del servidor
     * @param port       el puerto del servidor
     * @param playerName el nombre del jugador local
     * @throws IOException si la conexión falla
     */
    public void joinGame(String host, int port, String playerName) throws IOException {
        if (client != null && client.isConnected()) {
            Gdx.app.log("NetworkManager", "Already connected to a game");
            return;
        }

        isHost = false;

        client = new NetworkClient();
        client.connect(host, port, playerName);

        Gdx.app.log("NetworkManager", "Joined game at " + host + ":" + port);
    }

    /**
     * Envía un paquete a través de la red.
     * <p>
     * Si es host, envía a todos los clientes.
     * <p>
     * Si es cliente, envía al servidor.
     * 
     * @param packet el paquete a enviar
     */
    public void sendPacket(NetworkPacket packet) {
        if (client != null && client.isConnected()) {
            client.send(packet);
        }
    }

    /**
     * Asegura que el cliente haya recibido un ID solicitando nuevamente el join si
     * es necesario.
     */
    public void ensureJoinHandshake() {
        if (client != null) {
            client.resendJoinRequestIfNeeded();
        }
    }

    /**
     * Permite que el host envíe un paquete directamente desde el servidor.
     * 
     * @param packet el paquete a enviar
     */
    public void broadcastFromHost(NetworkPacket packet) {
        if (isHost && server != null && server.isRunning()) {
            server.broadcast(packet);
        } else {
            sendPacket(packet);
        }
    }

    /**
     * Registra un handler del lado del cliente.
     * 
     * @param handler el handler a registrar
     */
    public void registerClientHandler(ClientPacketHandler handler) {
        if (client != null) {
            client.registerHandler(handler);
        }
    }

    /**
     * Desregistra un handler del lado del cliente.
     * 
     * @param handler el handler a desregistrar
     */
    public void unregisterClientHandler(ClientPacketHandler handler) {
        if (client != null) {
            client.unregisterHandler(handler);
        }
    }

    /**
     * Registra un handler del lado del servidor (solo si somos host).
     * 
     * @param handler el handler a registrar
     */
    public void registerServerHandler(ServerPacketHandler handler) {
        if (server != null) {
            server.registerHandler(handler);
        }
    }

    /**
     * Desregistra un handler del lado del servidor.
     * 
     * @param handler el handler a desregistrar
     */
    public void unregisterServerHandler(ServerPacketHandler handler) {
        if (server != null) {
            server.unregisterHandler(handler);
        }
    }

    /**
     * Registra clases adicionales de Kryo para la serialización.
     * 
     * @param classes las clases a registrar
     */
    public void registerAdditionalClasses(Class<?>... classes) {
        if (server != null) {
            server.registerAdditionalClasses(classes);
        }
        if (client != null) {
            client.registerAdditionalClasses(classes);
        }
    }

    /**
     * Desconecta de la red y limpia los recursos.
     */
    public void disconnect() {
        if (server != null) {
            server.stop();
            server = null;
        }

        if (client != null) {
            client.disconnect();
            client = null;
        }

        isHost = false;
        Gdx.app.log("NetworkManager", "Disconnected");
    }

    /**
     * Verifica si este administrador de red es el host.
     * 
     * @return true si es host
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Obtiene el ID del jugador local.
     *
     * @return el ID del jugador local, o -1 si no está conectado
     */
    public int getMyId() {
        return client != null ? client.getMyPlayerId() : -1;
    }

    /**
     * Verifica si hay una conexión activa.
     *
     * @return true si está conectado
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Obtiene el mapa de jugadores conectados.
     *
     * @return mapa de ID de jugador a nombre de jugador
     */
    public ConcurrentHashMap<Integer, String> getConnectedPlayers() {
        if (client != null) {
            return client.getConnectedPlayers();
        }
        return new ConcurrentHashMap<>();
    }

    /**
     * Obtiene el número de jugadores conectados.
     *
     * @return número de jugadores
     */
    public int getPlayerCount() {
        return getConnectedPlayers().size();
    }
}
