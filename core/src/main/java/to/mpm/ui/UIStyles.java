package to.mpm.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * Constantes centralizadas para estilos de UI.
 * Define colores, tipografía, espaciado y tamaños de componentes.
 */
public class UIStyles {

    /**
     * Paleta de colores
     */
    public static class Colors {
        public static final Color BACKGROUND = new Color(0.15f, 0.15f, 0.2f, 1f);
        public static final Color PRIMARY = new Color(0.2f, 0.6f, 0.9f, 1f);
        public static final Color SECONDARY = new Color(0.9f, 0.7f, 0.2f, 1f);
        public static final Color ACCENT = new Color(0.9f, 0.3f, 0.3f, 1f);
        public static final Color TEXT_PRIMARY = Color.WHITE;
        public static final Color TEXT_SECONDARY = new Color(0.8f, 0.8f, 0.8f, 1f);
        public static final Color TEXT_DISABLED = new Color(0.5f, 0.5f, 0.5f, 1f);
        public static final Color PANEL_BG = new Color(0.2f, 0.2f, 0.25f, 0.9f);
        public static final Color BUTTON_BG = new Color(0.3f, 0.3f, 0.35f, 1f);
        public static final Color BUTTON_HOVER = new Color(0.4f, 0.4f, 0.45f, 1f);
    }

    /**
     * Tipografía
     */
    public static class Typography {
        public static final float TITLE_SCALE = 2.0f;
        public static final float SUBTITLE_SCALE = 1.5f;
        public static final float HEADING_SCALE = 1.3f;
        public static final float BODY_SCALE = 1.0f;
        public static final float SMALL_SCALE = 0.8f;
    }

    /**
     * Espaciado entre componentes
     */
    public static class Spacing {
        public static final float TINY = 5f;
        public static final float SMALL = 10f;
        public static final float MEDIUM = 20f;
        public static final float LARGE = 30f;
        public static final float XLARGE = 50f;
        public static final float XXLARGE = 70f;
    }

    /**
     * Tamaños de componentes comunes
     */
    public static class Sizes {
        public static final float BUTTON_WIDTH = 200f;
        public static final float BUTTON_HEIGHT = 50f;
        public static final float INPUT_WIDTH = 200f;
        public static final float INPUT_HEIGHT = 40f;
        public static final float PLAYER_ITEM_HEIGHT = 60f;
        public static final float SCORE_ITEM_WIDTH = 1200f;
        public static final float SCORE_ITEM_HEIGHT = 50f;
        public static final float ICON_SIZE = 40f;
        public static final float DUCK_PLACEHOLDER_SIZE = 300f;
    }

    /**
     * Bordes y radio de esquinas
     */
    public static class Borders {
        public static final float RADIUS_SMALL = 5f;
        public static final float RADIUS_MEDIUM = 10f;
        public static final float RADIUS_LARGE = 20f;
        public static final float BORDER_WIDTH = 2f;
    }

    /**
     * Duraciones de animación (en segundos)
     */
    public static class Animation {
        public static final float QUICK = 0.15f;
        public static final float NORMAL = 0.3f;
        public static final float SLOW = 0.5f;
    }

    /**
     * Layout
     */
    public static class Layout {
        public static final float MENU_MAX_WIDTH = 600f;
        public static final float PANEL_MAX_WIDTH = 500f;
        public static final float LIST_MAX_HEIGHT = 400f;
    }
}
