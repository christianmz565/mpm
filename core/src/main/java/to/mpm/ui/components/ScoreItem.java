package to.mpm.ui.components;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import to.mpm.ui.UIStyles;

/**
 * Constructor de elementos de visualización de puntuación.
 * <p>
 * Muestra posición del jugador, nombre y puntuación en una fila formateada.
 */
public class ScoreItem {
    /** Skin para renderizar componentes UI. */
    private final Skin skin;
    /** Posición del jugador en el ranking. */
    private Integer rank;
    /** Nombre del jugador. */
    private String playerName;
    /** Puntuación del jugador. */
    private int score;
    /** Indica si el elemento debe resaltarse. */
    private boolean isHighlighted = false;

    /**
     * Construye un nuevo ScoreItem con el skin especificado.
     *
     * @param skin skin de UI para renderizar componentes
     */
    public ScoreItem(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece la posición del jugador.
     *
     * @param rank posición en el ranking
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem rank(int rank) {
        this.rank = rank;
        return this;
    }

    /**
     * Establece el nombre del jugador.
     *
     * @param playerName nombre a mostrar
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem playerName(String playerName) {
        this.playerName = playerName;
        return this;
    }

    /**
     * Establece la puntuación del jugador.
     *
     * @param score puntuación a mostrar
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem score(int score) {
        this.score = score;
        return this;
    }

    /**
     * Establece si el elemento debe resaltarse visualmente.
     *
     * @param highlighted true para resaltar, false para estilo normal
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem highlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        return this;
    }

    /**
     * Construye y devuelve una tabla con el elemento de puntuación completo.
     *
     * @return tabla contenedora con todos los componentes configurados
     */
    public Table build() {
        Table container = new Table(skin);

        if (isHighlighted) {
            container.setColor(UIStyles.Colors.SECONDARY);
        }

        Label rankLabel = new Label(getRankText(rank != null ? rank : 0), skin);
        rankLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        rankLabel.setAlignment(Align.center);
        container.add(rankLabel)
                .width(80f)
                .padLeft(UIStyles.Spacing.LARGE)
                .padTop(UIStyles.Spacing.MEDIUM)
                .padBottom(UIStyles.Spacing.MEDIUM)
                .center();

        Label nameLabel = new Label(playerName != null ? playerName : "Player", skin);
        nameLabel.setFontScale(UIStyles.Typography.SUBTITLE_SCALE);
        nameLabel.setAlignment(Align.center);
        container.add(nameLabel)
                .expand()
                .fill()
                .center()
                .padLeft(UIStyles.Spacing.LARGE)
                .padTop(UIStyles.Spacing.MEDIUM)
                .padBottom(UIStyles.Spacing.MEDIUM);

        Label scoreLabel = new Label(String.format("%d", score), skin);
        scoreLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        scoreLabel.setAlignment(Align.center);
        container.add(scoreLabel)
                .width(150f)
                .center()
                .padLeft(UIStyles.Spacing.LARGE)
                .padRight(UIStyles.Spacing.LARGE)
                .padTop(UIStyles.Spacing.MEDIUM)
                .padBottom(UIStyles.Spacing.MEDIUM);

        container.setFillParent(false);

        return container;
    }

    /**
     * Formatea el número de posición con el indicador ordinal.
     *
     * @param rank posición del jugador
     * @return texto formateado con indicador ordinal
     */
    private String getRankText(int rank) {
        return rank + "°";
    }
}
