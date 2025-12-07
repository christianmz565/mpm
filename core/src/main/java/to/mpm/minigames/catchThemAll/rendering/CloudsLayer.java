package to.mpm.minigames.catchThemAll.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la capa de nubes con desplazamiento para el minijuego Atrapa a Todos.
 * <p>
 * Las nubes se desplazan horizontalmente creando un efecto de paralaje.
 */
public class CloudsLayer {
    private static final float CLOUDS_SCROLL_SPEED = -20f;
    
    private final float screenHeight;
    private final float cloudsHeight;
    
    private List<CloudInstance> clouds;
    
    /**
     * Representa una instancia individual de nube con su posición.
     */
    private static class CloudInstance {
        float x;
        float y;
        
        CloudInstance(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * Crea una capa de nubes.
     * 
     * @param screenWidth ancho de la pantalla en píxeles
     * @param screenHeight alto de la pantalla (alto del fondo) en píxeles
     */
    public CloudsLayer(float screenWidth, float screenHeight) {
        this.screenHeight = screenHeight;
        this.cloudsHeight = screenHeight * 2f / 3f;
        this.clouds = new ArrayList<>();
        
        clouds.add(new CloudInstance(0, screenHeight - cloudsHeight));
    }
    
    /**
     * Actualiza las posiciones de las nubes.
     * 
     * @param delta tiempo transcurrido desde el último frame en segundos
     * @param cloudsTexture textura de las nubes para verificar dimensiones
     */
    public void update(float delta, Texture cloudsTexture) {
        if (cloudsTexture == null) {
            return;
        }
        
        for (CloudInstance cloud : clouds) {
            cloud.x += CLOUDS_SCROLL_SPEED * delta;
        }
    }
    
    /**
     * Renderiza todas las instancias de nubes.
     * 
     * @param batch el SpriteBatch para renderizar
     * @param cloudsTexture textura de las nubes a renderizar
     */
    public void render(SpriteBatch batch, Texture cloudsTexture) {
        if (cloudsTexture == null) {
            return;
        }
        
        float aspectRatio = (float) cloudsTexture.getWidth() / cloudsTexture.getHeight();
        float cloudsWidth = cloudsHeight * aspectRatio;
        
        for (CloudInstance cloud : clouds) {
            batch.draw(cloudsTexture, cloud.x, cloud.y, cloudsWidth, cloudsHeight);
        }
    }
    
    /**
     * Reinicia la capa de nubes al estado inicial.
     */
    public void reset() {
        clouds.clear();
        clouds.add(new CloudInstance(0, screenHeight - cloudsHeight));
    }
}
