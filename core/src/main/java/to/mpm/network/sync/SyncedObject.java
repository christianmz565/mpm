package to.mpm.network.sync;

import com.badlogic.gdx.Gdx;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase base para objetos cuyos campos deben sincronizarse a través de la red.
 * Esta clase asigna un identificador único {@code objectId} a cada instancia para
 * identificarla en mensajes {@link Packets.SyncUpdate}.
 */
public class SyncedObject {
    private static final Map<Integer, SyncedObject> syncedObjects = new ConcurrentHashMap<>(); //!< Registro de todos los objetos sincronizados por id
    private static int nextObjectId = 0; //!< Contador global para asignar ids únicos a objetos
    private static long globalHandlerId = -1; //!< Handler ID for the global SyncUpdate handler
    
    private final int objectId; //!< Id único para este objeto sincronizado
    private final Map<String, Object> lastKnownValues; //!< Últimos valores conocidos de los campos sincronizados
    private boolean isLocallyOwned; //!< true si esta instancia debe enviar actualizaciones

    /**
     * Crea un nuevo objeto sincronizado.
     *
     * @param isLocallyOwned true si esta instancia debe enviar actualizaciones para sus campos anotados
     */
    public SyncedObject(boolean isLocallyOwned) {
        this.objectId = nextObjectId++;
        this.isLocallyOwned = isLocallyOwned;
        this.lastKnownValues = new HashMap<>();

        syncedObjects.put(objectId, this);

        if (globalHandlerId == -1) {
            globalHandlerId = NetworkManager.getInstance().registerHandler(
                Packets.SyncUpdate.class, 
                SyncedObject::handleGlobalSyncUpdate
            );
        }

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
    }

    /**
     * Limpia todos los objetos sincronizados.
     */
    public static void clearAll() {
        syncedObjects.clear();
        nextObjectId = 0;
        
        // Unregister the global handler
        if (globalHandlerId != -1) {
            NetworkManager.getInstance().unregisterHandler(globalHandlerId);
            globalHandlerId = -1;
        }
    }

    /**
     * Global static handler for all SyncUpdate packets.
     * Dispatches to the correct SyncedObject instance.
     *
     * @param packet the sync update packet
     */
    private static void handleGlobalSyncUpdate(Packets.SyncUpdate packet) {
        SyncedObject obj = syncedObjects.get(packet.objectId);
        if (obj != null && !obj.isLocallyOwned) {
            obj.applySyncUpdate(packet.fieldName, packet.value);
        }
    }

    /**
     * Ejecuta un ciclo de actualización, comprobando cambios en los campos sincronizados
     * y enviando actualizaciones si es necesario.
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
     * Envía una actualización de sincronización para un campo específico a los pares remotos.
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
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Ayudante para crear una copia del valor para una serialización segura.
     */
    private Object copyValue(Object value) {
        // TODO: make deep copies for mutable types if necessary
        return value;
    }

    /**
     * Devuelve el id único del objeto sincronizado.
     *
     * @return id del objeto
     */
    public int getObjectId() {
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
}
