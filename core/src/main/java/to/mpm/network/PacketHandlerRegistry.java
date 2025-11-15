package to.mpm.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registro que maneja los registradores de paquetes.
 * Permite registrar y desregistrar manejadores para diferentes clases de paquetes,
 * y invocar los manejadores apropiados cuando se recibe un paquete.
 */
public class PacketHandlerRegistry {
    private final ConcurrentHashMap<Class<?>, List<HandlerEntry<?>>> handlers; //!< mapa de clases de paquetes a listas de manejadores
    private long nextHandlerId = 0; //!< contador para asignar IDs únicos a los manejadores

    /**
     * Construye un nuevo registro de manejadores de paquetes.
     */
    public PacketHandlerRegistry() {
        handlers = new ConcurrentHashMap<>();
    }

    /**
     * Registra un manejador para una clase de paquete específica.
     * Devuelve un ID de manejador que se puede usar para desregistrar el manejador más tarde.
     *
     * @param packetClass la clase de paquete a manejar
     * @param handler el consumidor manejador
     * @param <T> el tipo de paquete
     * @return ID del manejador para su eliminación posterior
     */
    public <T> long registerHandler(Class<T> packetClass, Consumer<T> handler) {
        long handlerId = nextHandlerId++;
        
        handlers.computeIfAbsent(packetClass, k -> new ArrayList<>())
                .add(new HandlerEntry<>(handlerId, handler));
        
        return handlerId;
    }

    /**
     * Desregistra un manejador por su ID.
     *
     * @param handlerId el ID del manejador devuelto por registerHandler
     * @return true si el manejador fue encontrado y eliminado, false de lo contrario
     */
    public boolean unregisterHandler(long handlerId) {
        for (List<HandlerEntry<?>> handlerList : handlers.values()) {
            if (handlerList.removeIf(entry -> entry.id == handlerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Desregistra todos los manejadores para una clase de paquete específica.
     *
     * @param packetClass la clase de paquete
     * @return número de manejadores eliminados
     */
    public int unregisterAllHandlers(Class<?> packetClass) {
        List<HandlerEntry<?>> handlerList = handlers.remove(packetClass);
        return handlerList != null ? handlerList.size() : 0;
    }

    /**
     * Invoca todos los manejadores registrados para el paquete dado.
     *
     * @param packet el paquete a manejar
     */
    @SuppressWarnings("unchecked")
    public void invokeHandlers(Object packet) {
        List<HandlerEntry<?>> handlerList = handlers.get(packet.getClass());
        if (handlerList != null) {
            List<HandlerEntry<?>> copy = new ArrayList<>(handlerList);
            for (HandlerEntry<?> entry : copy) {
                try {
                    ((Consumer<Object>) entry.handler).accept(packet);
                } catch (Exception e) {
                    System.err.println("Error in packet handler: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Limpia todos los manejadores registrados.
     */
    public void clear() {
        handlers.clear();
    }

    /**
     * Obtiene el número de manejadores registrados para una clase de paquete específica.
     *
     * @param packetClass la clase de paquete
     * @return número de manejadores
     */
    public int getHandlerCount(Class<?> packetClass) {
        List<HandlerEntry<?>> handlerList = handlers.get(packetClass);
        return handlerList != null ? handlerList.size() : 0;
    }

    /**
     * Clase interna para almacenar entradas de manejadores con sus IDs.
     */
    private static class HandlerEntry<T> {
        final long id;
        final Consumer<T> handler;

        HandlerEntry(long id, Consumer<T> handler) {
            this.id = id;
            this.handler = handler;
        }
    }
}
