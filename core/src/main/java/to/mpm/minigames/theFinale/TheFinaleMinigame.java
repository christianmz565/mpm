package to.mpm.minigames.theFinale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.duckshooter.entities.Duck;
import to.mpm.minigames.duckshooter.entities.HealthPack;
import to.mpm.minigames.duckshooter.entities.Quack;
import to.mpm.minigames.duckshooter.network.DuckShooterPackets;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;

import java.util.*;

/**
 * Minijuego final tipo "Last Duck Standing Wins" - el último pato que sobreviva
 * gana.
 * Es el evento final del torneo, usa la misma mecánica del Duck Shooter.
 */
public class TheFinaleMinigame implements Minigame {
    private static final float SHOOT_COOLDOWN = GameConstants.DuckShooter.SHOOT_COOLDOWN;
    private static final float GAME_DURATION = GameConstants.TheFinale.GAME_DURATION;
    private static final float VIRTUAL_WIDTH = GameConstants.Screen.WIDTH;
    private static final float VIRTUAL_HEIGHT = GameConstants.Screen.HEIGHT;
    private static final float HEALTH_PACK_SPAWN_MIN = 10f; // Mínimo tiempo entre spawns
    private static final float HEALTH_PACK_SPAWN_MAX = 15f; // Máximo tiempo entre spawns

    private final int localPlayerId;
    private final boolean isSpectator;
    private final IntMap<Duck> ducks = new IntMap<>();
    private final List<Quack> quacks = new ArrayList<>();
    private final Map<Integer, Integer> scores = new HashMap<>();
    private final IntMap<to.mpm.minigames.duckshooter.entities.HealthPack> healthPacks = new IntMap<>();
    private final Random random = new Random();

    private OrthographicCamera camera;
    private Viewport viewport;
    private Duck localDuck;
    private float shootCooldown;
    private float gameTimer;
    private float healthPackSpawnTimer;
    private int nextHealthPackId;
    private boolean finished;
    private int winnerId = -1;

    private FinaleClientHandler clientHandler;
    private FinaleServerHandler serverHandler;

    public TheFinaleMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
        this.isSpectator = (localPlayerId == GameConstants.SPECTATOR_ID);
        this.gameTimer = GAME_DURATION;
        this.shootCooldown = 0f;
        this.healthPackSpawnTimer = random.nextFloat() * (HEALTH_PACK_SPAWN_MAX - HEALTH_PACK_SPAWN_MIN)
                + HEALTH_PACK_SPAWN_MIN;
        this.nextHealthPackId = 0;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        if (!isSpectator) {
            Color color = GameConstants.Player.COLORS[localPlayerId % GameConstants.Player.COLORS.length];
            float startX = 100 + (localPlayerId * 100) % 440;
            float startY = 100 + (localPlayerId * 80) % 280;

            localDuck = new Duck(localPlayerId, startX, startY, color);
            ducks.put(localPlayerId, localDuck);
            scores.put(localPlayerId, 0);

            Gdx.app.log("TheFinale",
                    "Initialized local player " + localPlayerId + " as " + (nm.isHost() ? "HOST" : "CLIENT"));
        } else {
            Gdx.app.log("TheFinale", "Initialized as SPECTATOR");
        }

