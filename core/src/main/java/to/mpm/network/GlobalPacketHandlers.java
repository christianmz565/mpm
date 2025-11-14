package to.mpm.network;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

/**
 * Global packet handlers that are common to all game states.
 * These handlers manage player connections and disconnections.
 * Registers itself with the NetworkClient and can be cleanly disposed.
 */
public class GlobalPacketHandlers {
    private final NetworkClient client; //!< instancia del cliente de red
    private final List<Long> handlerIds; //!< lista de IDs de manejadores registrados

    /**
     * Construye un nuevo conjunto de manejadores globales.
     *
     * @param client instancia del cliente de red
     */
    public GlobalPacketHandlers(NetworkClient client) {
        this.client = client;
        this.handlerIds = new ArrayList<>();
    }

    /**
     * Registra todos los manejadores globales en el cliente.
     */
    public void registerHandlers() {
        handlerIds.add(client.registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined));
        handlerIds.add(client.registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft));
        
        Gdx.app.log("GlobalPacketHandlers", "Global handlers registered");
    }

    /**
     * Desregistra todos los manejadores globales del cliente.
     */
    public void unregisterHandlers() {
        for (long handlerId : handlerIds) {
            client.unregisterHandler(handlerId);
        }
        handlerIds.clear();
        Gdx.app.log("GlobalPacketHandlers", "Global handlers unregistered");
    }

    /**
     * Maneja un paquete de jugador unido.
     * Agrega el jugador a la lista de jugadores conectados.
     * 
     * @param packet el paquete de jugador unido
     */
    private void onPlayerJoined(Packets.PlayerJoined packet) {
        client.getConnectedPlayers().put(packet.playerId, packet.playerName);
        Gdx.app.log("GlobalPacketHandlers", "Player joined: " + packet.playerName + " (ID: " + packet.playerId + ")");
    }

    /**
     * Maneja un paquete de jugador salido.
     * Elimina el jugador de la lista de jugadores conectados.
     * 
     * @param packet el paquete de jugador salido
     */
    private void onPlayerLeft(Packets.PlayerLeft packet) {
        String playerName = client.getConnectedPlayers().remove(packet.playerId);
        if (playerName != null) {
            Gdx.app.log("GlobalPacketHandlers", "Player left: " + playerName + " (ID: " + packet.playerId + ")");
        }
    }
}
