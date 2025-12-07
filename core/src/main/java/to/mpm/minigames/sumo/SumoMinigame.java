package to.mpm.minigames.sumo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import to.mpm.minigames.GameConstants;
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
import java.util.ArrayList;

public class SumoMinigame implements Minigame {
    private static final float MAP_CENTER_X = GameConstants.Sumo.MAP_CENTER_X;
    private static final float MAP_CENTER_Y = GameConstants.Sumo.MAP_CENTER_Y;
    private static final float MAP_RADIUS = GameConstants.Sumo.MAP_RADIUS;
    private static final float VIRTUAL_WIDTH = GameConstants.Screen.WIDTH;
    private static final float VIRTUAL_HEIGHT = GameConstants.Screen.HEIGHT;

    private static final int POINTS_REWARD = GameConstants.Sumo.POINTS_PER_KILL;

    private final int localPlayerId;
    private final boolean isSpectator;
    private final IntMap<SumoPlayer> players = new IntMap<>();
    private final Map<Integer, Integer> scores = new HashMap<>();

    private OrthographicCamera camera;
    private Viewport viewport;
    private boolean finished = false;
    private int winnerId = -1;

    private Texture backgroundTexture;
    private Texture islandTexture;
    private Texture playerTexture;

    private float backgroundOffsetX = 0f;
    private float backgroundOffsetY = 0f;
    private static final float BACKGROUND_SCROLL_SPEED = 10f;
    private static final float ISLAND_Y_OFFSET = -30f;

    private SumoClientHandler clientHandler;
    private SumoServerHandler serverHandler;

    public SumoMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
        this.isSpectator = (localPlayerId == GameConstants.SPECTATOR_ID);
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        backgroundTexture = new Texture(Gdx.files.internal("sprites/sumo/pond-bg.png"));
        islandTexture = new Texture(Gdx.files.internal("sprites/sumo/pond-island.png"));
        playerTexture = new Texture(Gdx.files.internal("sprites/sumo/player.png"));

        nm.registerAdditionalClasses(
                SumoPackets.PlayerKnockback.class,
                SumoPackets.PlayerFell.class,
                SumoPackets.GameEnd.class,
                SumoPackets.ScoreUpdate.class,
                SumoPackets.RoundReset.class);

        if (!isSpectator) {
            scores.put(localPlayerId, 0);
            spawnPlayer(localPlayerId);
            Gdx.app.log("Sumo", "Game started as player " + localPlayerId);
        } else {
            Gdx.app.log("Sumo", "Game started as SPECTATOR");
        }

