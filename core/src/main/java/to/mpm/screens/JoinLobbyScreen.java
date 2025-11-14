package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.network.NetworkConfig;
import to.mpm.network.NetworkManager;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.DuckPlaceholder;
import to.mpm.ui.components.InputField;
import to.mpm.ui.components.StyledButton;
import java.io.IOException;

/**
 * Pantalla para unirse a una sala de juego existente.
 * Muestra campos de entrada para IP, Puerto y Nombre del jugador.
 */
public class JoinLobbyScreen implements Screen {
    private final Main game; //!< instancia del juego principal
    private Stage stage; //!< stage para renderizar componentes de UI
    private Skin skin; //!< skin para estilizar componentes
    private TextField nameField; //!< campo de entrada para el nombre del jugador
    private TextField ipField; //!< campo de entrada para la IP del servidor
    private TextField portField; //!< campo de entrada para el puerto del servidor
    private Label statusLabel; //!< etiqueta para mostrar mensajes de estado

    /**
     * Construye una nueva pantalla de unión a sala.
     *
     * @param game instancia del juego principal
     */
    public JoinLobbyScreen(Main game) {
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

        Table formTable = new Table();
        formTable.top().left();

        Table headerTable = new Table();
        TextButton backButton = new TextButton("<-", skin);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        headerTable.add(backButton).padRight(UIStyles.Spacing.MEDIUM);

        Label titleLabel = new Label("MicroPatosMania\nUnirse a sala", skin);
        titleLabel.setFontScale(UIStyles.Typography.SUBTITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        headerTable.add(titleLabel).left();

        formTable.add(headerTable).left().padBottom(UIStyles.Spacing.LARGE).row();

        Table ipRow = new Table();
        Label ipLabel = new Label("IP", skin);
        ipRow.add(ipLabel).padRight(UIStyles.Spacing.SMALL).width(80f);
        ipField = new InputField(skin)
                .defaultValue("localhost")
                .messageText("...")
                .width(UIStyles.Sizes.INPUT_WIDTH)
                .buildField();
        ipRow.add(ipField);
        formTable.add(ipRow).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        Table portRow = new Table();
        Label portLabel = new Label("Puerto", skin);
        portRow.add(portLabel).padRight(UIStyles.Spacing.SMALL).width(80f);
        portField = new InputField(skin)
                .defaultValue(String.valueOf(NetworkConfig.DEFAULT_PORT))
                .width(UIStyles.Sizes.INPUT_WIDTH)
                .filter(new TextField.TextFieldFilter.DigitsOnlyFilter())
                .maxLength(5)
                .buildField();
        portRow.add(portField);
        formTable.add(portRow).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        Table nameRow = new Table();
        Label nameLabel = new Label("Nombre", skin);
        nameRow.add(nameLabel).padRight(UIStyles.Spacing.SMALL).width(80f);
        nameField = new InputField(skin)
                .defaultValue("")
                .messageText("...")
                .width(UIStyles.Sizes.INPUT_WIDTH)
                .maxLength(20)
                .buildField();
        nameRow.add(nameField);
        formTable.add(nameRow).left().padBottom(UIStyles.Spacing.LARGE).row();

        formTable.add(
                new StyledButton(skin)
                        .text("Unirse")
                        .onClick(this::joinGame)
                        .build())
                .left().padBottom(UIStyles.Spacing.MEDIUM).row();

        statusLabel = new Label("", skin);
        statusLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        formTable.add(statusLabel).left().row();

        Table duckTable = new DuckPlaceholder(skin).build();

        root.add(formTable).top().left().pad(UIStyles.Spacing.LARGE).expandY();
        root.add(duckTable).expand();
    }

    /**
     * Maneja la conexión a una sala de juego existente.
     * Valida los campos de entrada e intenta conectar al servidor.
     */
    private void joinGame() {
        String playerName = nameField.getText();
        String host = ipField.getText();
        int port;

        if (playerName.trim().isEmpty()) {
            statusLabel.setText("Please enter a player name");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid port number");
            return;
        }

        statusLabel.setText("Connecting...");

        try {
            NetworkManager.getInstance().joinGame(host, port, playerName);
            statusLabel.setText("Connected!");

            game.setScreen(new LobbyScreen(game, false, host, port));
            dispose();
        } catch (IOException e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
            Gdx.app.error("JoinLobbyScreen", "Failed to connect", e);
        }
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
    }
}
