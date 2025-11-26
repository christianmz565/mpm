package to.mpm.network;

/**
 * Pequeño contenedor para constantes relacionadas con la red.
 */
public class NetworkConfig {
    public static final int DEFAULT_PORT = 61232; // !< puerto UDP por defecto para hospedar/unirse
    public static final int TIMEOUT_MS = 5000; // !< tiempo de espera de conexión en milisegundos
    public static final int UDP_BUFFER_SIZE = 8192; // !< tamaño del buffer UDP en bytes
}
