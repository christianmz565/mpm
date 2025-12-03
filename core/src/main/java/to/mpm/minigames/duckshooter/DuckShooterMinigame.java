package to.mpm.minigames.duckshooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.duckshooter.entities.Duck;
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
 * Minijuego tipo shooter "Last Duck Standing Wins".
 * Los patos se disparan quacks entre sí. Cada pato tiene 3 hits antes de ser
 * eliminado.
 * El último pato que quede vivo gana el juego.
 */
public class DuckShooterMinigame implements Minigame {
    private static final float SHOOT_COOLDOWN = GameConstants.DuckShooter.SHOOT_COOLDOWN;
    private static final float GAME_DURATION = GameConstants.Timing.EXTENDED_GAME_DURATION;

    private final int localPlayerId;
    private final boolean isSpectator;
    private final IntMap<Duck> ducks = new IntMap<>();
    private final List<Quack> quacks = new ArrayList<>();
    private final Map<Integer, Integer> scores = new HashMap<>();

    private Duck localDuck;
    private float shootCooldown;
    private float gameTimer;
    private boolean finished;
    private int winnerId = -1;

    private DuckShooterClientHandler clientHandler;
    private DuckShooterServerHandler serverHandler;

    public DuckShooterMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
        this.isSpectator = (localPlayerId == GameConstants.SPECTATOR_ID);
        this.gameTimer = GAME_DURATION;
        this.shootCooldown = 0f;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        if (!isSpectator) {
            Color color = GameConstants.Player.COLORS[localPlayerId % GameConstants.Player.COLORS.length];
            float startX = 100 + (localPlayerId * 100) % 440;
            float startY = 100 + (localPlayerId * 80) % 280;

            localDuck = new Duck(localPlayerId, startX, startY, color);
            ducks.put(localPlayerId, localDuck);
            scores.put(localPlayerId, 0);

            Gdx.app.log("DuckShooter",
                    "Initialized local player " + localPlayerId + " as " + (nm.isHost() ? "HOST" : "CLIENT"));
        } else {
            Gdx.app.log("DuckShooter", "Initialized as SPECTATOR");
        }

        clientHandler = new DuckShooterClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverHandler = new DuckShooterServerHandler();
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

                    Gdx.app.log("DuckShooter", "HIT! Shooter=" + quack.shooterId + " Target=" + duck.playerId
                            + " Remaining=" + duck.getHits());

                    if (!duck.isAlive()) {
                        scores.merge(quack.shooterId, 1, Integer::sum);
                    }

                    DuckShooterPackets.QuackHit hitPacket = new DuckShooterPackets.QuackHit();
                    hitPacket.shooterId = quack.shooterId;
                    hitPacket.targetId = duck.playerId;
                    hitPacket.remainingHits = duck.getHits();
                    NetworkManager.getInstance().sendPacket(hitPacket);

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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

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

        shapeRenderer.end();
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
        float mouseX = Gdx.input.getX();
        float mouseY = 480 - Gdx.input.getY();

        Vector2 direction = new Vector2(
                mouseX - localDuck.position.x,
                mouseY - localDuck.position.y);

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

        Gdx.app.log("DuckShooter", "Player " + localPlayerId + " shot! Total ducks in map: " + ducks.size);

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
            Gdx.app.log("DuckShooter", "Host sent GameEnd! Winner: " + winnerId + " (Last Duck Standing)");
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
    }

    private class DuckShooterClientHandler implements ClientPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return Arrays.asList(
                    DuckShooterPackets.DuckState.class,
                    DuckShooterPackets.ShootQuack.class,
                    DuckShooterPackets.QuackHit.class,
                    DuckShooterPackets.DuckEliminated.class,
                    DuckShooterPackets.GameEnd.class);
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
            }
        }
    }

    private class DuckShooterServerHandler implements ServerPacketHandler {
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
            Gdx.app.log("DuckShooter", "Created remote duck for player " + state.playerId);
        }

        duck.setPosition(state.x, state.y);

        if (!NetworkManager.getInstance().isHost()) {
            duck.setHits(state.hits);
        }
    }

    private void handleShootQuack(DuckShooterPackets.ShootQuack shoot) {
        if (shoot.shooterId == localPlayerId)
            return;

        Duck shooter = ducks.get(shoot.shooterId);
        if (shooter == null) {
            Gdx.app.log("DuckShooter", "WARNING: Received ShootQuack from unknown player " + shoot.shooterId);
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
        Gdx.app.log("DuckShooter", "Remote player " + shoot.shooterId + " shot a quack");
    }

    private void handleQuackHit(DuckShooterPackets.QuackHit hit) {
        Duck target = ducks.get(hit.targetId);
        if (target != null) {
            target.setHits(hit.remainingHits);

            if (!isSpectator && hit.targetId == localPlayerId && localDuck != null) {
                localDuck.setHits(hit.remainingHits);
                Gdx.app.log("DuckShooter", "We got hit! Remaining hits: " + hit.remainingHits);
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
                Gdx.app.log("DuckShooter", "We were eliminated!");
            }
        }

        scores.merge(elim.killerId, 1, Integer::sum);
    }

    private void handleGameEnd(DuckShooterPackets.GameEnd gameEnd) {
        Gdx.app.log("DuckShooter", "Received GameEnd! Winner: " + gameEnd.winnerId);
        winnerId = gameEnd.winnerId;
        finished = true;
    }
}
