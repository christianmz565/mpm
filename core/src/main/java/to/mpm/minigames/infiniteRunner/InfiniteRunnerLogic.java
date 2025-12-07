package to.mpm.minigames.infiniteRunner;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.GameConstants;
import to.mpm.network.NetworkManager;

/**
 * Lógica del juego de carreras infinitas.
 */
public class InfiniteRunnerLogic {
    // Jugadores
    public IntMap<InfiniteRunnerPlayer> players;
    public InfiniteRunnerPlayer localPlayer;
    
    // Obstáculos
    public Array<Obstacle> obstacles;
    
    // Estado del juego
    public boolean finished = false;
    public boolean countdownActive = true;
    public float countdownTime = 3f;
    
    public float gameTime = 0f;
    public float cameraY = 0f;
    
    // Constantes
    private static final float CAMERA_SPEED = 200f;
    private static final float GRAVITY = -800f;
    private static final float OBSTACLE_SPAWN_RATE = 0.8f;
    private static final float ELIMINATION_THRESHOLD = 150f;
    private static final float SLOW_DURATION = 1.5f;
    
    private float lastObstacleTime = 0f;
    private int obstacleIdCounter = 0;
    
    public InfiniteRunnerLogic(int localPlayerId) {
        players = new IntMap<>();
        
        localPlayer = new InfiniteRunnerPlayer(
            localPlayerId,
            GameConstants.Screen.WIDTH / 2 - 32,
            100f,
            64,
            64
        );
        players.put(localPlayerId, localPlayer);
        
        NetworkManager nm = NetworkManager.getInstance();
        if (nm != null) {
            for (Integer playerId : nm.getConnectedPlayers().keySet()) {
                if (playerId != localPlayerId) {
                    float xOffset = 100 + (playerId * 80);
                    players.put(playerId, new InfiniteRunnerPlayer(
                        playerId,
                        xOffset,
                        100f,
                        64,
                        64
                    ));
                }
            }
        }
        
        obstacles = new Array<>();
    }
    
    public void update(float delta) {
        if (countdownActive) {
            countdownTime -= delta;
            if (countdownTime <= 0) {
                countdownActive = false;
                countdownTime = 0;
            }
            return;
        }
        
        if (finished)
            return;
        
        gameTime += delta;
        cameraY += CAMERA_SPEED * delta;
        
        updatePlayer(localPlayer, delta);
        
        lastObstacleTime += delta;
        if (lastObstacleTime >= OBSTACLE_SPAWN_RATE) {
            spawnObstacle();
            lastObstacleTime = 0f;
        }
        
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            
            if (obs.bounds.overlaps(localPlayer.getBounds())) {
                localPlayer.isSlowed = true;
                localPlayer.slowTimer = SLOW_DURATION;
                obs.hit = true;
            }
            
            if (obs.y < cameraY - 100) {
                obstacles.removeIndex(i);
            }
        }
        
        if (localPlayer.y < cameraY - ELIMINATION_THRESHOLD) {
            finished = true;
        }
    }
    
    private void updatePlayer(InfiniteRunnerPlayer player, float delta) {
        player.velocityY += GRAVITY * delta;
        player.y += player.velocityY * delta;
        
        if (player.y <= 100f) {
            player.y = 100f;
            player.velocityY = 0;
            player.isGrounded = true;
        } else {
            player.isGrounded = false;
        }
        
        if (player.isSlowed) {
            player.slowTimer -= delta;
            if (player.slowTimer <= 0) {
                player.isSlowed = false;
                player.slowTimer = 0;
            }
        }
    }
    
    private void spawnObstacle() {
        float obstacleX = MathUtils.random(50, GameConstants.Screen.WIDTH - 50);
        float obstacleY = cameraY + GameConstants.Screen.HEIGHT + 50;
        
        Obstacle obs = new Obstacle(
            obstacleIdCounter++,
            obstacleX,
            obstacleY,
            48,
            48
        );
        
        obstacles.add(obs);
    }
    
    public int getLocalScore() {
        return (int)(gameTime * 10000);
    }
    
    public static class Obstacle {
        public int id;
        public float x, y;
        public float width, height;
        public boolean hit = false;
        public Rectangle bounds;
        
        public Obstacle(int id, float x, float y, float width, float height) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.bounds = new Rectangle(x, y, width, height);
        }
        
        public void update() {
            bounds.set(x, y, width, height);
        }
    }
}