        clientHandler = new FinaleClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverHandler = new FinaleServerHandler();
            nm.registerServerHandler(serverHandler);
        }
    }

    @Override
    public void update(float delta) {
        if (finished)
            return;

        gameTimer -= delta;
        if (gameTimer <= 0) {
            endGame();
            return;
        }

        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        for (IntMap.Entry<Duck> entry : ducks) {
            entry.value.update(delta);
        }

        Iterator<Quack> it = quacks.iterator();
        while (it.hasNext()) {
            Quack quack = it.next();
            quack.update(delta);

            if (!quack.isActive()) {
                it.remove();
                continue;
            }

            if (NetworkManager.getInstance().isHost()) {
                checkQuackCollisions(quack);
            }
        }

        Iterator<IntMap.Entry<HealthPack>> hpIt = healthPacks.iterator();
        while (hpIt.hasNext()) {
            IntMap.Entry<HealthPack> entry = hpIt.next();
            HealthPack hp = entry.value;
            hp.update(delta);

            if (!hp.isActive()) {
                hpIt.remove();
                continue;
            }

            if (NetworkManager.getInstance().isHost()) {
                checkHealthPackCollisions(hp);
            }
        }

        if (NetworkManager.getInstance().isHost()) {
            healthPackSpawnTimer -= delta;
            if (healthPackSpawnTimer <= 0) {
                spawnHealthPack();
                healthPackSpawnTimer = random.nextFloat() * (HEALTH_PACK_SPAWN_MAX - HEALTH_PACK_SPAWN_MIN)
                        + HEALTH_PACK_SPAWN_MIN;
            }
        }

        int aliveDucks = 0;
        int lastAliveId = -1;
        for (IntMap.Entry<Duck> entry : ducks) {
            if (entry.value.isAlive()) {
                aliveDucks++;
                lastAliveId = entry.key;
            }
        }

        if (aliveDucks == 1 && ducks.size > 1) {
            winnerId = lastAliveId;
            endGame();
        } else if (aliveDucks == 0 && ducks.size > 1) {
            endGame();
        }

        if (!isSpectator) {
            sendDuckState();
        }
    }

    private void checkQuackCollisions(Quack quack) {
        for (IntMap.Entry<Duck> entry : ducks) {
            Duck duck = entry.value;

            if (quack.checkCollision(duck)) {
                boolean damaged = duck.takeDamage();

                if (damaged) {
                    quack.deactivate();

                    Gdx.app.log("TheFinale", "HIT! Shooter=" + quack.shooterId + " Target=" + duck.playerId
                            + " Remaining=" + duck.getHits());

                    if (!duck.isAlive()) {
                        scores.merge(quack.shooterId, 1, Integer::sum);
                    }

                    DuckShooterPackets.QuackHit hitPacket = new DuckShooterPackets.QuackHit();
                    hitPacket.shooterId = quack.shooterId;
                    hitPacket.targetId = duck.playerId;
                    hitPacket.remainingHits = duck.getHits();
                    NetworkManager.getInstance().broadcastFromHost(hitPacket);

                    if (!duck.isAlive()) {
                        DuckShooterPackets.DuckEliminated elimPacket = new DuckShooterPackets.DuckEliminated();
                        elimPacket.playerId = duck.playerId;
                        elimPacket.killerId = quack.shooterId;
                        NetworkManager.getInstance().sendPacket(elimPacket);
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        for (IntMap.Entry<Duck> entry : ducks) {
            Duck duck = entry.value;
            if (!duck.isAlive())
                continue;

            if (duck.isInvulnerable() && (System.currentTimeMillis() / 100) % 2 == 0) {
                continue;
            }

            shapeRenderer.setColor(duck.color);
            shapeRenderer.circle(duck.position.x, duck.position.y, duck.getRadius());

            float barWidth = 40f;
            float barHeight = 5f;
            float barX = duck.position.x - barWidth / 2;
            float barY = duck.position.y + duck.getRadius() + 10;

            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(barX, barY, barWidth * (duck.getHits() / 3f), barHeight);
        }

        for (Quack quack : quacks) {
            if (quack.isActive()) {
                shapeRenderer.setColor(quack.color);
                shapeRenderer.circle(quack.position.x, quack.position.y, quack.getRadius());
            }
        }

        for (IntMap.Entry<HealthPack> entry : healthPacks) {
            HealthPack hp = entry.value;
            if (hp.isActive()) {
                shapeRenderer.setColor(hp.getRenderColor());
                shapeRenderer.circle(hp.position.x, hp.position.y, hp.getRadius());

                shapeRenderer.setColor(Color.WHITE);
                float crossSize = hp.getRadius() * 0.6f;
                shapeRenderer.rectLine(hp.position.x - crossSize, hp.position.y,
                        hp.position.x + crossSize, hp.position.y, 2f);
                shapeRenderer.rectLine(hp.position.x, hp.position.y - crossSize,
                        hp.position.x, hp.position.y + crossSize, 2f);
            }
        }

        shapeRenderer.end();

        if (!isSpectator && localDuck != null && localDuck.isAlive()) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);

            float crossSize = 10f;
            shapeRenderer.line(mousePos.x - crossSize, mousePos.y, mousePos.x + crossSize, mousePos.y);
            shapeRenderer.line(mousePos.x, mousePos.y - crossSize, mousePos.x, mousePos.y + crossSize);

            shapeRenderer.end();
        }
    }

    @Override
    public void handleInput(float delta) {
        if (isSpectator || localDuck == null || !localDuck.isAlive())
            return;

        float dx = 0, dy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx = 1;
        }

        if (dx != 0 || dy != 0) {
            localDuck.move(dx, dy, delta);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && shootCooldown <= 0) {
            shoot();
        }
    }

    private void shoot() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        Vector2 direction = new Vector2(
                mousePos.x - localDuck.position.x,
                mousePos.y - localDuck.position.y);

        if (direction.len() < 10f) {
            direction.set(0, 1);
        }

        direction.nor();

        shootCooldown = SHOOT_COOLDOWN;

        Quack quack = new Quack(
                localPlayerId,
                localDuck.position.x,
                localDuck.position.y,
                direction.x,
                direction.y,
                localDuck.color);
        quacks.add(quack);

        Gdx.app.log("TheFinale", "Player " + localPlayerId + " shot!");

        DuckShooterPackets.ShootQuack packet = new DuckShooterPackets.ShootQuack();
        packet.shooterId = localPlayerId;
        packet.x = localDuck.position.x;
        packet.y = localDuck.position.y;
        packet.dirX = direction.x;
        packet.dirY = direction.y;
        NetworkManager.getInstance().sendPacket(packet);
    }

    private void sendDuckState() {
        DuckShooterPackets.DuckState packet = new DuckShooterPackets.DuckState();
        packet.playerId = localPlayerId;
        packet.x = localDuck.position.x;
        packet.y = localDuck.position.y;
        packet.hits = localDuck.getHits();
        NetworkManager.getInstance().sendPacket(packet);
    }

    private void spawnHealthPack() {
        float x = 50 + random.nextFloat() * (640 - 100);
        float y = 50 + random.nextFloat() * (480 - 100);

        int id = nextHealthPackId++;
        HealthPack hp = new HealthPack(id, x, y);
        healthPacks.put(id, hp);

        DuckShooterPackets.HealthPackSpawned spawnPacket = new DuckShooterPackets.HealthPackSpawned();
        spawnPacket.healthPackId = id;
        spawnPacket.x = x;
        spawnPacket.y = y;
        NetworkManager.getInstance().broadcastFromHost(spawnPacket);

        Gdx.app.log("TheFinale", "Spawned health pack " + id + " at (" + x + ", " + y + ")");
    }

    private void checkHealthPackCollisions(HealthPack hp) {
        for (IntMap.Entry<Duck> entry : ducks) {
            Duck duck = entry.value;

            if (hp.checkCollision(duck)) {
                if (duck.heal()) {
                    hp.pickup();

                    Gdx.app.log("TheFinale", "Player " + duck.playerId + " picked up health pack " + hp.id
                            + ". New health: " + duck.getHits());

                    DuckShooterPackets.HealthPackPickup pickupPacket = new DuckShooterPackets.HealthPackPickup();
                    pickupPacket.healthPackId = hp.id;
                    pickupPacket.playerId = duck.playerId;
                    pickupPacket.newHits = duck.getHits();
                    NetworkManager.getInstance().broadcastFromHost(pickupPacket);

                    break;
                }
            }
        }
    }

    private void endGame() {
        if (finished)
            return;

        finished = true;

        if (winnerId == -1) {
            int maxScore = -1;
            boolean foundAlive = false;

            for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
                Duck duck = ducks.get(entry.getKey());
                int score = entry.getValue();
                boolean isAlive = duck != null && duck.isAlive();

                if (isAlive) {
                    if (!foundAlive || score > maxScore) {
                        maxScore = score;
                        winnerId = entry.getKey();
                        foundAlive = true;
                    }
                } else if (!foundAlive) {
                    if (score > maxScore) {
                        maxScore = score;
                        winnerId = entry.getKey();
                    }
                }
            }
        }

        if (NetworkManager.getInstance().isHost()) {
            DuckShooterPackets.GameEnd gameEndPacket = new DuckShooterPackets.GameEnd();
            gameEndPacket.winnerId = winnerId;
            NetworkManager.getInstance().broadcastFromHost(gameEndPacket);
            Gdx.app.log("TheFinale", "Host sent GameEnd! Winner: " + winnerId + " (Last Duck Standing)");
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Map<Integer, Integer> getScores() {
        return new HashMap<>(scores);
    }

    @Override
    public int getWinnerId() {
        return winnerId;
    }

    @Override
    public void dispose() {
        NetworkManager nm = NetworkManager.getInstance();

        if (clientHandler != null) {
            nm.unregisterClientHandler(clientHandler);
            clientHandler = null;
        }

        if (serverHandler != null) {
            nm.unregisterServerHandler(serverHandler);
            serverHandler = null;
        }

        ducks.clear();
        quacks.clear();
        scores.clear();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();
    }

    private class FinaleClientHandler implements ClientPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return Arrays.asList(
                    DuckShooterPackets.DuckState.class,
                    DuckShooterPackets.ShootQuack.class,
                    DuckShooterPackets.QuackHit.class,
                    DuckShooterPackets.DuckEliminated.class,
                    DuckShooterPackets.GameEnd.class,
                    DuckShooterPackets.HealthPackSpawned.class,
                    DuckShooterPackets.HealthPackPickup.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof DuckShooterPackets.DuckState state) {
                handleDuckState(state);
            } else if (packet instanceof DuckShooterPackets.ShootQuack shoot) {
                handleShootQuack(shoot);
            } else if (packet instanceof DuckShooterPackets.QuackHit hit) {
                handleQuackHit(hit);
            } else if (packet instanceof DuckShooterPackets.DuckEliminated elim) {
                handleDuckEliminated(elim);
            } else if (packet instanceof DuckShooterPackets.GameEnd gameEnd) {
                handleGameEnd(gameEnd);
            } else if (packet instanceof DuckShooterPackets.HealthPackSpawned spawned) {
                handleHealthPackSpawned(spawned);
            } else if (packet instanceof DuckShooterPackets.HealthPackPickup pickup) {
                handleHealthPackPickup(pickup);
            }
        }
    }

    private class FinaleServerHandler implements ServerPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return Arrays.asList(
                    DuckShooterPackets.DuckState.class,
                    DuckShooterPackets.ShootQuack.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            context.broadcastExceptSender(packet);
        }
    }

    private void handleDuckState(DuckShooterPackets.DuckState state) {
        if (state.playerId == localPlayerId)
            return;

        Duck duck = ducks.get(state.playerId);
        if (duck == null) {
            Color color = GameConstants.Player.COLORS[state.playerId % GameConstants.Player.COLORS.length];
            duck = new Duck(state.playerId, state.x, state.y, color);
            ducks.put(state.playerId, duck);
            scores.putIfAbsent(state.playerId, 0);
            Gdx.app.log("TheFinale", "Created remote duck for player " + state.playerId);
        }

        duck.setPosition(state.x, state.y);

        if (state.hits <= duck.getHits()) {
            duck.setHits(state.hits);
        }
    }

    private void handleShootQuack(DuckShooterPackets.ShootQuack shoot) {
        if (shoot.shooterId == localPlayerId)
            return;

        Duck shooter = ducks.get(shoot.shooterId);
        if (shooter == null) {
            Gdx.app.log("TheFinale", "WARNING: Received ShootQuack from unknown player " + shoot.shooterId);
            return;
        }

        Quack quack = new Quack(
                shoot.shooterId,
                shoot.x,
                shoot.y,
                shoot.dirX,
                shoot.dirY,
                shooter.color);
        quacks.add(quack);
        Gdx.app.log("TheFinale", "Remote player " + shoot.shooterId + " shot a quack");
    }

    private void handleQuackHit(DuckShooterPackets.QuackHit hit) {
        Duck target = ducks.get(hit.targetId);
        if (target != null) {
            target.setHits(hit.remainingHits);

            if (!isSpectator && hit.targetId == localPlayerId && localDuck != null) {
                localDuck.setHits(hit.remainingHits);
                Gdx.app.log("TheFinale", "We got hit! Remaining hits: " + hit.remainingHits);
            }
        }

        if (hit.remainingHits <= 0) {
            scores.merge(hit.shooterId, 1, Integer::sum);
        }
    }

    private void handleDuckEliminated(DuckShooterPackets.DuckEliminated elim) {
        Duck duck = ducks.get(elim.playerId);
        if (duck != null) {
            duck.setHits(0);

            if (!isSpectator && elim.playerId == localPlayerId && localDuck != null) {
                localDuck.setHits(0);
                Gdx.app.log("TheFinale", "We were eliminated!");
            }
        }

        scores.merge(elim.killerId, 1, Integer::sum);
    }

    private void handleGameEnd(DuckShooterPackets.GameEnd gameEnd) {
        Gdx.app.log("TheFinale", "Received GameEnd! Winner: " + gameEnd.winnerId);
        winnerId = gameEnd.winnerId;
        finished = true;
    }

    private void handleHealthPackSpawned(DuckShooterPackets.HealthPackSpawned spawned) {
        HealthPack hp = new HealthPack(spawned.healthPackId, spawned.x, spawned.y);
        healthPacks.put(spawned.healthPackId, hp);
        Gdx.app.log("TheFinale",
                "Health pack " + spawned.healthPackId + " spawned at (" + spawned.x + ", " + spawned.y + ")");
    }

    private void handleHealthPackPickup(DuckShooterPackets.HealthPackPickup pickup) {
        healthPacks.remove(pickup.healthPackId);

        Duck duck = ducks.get(pickup.playerId);
        if (duck != null) {
            duck.setHits(pickup.newHits);

            if (pickup.playerId == localPlayerId) {
                localDuck.setHits(pickup.newHits);
                Gdx.app.log("TheFinale", "We picked up health pack! New health: " + pickup.newHits);
            }
        }
    }
}
