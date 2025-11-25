package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.Minigame;
import to.mpm.minigames.MinigameFactory;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkManager;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;

/**
 * Pantalla de espectador para observar el juego sin participar.
 * Muestra el minijuego en curso sin permitir interacción.
 */
public class SpectatorScreen implements Screen {
    private final Main game;
    private final MinigameType minigameType;
    private final int currentRound;
    private final int totalRounds;
    
    private Minigame currentMinigame;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Stage uiStage;
    private Skin skin;
    private Label timerLabel;
    private Label roundLabel;
    private float gameTimer = 60f;

    /**
     * Construye una nueva pantalla de espectador.
     *
     * @param game instancia del juego principal
     * @param minigameType tipo de minijuego que se está jugando
     * @param currentRound ronda actual
     * @param totalRounds total de rondas
     */
    public SpectatorScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds) {
        this.game = game;
        this.minigameType = minigameType;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    @Override
    public void show() {
        batch = game.batch;
        shapeRenderer = new ShapeRenderer();

        uiStage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(uiStage);

        Table uiRoot = new Table();
        uiRoot.setFillParent(true);
        uiRoot.top();
        uiStage.addActor(uiRoot);

        // Spectator indicator and game info at top
        Table topContainer = new Table();
        topContainer.pad(UIStyles.Spacing.MEDIUM);

        Label spectatorLabel = new Label("SPECTATING", skin);
        spectatorLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        spectatorLabel.setColor(UIStyles.Colors.ACCENT);
        topContainer.add(spectatorLabel).padBottom(UIStyles.Spacing.SMALL).row();

        if (currentRound > 0 && totalRounds > 0) {
            roundLabel = new Label("Round " + currentRound + "/" + totalRounds, skin);
            roundLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            roundLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
            topContainer.add(roundLabel).padBottom(UIStyles.Spacing.TINY).row();
        }

        // Timer (unless finale)
        boolean isFinale = minigameType == MinigameType.THE_FINALE;
        if (!isFinale) {
            timerLabel = new Label("Time: 60", skin);
            timerLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            timerLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
            topContainer.add(timerLabel).row();
        }

        uiRoot.add(topContainer).expandX().center().row();

        // Initialize minigame (local instance for spectating)
        int localPlayerId = NetworkManager.getInstance().getMyId();
        currentMinigame = MinigameFactory.createMinigame(minigameType, localPlayerId);
        currentMinigame.initialize();

        Gdx.app.log("SpectatorScreen", "Spectating minigame: " + minigameType.getDisplayName());
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        // Update minigame (but don't handle input)
        currentMinigame.update(delta);

        // Update timer (unless finale)
        boolean isFinale = minigameType == MinigameType.THE_FINALE;
        if (!isFinale && timerLabel != null) {
            gameTimer -= delta;
            int seconds = Math.max(0, (int) Math.ceil(gameTimer));
            timerLabel.setText("Time: " + seconds);
        }

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render minigame
        currentMinigame.render(batch, shapeRenderer);

        // Render UI overlay
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
        uiStage.getViewport().update(width, height, true);
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
    }
}
