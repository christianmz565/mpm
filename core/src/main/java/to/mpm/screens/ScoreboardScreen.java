package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Table.Debug;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.ui.UIStyles;
import to.mpm.ui.components.ScoreItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pantalla de marcador mostrando los puntajes actuales de los jugadores durante
 * el juego.
 * Los jugadores se ordenan por puntaje de forma descendente.
 */
public class ScoreboardScreen implements Screen {
    private final Main game; //!< Referencia a la instancia principal del juego
    private Stage stage; //!< Escenario para elementos de UI
    private Skin skin; //!< Skin para los componentes de UI
    private Table scoresContainer; //!< Contenedor de la lista de puntajes

    private List<PlayerScore> playerScores; //!< Lista de puntajes de jugadores

    /**
     * Constructor de la pantalla de marcador.
     *
     * @param game referencia a la instancia principal del juego
     */
    public ScoreboardScreen(Main game) {
        this.game = game;
        this.playerScores = new ArrayList<>();

        playerScores.add(new PlayerScore("Nombre", 123000));
        playerScores.add(new PlayerScore("Nombre", 123000));
        playerScores.add(new PlayerScore("Nombre", 123000));
        playerScores.add(new PlayerScore("Nombre", 123000));
        playerScores.add(new PlayerScore("Nombre", 123000));
        playerScores.add(new PlayerScore("Nombre", 123000));
    }

    /**
     * Inicializa la pantalla de marcador con el título y la lista de puntajes.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label titleLabel = new Label("Puntajes", skin);
        titleLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        root.add(titleLabel).padBottom(UIStyles.Spacing.LARGE).padTop(UIStyles.Spacing.LARGE).row();

        scoresContainer = new Table();
        ScrollPane scoresScroll = new ScrollPane(scoresContainer, skin);
        scoresScroll.setFadeScrollBars(false);

        root.add(scoresScroll).width(UIStyles.Layout.PANEL_MAX_WIDTH)
                .height(UIStyles.Layout.LIST_MAX_HEIGHT)
                .expand();

        updateScoresList();
    }

    /**
     * Actualiza la lista de puntajes, ordenándolos de mayor a menor.
     */
    private void updateScoresList() {
        scoresContainer.clear();

        playerScores.sort(Comparator.comparingInt((PlayerScore ps) -> ps.score).reversed());

        for (int i = 0; i < playerScores.size(); i++) {
            PlayerScore ps = playerScores.get(i);

            Table scoreItem = new ScoreItem(skin)
                    .playerName(ps.name)
                    .score(ps.score)
                    .build();

            scoresContainer.add(scoreItem).fillX().expandX().padBottom(UIStyles.Spacing.SMALL).row();
        }
    }

    /**
     * Actualiza el puntaje de un jugador específico.
     *
     * @param playerName nombre del jugador a actualizar
     * @param newScore   nuevo puntaje del jugador
     */
    public void updatePlayerScore(String playerName, int newScore) {
        for (PlayerScore ps : playerScores) {
            if (ps.name.equals(playerName)) {
                ps.score = newScore;
                updateScoresList();
                return;
            }
        }
        playerScores.add(new PlayerScore(playerName, newScore));
        updateScoresList();
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(UIStyles.Colors.BACKGROUND.r, UIStyles.Colors.BACKGROUND.g,
                UIStyles.Colors.BACKGROUND.b, UIStyles.Colors.BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Maneja el redimensionamiento de la ventana.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Método llamado cuando la aplicación es pausada.
     */
    @Override
    public void pause() {
    }

    /**
     * Método llamado cuando la aplicación es reanudada.
     */
    @Override
    public void resume() {
    }

    /**
     * Método llamado cuando esta pantalla deja de ser la pantalla actual.
     */
    @Override
    public void hide() {
    }

    /**
     * Libera los recursos utilizados por esta pantalla.
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    /**
     * Clase interna simple para almacenar información de puntaje de jugador.
     */
    private static class PlayerScore {
        String name; //!< Nombre del jugador
        int score; //!< Puntaje del jugador

        /**
         * Constructor del puntaje de jugador.
         *
         * @param name  nombre del jugador
         * @param score puntaje del jugador
         */
        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}
