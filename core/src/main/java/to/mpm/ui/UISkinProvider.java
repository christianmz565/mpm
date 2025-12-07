package to.mpm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Proveedor centralizado del skin de UI.
 * <p>
 * Gestiona una instancia compartida para evitar recargas repetidas del recurso.
 */
public final class UISkinProvider {
    /** Instancia compartida del skin. */
    private static Skin sharedSkin;

    private UISkinProvider() {
    }

    /**
     * Obtiene la instancia compartida del skin de UI.
     *
     * @return instancia reutilizable de Skin
     */
    public static Skin obtain() {
        if (sharedSkin == null) {
            sharedSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));
            loadCustomFonts(sharedSkin);
        }
        return sharedSkin;
    }

    /**
     * Carga fuentes personalizadas en el skin.
     *
     * @param skin skin al que agregar las fuentes
     */
    private static void loadCustomFonts(Skin skin) {
        BitmapFont sixtyfour12 = UIStyles.Fonts.loadSixtyfour(12, Color.WHITE);
        BitmapFont sixtyfour14 = UIStyles.Fonts.loadSixtyfour(14, Color.WHITE);
        BitmapFont sixtyfour16 = UIStyles.Fonts.loadSixtyfour(16, Color.WHITE);
        BitmapFont sixtyfour18 = UIStyles.Fonts.loadSixtyfour(18, Color.WHITE);
        BitmapFont sixtyfour20 = UIStyles.Fonts.loadSixtyfour(20, Color.WHITE);
        BitmapFont sixtyfour24 = UIStyles.Fonts.loadSixtyfour(24, Color.WHITE);
        BitmapFont sixtyfour32 = UIStyles.Fonts.loadSixtyfour(32, Color.WHITE);
        BitmapFont sixtyfour40 = UIStyles.Fonts.loadSixtyfour(40, Color.WHITE);
        BitmapFont sixtyfour48 = UIStyles.Fonts.loadSixtyfour(48, Color.WHITE);
        
        skin.add("sixtyfour-12", sixtyfour12, BitmapFont.class);
        skin.add("sixtyfour-14", sixtyfour14, BitmapFont.class);
        skin.add("sixtyfour-16", sixtyfour16, BitmapFont.class);
        skin.add("sixtyfour-18", sixtyfour18, BitmapFont.class);
        skin.add("sixtyfour-20", sixtyfour20, BitmapFont.class);
        skin.add("sixtyfour-24", sixtyfour24, BitmapFont.class);
        skin.add("sixtyfour-32", sixtyfour32, BitmapFont.class);
        skin.add("sixtyfour-40", sixtyfour40, BitmapFont.class);
        skin.add("sixtyfour-48", sixtyfour48, BitmapFont.class);
        skin.add("sixtyfour", sixtyfour32, BitmapFont.class);
    }

    /**
     * Libera la instancia compartida del skin si existe.
     */
    public static void dispose() {
        if (sharedSkin != null) {
            sharedSkin.dispose();
            sharedSkin = null;
        }
    }
}
