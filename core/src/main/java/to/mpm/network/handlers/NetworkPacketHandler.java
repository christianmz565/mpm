package to.mpm.network.handlers;

import to.mpm.network.NetworkPacket;

import java.util.Collection;

/**
 * Interfaz para el manejo de paquetes de red.
 * <p>
 * Sus implementaciones declaran los paquetes en los que están interesados.
 * 
 * @param <C> el tipo de contexto de paquete manejado
 */
public interface NetworkPacketHandler<C extends PacketContext> {
    /**
     * Devuelve las clases de paquetes que este manejador puede procesar.
     * 
     * @return colección de clases de paquetes recibibles
     */
    Collection<Class<? extends NetworkPacket>> receivablePackets();

    /**
     * Maneja el paquete entrante.
     * 
     * @param context el contexto del paquete
     * @param packet  el paquete de red a manejar
     */
    void handle(C context, NetworkPacket packet);
}
