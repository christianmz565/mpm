package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import to.mpm.ui.UIStyles;

/**
 * Contenedor reutilizable para mensajes de marcador de posición.
 * <p>
 * Crea un Label centrado con ancho controlado para evitar desbordes verticales.
 */
public class PlaceholderPanel {
    private final Skin skin; //!< skin para renderizar componentes UI
    private String text = ""; //!< texto del marcador de posición
    private float width = UIStyles.Layout.PANEL_MAX_WIDTH; //!< ancho máximo utilizado por el label
    private float padding = UIStyles.Spacing.XLARGE; //!< padding interno del contenedor

    /**
     * Construye un nuevo panel de marcador de posición.
     *
     * @param skin skin para estilizar el Label
     */
    public PlaceholderPanel(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece el texto a mostrar.
     *
     * @param text texto del marcador
     * @return esta instancia para encadenamiento
     */
    public PlaceholderPanel text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Establece el ancho máximo del label.
     *
     * @param width ancho deseado
     * @return esta instancia para encadenamiento
     */
    public PlaceholderPanel width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Establece el padding interno del contenedor.
     *
     * @param padding valor de padding
     * @return esta instancia para encadenamiento
     */
    public PlaceholderPanel padding(float padding) {
        this.padding = padding;
        return this;
    }

    /**
     * Construye el contenedor listo para agregarse a la escena.
     *
     * @return tabla con el marcador configurado
     */
    public Table build() {
        Table container = new Table();
        Label label = new Label(text, skin);
        label.setWrap(true);
        label.setAlignment(Align.center);
        container.add(label).width(width).pad(padding).fill();
        return container;
    }
}
