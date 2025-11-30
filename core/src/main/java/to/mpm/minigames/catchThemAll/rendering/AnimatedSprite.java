package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.graphics.Texture;

/**
 * Handles sprite animation with multiple frames.
 * Cycles through frames based on elapsed time.
 */
public class AnimatedSprite {
    private final Texture[] frames;
    private final float frameDuration; // Duration per frame in seconds
    private float stateTime;
    private int currentFrameIndex;
    private boolean paused;
    
    /**
     * Create an animated sprite.
     * @param frames Array of textures for each frame
     * @param frameDuration Duration each frame is displayed (in seconds)
     */
    public AnimatedSprite(Texture[] frames, float frameDuration) {
        if (frames == null || frames.length == 0) {
            throw new IllegalArgumentException("Frames array cannot be null or empty");
        }
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.stateTime = 0f;
        this.currentFrameIndex = 0;
        this.paused = false;
    }
    
    /**
     * Update animation state.
     * @param deltaTime Time elapsed since last update
     */
    public void update(float deltaTime) {
        if (paused || frames.length <= 1) {
            return;
        }
        
        stateTime += deltaTime;
        
        // Calculate current frame based on elapsed time
        int totalFrames = frames.length;
        currentFrameIndex = (int) ((stateTime / frameDuration) % totalFrames);
    }
    
    /**
     * Get the current frame texture.
     * @return Current frame texture
     */
    public Texture getCurrentFrame() {
        return frames[currentFrameIndex];
    }
    
    /**
     * Get a specific frame by index.
     * @param index Frame index
     * @return Texture at the specified index
     */
    public Texture getFrame(int index) {
        if (index < 0 || index >= frames.length) {
            return frames[0];
        }
        return frames[index];
    }
    
    /**
     * Reset animation to first frame.
     */
    public void reset() {
        stateTime = 0f;
        currentFrameIndex = 0;
    }
    
    /**
     * Pause animation.
     */
    public void pause() {
        paused = true;
    }
    
    /**
     * Resume animation.
     */
    public void resume() {
        paused = false;
    }
    
    /**
     * Check if animation is paused.
     */
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * Get number of frames.
     */
    public int getFrameCount() {
        return frames.length;
    }
    
    /**
     * Get current frame index.
     */
    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }
}
