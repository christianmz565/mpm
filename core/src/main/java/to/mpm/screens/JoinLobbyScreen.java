package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkConfig;
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

import java.io.IOException;

/**
 * Código de prueba, solo para pruebas de red simples.
 */
public class JoinLobbyScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private TextField nameField;
    private TextField ipField;
    private TextField portField;
    private Label statusLabel;

    public JoinLobbyScreen(Main game) {
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

        Label titleLabel = new Label("Join Game", skin);
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).colspan(2).padBottom(30).row();

        Label nameLabel = new Label("Player Name:", skin);
        table.add(nameLabel).padRight(10);

        nameField = new TextField("Player", skin);
        table.add(nameField).width(200).padBottom(10).row();

        Label ipLabel = new Label("IP Address:", skin);
        table.add(ipLabel).padRight(10);

        ipField = new TextField("localhost", skin);
        table.add(ipField).width(200).padBottom(10).row();

        Label portLabel = new Label("Port:", skin);
        table.add(portLabel).padRight(10);

        portField = new TextField(String.valueOf(NetworkConfig.DEFAULT_PORT), skin);
        table.add(portField).width(200).padBottom(20).row();

        statusLabel = new Label("", skin);
        table.add(statusLabel).colspan(2).padBottom(20).row();

        TextButton joinButton = new TextButton("Join", skin);
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                joinGame();
            }
        });
        table.add(joinButton).width(200).height(50).colspan(2).padBottom(20).row();

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).width(200).height(50).colspan(2).row();

        NetworkManager.getInstance().registerHandler(Packets.StartGame.class, this::onGameStart);
    }

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
            statusLabel.setText("Connected! Waiting for host to start...");
        } catch (IOException e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
            Gdx.app.error("JoinLobbyScreen", "Failed to connect", e);
        }
    }

    private void onGameStart(Packets.StartGame packet) {
        // Client receives the minigame type directly and starts the game
        if (packet.minigameType != null && !packet.minigameType.isEmpty()) {
            try {
                MinigameType type = MinigameType.valueOf(packet.minigameType);
                game.setScreen(new GameScreen(game, type));
                dispose();
            } catch (IllegalArgumentException e) {
                Gdx.app.error("JoinLobbyScreen", "Unknown minigame type: " + packet.minigameType);
                statusLabel.setText("Error: Unknown game type!");
            }
        } else {
            // Old behavior: go to selection screen (shouldn't happen anymore)
            game.setScreen(new MinigameSelectionScreen(game, false));
            dispose();
        }
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
