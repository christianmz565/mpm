package to.mpm.network.handlers;

import to.mpm.network.NetworkClient;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Transports;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase que contiene el contexto para el manejo de paquetes en el cliente.
 */
public class ClientPacketContext extends PacketContext {
    private final NetworkClient client; // !< instancia del cliente de red

    /**
     * Construye un nuevo contexto de paquete para el cliente.
     *
     * @param client instancia del cliente de red
     */
    public ClientPacketContext(NetworkClient client) {
        this.client = client;
    }

    /**
     * Obtiene la instancia del cliente de red.
     *
     * @return instancia del cliente de red
     */
    public NetworkClient getClient() {
        return client;
    }

    /**
     * Obtiene el ID del jugador local.
     *
     * @return ID del jugador local
     */
    public int getLocalPlayerId() {
        return client.getMyPlayerId();
    }

    /**
     * Obtiene un mapa de los jugadores conectados.
     *
     * @return mapa de IDs de jugadores a nombres
     */
    public ConcurrentHashMap<Integer, String> getConnectedPlayers() {
        return client.getConnectedPlayers();
    }

    /**
     * Envía un paquete al servidor respetando su transporte preferido.
     * 
     * @param packet paquete de red a enviar
     */
    public void send(NetworkPacket packet) {
        if (packet.getTransportMode() == Transports.UDP) {
            client.sendUDP(packet);
        } else {
            client.sendTCP(packet);
        }
    }
}
