package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.MinigameFactory;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;
import to.mpm.ui.UIStyles;

/**
 * Pantalla principal de juego que ejecuta el minijuego seleccionado.
 */
public class GameScreen implements Screen {
    private final Main game;
    private final MinigameType minigameType;
    private Minigame currentMinigame;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    // UI overlay
    private Stage uiStage;
    private Skin skin;
    private Label scoreLabel;

    public GameScreen(Main game, MinigameType minigameType) {
        this.game = game;
        this.minigameType = minigameType;
    }

    /**
     * Inicializa la pantalla de juego, configurando UI y manejadores de red.
     */
    @Override
    public void show() {
        batch = game.batch;
        shapeRenderer = new ShapeRenderer();

        // Initialize UI overlay
        uiStage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table uiRoot = new Table();
        uiRoot.setFillParent(true);
        uiRoot.top().right();
        uiStage.addActor(uiRoot);

        Table scoreContainer = new Table(skin);
        scoreContainer.pad(UIStyles.Spacing.MEDIUM);

        Table scoreContent = new Table();
        scoreLabel = new Label("0 pts", skin);
        scoreLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        scoreLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        scoreContent.add(scoreLabel).row();

        Label incrementLabel = new Label("+0\n+0\n+0\n+0", skin);
        incrementLabel.setFontScale(UIStyles.Typography.SMALL_SCALE);
        incrementLabel.setColor(UIStyles.Colors.SECONDARY);
        incrementLabel.setAlignment(com.badlogic.gdx.utils.Align.right);
        scoreContent.add(incrementLabel).padTop(UIStyles.Spacing.TINY).right();

        scoreContainer.add(scoreContent);
        uiRoot.add(scoreContainer).pad(UIStyles.Spacing.MEDIUM);

        // Initialize minigame
        int localPlayerId = NetworkManager.getInstance().getMyId();
        currentMinigame = MinigameFactory.createMinigame(minigameType, localPlayerId);
        currentMinigame.initialize();

        Gdx.app.log("GameScreen", "Started minigame: " + minigameType.getDisplayName());
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        // Handle input
        currentMinigame.handleInput(delta);

        // Update game logic
        currentMinigame.update(delta);

        // Update score display
        int localPlayerId = NetworkManager.getInstance().getMyId();
        int currentScore = currentMinigame.getScores().getOrDefault(localPlayerId, 0);
        scoreLabel.setText(currentScore + " pts");

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

        // Render minigame
        currentMinigame.render(batch, shapeRenderer);
        
        // Render UI overlay on top
        uiStage.act(delta);
        uiStage.draw();
    }

    /**
     * Maneja el redimensionamiento de la ventana.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    @Override
    public void resize(int width, int height) {
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
        if (currentMinigame != null) {
            currentMinigame.resize(width, height);
        }
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
        if (currentMinigame != null) {
            currentMinigame.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
    }
}
