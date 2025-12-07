package to.mpm.network;

/**
 * Pequeño contenedor para constantes relacionadas con la red.
 */
public class NetworkConfig {
    /** Puerto UDP por defecto para hospedar/unirse. */
    public static final int DEFAULT_PORT = 61232;
    /** Tiempo de espera de conexión en milisegundos. */
    public static final int TIMEOUT_MS = 5000;
    /** Tamaño del buffer UDP en bytes. */
    public static final int UDP_BUFFER_SIZE = 8192;
}
