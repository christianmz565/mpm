package to.mpm.minigames.duckshooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
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
    private static final float SHOOT_COOLDOWN = 0.5f;
    private static final float GAME_DURATION = 180f; // 3 minutos (más tiempo para batalla royale)
    private static final Color[] DUCK_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.CYAN
    };

    private final int localPlayerId;
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
    private BitmapFont font;

    public DuckShooterMinigame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
        this.gameTimer = GAME_DURATION;
        this.shootCooldown = 0f;
    }

    @Override
    public void initialize() {
        NetworkManager nm = NetworkManager.getInstance();

        // Crear pato local
        Color color = DUCK_COLORS[localPlayerId % DUCK_COLORS.length];
        float startX = 100 + (localPlayerId * 100) % 440;
        float startY = 100 + (localPlayerId * 80) % 280;

        localDuck = new Duck(localPlayerId, startX, startY, color);
        ducks.put(localPlayerId, localDuck);
        scores.put(localPlayerId, 0);

        Gdx.app.log("DuckShooter",
                "Initialized local player " + localPlayerId + " as " + (nm.isHost() ? "HOST" : "CLIENT"));

        // Configurar red
        clientHandler = new DuckShooterClientHandler();
        nm.registerClientHandler(clientHandler);

        if (nm.isHost()) {
            serverHandler = new DuckShooterServerHandler();
            nm.registerServerHandler(serverHandler);
        }

        // Crear fuente para UI
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
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

        // Enviar estado del pato local
        sendDuckState();
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
        // Renderizar patos
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

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

        // Renderizar UI
        batch.begin();

        // Temporizador
        int minutes = (int) gameTimer / 60;
        int seconds = (int) gameTimer % 60;
        String timeText = String.format("Time: %d:%02d", minutes, seconds);
        font.draw(batch, timeText, 10, 470);

        // Información del jugador
        String status = localDuck.isAlive() ? "ALIVE" : "ELIMINATED";
        String playerInfo = String.format("Player %d | Hits: %d/3 | Kills: %d | %s",
                localPlayerId,
                localDuck.getHits(),
                scores.getOrDefault(localPlayerId, 0),
                status);
        font.draw(batch, playerInfo, 10, 440);

        // Cooldown de disparo
        if (shootCooldown > 0) {
            font.setColor(Color.RED);
            font.draw(batch, "Reloading...", 10, 410);
            font.setColor(Color.WHITE);
        } else if (localDuck.isAlive()) {
            font.setColor(Color.GREEN);
            font.draw(batch, "[SPACE] to Shoot", 10, 410);
            font.setColor(Color.WHITE);
        }

        // Controles
        font.getData().setScale(1f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "WASD/Arrows: Move | SPACE: Shoot | Mouse: Aim", 10, 25);
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // Tabla de jugadores (ordenar por vivos primero, luego por kills)
        int y = 380;
        font.draw(batch, "Players:", 450, y);
        y -= 25;

        List<Map.Entry<Integer, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort((a, b) -> {
            Duck duckA = ducks.get(a.getKey());
            Duck duckB = ducks.get(b.getKey());
            boolean aliveA = duckA != null && duckA.isAlive();
            boolean aliveB = duckB != null && duckB.isAlive();

            // Vivos primero
            if (aliveA != aliveB) {
                return aliveA ? -1 : 1;
            }
            // Luego por kills
            return b.getValue().compareTo(a.getValue());
        });

        for (Map.Entry<Integer, Integer> entry : sortedScores) {
            Duck duck = ducks.get(entry.getKey());
            String playerStatus = duck != null && duck.isAlive() ? "⬤ ALIVE" : "✕ OUT";
            String scoreText = String.format("P%d: %d kills %s", entry.getKey(), entry.getValue(), playerStatus);

            // Resaltar si es el jugador local
            if (entry.getKey() == localPlayerId) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "> " + scoreText, 450, y);
                font.setColor(Color.WHITE);
            } else {
                font.draw(batch, scoreText, 450, y);
            }
            y -= 20;
        }

        batch.end();
    }

    @Override
    public void handleInput(float delta) {
        if (!localDuck.isAlive())
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
        // Dirección hacia el mouse
        float mouseX = Gdx.input.getX();
        float mouseY = 480 - Gdx.input.getY(); // Invertir Y (coordenadas de pantalla)

        Vector2 direction = new Vector2(
                mouseX - localDuck.position.x,
                mouseY - localDuck.position.y);

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

        Gdx.app.log("DuckShooter", "Player " + localPlayerId + " shot! Total ducks in map: " + ducks.size);

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

        if (font != null) {
            font.dispose();
        }

        ducks.clear();
        quacks.clear();
        scores.clear();
    }

    @Override
    public void resize(int width, int height) {
        // No necesita ajustes especiales
    }

    // ==================== Handlers de red ====================

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
            // Retransmitir a todos excepto el emisor
            context.broadcastExceptSender(packet);
        }
    }

    private void handleDuckState(DuckShooterPackets.DuckState state) {
        if (state.playerId == localPlayerId)
            return;

        Duck duck = ducks.get(state.playerId);
        if (duck == null) {
            Color color = DUCK_COLORS[state.playerId % DUCK_COLORS.length];
            duck = new Duck(state.playerId, state.x, state.y, color);
            ducks.put(state.playerId, duck);
            scores.putIfAbsent(state.playerId, 0);
            Gdx.app.log("DuckShooter", "Created remote duck for player " + state.playerId);
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

            // Si somos nosotros, actualizar nuestro localDuck también
            if (hit.targetId == localPlayerId) {
                localDuck.setHits(hit.remainingHits);
                Gdx.app.log("DuckShooter", "We got hit! Remaining hits: " + hit.remainingHits);
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

            // Si somos nosotros, actualizar nuestro localDuck también
            if (elim.playerId == localPlayerId) {
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
