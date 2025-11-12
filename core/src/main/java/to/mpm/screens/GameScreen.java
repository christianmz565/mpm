package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import to.mpm.Main;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.MinigameFactory;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;


/**
 * Pantalla principal de juego que ejecuta el minijuego seleccionado.
 */
public class GameScreen implements Screen {
    private final Main game;
    private final MinigameType minigameType;
    private Minigame currentMinigame;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    public GameScreen(Main game, MinigameType minigameType) {
        this.game = game;
        this.minigameType = minigameType;
    }

    @Override
    public void show() {
        batch = game.batch;
        shapeRenderer = new ShapeRenderer();

        int localPlayerId = NetworkManager.getInstance().getMyId();
        currentMinigame = MinigameFactory.createMinigame(minigameType, localPlayerId);
        currentMinigame.initialize();

        Gdx.app.log("GameScreen", "Started minigame: " + minigameType.getDisplayName());
    }

    @Override
    public void render(float delta) {
        // Handle input
        currentMinigame.handleInput(delta);

        // Update game logic
        currentMinigame.update(delta);

        // Check if ESC is pressed to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // Check if game is finished
        if (currentMinigame.isFinished()) {
            // TODO: Go to results screen
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // Render
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        currentMinigame.render(batch, shapeRenderer);
    }

    @Override
    public void resize(int width, int height) {
        if (currentMinigame != null) {
            currentMinigame.resize(width, height);
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        if (currentMinigame != null) {
            currentMinigame.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
