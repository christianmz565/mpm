package to.mpm.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Constantes centralizadas para estilos de UI.
 * <p>
 * Define colores, tipografía, espaciado y tamaños de componentes
 * utilizados en toda la interfaz del juego.
 */
public class UIStyles {

    /**
     * Paleta de colores de la aplicación.
     * <p>
     * Define todos los colores utilizados en la interfaz de usuario.
     */
    public static class Colors {
        /** Color de fondo principal de la aplicación. */
        public static final Color BACKGROUND = new Color(0.15f, 0.15f, 0.2f, 1f);
        /** Color primario para elementos destacados. */
        public static final Color PRIMARY = new Color(0.2f, 0.6f, 0.9f, 1f);
        /** Color secundario para acentos. */
        public static final Color SECONDARY = new Color(0.9f, 0.7f, 0.2f, 1f);
        /** Color de acento para elementos importantes. */
        public static final Color ACCENT = new Color(0.9f, 0.3f, 0.3f, 1f);
        /** Color para indicar éxito. */
        public static final Color SUCCESS = new Color(0.3f, 0.8f, 0.3f, 1f);
        /** Color para indicar error. */
        public static final Color ERROR = new Color(0.9f, 0.3f, 0.3f, 1f);
        /** Color de texto principal. */
        public static final Color TEXT_PRIMARY = Color.WHITE;
        /** Color de texto secundario. */
        public static final Color TEXT_SECONDARY = new Color(0.8f, 0.8f, 0.8f, 1f);
        /** Color de texto deshabilitado. */
        public static final Color TEXT_DISABLED = new Color(0.5f, 0.5f, 0.5f, 1f);
        /** Color de fondo de paneles. */
        public static final Color PANEL_BG = new Color(0.2f, 0.2f, 0.25f, 0.9f);
        /** Color de fondo de botones. */
        public static final Color BUTTON_BG = new Color(0.3f, 0.3f, 0.35f, 1f);
        /** Color de botón al pasar el cursor. */
        public static final Color BUTTON_HOVER = new Color(0.4f, 0.4f, 0.45f, 1f);
    }

    /**
     * Escalas de tipografía para diferentes contextos.
     * <p>
     * Define las escalas de fuente para títulos, subtítulos y texto normal.
     */
    public static class Typography {
        /** Escala para títulos principales. */
        public static final float TITLE_SCALE = 2.0f;
        /** Escala para subtítulos. */
        public static final float SUBTITLE_SCALE = 1.5f;
        /** Escala para encabezados. */
        public static final float HEADING_SCALE = 1.3f;
        /** Escala para texto normal. */
        public static final float BODY_SCALE = 1.0f;
        /** Escala para texto pequeño. */
        public static final float SMALL_SCALE = 0.8f;
    }

    /**
     * Valores de espaciado entre componentes.
     * <p>
     * Define los márgenes y paddings estándar de la interfaz.
     */
    public static class Spacing {
        /** Espaciado mínimo. */
        public static final float TINY = 5f;
        /** Espaciado pequeño. */
        public static final float SMALL = 10f;
        /** Espaciado mediano. */
        public static final float MEDIUM = 20f;
        /** Espaciado grande. */
        public static final float LARGE = 30f;
        /** Espaciado extra grande. */
        public static final float XLARGE = 50f;
        /** Espaciado extra extra grande. */
        public static final float XXLARGE = 70f;
    }

    /**
     * Tamaños de componentes comunes de la interfaz.
     * <p>
     * Define dimensiones estándar para botones, campos de entrada y otros elementos.
     */
    public static class Sizes {
        /** Ancho estándar de botones. */
        public static final float BUTTON_WIDTH = 200f;
        /** Alto estándar de botones. */
        public static final float BUTTON_HEIGHT = 50f;
        /** Ancho estándar de campos de entrada. */
        public static final float INPUT_WIDTH = 200f;
        /** Alto estándar de campos de entrada. */
        public static final float INPUT_HEIGHT = 40f;
        /** Alto de elementos de lista de jugadores. */
        public static final float PLAYER_ITEM_HEIGHT = 60f;
        /** Ancho de elementos de puntuación. */
        public static final float SCORE_ITEM_WIDTH = 1200f;
        /** Alto de elementos de puntuación. */
        public static final float SCORE_ITEM_HEIGHT = 50f;
        /** Tamaño de iconos. */
        public static final float ICON_SIZE = 40f;
        /** Tamaño del marcador de posición del pato. */
        public static final float DUCK_PLACEHOLDER_SIZE = 300f;
    }

    /**
     * Configuración de bordes y radios de esquina.
     * <p>
     * Define los valores para esquinas redondeadas y bordes de componentes.
     */
    public static class Borders {
        /** Radio de esquina pequeño. */
        public static final float RADIUS_SMALL = 5f;
        /** Radio de esquina mediano. */
        public static final float RADIUS_MEDIUM = 10f;
        /** Radio de esquina grande. */
        public static final float RADIUS_LARGE = 20f;
        /** Ancho de borde estándar. */
        public static final float BORDER_WIDTH = 2f;
    }

    /**
     * Duraciones de animación en segundos.
     * <p>
     * Define los tiempos estándar para transiciones y efectos.
     */
    public static class Animation {
        /** Duración de animación rápida. */
        public static final float QUICK = 0.15f;
        /** Duración de animación normal. */
        public static final float NORMAL = 0.3f;
        /** Duración de animación lenta. */
        public static final float SLOW = 0.5f;
    }

    /**
     * Configuración de layout y dimensiones máximas.
     * <p>
     * Define límites de tamaño para contenedores y paneles.
     */
    public static class Layout {
        /** Ancho máximo del menú principal. */
        public static final float MENU_MAX_WIDTH = 600f;
        /** Ancho máximo de paneles. */
        public static final float PANEL_MAX_WIDTH = 500f;
        /** Alto máximo de listas. */
        public static final float LIST_MAX_HEIGHT = 400f;
    }
    
    /**
     * Crea un fondo semitransparente como Drawable.
     * 
     * @param r componente rojo (0-1)
     * @param g componente verde (0-1)
     * @param b componente azul (0-1)
     * @param a componente alfa (0-1), 0 es transparente, 1 es opaco
     * @return Drawable con el color y transparencia especificados
     */
    public static Drawable createSemiTransparentBackground(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
