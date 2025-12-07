package to.mpm.network.sync;

import com.badlogic.gdx.Gdx;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase base para objetos cuyos campos deben sincronizarse a través de la red.
 * <p>
 * Esta clase asigna un identificador único {@code objectId} a cada instancia
 * para identificarla en mensajes {@link Packets.SyncUpdate}.
 */
public class SyncedObject {
    /** Registro de todos los objetos sincronizados por ID. */
    private static final Map<UUID, SyncedObject> syncedObjects = new ConcurrentHashMap<>();
    /** Handler global del lado del cliente. */
    private static ClientPacketHandler clientHandler;
    /** Handler global del lado del servidor. */
    private static ServerPacketHandler serverHandler;

    /** ID único para este objeto sincronizado. */
    private final UUID objectId;
    /** Últimos valores conocidos de los campos sincronizados. */
    private final Map<String, Object> lastKnownValues;
    /** True si esta instancia debe enviar actualizaciones. */
    private boolean isLocallyOwned;

    /**
     * Crea un nuevo objeto sincronizado.
     *
     * @param isLocallyOwned true si esta instancia debe enviar actualizaciones para
     *                       sus campos anotados
     */
    public SyncedObject(boolean isLocallyOwned) {
        this.objectId = UUID.randomUUID();
        this.isLocallyOwned = isLocallyOwned;
        this.lastKnownValues = new HashMap<>();

        syncedObjects.put(objectId, this);
        ensureHandlersRegistered();

        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Synchronized.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    lastKnownValues.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    Gdx.app.error("SyncedObject", "No se pudo leer el campo: " + field.getName(), e);
                }
            }
        }

        if (isLocallyOwned) {
            announceCreation();
        }
    }

    /**
     * Limpia todos los objetos sincronizados.
     */
    public static void clearAll() {
        syncedObjects.clear();
        NetworkManager nm = NetworkManager.getInstance();
        if (clientHandler != null) {
            nm.unregisterClientHandler(clientHandler);
            clientHandler = null;
        }
        if (serverHandler != null) {
            nm.unregisterServerHandler(serverHandler);
            serverHandler = null;
        }
    }

    /**
     * Manejador estático global para todos los paquetes SyncUpdate.
     * <p>
     * Envía al objeto SyncedObject correcto.
     * 
     * @param packet el paquete de actualización de sincronización
     */
    private static void handleGlobalSyncUpdate(Packets.SyncUpdate packet) {
        SyncedObject obj = syncedObjects.get(packet.objectId);
        if (obj != null && !obj.isLocallyOwned) {
            obj.applySyncUpdate(packet.fieldName, packet.value);
        }
    }

    /**
     * Ejecuta un ciclo de actualización, comprobando cambios en los campos
     * sincronizados y enviando actualizaciones si es necesario.
     */
    public void update() {
        if (!isLocallyOwned) {
            return;
        }

        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Synchronized.class)) {
                field.setAccessible(true);
                try {
                    Object currentValue = field.get(this);
                    Object lastValue = lastKnownValues.get(field.getName());

                    if (!valueEquals(currentValue, lastValue)) {
                        lastKnownValues.put(field.getName(), currentValue);
                        sendSyncUpdate(field.getName(), currentValue);
                    }
                } catch (IllegalAccessException e) {
                    Gdx.app.error("SyncedObject", "No se pudo leer el campo: " + field.getName(), e);
                }
            }
        }
    }

    /**
     * Envía una actualización de sincronización para un campo específico a los
     * pares remotos.
     *
     * @param fieldName nombre del campo que cambió
     * @param value     nuevo valor del campo
     */
    private void sendSyncUpdate(String fieldName, Object value) {
        Packets.SyncUpdate packet = new Packets.SyncUpdate();
        packet.objectId = this.objectId;
        packet.fieldName = fieldName;
        packet.value = copyValue(value);

        NetworkManager.getInstance().sendPacket(packet);
    }

    /**
     * Aplica una actualización de sincronización a un campo local.
     *
     * @param fieldName nombre del campo a actualizar
     * @param value     nuevo valor a asignar
     */
    private void applySyncUpdate(String fieldName, Object value) {
        try {
            Field field = getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
            lastKnownValues.put(fieldName, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Gdx.app.error("SyncedObject", "No se pudo aplicar la actualización de sincronización: " + fieldName, e);
        }
    }

    /**
     * Ayudante para comparar valores, manejando nulos.
     *
     * @param a primer valor
     * @param b segundo valor
     * @return true si los valores son iguales
     */
    private boolean valueEquals(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        return a.equals(b);
    }

    /**
     * Ayudante para crear una copia del valor para una serialización segura.
     */
    private Object copyValue(Object value) {
        return value;
    }

    /**
     * Devuelve el id único del objeto sincronizado.
     *
     * @return id del objeto
     */
    public UUID getObjectId() {
        return objectId;
    }

    /**
     * Indica si esta instancia es de propiedad local y envía actualizaciones.
     *
     * @return true si es propiedad local
     */
    public boolean isLocallyOwned() {
        return isLocallyOwned;
    }

    /**
     * Cambia la propiedad de este objeto sincronizado.
     *
     * @param locallyOwned true para marcar esta instancia como propiedad local
     *                     y que envíe actualizaciones
     */
    public void setLocallyOwned(boolean locallyOwned) {
        this.isLocallyOwned = locallyOwned;
    }

    /**
     * Limpia y elimina este objeto del registro global.
     */
    public void dispose() {
        syncedObjects.remove(objectId);
    }

    /**
     * Anuncia la creación de este objeto sincronizado a los pares remotos.
     */
    private void announceCreation() {
        Packets.SyncedObjectCreated created = new Packets.SyncedObjectCreated();
        created.objectId = objectId;
        created.objectType = getClass().getSimpleName();
        NetworkManager.getInstance().sendPacket(created);
    }

    /**
     * Asegura que los manejadores globales estén registrados.
     */
    private void ensureHandlersRegistered() {
        NetworkManager nm = NetworkManager.getInstance();
        if (clientHandler == null) {
            clientHandler = new SyncClientHandler();
            nm.registerClientHandler(clientHandler);
        }
        if (serverHandler == null && nm.isHost()) {
            serverHandler = new SyncServerRelay();
            nm.registerServerHandler(serverHandler);
        }
    }

    /**
     * Manejador de paquetes del lado del cliente para actualizaciones de
     * sincronización.
     */
    private static final class SyncClientHandler implements ClientPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(Packets.SyncUpdate.class, Packets.SyncedObjectCreated.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.SyncUpdate update) {
                handleGlobalSyncUpdate(update);
            } else if (packet instanceof Packets.SyncedObjectCreated created) {
                Gdx.app.log("SyncedObject",
                        "Remote object announced: " + created.objectId + " (" + created.objectType + ")");
            }
        }
    }

    /**
     * Manejador de paquetes del lado del servidor para retransmitir actualizaciones
     * de sincronización.
     */
    private static final class SyncServerRelay implements ServerPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(Packets.SyncUpdate.class, Packets.SyncedObjectCreated.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            context.broadcastExceptSender(packet);
        }
    }
}
