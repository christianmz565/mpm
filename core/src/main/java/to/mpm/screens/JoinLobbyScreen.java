package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.network.NetworkConfig;
import to.mpm.network.NetworkManager;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.InputField;
import to.mpm.ui.components.StyledButton;
import java.io.IOException;

/**
 * Pantalla para unirse a una sala de juego existente.
 * <p>
 * Muestra campos de entrada con estilo retro minimalista centrado.
 */
public class JoinLobbyScreen implements Screen {
    /** Instancia del juego principal. */
    private final Main game;
    /** Stage para renderizar componentes de UI. */
    private Stage stage;
    /** Skin para estilizar componentes. */
    private Skin skin;
    /** Campo de entrada para el nombre del jugador. */
    private TextField nameField;
    /** Campo de entrada para la IP del servidor. */
    private TextField ipField;
    /** Campo de entrada para el puerto del servidor. */
    private TextField portField;
    /** Etiqueta para mostrar mensajes de estado. */
    private Label statusLabel;

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

        TextButton backButton = new StyledButton(skin)
                .text("< Volver")
                .width(180f)
                .fontSize(18)
                .height(60f)
                .onClick(() -> {
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                })
                .build();
        formTable.add(backButton).size(180f, 60f).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        Label titleLabel = new Label("Unirse a sala", skin);
        BitmapFont titleFont = skin.getFont("sixtyfour-24");
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, UIStyles.Colors.TEXT_PRIMARY);
        titleLabel.setStyle(titleStyle);
        formTable.add(titleLabel).padBottom(UIStyles.Spacing.LARGE).row();

        float labelWidth = 150f;
        float fieldWidth = 250f;

        BitmapFont bodyFont = skin.getFont("sixtyfour-24");
        Label.LabelStyle bodyStyle = new Label.LabelStyle(bodyFont, UIStyles.Colors.TEXT_PRIMARY);

        Table ipRow = new Table();
        Label ipLabel = new Label("IP", skin);
        ipLabel.setStyle(bodyStyle);
        ipRow.add(ipLabel).width(labelWidth).left();
        ipField = new InputField(skin)
                .defaultValue("localhost")
                .messageText("...")
                .width(fieldWidth)
                .buildField();
        ipRow.add(ipField).width(fieldWidth);
        formTable.add(ipRow).padBottom(UIStyles.Spacing.MEDIUM).row();

        Table portRow = new Table();
        Label portLabel = new Label("Puerto", skin);
        portLabel.setStyle(bodyStyle);
        portRow.add(portLabel).width(labelWidth).left();
        portField = new InputField(skin)
                .defaultValue(String.valueOf(NetworkConfig.DEFAULT_PORT))
                .width(fieldWidth)
                .filter(new TextField.TextFieldFilter.DigitsOnlyFilter())
                .maxLength(5)
                .buildField();
        portRow.add(portField).width(fieldWidth);
        formTable.add(portRow).padBottom(UIStyles.Spacing.MEDIUM).row();

        Table nameRow = new Table();
        Label nameLabel = new Label("Nombre", skin);
        nameLabel.setStyle(bodyStyle);
        nameRow.add(nameLabel).width(labelWidth).left();
        nameField = new InputField(skin)
                .defaultValue("")
                .messageText("...")
                .width(fieldWidth)
                .maxLength(20)
                .buildField();
        nameRow.add(nameField).width(fieldWidth);
        formTable.add(nameRow).padBottom(UIStyles.Spacing.LARGE).row();

        formTable.add(
                new StyledButton(skin)
                        .text("Unirse")
                        .width(350f)
                        .height(60f)
                        .onClick(this::joinGame)
                        .build())
                .size(350f, 60f).padBottom(UIStyles.Spacing.MEDIUM).row();

        statusLabel = new Label("", skin);
        statusLabel.setStyle(bodyStyle);
        formTable.add(statusLabel).row();

        root.add(formTable).center();
    }

    /**
     * Maneja la conexión a una sala de juego existente.
     * <p>
     * Valida los campos de entrada e intenta conectar al servidor.
     */
    private void joinGame() {
        String playerName = nameField.getText();
        String host = ipField.getText();
        int port;

        if (playerName.trim().isEmpty()) {
            statusLabel.setText("Por favor ingresa un nombre de jugador");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            statusLabel.setText("Número de puerto inválido");
            return;
        }

        statusLabel.setText("Conectando...");

        try {
            NetworkManager.getInstance().joinGame(host, port, playerName);

            NetworkManager.getInstance().registerAdditionalClasses(
                    to.mpm.minigames.manager.ManagerPackets.RoomConfig.class,
                    to.mpm.minigames.manager.ManagerPackets.ShowScoreboard.class,
                    to.mpm.minigames.manager.ManagerPackets.StartNextRound.class,
                    to.mpm.minigames.manager.ManagerPackets.ShowResults.class,
                    to.mpm.minigames.manager.ManagerPackets.ReturnToLobby.class,
                    java.util.HashMap.class,
                    java.util.ArrayList.class);

            statusLabel.setText("¡Conectado!");

            game.setScreen(new LobbyScreen(game, false, host, port));
            dispose();
        } catch (IOException e) {
            statusLabel.setText("Conexión fallida: " + e.getMessage());
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
