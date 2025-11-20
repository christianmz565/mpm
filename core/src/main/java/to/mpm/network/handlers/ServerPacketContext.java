package to.mpm.network.handlers;

import com.esotericsoftware.kryonet.Connection;
import to.mpm.network.NetworkPacket;
import to.mpm.network.NetworkServer;
import to.mpm.network.Transports;

/**
 * Clase que contiene el contexto para el manejo de paquetes en el servidor.
 */
public class ServerPacketContext extends PacketContext {
    private final NetworkServer server; // !< instancia del servidor de red
    private final Connection connection; // !< conexión del cliente que envió el paquete

    /**
     * Construye un nuevo contexto de paquete para el servidor.
     *
     * @param server     instancia del servidor de red
     * @param connection conexión del cliente que envió el paquete
     */
    public ServerPacketContext(NetworkServer server, Connection connection) {
        this.server = server;
        this.connection = connection;
    }

    /**
     * Obtiene la instancia del servidor de red.
     *
     * @return instancia del servidor de red
     */
    public NetworkServer getServer() {
        return server;
    }

    /**
     * Obtiene la conexión del cliente que envió el paquete.
     *
     * @return conexión del cliente
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Responde al remitente usando el transporte preferido del paquete.
     * 
     * @param packet paquete de red a enviar como respuesta
     */
    public void reply(NetworkPacket packet) {
        if (packet.getTransportMode() == Transports.UDP) {
            connection.sendUDP(packet);
        } else {
            connection.sendTCP(packet);
        }
    }

    /**
     * Envía un paquete a todos los clientes conectados, incluyendo el remitente.
     * 
     * @param packet paquete de red a enviar
     */
    public void broadcast(NetworkPacket packet) {
        server.broadcast(packet);
    }

    /**
     * Envía un paquete a todos los clientes conectados, excepto el remitente.
     * 
     * @param packet paquete de red a enviar
     */
    public void broadcastExceptSender(NetworkPacket packet) {
        server.broadcastExcept(connection, packet);
    }
}
