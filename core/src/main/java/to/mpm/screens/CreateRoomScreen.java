package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.network.NetworkManager;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.DuckPlaceholder;
import to.mpm.ui.components.InputField;
import to.mpm.ui.components.StyledButton;
import to.mpm.utils.FirewallHelper;
import java.io.IOException;

/**
 * Pantalla para crear una nueva sala de juego.
 * <p>
 * Muestra campos de entrada para Puerto, Rondas y Nombre de sala.
 */
public class CreateRoomScreen implements Screen {
    private final Main game; // !< instancia del juego principal
    private Stage stage; // !< stage para renderizar componentes de UI
    private Skin skin; // !< skin para estilizar componentes
    private TextField portField; // !< campo de entrada para el puerto
    private TextField roundsField; // !< campo de entrada para el número de rondas
    private TextField nameField; // !< campo de entrada para el nombre del jugador
    private Label statusLabel; // !< etiqueta para mostrar mensajes de estado

    /**
     * Construye una nueva pantalla de creación de sala.
     *
     * @param game instancia del juego principal
     */
    public CreateRoomScreen(Main game) {
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
        TextButton backButton = new StyledButton(skin)
                .text("< Volver")
                .width(120f)
                .height(45f)
                .onClick(() -> {
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                })
                .build();
        headerTable.add(backButton).size(120f, 45f).padRight(UIStyles.Spacing.MEDIUM);

        Label titleLabel = new Label("Crear sala", skin);
        titleLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        headerTable.add(titleLabel).left();

        formTable.add(headerTable).left().padBottom(UIStyles.Spacing.XLARGE).row();

        // Form fields with consistent label width for alignment
        float labelWidth = 100f;
        float fieldWidth = 175f;

        Table portRow = new Table();
        Label portLabel = new Label("Puerto", skin);
        portLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
        portRow.add(portLabel).width(labelWidth).left();
        portField = new InputField(skin)
                .defaultValue("61232")
                .width(fieldWidth)
                .filter(new TextField.TextFieldFilter.DigitsOnlyFilter())
                .maxLength(5)
                .buildField();
        portRow.add(portField).width(fieldWidth);
        formTable.add(portRow).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        Table roundsRow = new Table();
        Label roundsLabel = new Label("Rondas", skin);
        roundsLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
        roundsRow.add(roundsLabel).width(labelWidth).left();
        roundsField = new InputField(skin)
                .defaultValue("6")
                .width(fieldWidth)
                .filter(new TextField.TextFieldFilter.DigitsOnlyFilter())
                .maxLength(2)
                .buildField();
        roundsRow.add(roundsField).width(fieldWidth);
        formTable.add(roundsRow).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        Table nameRow = new Table();
        Label nameLabel = new Label("Nombre", skin);
        nameLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
        nameRow.add(nameLabel).width(labelWidth).left();
        nameField = new InputField(skin)
                .defaultValue("")
                .messageText("...")
                .width(fieldWidth)
                .maxLength(20)
                .buildField();
        nameRow.add(nameField).width(fieldWidth);
        formTable.add(nameRow).left().padBottom(UIStyles.Spacing.XLARGE).row();

        formTable.add(
                new StyledButton(skin)
                        .text("Crear")
                        .width(250f)
                        .height(60f)
                        .onClick(this::createRoom)
                        .build())
                .size(250f, 60f).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        statusLabel = new Label("", skin);
        statusLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        statusLabel.setWrap(true);
        formTable.add(statusLabel).width(UIStyles.Sizes.INPUT_WIDTH).left().row();

        Table duckTable = new DuckPlaceholder(skin).build();

        root.add(formTable).top().left().pad(UIStyles.Spacing.LARGE).expandY();
        root.add(duckTable).expand();
    }

    /**
     * Maneja la creación de una nueva sala de juego.
     * <p>
     * Valida los campos de entrada e inicia el servidor.
     */
    private void createRoom() {
        try {
            int port = Integer.parseInt(portField.getText());
            int rounds = Integer.parseInt(roundsField.getText());
            String playerName = nameField.getText().trim();

            if (rounds < 2) {
                statusLabel.setText("Las rondas deben ser al menos 2");
                return;
            }

            if (playerName.isEmpty()) {
                playerName = "Host";
            }

            statusLabel.setText("Creando sala...");

            NetworkManager.getInstance().hostGame(playerName, port);
            FirewallHelper.requestFirewallPermission(port);

            NetworkManager.getInstance().registerAdditionalClasses(
                    to.mpm.minigames.manager.ManagerPackets.RoomConfig.class,
                    to.mpm.minigames.manager.ManagerPackets.ShowScoreboard.class,
                    to.mpm.minigames.manager.ManagerPackets.StartNextRound.class,
                    to.mpm.minigames.manager.ManagerPackets.ShowResults.class,
                    to.mpm.minigames.manager.ManagerPackets.ReturnToLobby.class,
                    java.util.HashMap.class,
                    java.util.ArrayList.class);

            to.mpm.minigames.manager.ManagerPackets.RoomConfig roomConfig = new to.mpm.minigames.manager.ManagerPackets.RoomConfig(
                    rounds);
            NetworkManager.getInstance().broadcastFromHost(roomConfig);

            game.setScreen(new LobbyScreen(game, true, rounds));
            dispose();

        } catch (NumberFormatException e) {
            statusLabel.setText("Puerto y rondas deben ser números");
        } catch (IOException e) {
            statusLabel.setText("Error al crear sala: " + e.getMessage());
            Gdx.app.error("CreateRoomScreen", "Failed to start server", e);
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