        clientHandler = new SumoClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverHandler = new SumoServerHandler();
            nm.registerServerHandler(serverHandler);
        }
    }

    private void spawnPlayer(int id) {
        float x = MAP_CENTER_X + (float) Math.cos(id) * 50;
        float y = MAP_CENTER_Y + (float) Math.sin(id) * 50;
        Color c = GameConstants.Player.COLORS[id % GameConstants.Player.COLORS.length];
        players.put(id, new SumoPlayer(id, x, y, c));
        if (!scores.containsKey(id)) {
            scores.put(id, 0);
        }
    }

    @Override
    public void update(float delta) {
        backgroundOffsetX -= BACKGROUND_SCROLL_SPEED * delta;
        backgroundOffsetY -= BACKGROUND_SCROLL_SPEED * delta;

        for (IntMap.Entry<SumoPlayer> entry : players) {
            entry.value.update(delta);
        }

        if (NetworkManager.getInstance().isHost() && !finished) {
            checkCollisions();
            checkFallout();
            checkRoundReset();
        }

        if (!isSpectator) {
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
        List<SumoPlayer> playerList = new ArrayList<>();
        for (IntMap.Entry<SumoPlayer> entry : players) {
            playerList.add(entry.value);
        }

        for (SumoPlayer p1 : playerList) {
            for (SumoPlayer p2 : playerList) {
                if (p1.id == p2.id || !p1.isAlive || !p2.isAlive)
                    continue;

                float dist = p1.position.dst(p2.position);
                if (dist < SumoPlayer.RADIUS * 2) {
                    Vector2 dir = new Vector2(p2.position).sub(p1.position).nor();
                    float force = 300f;
                    p2.velocity.add(dir.x * force, dir.y * force);

                    p2.angularVelocity += (Math.random() > 0.5 ? 1 : -1) * (200f + (float) Math.random() * 300f);

                    p2.lastHitterId = p1.id;
                    p2.timeSinceLastHit = 0f;

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
            SumoPlayer victim = entry.value;
            if (!victim.isAlive)
                continue;

            float distFromCenter = victim.position.dst(MAP_CENTER_X, MAP_CENTER_Y);
            if (distFromCenter > MAP_RADIUS) {
                victim.isAlive = false;

                if (victim.lastHitterId != -1 && victim.lastHitterId != victim.id) {
                    int killerId = victim.lastHitterId;
                    int newScore = scores.getOrDefault(killerId, 0) + POINTS_REWARD;
                    scores.put(killerId, newScore);

                    SumoPackets.ScoreUpdate scorePkt = new SumoPackets.ScoreUpdate();
                    scorePkt.playerId = killerId;
                    scorePkt.newScore = newScore;
                    NetworkManager.getInstance().broadcastFromHost(scorePkt);
                }

                SumoPackets.PlayerFell pkt = new SumoPackets.PlayerFell();
                pkt.playerId = victim.id;
                NetworkManager.getInstance().broadcastFromHost(pkt);
            }
        }
    }

    private void checkRoundReset() {
        int aliveCount = 0;

        for (IntMap.Entry<SumoPlayer> entry : players) {
            if (entry.value.isAlive) {
                aliveCount++;
            }
        }

        if (aliveCount <= 1 && players.size > 1) {
            resetRoundLocally();
            NetworkManager.getInstance().broadcastFromHost(new SumoPackets.RoundReset());
        }
    }

    private void resetRoundLocally() {
        for (IntMap.Entry<SumoPlayer> entry : players) {
            int id = entry.key;
            float x = MAP_CENTER_X + (float) Math.cos(id) * 50;
            float y = MAP_CENTER_Y + (float) Math.sin(id) * 50;
            entry.value.reset(x, y);
        }
        Gdx.app.log("Sumo", "Ronda Reiniciada!");
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        float bgWidth = VIRTUAL_WIDTH * 2;
        float bgHeight = VIRTUAL_HEIGHT * 2;
        batch.draw(backgroundTexture, backgroundOffsetX, backgroundOffsetY, bgWidth, bgHeight);

        float islandSize = MAP_RADIUS * 2.2f;
        float islandX = MAP_CENTER_X - islandSize / 2;
        float islandY = MAP_CENTER_Y - islandSize / 2 + ISLAND_Y_OFFSET;
        batch.draw(islandTexture, islandX, islandY, islandSize, islandSize);

        for (IntMap.Entry<SumoPlayer> entry : players) {
            SumoPlayer p = entry.value;
            if (p.isAlive) {
                batch.setColor(p.color);
                float aspectRatio = 1.5f;
                float width = SumoPlayer.RADIUS * 2 * aspectRatio;
                float height = SumoPlayer.RADIUS * 2;
                batch.draw(playerTexture,
                        p.position.x - width / 2,
                        p.position.y - height / 2,
                        width / 2,
                        height / 2,
                        width,
                        height,
                        1f,
                        1f,
                        p.rotation,
                        0,
                        0,
                        playerTexture.getWidth(),
                        playerTexture.getHeight(),
                        false,
                        false);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void handleInput(float delta) {
        if (isSpectator)
            return;

        SumoPlayer me = players.get(localPlayerId);
        if (me == null || !me.isAlive)
            return;

        float speed = GameConstants.Player.DEFAULT_MOVE_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W))
            me.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            me.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            me.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            me.position.x += speed;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Map<Integer, Integer> getScores() {
        return scores;
    }

    @Override
    public int getWinnerId() {
        return winnerId;
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        NetworkManager nm = NetworkManager.getInstance();
        if (clientHandler != null)
            nm.unregisterClientHandler(clientHandler);
        if (serverHandler != null)
            nm.unregisterServerHandler(serverHandler);
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (islandTexture != null)
            islandTexture.dispose();
        if (playerTexture != null)
            playerTexture.dispose();
    }

    private class SumoClientHandler implements ClientPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(
                    Packets.PlayerPosition.class,
                    SumoPackets.PlayerKnockback.class,
                    SumoPackets.PlayerFell.class,
                    SumoPackets.GameEnd.class,
                    SumoPackets.ScoreUpdate.class,
                    SumoPackets.RoundReset.class,
                    Packets.PlayerJoined.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerJoined p) {
                if (!players.containsKey(p.playerId))
                    spawnPlayer(p.playerId);
            } else if (packet instanceof Packets.PlayerPosition p) {
                if (p.playerId == localPlayerId)
                    return;
                if (!players.containsKey(p.playerId))
                    spawnPlayer(p.playerId);
                players.get(p.playerId).position.set(p.x, p.y);
            } else if (packet instanceof SumoPackets.PlayerKnockback p) {
                SumoPlayer player = players.get(p.playerId);
                if (player != null) {
                    player.velocity.set(p.velocityX, p.velocityY);
                    player.angularVelocity += (Math.random() > 0.5 ? 1 : -1) * (200f + (float) Math.random() * 300f);
                }
            } else if (packet instanceof SumoPackets.PlayerFell p) {
                SumoPlayer player = players.get(p.playerId);
                if (player != null)
                    player.isAlive = false;
            } else if (packet instanceof SumoPackets.ScoreUpdate p) {
                scores.put(p.playerId, p.newScore);
            } else if (packet instanceof SumoPackets.RoundReset) {
                resetRoundLocally();
            } else if (packet instanceof SumoPackets.GameEnd p) {
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
                if (players.containsKey(pos.playerId)) {
                    players.get(pos.playerId).position.set(pos.x, pos.y);
                } else {
                    spawnPlayer(pos.playerId);
                }
            }
        }
    }
}