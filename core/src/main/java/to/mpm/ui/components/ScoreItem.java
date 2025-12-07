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
    /** Tamaño de fuente para el rango (null para usar predeterminado). */
    private Integer rankFontSize = null;
    /** Tamaño de fuente para el nombre (null para usar predeterminado). */
    private Integer nameFontSize = null;
    /** Tamaño de fuente para la puntuación (null para usar predeterminado). */
    private Integer scoreFontSize = null;

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
     * Establece el tamaño de fuente para el rango.
     *
     * @param fontSize tamaño de fuente en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem rankFontSize(int fontSize) {
        this.rankFontSize = fontSize;
        return this;
    }

    /**
     * Establece el tamaño de fuente para el nombre del jugador.
     *
     * @param fontSize tamaño de fuente en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem nameFontSize(int fontSize) {
        this.nameFontSize = fontSize;
        return this;
    }

    /**
     * Establece el tamaño de fuente para la puntuación.
     *
     * @param fontSize tamaño de fuente en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public ScoreItem scoreFontSize(int fontSize) {
        this.scoreFontSize = fontSize;
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
        String rankFontName = rankFontSize != null ? "sixtyfour-" + rankFontSize : "sixtyfour-32";
        com.badlogic.gdx.graphics.g2d.BitmapFont rankFont = skin.getFont(rankFontName);
        Label.LabelStyle rankStyle = new Label.LabelStyle(rankFont, com.badlogic.gdx.graphics.Color.WHITE);
        rankLabel.setStyle(rankStyle);
        rankLabel.setAlignment(Align.center);
        container.add(rankLabel)
                .width(80f)
                .padLeft(UIStyles.Spacing.LARGE)
                .padTop(UIStyles.Spacing.MEDIUM)
                .padBottom(UIStyles.Spacing.MEDIUM)
                .center();

        Label nameLabel = new Label(playerName != null ? playerName : "Player", skin);
        String nameFontName = nameFontSize != null ? "sixtyfour-" + nameFontSize : "sixtyfour-24";
        com.badlogic.gdx.graphics.g2d.BitmapFont nameFont = skin.getFont(nameFontName);
        Label.LabelStyle nameStyle = new Label.LabelStyle(nameFont, com.badlogic.gdx.graphics.Color.WHITE);
        nameLabel.setStyle(nameStyle);
        nameLabel.setAlignment(Align.center);
        container.add(nameLabel)
                .expand()
                .fill()
                .center()
                .padLeft(UIStyles.Spacing.LARGE)
                .padTop(UIStyles.Spacing.MEDIUM)
                .padBottom(UIStyles.Spacing.MEDIUM);

        Label scoreLabel = new Label(String.format("%d", score), skin);
        String scoreFontName = scoreFontSize != null ? "sixtyfour-" + scoreFontSize : "sixtyfour-32";
        com.badlogic.gdx.graphics.g2d.BitmapFont scoreFont = skin.getFont(scoreFontName);
        Label.LabelStyle scoreStyle = new Label.LabelStyle(scoreFont, com.badlogic.gdx.graphics.Color.WHITE);
        scoreLabel.setStyle(scoreStyle);
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
