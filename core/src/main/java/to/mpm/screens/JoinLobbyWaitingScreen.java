package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;
import to.mpm.ui.UIStyles;
import to.mpm.ui.components.PlayerListItem;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pantalla de sala mostrada después de unirse a un juego como cliente.
 * Muestra jugadores conectados y espera a que el anfitrión inicie el juego.
 */
public class JoinLobbyWaitingScreen implements Screen {
    private final Main game; //!< instancia del juego principal
    private final String serverIp; //!< dirección IP del servidor
    private final int serverPort; //!< puerto del servidor
    private Stage stage; //!< stage para renderizar componentes de UI
    private Skin skin; //!< skin para estilizar componentes
    private Label ipLabel; //!< etiqueta que muestra la IP del servidor
    private Label portLabel; //!< etiqueta que muestra el puerto del servidor
    private Table playersContainer; //!< contenedor de la lista de jugadores

    /**
     * Construye una nueva pantalla de espera en sala.
     *
     * @param game       instancia del juego principal
     * @param serverIp   dirección IP del servidor
     * @param serverPort puerto del servidor
     */
    public JoinLobbyWaitingScreen(Main game, String serverIp, int serverPort) {
        this.game = game;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table topRow = new Table();
        topRow.setFillParent(true);

        Table leftHeader = new Table();
        TextButton backButton = new TextButton("<-", skin);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                NetworkManager.getInstance().disconnect();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        leftHeader.add(backButton).padRight(UIStyles.Spacing.MEDIUM);

        Label titleLabel = new Label("MicroPatosMania\nSala", skin);
        titleLabel.setFontScale(UIStyles.Typography.SUBTITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        leftHeader.add(titleLabel).left();

        Table rightHeader = new Table();
        ipLabel = new Label("IP: " + serverIp, skin);
        portLabel = new Label("Puerto: " + serverPort, skin);
        rightHeader.add(ipLabel).padBottom(UIStyles.Spacing.TINY).row();
        rightHeader.add(portLabel).row();

        topRow.add(leftHeader).left().expand().pad(UIStyles.Spacing.LARGE);
        topRow.add(rightHeader).right().pad(UIStyles.Spacing.LARGE);

        root.add(topRow).top().fillX().row();

        playersContainer = new Table();
        playersContainer.top();
        ScrollPane playersScroll = new ScrollPane(playersContainer, skin);
        playersScroll.setFadeScrollBars(false);

        root.add(playersScroll).expand().fill().pad(UIStyles.Spacing.LARGE).row();

        Label statusLabel = new Label("Esperando a que el anfitrión inicie el juego...", skin);
        statusLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        root.add(statusLabel).bottom().pad(UIStyles.Spacing.LARGE).row();

        updatePlayersList();

        NetworkManager.getInstance().registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined);
        NetworkManager.getInstance().registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft);
        NetworkManager.getInstance().registerHandler(Packets.StartGame.class, this::onGameStart);
    }

    /**
     * Maneja el evento de nuevo jugador conectado.
     *
     * @param packet paquete con información del jugador que se unió
     */
    private void onPlayerJoined(Packets.PlayerJoined packet) {
        Gdx.app.log("JoinLobbyWaitingScreen", "Player " + packet.playerName + " joined!");
        updatePlayersList();
    }

    /**
     * Maneja el evento de jugador desconectado.
     *
     * @param packet paquete con información del jugador que salió
     */
    private void onPlayerLeft(Packets.PlayerLeft packet) {
        Gdx.app.log("JoinLobbyWaitingScreen", "Player " + packet.playerId + " left");
        updatePlayersList();
    }

    /**
     * Maneja el evento de inicio de juego enviado por el anfitrión.
     *
     * @param packet paquete de inicio de juego
     */
    private void onGameStart(Packets.StartGame packet) {
        if (packet.minigameType != null && !packet.minigameType.isEmpty()) {
            try {
                MinigameType type = MinigameType.valueOf(packet.minigameType);
                game.setScreen(new GameScreen(game, type));
                dispose();
            } catch (IllegalArgumentException e) {
                Gdx.app.error("JoinLobbyWaitingScreen", "Unknown minigame type: " + packet.minigameType);
            }
        } else {
            Gdx.app.error("JoinLobbyWaitingScreen", "Received StartGame packet without minigame type!");
        }
    }

    /**
     * Actualiza la lista visual de jugadores conectados.
     */
    private void updatePlayersList() {
        playersContainer.clear();

        ConcurrentHashMap<Integer, String> players = NetworkManager.getInstance().getConnectedPlayers();

        for (var entry : players.entrySet()) {
            int playerId = entry.getKey();
            String playerName = entry.getValue();

            String role = playerId == 0 ? "Anfitrión" : "Jugador";

            Table playerItem = new PlayerListItem(skin)
                    .playerName(playerName)
                    .role(role)
                    .build();

            playersContainer.add(playerItem).fillX().expandX().padBottom(UIStyles.Spacing.SMALL).row();
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
        skin.dispose();
    }
}
