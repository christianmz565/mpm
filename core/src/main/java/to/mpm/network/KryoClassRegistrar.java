package to.mpm.network;

import com.esotericsoftware.kryo.Kryo;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import to.mpm.minigames.duckshooter.network.DuckShooterPackets;

import java.util.UUID;

/**
 * Registrador de clases Kryo para la serialización de paquetes de red y otros
 * objetos.
 */
public final class KryoClassRegistrar {
    /**
     * Constructor por defecto privado para evitar instanciación.
     */
    private KryoClassRegistrar() {
    }

    /**
     * Registra las clases centrales utilizadas en la red.
     * 
     * @param kryo la instancia de Kryo donde se registran las clases
     */
    public static void registerCoreClasses(Kryo kryo) {
        kryo.register(NetworkPacket.class);
        kryo.register(Transports.class);
        kryo.register(UUID.class, new UUIDSerializer());

        kryo.register(Packets.PlayerJoinRequest.class);
        kryo.register(Packets.PlayerJoined.class);
        kryo.register(Packets.PlayerLeft.class);
        kryo.register(Packets.StartGame.class);
        kryo.register(Packets.SyncUpdate.class);
        kryo.register(Packets.SyncedObjectCreated.class);
        kryo.register(Packets.PlayerPosition.class);
        kryo.register(Packets.SpectatorStatus.class);
        kryo.register(Packets.RPC.class);
        kryo.register(Packets.Ping.class);
        kryo.register(Packets.Pong.class);
        kryo.register(Object[].class);

        // Duck Shooter packets
        kryo.register(DuckShooterPackets.DuckState.class);
        kryo.register(DuckShooterPackets.ShootQuack.class);
        kryo.register(DuckShooterPackets.QuackHit.class);
        kryo.register(DuckShooterPackets.DuckEliminated.class);
        kryo.register(DuckShooterPackets.GameEnd.class);
    }

    /**
     * Serializador personalizado para la clase UUID.
     */
    private static final class UUIDSerializer extends Serializer<UUID> {
        @Override
        public void write(Kryo kryo, Output output, UUID object) {
            output.writeLong(object.getMostSignificantBits());
            output.writeLong(object.getLeastSignificantBits());
        }

        @Override
        public UUID read(Kryo kryo, Input input, Class<UUID> type) {
            long most = input.readLong();
            long least = input.readLong();
            return new UUID(most, least);
        }
    }
}
