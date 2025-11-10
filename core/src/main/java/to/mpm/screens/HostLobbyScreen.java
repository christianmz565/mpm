package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.network.NetworkConfig;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;
import to.mpm.utils.FirewallHelper;

import java.io.IOException;

/**
 * Código de prueba, solo para pruebas de red simples.
 */
public class HostLobbyScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Label statusLabel;
    private Label playersLabel;
    private TextButton startButton;

    public HostLobbyScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create UI
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Host Lobby", skin);
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(30).row();

        statusLabel = new Label("Starting server...", skin);
        table.add(statusLabel).padBottom(20).row();

        playersLabel = new Label("Players: 0", skin);
        table.add(playersLabel).padBottom(30).row();

        startButton = new TextButton("Start Game", skin);
        startButton.setDisabled(true);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!startButton.isDisabled()) {
                    startGame();
                }
            }
        });
        table.add(startButton).width(200).height(50).padBottom(20).row();

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NetworkManager.getInstance().disconnect();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).width(200).height(50).row();

        try {
            FirewallHelper.requestFirewallPermission(NetworkConfig.DEFAULT_PORT);

            NetworkManager.getInstance().hostGame();
            statusLabel.setText("Server started on port " + NetworkConfig.DEFAULT_PORT);
            updatePlayerCount();

            NetworkManager.getInstance().registerHandler(Packets.PlayerJoined.class, this::onPlayerJoined);
            NetworkManager.getInstance().registerHandler(Packets.PlayerLeft.class, this::onPlayerLeft);

        } catch (IOException e) {
            statusLabel.setText("Failed to start server: " + e.getMessage());
            Gdx.app.error("HostLobbyScreen", "Failed to start server", e);
        }
    }

    private void onPlayerJoined(Packets.PlayerJoined packet) {
        statusLabel.setText("Player " + packet.playerName + " connected!");
        updatePlayerCount();
        startButton.setDisabled(false);
    }

    private void onPlayerLeft(Packets.PlayerLeft packet) {
        statusLabel.setText("Player " + packet.playerId + " disconnected");
        updatePlayerCount();
        if (NetworkManager.getInstance().getPlayerCount() <= 1) {
            startButton.setDisabled(true);
        }
    }

    private void updatePlayerCount() {
        int playerCount = NetworkManager.getInstance().getPlayerCount();
        playersLabel.setText("Players: " + playerCount);
    }

    private void startGame() {
        Packets.StartGame startPacket = new Packets.StartGame();
        NetworkManager.getInstance().sendPacket(startPacket);

        game.setScreen(new GameScreen(game));
        dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
