package to.mpm.minigames.catchThemAll.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import to.mpm.minigames.catchThemAll.entities.Player;

/**
 * Handles collision detection and resolution between players.
 * New physics rules:
 * - Static player (not moving) = immovable wall
 * - Player on top of another = the one below acts as floor
 * - Both moving and collide = both stop
 */
public class CollisionHandler {
    private static final float SCREEN_WIDTH = 640f;
    
    /**
     * Handle collisions between all players (only run by host).
     * 
     * @param players map of all active players
     */
    public static void handlePlayerCollisions(IntMap<Player> players) {
        // Convert to array to avoid nested iterator issues
        IntMap.Keys keys = players.keys();
        int[] playerIds = new int[players.size];
        int index = 0;
        while (keys.hasNext) {
            playerIds[index++] = keys.next();
        }
        
        // Check all player pairs for collisions
        for (int i = 0; i < playerIds.length; i++) {
            Player p1 = players.get(playerIds[i]);
            if (p1 == null) continue;
            
            for (int j = i + 1; j < playerIds.length; j++) {
                Player p2 = players.get(playerIds[j]);
                if (p2 == null) continue;
                
                resolveCollision(p1, p2);
            }
        }
    }
    
    /**
     * Resolve collision between two players.
     */
    private static void resolveCollision(Player p1, Player p2) {
        Rectangle b1 = p1.getBounds();
        Rectangle b2 = p2.getBounds();
        
        // Check if they're overlapping
        if (!b1.overlaps(b2)) return;
        
        // Calculate overlap amounts
        float overlapX = Math.min(b1.x + b1.width - b2.x, b2.x + b2.width - b1.x);
        float overlapY = Math.min(b1.y + b1.height - b2.y, b2.y + b2.height - b1.y);
        
        // Determine if players are moving
        boolean p1Moving = Math.abs(p1.lastVelocityX) > 1f;
        boolean p2Moving = Math.abs(p2.lastVelocityX) > 1f;
        
        // Separate on the axis with smallest overlap
        if (overlapX < overlapY) {
            resolveHorizontalCollision(p1, p2, overlapX, p1Moving, p2Moving, b1, b2);
        } else {
            resolveVerticalCollision(p1, p2, b1, b2);
        }
        
        // Keep players in bounds after collision
        p1.x = Math.max(0, Math.min(SCREEN_WIDTH - Player.PLAYER_WIDTH, p1.x));
        p2.x = Math.max(0, Math.min(SCREEN_WIDTH - Player.PLAYER_WIDTH, p2.x));
        
        // Update their bounds
        p1.updateBounds();
        p2.updateBounds();
    }
    
    /**
     * Resolve horizontal collision between two players.
     */
    private static void resolveHorizontalCollision(Player p1, Player p2, float overlapX, 
                                                   boolean p1Moving, boolean p2Moving,
                                                   Rectangle b1, Rectangle b2) {
        // Rule 1: One static, one moving = static is immovable wall
        if (!p1Moving && p2Moving) {
            // p1 is static wall, only p2 moves back
            if (b2.x < b1.x) {
                p2.x -= overlapX;
            } else {
                p2.x += overlapX;
            }
            p2.lastVelocityX = 0; // Stop the moving player
        } 
        else if (p1Moving && !p2Moving) {
            // p2 is static wall, only p1 moves back
            if (b1.x < b2.x) {
                p1.x -= overlapX;
            } else {
                p1.x += overlapX;
            }
            p1.lastVelocityX = 0; // Stop the moving player
        }
        else if (p1Moving && p2Moving) {
            // Rule 2: Both moving = both stop at collision point
            float pushX = overlapX / 2;
            if (b1.x < b2.x) {
                p1.x -= pushX;
                p2.x += pushX;
            } else {
                p1.x += pushX;
                p2.x -= pushX;
            }
            // Both stop
            p1.lastVelocityX = 0;
            p2.lastVelocityX = 0;
        }
        // If both static, no movement needed
    }
    
    /**
     * Resolve vertical collision between two players.
     * Player on top of another = the one below acts as floor.
     */
    private static void resolveVerticalCollision(Player p1, Player p2, Rectangle b1, Rectangle b2) {
        // Determine who is on top
        if (b1.y > b2.y) {
            // p1 is on top of p2
            p1.y = b2.y + b2.height; // Place p1 on top of p2
            p1.velocityY = 0;
            p1.isGrounded = true; // p1 can stand on p2
            
            // If p2 moves horizontally, p1 moves with it
            if (Math.abs(p2.lastVelocityX) > 0.1f) {
                p1.x += p2.lastVelocityX * Gdx.graphics.getDeltaTime();
            }
        } else {
            // p2 is on top of p1
            p2.y = b1.y + b1.height; // Place p2 on top of p1
            p2.velocityY = 0;
            p2.isGrounded = true; // p2 can stand on p1
            
            // If p1 moves horizontally, p2 moves with it
            if (Math.abs(p1.lastVelocityX) > 0.1f) {
                p2.x += p1.lastVelocityX * Gdx.graphics.getDeltaTime();
            }
        }
    }
}
