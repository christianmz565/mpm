package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import to.mpm.ui.UIStyles;

/**
 * Constructor de botones con estilo consistente.
 */
public class StyledButton {
    /** Skin para renderizar el botón. */
    private final Skin skin;
    /** Texto a mostrar en el botón. */
    private String text;
    /** Ancho del botón en píxeles. */
    private float width = UIStyles.Sizes.BUTTON_WIDTH;
    /** Alto del botón en píxeles. */
    private float height = UIStyles.Sizes.BUTTON_HEIGHT;
    /** Listener para eventos de clic. */
    private ChangeListener listener;
    /** Indica si el botón está deshabilitado. */
    private boolean disabled = false;
    /** Nombre del estilo visual del botón. */
    private String style = "default";
    /** Tamaño de fuente personalizado (null para usar el predeterminado). */
    private Integer fontSize = null;
    /** Indica si el texto debe ajustarse automáticamente en varias líneas. */
    private boolean wrapText = false;

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
     * Establece el tamaño de fuente personalizado para el botón.
     *
     * @param fontSize tamaño de fuente en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton fontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * Habilita el ajuste automático de texto en varias líneas.
     *
     * @return esta instancia para encadenamiento de métodos
     */
    public StyledButton wrapText() {
        this.wrapText = true;
        return this;
    }

    /**
     * Construye y devuelve el botón configurado.
     *
     * @return botón de texto con la configuración especificada
     */
    public TextButton build() {
        TextButton button = new TextButton(text, skin, style);
        
        String fontName = fontSize != null ? "sixtyfour-" + fontSize : "sixtyfour-24";
        com.badlogic.gdx.graphics.g2d.BitmapFont buttonFont = skin.getFont(fontName);
        
        // Properly copy all style properties including backgrounds
        TextButton.TextButtonStyle originalStyle = button.getStyle();
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = originalStyle.fontColor;
        buttonStyle.downFontColor = originalStyle.downFontColor;
        buttonStyle.overFontColor = originalStyle.overFontColor;
        buttonStyle.checkedFontColor = originalStyle.checkedFontColor;
        buttonStyle.checkedOverFontColor = originalStyle.checkedOverFontColor;
        buttonStyle.disabledFontColor = originalStyle.disabledFontColor;
        
        // Copy all drawable states for proper button backgrounds
        buttonStyle.up = originalStyle.up;
        buttonStyle.down = originalStyle.down;
        buttonStyle.over = originalStyle.over;
        buttonStyle.checked = originalStyle.checked;
        buttonStyle.checkedOver = originalStyle.checkedOver;
        buttonStyle.disabled = originalStyle.disabled;
        buttonStyle.focused = originalStyle.focused;
        
        button.setStyle(buttonStyle);
        
        button.setSize(width, height);
        button.setDisabled(disabled);
        
        if (wrapText) {
            button.getLabel().setWrap(true);
        }
        
        if (listener != null) {
            button.addListener(listener);
        }
        return button;
    }
}
