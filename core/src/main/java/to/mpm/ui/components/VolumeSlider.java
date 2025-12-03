package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import to.mpm.ui.UIStyles;

/**
 * Constructor de controles deslizantes de volumen con etiqueta y visualización
 * de porcentaje.
 */
public class VolumeSlider {
    /** Skin para renderizar componentes UI. */
    private final Skin skin;
    /** Valor inicial del deslizador (25% por defecto). */
    private float initialValue = 0.25f;
    /** Valor mínimo del rango. */
    private float min = 0f;
    /** Valor máximo del rango. */
    private float max = 1f;
    /** Incremento entre valores. */
    private float stepSize = 0.01f;
    /** Ancho del deslizador en píxeles. */
    private float width = 300f;
    /** Listener para cambios de valor. */
    private ChangeListener listener;

    /**
     * Construye un nuevo VolumeSlider con el skin especificado.
     *
     * @param skin skin de UI para renderizar componentes
     */
    public VolumeSlider(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece el valor inicial del deslizador.
     *
     * @param value valor inicial entre min y max
     * @return esta instancia para encadenamiento de métodos
     */
    public VolumeSlider initialValue(float value) {
        this.initialValue = value;
        return this;
    }

    /**
     * Establece el rango de valores del deslizador.
     *
     * @param min valor mínimo
     * @param max valor máximo
     * @return esta instancia para encadenamiento de métodos
     */
    public VolumeSlider range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    /**
     * Establece el tamaño del paso entre valores.
     *
     * @param stepSize incremento entre valores
     * @return esta instancia para encadenamiento de métodos
     */
    public VolumeSlider stepSize(float stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    /**
     * Establece el ancho del deslizador.
     *
     * @param width ancho en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public VolumeSlider width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Establece el callback a ejecutar cuando cambia el valor.
     *
     * @param callback función a ejecutar con el nuevo valor
     * @return esta instancia para encadenamiento de métodos
     */
    public VolumeSlider onChange(OnVolumeChange callback) {
        this.listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof Slider) {
                    callback.onChanged(((Slider) actor).getValue());
                }
            }
        };
        return this;
    }

    /**
     * Construye y devuelve una tabla con el deslizador de volumen completo.
     *
     * @return tabla contenedora con todos los componentes configurados
     */
    public Table build() {
        Table container = new Table();

        Label volumeLabel = new Label("Volumen", skin);
        volumeLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        container.add(volumeLabel).padBottom(UIStyles.Spacing.MEDIUM).row();

        Slider slider = new Slider(min, max, stepSize, false, skin);
        slider.setValue(initialValue);

        Label percentageLabel = new Label(String.format("%.0f%%", initialValue * 100), skin);

        if (listener != null) {
            slider.addListener(listener);
        }

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = ((Slider) actor).getValue();
                percentageLabel.setText(String.format("%.0f%%", value * 100));
            }
        });

        container.add(slider).width(width).padBottom(UIStyles.Spacing.SMALL).row();
        container.add(percentageLabel);

        return container;
    }

    /**
     * Interfaz funcional para callbacks de cambio de volumen.
     */
    @FunctionalInterface
    public interface OnVolumeChange {
        /**
         * Método invocado cuando cambia el valor del volumen.
         *
         * @param newValue nuevo valor del volumen
         */
        void onChanged(float newValue);
    }
}
