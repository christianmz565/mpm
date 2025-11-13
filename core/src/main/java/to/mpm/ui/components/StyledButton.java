package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import to.mpm.ui.UIStyles;

/**
 * Constructor de botones con estilo consistente.
 * Utiliza el patrón Builder para configuración flexible de botones de interfaz.
 */
public class StyledButton {
    private final Skin skin; //!< skin para renderizar el botón
    private String text; //!< texto a mostrar en el botón
    private float width = UIStyles.Sizes.BUTTON_WIDTH; //!< ancho del botón en píxeles
    private float height = UIStyles.Sizes.BUTTON_HEIGHT; //!< alto del botón en píxeles
    private ChangeListener listener; //!< listener para eventos de clic
    private boolean disabled = false; //!< indica si el botón está deshabilitado
    private String style = "default"; //!< nombre del estilo visual del botón

    /**
     * Construye un nuevo StyledButton con el skin especificado.
     *
     * @param skin skin de UI para renderizar el botón
     */
    public StyledButton(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece el texto a mostrar en el botón.
     *
     * @param text texto del botón
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Establece el ancho del botón.
     *
     * @param width ancho en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Establece el alto del botón.
     *
     * @param height alto en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton height(float height) {
        this.height = height;
        return this;
    }

    /**
     * Establece tanto el ancho como el alto del botón.
     *
     * @param width  ancho en píxeles
     * @param height alto en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Establece la acción a ejecutar cuando se hace clic en el botón.
     *
     * @param action acción a ejecutar al hacer clic
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton onClick(Runnable action) {
        this.listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
        return this;
    }

    /**
     * Establece si el botón está deshabilitado.
     *
     * @param disabled true para deshabilitar, false para habilitar
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Establece el estilo visual del botón.
     *
     * @param style nombre del estilo a aplicar
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton style(String style) {
        this.style = style;
        return this;
    }

    /**
     * Construye y devuelve el botón configurado.
     *
     * @return botón de texto con la configuración especificada
     */
    public TextButton build() {
        TextButton button = new TextButton(text, skin, style);
        button.setSize(width, height);
        button.setDisabled(disabled);
        if (listener != null) {
            button.addListener(listener);
        }
        return button;
    }
}
