package to.mpm.network;

/**
 * Clase base para paquetes de red.
 */
public abstract class NetworkPacket {
    /** Modo de transporte preferido. */
    private Transports transportMode = Transports.TCP;

    /**
     * Constructor protegido para permitir la extensión.
     */
    protected NetworkPacket() {
    }

    /**
     * Devuelve el modo de transporte preferido para el paquete.
     * 
     * @return modo de transporte preferido
     */
    public Transports getTransportMode() {
        return transportMode;
    }

    /**
     * Permite a las subclases anular la preferencia de transporte predeterminada.
     * 
     * @param transport el modo de transporte preferido
     */
    protected void preferTransport(Transports transport) {
        this.transportMode = transport;
    }
}
