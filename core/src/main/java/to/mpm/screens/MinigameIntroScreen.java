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
 * Intro screen shown before each minigame.
 * Displays a description of the minigame and a video preview.
 * Supports both players and spectators.
 */
public class MinigameIntroScreen implements Screen {
    private final Main game;
    private final MinigameType minigameType;
    private final int currentRound;
    private final int totalRounds;
    private final boolean isSpectator;
    
    private Stage stage;
    private Skin skin;
    private float timer;
    private Label timerLabel;
    private Texture previewTexture;
    private Image previewImage;

    public MinigameIntroScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds) {
        this(game, minigameType, currentRound, totalRounds, false);
    }

    public MinigameIntroScreen(Main game, MinigameType minigameType, int currentRound, int totalRounds, boolean isSpectator) {
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

        // Main container with dark semi-transparent background
        Table mainContainer = new Table();
        mainContainer.setFillParent(true);
        mainContainer.setBackground(UIStyles.createSemiTransparentBackground(0.1f, 0.1f, 0.15f, 0.95f));
        stage.addActor(mainContainer);

        // Content container - centered with padding
        Table contentContainer = new Table();
        contentContainer.setBackground(UIStyles.createSemiTransparentBackground(0.2f, 0.2f, 0.25f, 0.9f));
        contentContainer.pad(UIStyles.Spacing.LARGE);

        // Calculate responsive widths based on screen size
        float screenWidth = Gdx.graphics.getWidth();
        float descriptionWidth = Math.min(400f, screenWidth * 0.35f);
        float videoWidth = Math.min(320f, screenWidth * 0.3f);
        float videoHeight = videoWidth * 0.75f; // 4:3 aspect ratio

        // Left side - Description
        Table leftSide = new Table();
        leftSide.top().left();
        
        // Minigame name
        Label titleLabel = new Label(minigameType.getDisplayName(), skin);
        titleLabel.setFontScale(UIStyles.Typography.TITLE_SCALE);
        titleLabel.setColor(UIStyles.Colors.PRIMARY);
        leftSide.add(titleLabel).left().padBottom(UIStyles.Spacing.MEDIUM).row();
        
        // Round info
        if (currentRound > 0 && totalRounds > 0) {
            Label roundLabel = new Label("Round " + currentRound + " of " + totalRounds, skin);
            roundLabel.setFontScale(UIStyles.Typography.SUBTITLE_SCALE);
            roundLabel.setColor(UIStyles.Colors.SECONDARY);
            leftSide.add(roundLabel).left().padBottom(UIStyles.Spacing.MEDIUM).row();
        }
        
        // Spectator badge if spectating
        if (isSpectator) {
            Label spectatorLabel = new Label("ESPECTANDO", skin);
            spectatorLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
            spectatorLabel.setColor(UIStyles.Colors.ACCENT);
            leftSide.add(spectatorLabel).left().padBottom(UIStyles.Spacing.LARGE).row();
        }
        
        // Description
        String description = getMinigameDescription(minigameType);
        Label descriptionLabel = new Label(description, skin);
        descriptionLabel.setFontScale(UIStyles.Typography.BODY_SCALE);
        descriptionLabel.setColor(UIStyles.Colors.TEXT_PRIMARY);
        descriptionLabel.setWrap(true);
        descriptionLabel.setAlignment(Align.topLeft);
        leftSide.add(descriptionLabel).width(descriptionWidth).left().padBottom(UIStyles.Spacing.LARGE).row();
        
        // Controls hint
        String controls = getMinigameControls(minigameType);
        Label controlsLabel = new Label("Controls: " + controls, skin);
        controlsLabel.setFontScale(UIStyles.Typography.SMALL_SCALE);
        controlsLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        controlsLabel.setWrap(true);
        leftSide.add(controlsLabel).width(descriptionWidth).left().row();

        // Right side - Preview image
        Table rightSide = new Table();
        rightSide.center();
        
        // Create preview container with static image
        Table previewContainer = createPreviewContainer(videoWidth, videoHeight);
        rightSide.add(previewContainer).size(videoWidth, videoHeight);

        // Add sides to content container
        contentContainer.add(leftSide).pad(UIStyles.Spacing.MEDIUM).top().left();
        contentContainer.add(rightSide).pad(UIStyles.Spacing.MEDIUM).center();

        mainContainer.add(contentContainer).center().expand();

        // Timer label at the bottom
        Table bottomContainer = new Table();
        timerLabel = new Label("Starting in " + (int) timer + "...", skin);
        timerLabel.setFontScale(UIStyles.Typography.HEADING_SCALE);
        timerLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
        bottomContainer.add(timerLabel);
        
        mainContainer.row();
        mainContainer.add(bottomContainer).padBottom(UIStyles.Spacing.LARGE).bottom();

        // Load preview image
        loadPreviewImage();

        Gdx.app.log("MinigameIntroScreen", "Showing intro for: " + minigameType.getDisplayName());
    }

    private void loadPreviewImage() {
        try {
            // Try to load minigame-specific image first, fall back to placeholder
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
        
        // Create an image widget for the preview
        previewImage = new Image();
        placeholder.add(previewImage).size(width, height);
        
        return placeholder;
    }

    private String getMinigameDescription(MinigameType type) {
        return switch (type) {
            case CATCH_THEM_ALL -> "Catch falling ducks with your basket! Good ducks give you points, " +
                    "but watch out for bad ducks that will reduce your score. Be quick and accurate!";
            case SUMO -> "Push other players off the platform! The arena is a circular platform - " +
                    "collide with other players to knock them into the water. Last duck standing wins!";
            case THE_FINALE -> "The ultimate showdown! Only the top players compete in this final battle. " +
                    "Shoot other ducks to eliminate them. The last duck standing is the champion!";
        };
    }

    private String getMinigameControls(MinigameType type) {
        return switch (type) {
            case SUMO -> "WASD or Arrow Keys to move";
            case CATCH_THEM_ALL -> "A/D or Left/Right Arrows to move";
            case THE_FINALE -> "WASD to move, Mouse to aim, SPACE to shoot";
        };
    }

    @Override
    public void render(float delta) {
        timer -= delta;

        // Update timer label
        if (timerLabel != null) {
            timerLabel.setText("Starting in " + Math.max(1, (int) Math.ceil(timer)) + "...");
        }

        if (timer <= 0) {
            // Transition to game or spectator screen based on role
            if (isSpectator) {
                game.setScreen(new SpectatorScreen(game, minigameType, currentRound, totalRounds));
            } else {
                game.setScreen(new GameScreen(game, minigameType, currentRound, totalRounds));
            }
            return;
        }

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
        if (previewTexture != null) {
            previewTexture.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }
}
