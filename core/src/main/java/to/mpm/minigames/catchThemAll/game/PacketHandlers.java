package to.mpm.minigames.catchThemAll.game;

import to.mpm.minigames.catchThemAll.entities.Duck;
import to.mpm.minigames.catchThemAll.entities.Player;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

/**
 * Network packet handlers for Catch Them All minigame.
 */
public class PacketHandlers {
    private static final float SIGNIFICANT_VELOCITY_THRESHOLD = 5f;
    private static final float NEAR_ZERO_VELOCITY_THRESHOLD = 1f;
    private static final float COLLISION_BLOCK_DURATION = 0.1f;
    
    /**
     * Client-side packet handler.
     * Receives updates from server and applies them to game state.
     */
    public static class ClientHandler implements ClientPacketHandler {
        private final GameState state;

        public ClientHandler(GameState state) {
            this.state = state;
        }

        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(
                    Packets.PlayerPosition.class,
                    Packets.PlayerJoined.class,
                    Packets.PlayerLeft.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class
            );
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerPosition position) {
                handlePlayerPosition(position);
            } else if (packet instanceof Packets.PlayerJoined joined) {
                state.createRemotePlayer(joined.playerId);
            } else if (packet instanceof Packets.PlayerLeft left) {
                state.removePlayer(left.playerId);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned spawned) {
                Duck.DuckType type = Duck.DuckType.valueOf(spawned.duckType);
                Duck duck = new Duck(spawned.duckId, spawned.x, spawned.y, type);
                state.addDuck(duck);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate update) {
                for (Duck duck : state.getDucks()) {
                    if (duck.id == update.duckId) {
                        duck.setPosition(update.x, update.y);
                        break;
                    }
                }
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved removed) {
                state.removeDuck(removed.duckId);
            } else if (packet instanceof to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate scoreUpdate) {
                state.updateScore(scoreUpdate.playerId, scoreUpdate.score);
            }
        }

        private void handlePlayerPosition(Packets.PlayerPosition packet) {
            Player player = state.getPlayers().get(packet.playerId);
            if (player == null) {
                float[] color = GameState.PLAYER_COLORS[packet.playerId % GameState.PLAYER_COLORS.length];
                player = new Player(false, packet.x, packet.y, color[0], color[1], color[2]);
                state.getPlayers().put(packet.playerId, player);
            } else {
                if (packet.playerId == state.getLocalPlayerId() && 
                    Math.abs(player.lastVelocityX) > SIGNIFICANT_VELOCITY_THRESHOLD && 
                    Math.abs(packet.lastVelocityX) < NEAR_ZERO_VELOCITY_THRESHOLD) {
                    player.blockedTimer = COLLISION_BLOCK_DURATION;
                }
                
                player.x = packet.x;
                player.y = packet.y;
                player.velocityY = packet.velocityY;
                player.lastVelocityX = packet.lastVelocityX;
                player.isGrounded = packet.isGrounded;
                player.updateBounds();
            }
        }
    }

    /**
     * Server-side packet relay.
     * Broadcasts packets to all clients except sender.
     */
    public static class ServerRelay implements ServerPacketHandler {
        @Override
        public java.util.Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return java.util.List.of(
                    Packets.PlayerPosition.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckSpawned.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckUpdate.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.DuckRemoved.class,
                    to.mpm.minigames.catchThemAll.network.CatchThemAllPackets.ScoreUpdate.class
            );
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            context.broadcastExceptSender(packet);
        }
    }
}
