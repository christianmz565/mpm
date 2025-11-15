package to.mpm.network;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Coordinador principal de la red.
 * Maneja la lógica tanto del servidor como del cliente,
 * y proporciona una interfaz unificada para enviar paquetes
 */
public class NetworkManager {
    private static NetworkManager instance; //!< instancia singleton
    
    private NetworkServer server; //!< instancia del servidor de red
    private NetworkClient client; //!< instancia del cliente de red
    private GlobalPacketHandlers globalHandlers; //!< manejadores globales de paquetes
    private boolean isHost; //!< indica si este es el host

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
     * Crea tanto un NetworkServer como un NetworkClient (para el host).
     *
     * @param hostPlayerName el nombre del jugador host
     * @param port el puerto en el que escuchar
     * @throws IOException si el servidor no puede iniciarse
     */
    public void hostGame(String hostPlayerName, int port) throws IOException {
        if (server != null) {
            Gdx.app.log("NetworkManager", "Server is already running");
            return;
        }

        isHost = true;

        server = new NetworkServer();
        server.start(port, hostPlayerName);

        server.setPacketReceivedCallback((packet, connection) -> {
            if (client != null) {
                client.processPacket(packet);
            }
        });

        client = new NetworkClient();
        client.connectAsLocalHost(server.getHostPlayerId(), hostPlayerName);

        globalHandlers = new GlobalPacketHandlers(client);
        globalHandlers.registerHandlers();

        Gdx.app.log("NetworkManager", "Hosting game as player ID: " + server.getHostPlayerId());
    }

    /**
     * Se une a un juego como cliente.
     *
     * @param host la dirección del servidor
     * @param port el puerto del servidor
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

        globalHandlers = new GlobalPacketHandlers(client);
        globalHandlers.registerHandlers();

        Gdx.app.log("NetworkManager", "Joined game at " + host + ":" + port);
    }

    /**
     * Envía un paquete a través de la red.
     * Si es host, envía a todos los clientes.
     * Si es cliente, envía al servidor.
     * 
     * @param packet el paquete a enviar
     */
    public void sendPacket(Object packet) {
        if (isHost && server != null) {
            server.sendToAllUDP(packet);
        } else if (client != null && client.isConnected()) {
            client.sendUDP(packet);
        }
    }

    /**
     * Registra un manejador de paquetes para una clase de paquete específica.
     * 
     * @param <T> la clase de paquete
     * @param packetClass la clase del paquete
     * @param handler el manejador de paquetes
     * @return el ID del manejador registrado
     */
    public <T> long registerHandler(Class<T> packetClass, Consumer<T> handler) {
        if (client != null) {
            return client.registerHandler(packetClass, handler);
        }
        return -1;
    }

    /**
     * Desregistra un manejador de paquetes por su ID.
     * 
     * @param handlerId el ID del manejador
     * @return true si se desregistró con éxito
     */
    public boolean unregisterHandler(long handlerId) {
        if (client != null) {
            return client.unregisterHandler(handlerId);
        }
        return false;
    }

    /**
     * Desregistra todos los manejadores para una clase de paquete específica.
     * 
     * @param packetClass la clase del paquete
     * @return el número de manejadores desregistrados
     */
    public int unregisterAllHandlers(Class<?> packetClass) {
        if (client != null) {
            return client.unregisterAllHandlers(packetClass);
        }
        return 0;
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
        if (globalHandlers != null) {
            globalHandlers.unregisterHandlers();
            globalHandlers = null;
        }
        
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
        if (isHost) {
            return server != null && server.isRunning();
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
