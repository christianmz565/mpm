package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import to.mpm.ui.UIStyles;

/**
 * Constructor de campos de entrada etiquetados.
 */
public class InputField {
    /** Skin para renderizar componentes UI. */
    private final Skin skin;
    /** Texto de la etiqueta del campo. */
    private String labelText;
    /** Valor por defecto del campo de texto. */
    private String defaultValue = "";
    /** Texto de marcador de posición cuando está vacío. */
    private String messageText = "";
    /** Ancho del campo de texto en píxeles. */
    private float width = UIStyles.Sizes.INPUT_WIDTH;
    /** Alto del campo de texto en píxeles. */
    private float height = UIStyles.Sizes.INPUT_HEIGHT;
    /** Filtro opcional para validar entrada. */
    private TextField.TextFieldFilter filter;
    /** Longitud máxima de caracteres (0 = sin límite). */
    private int maxLength = 0;

    /**
     * Construye un nuevo InputField con el skin especificado.
     *
     * @param skin skin de UI para renderizar componentes
     */
    public InputField(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece el texto de la etiqueta del campo.
     *
     * @param labelText texto a mostrar como etiqueta
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField label(String labelText) {
        this.labelText = labelText;
        return this;
    }

    /**
     * Establece el valor por defecto del campo de texto.
     *
     * @param value texto inicial a mostrar en el campo
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField defaultValue(String value) {
        this.defaultValue = value;
        return this;
    }

    /**
     * Establece el texto de marcador de posición cuando el campo está vacío.
     *
     * @param messageText texto de marcador a mostrar
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField messageText(String messageText) {
        this.messageText = messageText;
        return this;
    }

    /**
     * Establece el ancho del campo de texto.
     *
     * @param width ancho en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Establece el alto del campo de texto.
     *
     * @param height alto en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField height(float height) {
        this.height = height;
        return this;
    }

    /**
     * Establece un filtro para validar la entrada del usuario.
     *
     * @param filter filtro de texto a aplicar
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField filter(TextField.TextFieldFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Establece la longitud máxima de caracteres permitida.
     *
     * @param maxLength número máximo de caracteres (0 = sin límite)
     * @return esta instancia para encadenamiento de métodos
     */
    public InputField maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Construye y devuelve una tabla contenedora con la etiqueta y el campo de
     * texto.
     *
     * @return tabla contenedora con los componentes configurados
     */
    public Table build() {
        Table container = new Table();

        if (labelText != null && !labelText.isEmpty()) {
            Label label = new Label(labelText, skin);
            container.add(label).padRight(UIStyles.Spacing.SMALL);
        }

        TextField textField = new TextField(defaultValue, skin);
        textField.setMessageText(messageText);
        if (filter != null) {
            textField.setTextFieldFilter(filter);
        }
        if (maxLength > 0) {
            textField.setMaxLength(maxLength);
        }

        container.add(textField).width(width).height(height);

        return container;
    }

    /**
     * Construye y devuelve únicamente el campo de texto sin tabla contenedora.
     *
     * @return campo de texto configurado
     */
    public TextField buildField() {
        TextField textField = new TextField(defaultValue, skin);
        
        com.badlogic.gdx.graphics.g2d.BitmapFont inputFont = skin.getFont("sixtyfour-24");
        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle(textField.getStyle());
        fieldStyle.font = inputFont;
        textField.setStyle(fieldStyle);
        
        textField.setMessageText(messageText);
        if (filter != null) {
            textField.setTextFieldFilter(filter);
        }
        if (maxLength > 0) {
            textField.setMaxLength(maxLength);
        }
        return textField;
    }
}
