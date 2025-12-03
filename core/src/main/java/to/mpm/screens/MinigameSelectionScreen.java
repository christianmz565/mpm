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
import to.mpm.ui.UISkinProvider;

/**
 * Pantalla de selección de minijuegos.
 * <p>
 * Solo el host puede seleccionar el minijuego.
 */
public class MinigameSelectionScreen implements Screen {
    private final Main game;
    private final boolean isHost;
    private Stage stage;
    private Skin skin;
    private Label statusLabel;

    /**
     * Constructor de la pantalla de selección de minijuegos.
     * 
     * @param game   instancia del juego principal
     * @param isHost indica si el jugador es el anfitrión
     */
    public MinigameSelectionScreen(Main game, boolean isHost) {
        this.game = game;
        this.isHost = isHost;
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Elegir microjuego", skin);
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(30).colspan(2).row();

        if (isHost) {
            statusLabel = new Label("Seleccione un microjuego para jugar:", skin);
            table.add(statusLabel).padBottom(20).colspan(2).row();

            for (MinigameType type : MinigameType.values()) {
                Table gameRow = new Table();

                Label nameLabel = new Label(type.getDisplayName(), skin);
                nameLabel.setFontScale(1.2f);
                gameRow.add(nameLabel).width(200).padRight(20);

                Label descLabel = new Label(type.getDescription(), skin);
                descLabel.setWrap(true);
                gameRow.add(descLabel).width(300).padRight(20);

                TextButton selectButton = new TextButton("Jugar", skin);
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
            TextButton backButton = new TextButton("Volver al lobby", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new LobbyScreen(game, true));
                    dispose();
                }
            });
            table.add(backButton).width(200).height(50).colspan(2).row();

        } else {
            statusLabel = new Label("Esperando a que el anfitrión seleccione el microjuego...", skin);
            table.add(statusLabel).padBottom(30).colspan(2).row();

            TextButton backButton = new TextButton("Volver al lobby", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new JoinLobbyScreen(game));
                    dispose();
                }
            });
            table.add(backButton).width(200).height(50).colspan(2).row();
        }
    }

    /**
     * Maneja la selección de un minijuego por parte del anfitrión.
     * 
     * @param type tipo de minijuego seleccionado
     */
    private void selectMinigame(MinigameType type) {
        statusLabel.setText("Starting " + type.getDisplayName() + "...");

        Packets.StartGame packet = new Packets.StartGame();
        packet.minigameType = type.name();
        packet.currentRound = 1;
        packet.totalRounds = 1;
        NetworkManager.getInstance().broadcastFromHost(packet);

        startGame(type);
    }

    /**
     * Inicia el juego con el minijuego seleccionado.
     * 
     * @param type tipo de minijuego a iniciar
     */
    private void startGame(MinigameType type) {
        game.setScreen(new MinigameIntroScreen(game, type, 1, 1));
        dispose();
    }

    /**
     * Renderiza la pantalla y actualiza la lógica del frame.
     *
     * @param delta tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Ajusta el tamaño de la pantalla.
     *
     * @param width  nuevo ancho de la ventana
     * @param height nuevo alto de la ventana
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Pausa la pantalla.
     */
    @Override
    public void pause() {
    }

    /**
     * Reanuda la pantalla.
     */
    @Override
    public void resume() {
    }

    /**
     * Oculta la pantalla.
     */
    @Override
    public void hide() {
    }

    /**
     * Libera los recursos utilizados por la pantalla.
     */
    @Override
    public void dispose() {
        stage.dispose();
    }

}
