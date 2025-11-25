package to.mpm.minigames.sumo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.Minigame;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumoMinigame implements Minigame {
    // Configuración del mapa
    private static final float MAP_CENTER_X = 320f;
    private static final float MAP_CENTER_Y = 240f;
    private static final float MAP_RADIUS = 200f; 
    
    private final int localPlayerId;
    private final IntMap<SumoPlayer> players = new IntMap<>();
    private boolean finished = false;
    private int winnerId = -1;
    private static final Color[] COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE};

    // Handlers
    private SumoClientHandler clientHandler;
    private SumoServerHandler serverHandler;

    public SumoMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        // Registrar paquetes
        nm.registerAdditionalClasses(
            SumoPackets.PlayerKnockback.class,
            SumoPackets.PlayerFell.class,
            SumoPackets.GameEnd.class
        );

        if (localPlayerId != -1)
            spawnPlayer(localPlayerId);

        clientHandler = new SumoClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverHandler = new SumoServerHandler();
            nm.registerServerHandler(serverHandler);
        }
        
        Gdx.app.log("Sumo", "Juego iniciado. ID: " + localPlayerId);
    }

    private void spawnPlayer(int id) {
        float x = MAP_CENTER_X + (float)Math.cos(id) * 50;
        float y = MAP_CENTER_Y + (float)Math.sin(id) * 50;
        Color c = COLORS[id % COLORS.length];
        players.put(id, new SumoPlayer(id, x, y, c));
    }

    @Override
    public void update(float delta) {
        for (IntMap.Entry<SumoPlayer> entry : players) {
            entry.value.update(delta);
        }

        if (NetworkManager.getInstance().isHost() && !finished) {
            checkCollisions();
            checkFallout();
            checkWinCondition();
        }

        if (localPlayerId != -1) {
            SumoPlayer me = players.get(localPlayerId);
            if (me != null && me.isAlive) {
                Packets.PlayerPosition p = new Packets.PlayerPosition();
                p.playerId = localPlayerId;
                p.x = me.position.x;
                p.y = me.position.y;
                NetworkManager.getInstance().sendPacket(p);
            }
        }
    }

     private void checkCollisions() {
        java.util.List<SumoPlayer> playerList = new java.util.ArrayList<>();
        for (IntMap.Entry<SumoPlayer> entry : players) {
            playerList.add(entry.value);
        }

        for (SumoPlayer p1 : playerList) {
            for (SumoPlayer p2 : playerList) {
                
                if (p1.id == p2.id || !p1.isAlive || !p2.isAlive) continue;

                float dist = p1.position.dst(p2.position);
                if (dist < SumoPlayer.RADIUS * 2) {
                    Vector2 dir = new Vector2(p2.position).sub(p1.position).nor();
                    float force = 300f; // Fuerza del empuje

                    // Aplicar fuerza a P2
                    p2.velocity.add(dir.x * force, dir.y * force);
                    
                    // Enviar paquete de knockback
                    SumoPackets.PlayerKnockback pkt = new SumoPackets.PlayerKnockback();
                    pkt.playerId = p2.id;
                    pkt.velocityX = p2.velocity.x;
                    pkt.velocityY = p2.velocity.y;
                    NetworkManager.getInstance().broadcastFromHost(pkt);
                }
            }
        }
    }

    private void checkFallout() {
        for (IntMap.Entry<SumoPlayer> entry : players) {
            SumoPlayer p = entry.value;
            if (!p.isAlive) continue;

            float distFromCenter = p.position.dst(MAP_CENTER_X, MAP_CENTER_Y);
            if (distFromCenter > MAP_RADIUS) {
                p.isAlive = false;
                SumoPackets.PlayerFell pkt = new SumoPackets.PlayerFell();
                pkt.playerId = p.id;
                NetworkManager.getInstance().broadcastFromHost(pkt);
            }
        }
    }
    
    private void checkWinCondition() {
        int aliveCount = 0;
        int lastAliveId = -1;
        
        for (IntMap.Entry<SumoPlayer> entry : players) {
            if (entry.value.isAlive) {
                aliveCount++;
                lastAliveId = entry.key;
            }
        }
        
        if (aliveCount <= 1 && players.size > 1) {
            finished = true;
            winnerId = lastAliveId; 
            SumoPackets.GameEnd pkt = new SumoPackets.GameEnd();
            pkt.winnerId = winnerId;
            NetworkManager.getInstance().broadcastFromHost(pkt);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Agua (Fondo)
        shapeRenderer.setColor(0, 0.5f, 1, 1); 
        shapeRenderer.rect(0, 0, 640, 480);
        
        // Arena
        shapeRenderer.setColor(new Color(0.96f, 0.87f, 0.70f, 1f));
        shapeRenderer.circle(MAP_CENTER_X, MAP_CENTER_Y, MAP_RADIUS);
        
        for (IntMap.Entry<SumoPlayer> entry : players) {
            SumoPlayer p = entry.value;
            if (p.isAlive) {
                shapeRenderer.setColor(p.color);
                shapeRenderer.circle(p.position.x, p.position.y, SumoPlayer.RADIUS);
                shapeRenderer.setColor(Color.BLACK);
                shapeRenderer.circle(p.position.x + 5, p.position.y + 5, 3);
            }
        }
        shapeRenderer.end();
    }

    @Override
    public void handleInput(float delta) {
        SumoPlayer me = players.get(localPlayerId);
        if (me == null || !me.isAlive) return;

        float speed = 200f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) me.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) me.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) me.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) me.position.x += speed;
    }

    @Override
    public boolean isFinished() { return finished; }
    @Override
    public Map<Integer, Integer> getScores() { return new HashMap<>(); }
    @Override
    public int getWinnerId() { return winnerId; }
    @Override
    public void resize(int w, int h) {}

    @Override
    public void dispose() {
        NetworkManager nm = NetworkManager.getInstance();
        if (clientHandler != null) nm.unregisterClientHandler(clientHandler);
        if (serverHandler != null) nm.unregisterServerHandler(serverHandler);
    }

    private class SumoClientHandler implements ClientPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(
                Packets.PlayerPosition.class,
                SumoPackets.PlayerKnockback.class,
                SumoPackets.PlayerFell.class,
                SumoPackets.GameEnd.class,
                Packets.PlayerJoined.class
            );
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerJoined p) {
                if (!players.containsKey(p.playerId)) spawnPlayer(p.playerId);
            }
            else if (packet instanceof Packets.PlayerPosition p) {
                if (p.playerId == localPlayerId) return; 
                if (!players.containsKey(p.playerId)) spawnPlayer(p.playerId);
                players.get(p.playerId).position.set(p.x, p.y);
            }
            else if (packet instanceof SumoPackets.PlayerKnockback p) {
                SumoPlayer player = players.get(p.playerId);
                if (player != null) {
                    player.velocity.set(p.velocityX, p.velocityY);
                }
            }
            else if (packet instanceof SumoPackets.PlayerFell p) {
                SumoPlayer player = players.get(p.playerId);
                if (player != null) player.isAlive = false;
            }
            else if (packet instanceof SumoPackets.GameEnd p) {
                finished = true;
                winnerId = p.winnerId;
            }
        }
    }

    private class SumoServerHandler implements ServerPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(Packets.PlayerPosition.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerPosition) {
                context.broadcastExceptSender(packet);
                Packets.PlayerPosition pos = (Packets.PlayerPosition) packet;
                if(players.containsKey(pos.playerId)) {
                    players.get(pos.playerId).position.set(pos.x, pos.y);
                } else {
                    spawnPlayer(pos.playerId);
                }
            }
        }
    }
}