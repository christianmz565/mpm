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
import to.mpm.network.NetworkManager;
import to.mpm.network.Packets;

/**
 * Pantalla de selección de minijuegos.
 * Solo el host puede seleccionar el minijuego.
 */
public class MinigameSelectionScreen implements Screen {
    private final Main game;
    private final boolean isHost;
    private Stage stage;
    private Skin skin;
    private Label statusLabel;

    public MinigameSelectionScreen(Main game, boolean isHost) {
        this.game = game;
        this.isHost = isHost;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Select Minigame", skin);
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(30).colspan(2).row();

        if (isHost) {
            statusLabel = new Label("Select a minigame to play:", skin);
            table.add(statusLabel).padBottom(20).colspan(2).row();

            // Create buttons for each minigame
            for (MinigameType type : MinigameType.values()) {
                Table gameRow = new Table();
                
                Label nameLabel = new Label(type.getDisplayName(), skin);
                nameLabel.setFontScale(1.2f);
                gameRow.add(nameLabel).width(200).padRight(20);

                Label descLabel = new Label(type.getDescription(), skin);
                descLabel.setWrap(true);
                gameRow.add(descLabel).width(300).padRight(20);

                TextButton selectButton = new TextButton("Play", skin);
                final MinigameType gameType = type;
                selectButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectMinigame(gameType);
                    }
                });
                gameRow.add(selectButton).width(100).height(40);

                table.add(gameRow).padBottom(15).colspan(2).row();
            }

            table.row().padTop(20);
            TextButton backButton = new TextButton("Back to Lobby", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new HostLobbyScreen(game));
                    dispose();
                }
            });
            table.add(backButton).width(200).height(50).colspan(2).row();

        } else {
            // Client just waits
            statusLabel = new Label("Waiting for host to select minigame...", skin);
            table.add(statusLabel).padBottom(30).colspan(2).row();

            TextButton backButton = new TextButton("Back to Lobby", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new JoinLobbyScreen(game));
                    dispose();
                }
            });
            table.add(backButton).width(200).height(50).colspan(2).row();
        }

        // Register handler for game start
        NetworkManager.getInstance().registerHandler(Packets.StartGame.class, this::onGameStart);
    }

    private void selectMinigame(MinigameType type) {
        statusLabel.setText("Starting " + type.getDisplayName() + "...");

        // Send packet to all clients
        Packets.StartGame packet = new Packets.StartGame();
        packet.minigameType = type.name();
        NetworkManager.getInstance().sendPacket(packet);

        // Start game locally
        startGame(type);
    }

    private void onGameStart(Packets.StartGame packet) {
        if (!isHost) {
            try {
                MinigameType type = MinigameType.valueOf(packet.minigameType);
                startGame(type);
            } catch (IllegalArgumentException e) {
                Gdx.app.error("MinigameSelection", "Unknown minigame type: " + packet.minigameType);
            }
        }
    }

    private void startGame(MinigameType type) {
        game.setScreen(new GameScreen(game, type));
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
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
