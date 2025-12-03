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

    private final int localPlayerId;
    private final boolean isSpectator;
    private final IntMap<Duck> ducks = new IntMap<>();
    private final List<Quack> quacks = new ArrayList<>();
    private final Map<Integer, Integer> scores = new HashMap<>();

    private OrthographicCamera camera;
    private Viewport viewport;
    private Duck localDuck;
    private float shootCooldown;
    private float gameTimer;
    private boolean finished;
    private int winnerId = -1;

    private FinaleClientHandler clientHandler;
    private FinaleServerHandler serverHandler;

    public TheFinaleMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
        this.isSpectator = (localPlayerId == GameConstants.SPECTATOR_ID);
        this.gameTimer = GAME_DURATION;
        this.shootCooldown = 0f;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        // Set up camera and viewport for scaling
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();

        // Only create local duck if not a spectator
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

        // Configure network handlers
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

        // Actualizar temporizador
        gameTimer -= delta;
        if (gameTimer <= 0) {
            endGame();
            return;
        }

        // Actualizar cooldown de disparo
        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        // Actualizar patos
        for (IntMap.Entry<Duck> entry : ducks) {
            entry.value.update(delta);
        }

        // Actualizar proyectiles
        Iterator<Quack> it = quacks.iterator();
        while (it.hasNext()) {
            Quack quack = it.next();
            quack.update(delta);

            if (!quack.isActive()) {
                it.remove();
                continue;
            }

            // Verificar colisiones (solo el host)
            if (NetworkManager.getInstance().isHost()) {
                checkQuackCollisions(quack);
            }
        }

        // Verificar si solo queda un pato vivo (victoria por eliminación)
        int aliveDucks = 0;
        int lastAliveId = -1;
        for (IntMap.Entry<Duck> entry : ducks) {
            if (entry.value.isAlive()) {
                aliveDucks++;
                lastAliveId = entry.key;
            }
        }

        // Si solo queda un pato vivo, ese pato gana inmediatamente
        if (aliveDucks == 1 && ducks.size > 1) {
            winnerId = lastAliveId;
            endGame();
        } else if (aliveDucks == 0 && ducks.size > 1) {
            // Si todos murieron al mismo tiempo, empate (no debería pasar normalmente)
            endGame();
        }

        // Enviar estado del pato local (solo si no es espectador)
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

                    // Actualizar scores localmente (para el host)
                    if (!duck.isAlive()) {
                        scores.merge(quack.shooterId, 1, Integer::sum);
                    }

                    // Enviar notificación de impacto a los clientes
                    DuckShooterPackets.QuackHit hitPacket = new DuckShooterPackets.QuackHit();
                    hitPacket.shooterId = quack.shooterId;
                    hitPacket.targetId = duck.playerId;
                    hitPacket.remainingHits = duck.getHits();
                    NetworkManager.getInstance().sendPacket(hitPacket);

                    // Si el pato murió, enviar notificación de eliminación
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
        // Apply viewport and camera
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Draw dark background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Renderizar patos
        for (IntMap.Entry<Duck> entry : ducks) {
            Duck duck = entry.value;
            if (!duck.isAlive())
                continue;

            // Parpadeo si es invulnerable
            if (duck.isInvulnerable() && (System.currentTimeMillis() / 100) % 2 == 0) {
                continue;
            }

            shapeRenderer.setColor(duck.color);
            shapeRenderer.circle(duck.position.x, duck.position.y, duck.getRadius());

            // Barra de vida
            float barWidth = 40f;
            float barHeight = 5f;
            float barX = duck.position.x - barWidth / 2;
            float barY = duck.position.y + duck.getRadius() + 10;

            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(barX, barY, barWidth * (duck.getHits() / 3f), barHeight);
        }

        // Renderizar quacks
        for (Quack quack : quacks) {
            if (quack.isActive()) {
                shapeRenderer.setColor(quack.color);
                shapeRenderer.circle(quack.position.x, quack.position.y, quack.getRadius());
            }
        }

        shapeRenderer.end();

        // Draw crosshair at mouse position (for aiming)
        if (!isSpectator && localDuck != null && localDuck.isAlive()) {
            // Unproject mouse coordinates to game world
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);

            // Draw crosshair (+) at mouse position
            float crossSize = 10f;
            shapeRenderer.line(mousePos.x - crossSize, mousePos.y, mousePos.x + crossSize, mousePos.y);
            shapeRenderer.line(mousePos.x, mousePos.y - crossSize, mousePos.x, mousePos.y + crossSize);

            shapeRenderer.end();
        }

        // Minimal UI - just render the game entities, GameScreen overlay handles the rest
        // No cluttered UI elements in the finale
    }

    @Override
    public void handleInput(float delta) {
        // Spectators don't handle input
        if (isSpectator || localDuck == null || !localDuck.isAlive())
            return;

        // Movimiento
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

        // Disparo con espacio
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && shootCooldown <= 0) {
            shoot();
        }
    }

    private void shoot() {
        // Unproject mouse coordinates to game world
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        Vector2 direction = new Vector2(
                mousePos.x - localDuck.position.x,
                mousePos.y - localDuck.position.y);

        // Si el mouse está muy cerca del pato, disparar hacia arriba por defecto
        if (direction.len() < 10f) {
            direction.set(0, 1);
        }

        direction.nor();

        shootCooldown = SHOOT_COOLDOWN;

        // Crear el quack localmente primero
        Quack quack = new Quack(
                localPlayerId,
                localDuck.position.x,
                localDuck.position.y,
                direction.x,
                direction.y,
                localDuck.color);
        quacks.add(quack);

        Gdx.app.log("TheFinale", "Player " + localPlayerId + " shot!");

        // Enviar paquete de disparo a otros jugadores
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

        // Si no hay un ganador ya determinado (por ser el último vivo)
        if (winnerId == -1) {
            // Encontrar ganador por tiempo: el pato vivo con más kills
            // Si no hay vivos, el que más kills tenga
            int maxScore = -1;
            boolean foundAlive = false;

            for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
                Duck duck = ducks.get(entry.getKey());
                int score = entry.getValue();
                boolean isAlive = duck != null && duck.isAlive();

                // Priorizar patos vivos
                if (isAlive) {
                    if (!foundAlive || score > maxScore) {
                        maxScore = score;
                        winnerId = entry.getKey();
                        foundAlive = true;
                    }
                } else if (!foundAlive) {
                    // Solo considerar muertos si no hay vivos
                    if (score > maxScore) {
                        maxScore = score;
                        winnerId = entry.getKey();
                    }
                }
            }
        }

        // Si somos el host, notificar a todos los clientes que el juego terminó
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

    // ==================== Handlers de red ====================

    private class FinaleClientHandler implements ClientPacketHandler {
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

    private class FinaleServerHandler implements ServerPacketHandler {
        @Override
        public Collection<Class<? extends NetworkPacket>> receivablePackets() {
            return Arrays.asList(
                    DuckShooterPackets.DuckState.class,
                    DuckShooterPackets.ShootQuack.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            // Retransmitir a todos excepto el emisor
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

        // Solo el host ignora los hits de DuckState (porque él es la autoridad)
        // Los clientes sí deben actualizar hits de DuckState para tener la vista
        // correcta
        if (!NetworkManager.getInstance().isHost()) {
            duck.setHits(state.hits);
        }
    }

    private void handleShootQuack(DuckShooterPackets.ShootQuack shoot) {
        // Filtrar disparos del jugador local porque ya los creamos directamente en
        // shoot()
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

            // Si somos nosotros, actualizar nuestro localDuck también (only if not spectator)
            if (!isSpectator && hit.targetId == localPlayerId && localDuck != null) {
                localDuck.setHits(hit.remainingHits);
                Gdx.app.log("TheFinale", "We got hit! Remaining hits: " + hit.remainingHits);
            }
        }

        // Actualizar puntuación del shooter
        if (hit.remainingHits <= 0) {
            scores.merge(hit.shooterId, 1, Integer::sum);
        }
    }

    private void handleDuckEliminated(DuckShooterPackets.DuckEliminated elim) {
        Duck duck = ducks.get(elim.playerId);
        if (duck != null) {
            duck.setHits(0);

            // Si somos nosotros, actualizar nuestro localDuck también (only if not spectator)
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
}
