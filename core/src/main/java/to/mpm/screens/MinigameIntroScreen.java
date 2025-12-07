package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.GameConstants;
import to.mpm.minigames.MinigameType;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;

/**
 * Pantalla de introducción mostrada antes de cada minijuego.
 * <p>
 * Muestra la descripción del minijuego y una vista previa.
 * Soporta tanto jugadores como espectadores.
 */
public class MinigameIntroScreen implements Screen {
    /** Instancia del juego principal. */
    private final Main game;
    /** Tipo de minijuego a mostrar. */
    private final MinigameType minigameType;
    /** Ronda actual. */
    private final int currentRound;
    /** Total de rondas. */
    private final int totalRounds;
    /** Indica si el usuario es espectador. */
    private final boolean isSpectator;

    /** Stage para renderizar componentes de UI. */
    private Stage stage;
    /** Skin para estilizar componentes. */
    private Skin skin;
    /** Temporizador para la cuenta regresiva. */
    private float timer;
    /** Etiqueta para mostrar el temporizador. */
    private Label timerLabel;
    /** Textura de la imagen de vista previa. */
    private Texture previewTexture;
    /** Imagen de vista previa del minijuego. */
    private Image previewImage;

    /**
     * Construye una nueva pantalla de introducción para un jugador.
     *
     * @param game         instancia del juego principal
     * @param minigameType tipo de minijuego a mostrar
     * @param currentRound ronda actual
     * @param totalRounds  total de rondas
     */
    public MinigameIntroScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds) {
        this(game, minigameType, currentRound, totalRounds, false);
    }

    /**
     * Construye una nueva pantalla de introducción.
     *
     * @param game         instancia del juego principal
     * @param minigameType tipo de minijuego a mostrar
     * @param currentRound ronda actual
     * @param totalRounds  total de rondas
     * @param isSpectator  indica si el usuario es espectador
     */
    public MinigameIntroScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds,
            boolean isSpectator) {
        this.game = game;
        this.minigameType = minigameType;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
        this.isSpectator = isSpectator;
        this.timer = GameConstants.Timing.INTRO_SCREEN_DURATION;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        Gdx.input.setInputProcessor(stage);
        game.getSettingsOverlayManager().attachStage(stage);

        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.setBackground(UIStyles.createSemiTransparentBackground(0.1f, 0.1f, 0.15f, 0.95f));
        stage.addActor(mainContainer);

        Table contentContainer = new Table();
        contentContainer.setBackground(UIStyles.createSemiTransparentBackground(0.2f, 0.2f, 0.25f, 0.9f));
        contentContainer.pad(UIStyles.Spacing.LARGE);

        float screenWidth = Gdx.graphics.getWidth();
        float descriptionWidth = Math.min(400f, screenWidth * 0.35f);
        float videoWidth = Math.min(320f, screenWidth * 0.3f);
        float videoHeight = videoWidth * 0.75f;

        Table leftSide = new Table();
        leftSide.top().left();

        Label titleLabel = new Label(minigameType.getDisplayName(), skin);
        com.badlogic.gdx.graphics.g2d.BitmapFont titleFont = skin.getFont("sixtyfour-16");
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, UIStyles.Colors.PRIMARY);
        titleLabel.setStyle(titleStyle);
        leftSide.add(titleLabel).left().padBottom(UIStyles.Spacing.SMALL).row();

        if (currentRound > 0 && totalRounds > 0) {
            Label roundLabel = new Label("Ronda " + currentRound + " de " + totalRounds, skin);
            com.badlogic.gdx.graphics.g2d.BitmapFont subtitleFont = skin.getFont("sixtyfour-14");
            Label.LabelStyle subtitleStyle = new Label.LabelStyle(subtitleFont, UIStyles.Colors.SECONDARY);
            roundLabel.setStyle(subtitleStyle);
            leftSide.add(roundLabel).left().padBottom(UIStyles.Spacing.SMALL).row();
        }

        if (isSpectator) {
            Label spectatorLabel = new Label("ESPECTANDO", skin);
            com.badlogic.gdx.graphics.g2d.BitmapFont headingFont = skin.getFont("sixtyfour-14");
            Label.LabelStyle headingStyle = new Label.LabelStyle(headingFont, UIStyles.Colors.ACCENT);
            spectatorLabel.setStyle(headingStyle);
            leftSide.add(spectatorLabel).left().padBottom(UIStyles.Spacing.MEDIUM).row();
        }

        Label descriptionLabel = new Label(minigameType.getDescription(), skin);
        com.badlogic.gdx.graphics.g2d.BitmapFont bodyFont = skin.getFont("sixtyfour-12");
        Label.LabelStyle bodyStyle = new Label.LabelStyle(bodyFont, UIStyles.Colors.TEXT_PRIMARY);
        descriptionLabel.setStyle(bodyStyle);
        descriptionLabel.setWrap(true);
        descriptionLabel.setAlignment(Align.topLeft);
        leftSide.add(descriptionLabel).width(descriptionWidth).left().padBottom(UIStyles.Spacing.MEDIUM).row();

        Label controlsLabel = new Label("Controles: " + minigameType.getControls(), skin);
        com.badlogic.gdx.graphics.g2d.BitmapFont smallFont = skin.getFont("sixtyfour-14");
        Label.LabelStyle smallStyle = new Label.LabelStyle(smallFont, UIStyles.Colors.TEXT_SECONDARY);
        controlsLabel.setStyle(smallStyle);
        controlsLabel.setWrap(true);
        leftSide.add(controlsLabel).width(descriptionWidth).left().row();

        Table rightSide = new Table();
        rightSide.center();

        Table previewContainer = createPreviewContainer(videoWidth, videoHeight);
        rightSide.add(previewContainer).size(videoWidth, videoHeight);

        contentContainer.add(leftSide).pad(UIStyles.Spacing.MEDIUM).top().left();
        contentContainer.add(rightSide).pad(UIStyles.Spacing.MEDIUM).center();

        mainContainer.add(contentContainer).center().expand();

        Table bottomContainer = new Table();
        timerLabel = new Label("Iniciando en " + (int) timer + "...", skin);
        com.badlogic.gdx.graphics.g2d.BitmapFont headingFont = skin.getFont("sixtyfour-14");
        Label.LabelStyle headingStyle = new Label.LabelStyle(headingFont, UIStyles.Colors.TEXT_SECONDARY);
        timerLabel.setStyle(headingStyle);
        bottomContainer.add(timerLabel);

        mainContainer.row();
        mainContainer.add(bottomContainer).padBottom(UIStyles.Spacing.LARGE).bottom();

        loadPreviewImage();

        Gdx.app.log("MinigameIntroScreen", "Showing intro for: " + minigameType.getDisplayName());
    }

    private void loadPreviewImage() {
        try {
            String imagePath = "images/guides/" + minigameType.name().toLowerCase() + ".png";
            if (!Gdx.files.internal(imagePath).exists()) {
                imagePath = "images/guides/placeholder.png";
            }

            previewTexture = new Texture(Gdx.files.internal(imagePath));
            previewImage.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(previewTexture)));
            Gdx.app.log("MinigameIntroScreen", "Loaded preview image: " + imagePath);
        } catch (Exception e) {
            Gdx.app.error("MinigameIntroScreen", "Error loading preview image", e);
        }
    }

    private Table createPreviewContainer(float width, float height) {
        Table placeholder = new Table();
        placeholder.setBackground(UIStyles.createSemiTransparentBackground(0.1f, 0.1f, 0.15f, 1f));

        previewImage = new Image();
        placeholder.add(previewImage).size(width, height);

        return placeholder;
    }



    @Override
    public void render(float delta) {
        timer -= delta;

        if (timerLabel != null) {
            timerLabel.setText("Iniciando en " + Math.max(1, (int) Math.ceil(timer)) + "...");
        }

        if (timer <= 0) {
            if (isSpectator) {
                game.setScreen(new SpectatorScreen(game, minigameType, currentRound, totalRounds));
            } else {
                game.setScreen(new GameScreen(game, minigameType, currentRound, totalRounds));
            }
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
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
        if (previewTexture != null) {
            previewTexture.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }
}
