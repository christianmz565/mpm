package to.mpm.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Proveedor centralizado del skin de UI.
 * Gestiona una instancia compartida para evitar recargas repetidas del recurso.
 */
public final class UISkinProvider {
    private static Skin sharedSkin; //!< instancia compartida del skin

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
        }
        return sharedSkin;
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
