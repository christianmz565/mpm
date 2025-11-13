package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import to.mpm.ui.UIStyles;

/**
 * Constructor de marcador de posición para la ilustración del pato.
 * Será reemplazado con renderizado de sprites una vez que los recursos estén
 * listos.
 */
public class DuckPlaceholder {
    private final Skin skin; //!< skin para renderizar componentes UI
    private float size = UIStyles.Sizes.DUCK_PLACEHOLDER_SIZE; //!< tamaño del marcador en píxeles
    private String text = "cuac"; //!< texto a mostrar en el marcador

    /**
     * Construye un nuevo DuckPlaceholder con el skin especificado.
     *
     * @param skin skin de UI para renderizar componentes
     */
    public DuckPlaceholder(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece el tamaño del marcador.
     *
     * @param size tamaño en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public DuckPlaceholder size(float size) {
        this.size = size;
        return this;
    }

    /**
     * Establece el texto a mostrar en el marcador.
     *
     * @param text texto del marcador
     * @return esta instancia para encadenamiento de métodos
     */
    public DuckPlaceholder text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Construye y devuelve una tabla con el marcador de posición configurado.
     *
     * @return tabla contenedora con el marcador
     */
    public Table build() {
        Table container = new Table(skin);
        container.setSize(size, size);

        Label placeholderLabel = new Label(text, skin);
        placeholderLabel.setWrap(true);
        placeholderLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        placeholderLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);

        container.add(placeholderLabel).center().expand().pad(UIStyles.Spacing.MEDIUM);

        return container;
    }
}
