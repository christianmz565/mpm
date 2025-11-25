package to.mpm.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import to.mpm.ui.UIStyles;

/**
 * Constructor de elementos de lista de jugadores.
 * <p>
 * Muestra nombre del jugador, indicador de rol y puntuación opcional.
 */
public class PlayerListItem {
    private final Skin skin; //!< skin para renderizar componentes UI
    private String playerName; //!< nombre del jugador a mostrar
    private String role; //!< rol del jugador ("Creador", "Jugador", "Espectador")
    private Integer score; //!< puntuación opcional del jugador
    private float width = UIStyles.Sizes.BUTTON_WIDTH; //!< ancho del elemento en píxeles
    private float height = UIStyles.Sizes.PLAYER_ITEM_HEIGHT; //!< alto del elemento en píxeles

    /**
     * Construye un nuevo PlayerListItem con el skin especificado.
     *
     * @param skin skin de UI para renderizar componentes
     */
    public PlayerListItem(Skin skin) {
        this.skin = skin;
    }

    /**
     * Establece el nombre del jugador.
     *
     * @param playerName nombre a mostrar
     * @return esta instancia para encadenamiento de métodos
     */
    public PlayerListItem playerName(String playerName) {
        this.playerName = playerName;
        return this;
    }

    /**
     * Establece el rol del jugador.
     *
     * @param role rol del jugador
     * @return esta instancia para encadenamiento de métodos
     */
    public PlayerListItem role(String role) {
        this.role = role;
        return this;
    }

    /**
     * Establece la puntuación del jugador.
     *
     * @param score puntuación a mostrar
     * @return esta instancia para encadenamiento de métodos
     */
    public PlayerListItem score(Integer score) {
        this.score = score;
        return this;
    }

    /**
     * Establece el ancho del elemento.
     *
     * @param width ancho en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public PlayerListItem width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Establece el alto del elemento.
     *
     * @param height alto en píxeles
     * @return esta instancia para encadenamiento de métodos
     */
    public PlayerListItem height(float height) {
        this.height = height;
        return this;
    }

    /**
     * Construye y devuelve una tabla con el elemento de jugador completo.
     *
     * @return tabla contenedora con todos los componentes configurados
     */
    public Table build() {
        Table container = new Table(skin);
        container.setSize(width, height);

        Label roleIndicator = new Label("-", skin);
        roleIndicator.setColor(getRoleColor(role));
        container.add(roleIndicator).padLeft(UIStyles.Spacing.SMALL).padRight(UIStyles.Spacing.SMALL);

        Label nameLabel = new Label(playerName != null ? playerName : "Player", skin);
        container.add(nameLabel).expandX().left().padRight(UIStyles.Spacing.SMALL);

        if (role != null && !role.isEmpty()) {
            Label roleLabel = new Label(role, skin);
            roleLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
            roleLabel.setFontScale(UIStyles.Typography.SMALL_SCALE);
            container.add(roleLabel).padRight(UIStyles.Spacing.SMALL);
        }

        if (score != null) {
            Label scoreLabel = new Label(String.format("%d", score), skin);
            scoreLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
            container.add(scoreLabel).padRight(UIStyles.Spacing.MEDIUM);
        }

        return container;
    }

    /**
     * Determina el color del indicador basado en el rol del jugador.
     *
     * @param role rol del jugador
     * @return color correspondiente al rol
     */
    private Color getRoleColor(String role) {
        if (role == null)
            return UIStyles.Colors.TEXT_SECONDARY;

        switch (role.toLowerCase()) {
            case "creador":
            case "creator":
            case "host":
                return UIStyles.Colors.ACCENT;
            case "jugador":
            case "player":
                return UIStyles.Colors.PRIMARY;
            case "espectador":
            case "spectator":
                return UIStyles.Colors.SECONDARY;
            default:
                return UIStyles.Colors.TEXT_SECONDARY;
        }
    }
}
