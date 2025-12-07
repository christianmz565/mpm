package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.StyledButton;

/**
 * Pantalla del menú principal de MicroPatosMania.
 * <p>
 * Muestra título y botones con estilo retro minimalista centrado.
 */
public class MainMenuScreen implements Screen {
    /** Instancia del juego principal. */
    private final Main game;
    /** Stage para renderizar componentes de UI. */
    private Stage stage;
    /** Skin para estilizar componentes. */
    private Skin skin;

    /**
     * Construye una nueva pantalla de menú principal.
     *
     * @param game instancia del juego principal
     */
    public MainMenuScreen(Main game) {
        this.game = game;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(stage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table menuTable = new Table();

        Label titleLabel = new Label("MicroPatosMania", skin);
        BitmapFont titleFont = skin.getFont("sixtyfour-32");
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, UIStyles.Colors.TEXT_PRIMARY);
        titleLabel.setStyle(titleStyle);
        menuTable.add(titleLabel).padBottom(UIStyles.Spacing.XXLARGE).row();

        menuTable.add(
                new StyledButton(skin)
                        .text("Crear")
                        .width(350f)
                        .height(80f)
                        .onClick(() -> {
                            game.setScreen(new CreateRoomScreen(game));
                            dispose();
                        })
                        .build())
                .size(350f, 80f).padBottom(UIStyles.Spacing.MEDIUM).row();

        menuTable.add(
                new StyledButton(skin)
                        .text("Unirse")
                        .width(350f)
                        .height(80f)
                        .onClick(() -> {
                            game.setScreen(new JoinLobbyScreen(game));
                            dispose();
                        })
                        .build())
                .size(350f, 80f).padBottom(UIStyles.Spacing.MEDIUM).row();

        menuTable.add(
                new StyledButton(skin)
                        .text("Salir")
                        .width(350f)
                        .height(80f)
                        .onClick(() -> {
                            Gdx.app.exit();
                        })
                        .build())
                .size(350f, 80f).row();

        root.add(menuTable).center();
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
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
    }
}
