package to.mpm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import to.mpm.Main;
import to.mpm.minigames.MinigameType;
import to.mpm.network.NetworkConfig;
import to.mpm.network.NetworkManager;
import to.mpm.network.NetworkPacket;
import to.mpm.network.Packets;
import to.mpm.network.ServerEvents;
import to.mpm.network.handlers.ClientPacketContext;
import to.mpm.network.handlers.ClientPacketHandler;
import to.mpm.network.handlers.ServerPacketContext;
import to.mpm.network.handlers.ServerPacketHandler;
import to.mpm.ui.UIStyles;
import to.mpm.ui.UISkinProvider;
import to.mpm.ui.components.PlayerListItem;
import to.mpm.ui.components.StyledButton;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Pantalla de sala unificada que se muestra tanto para el host como para los
 * clientes.
 * Muestra información de la sala, jugadores conectados y controles apropiados
 * según el rol.
 */
public class LobbyScreen implements Screen {
    private final Main game; // !< instancia del juego principal
    private final boolean isHost; // !< indica si este jugador es el host
    private final String serverIp; // !< dirección IP del servidor (para clientes)
    private final int serverPort; // !< puerto del servidor (para clientes)
    private int rounds; // !< número de rondas configuradas
    private Stage stage; // !< stage para renderizar componentes de UI
    private Skin skin; // !< skin para estilizar componentes
    private Label ipLabel; // !< etiqueta que muestra la IP del servidor
    private Label portLabel; // !< etiqueta que muestra el puerto del servidor
    private Table playersContainer; // !< contenedor de la lista de jugadores
    private TextButton startButton; // !< botón para iniciar el juego (solo host)
    private TextButton spectatorButton; // !< botón para alternar modo espectador
    private final Set<Integer> spectators = new HashSet<>(); // !< conjunto de IDs de jugadores espectadores
    private LobbyClientHandler lobbyClientHandler;
    private LobbyServerHandler lobbyServerHandler;
    private StartGameClientHandler startGameClientHandler;

    /**
     * Construye una nueva pantalla de sala para el host.
     *
     * @param game   instancia del juego principal
     * @param isHost true si este jugador es el host, false si es cliente
     * @param rounds número de rondas (solo usado por host inicialmente)
     */
    public LobbyScreen(Main game, boolean isHost, int rounds) {
        this(game, isHost, null, 0);
        this.rounds = rounds;
    }

    /**
     * Construye una nueva pantalla de sala para el host (sin rondas especificadas).
     *
     * @param game   instancia del juego principal
     * @param isHost true si este jugador es el host, false si es cliente
     */
    public LobbyScreen(Main game, boolean isHost) {
        this(game, isHost, null, 0);
        this.rounds = 0; // Will be set via RoomConfig packet
    }

    /**
     * Construye una nueva pantalla de sala con información del servidor.
     *
     * @param game       instancia del juego principal
     * @param isHost     true si este jugador es el host, false si es cliente
     * @param serverIp   dirección IP del servidor (para clientes)
     * @param serverPort puerto del servidor (para clientes)
     */
    public LobbyScreen(Main game, boolean isHost, String serverIp, int serverPort) {
        this.game = game;
        this.isHost = isHost;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.rounds = 0; // Will be set via RoomConfig packet
    }

    /**
     * Inicializa y configura todos los componentes de la pantalla.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = UISkinProvider.obtain();
        game.getSettingsOverlayManager().attachStage(stage);

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
        if (isHost) {
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                ipLabel = new Label("IP: " + hostAddress, skin);
            } catch (Exception e) {
                ipLabel = new Label("IP: ---", skin);
            }
            portLabel = new Label("Puerto: " + NetworkConfig.DEFAULT_PORT, skin);
        } else {
            ipLabel = new Label("IP: " + serverIp, skin);
            portLabel = new Label("Puerto: " + serverPort, skin);
        }
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

        // Bottom row with spectator toggle and start button
        Table bottomRow = new Table();
        
        spectatorButton = new StyledButton(skin)
                .text("Modo Espectador")
                .onClick(this::toggleSpectator)
                .build();
        bottomRow.add(spectatorButton).padRight(UIStyles.Spacing.MEDIUM);

        if (isHost) {
            startButton = new StyledButton(skin)
                    .text("Iniciar Juego")
                    .disabled(true)
                    .onClick(this::startGame)
                    .build();
            bottomRow.add(startButton);

            root.add(bottomRow).bottom().pad(UIStyles.Spacing.LARGE).row();
        } else {
            root.add(bottomRow).bottom().pad(UIStyles.Spacing.LARGE).row();
            
            Label statusLabel = new Label("Esperando a que el anfitrión inicie el juego...", skin);
            statusLabel.setColor(UIStyles.Colors.TEXT_SECONDARY);
            root.add(statusLabel).bottom().padBottom(UIStyles.Spacing.MEDIUM).row();
        }

        updatePlayersList();

        // Enable start button if there are already players connected (returning from game)
        if (isHost && startButton != null && NetworkManager.getInstance().getPlayerCount() > 1) {
            startButton.setDisabled(false);
        }

        NetworkManager networkManager = NetworkManager.getInstance();
        lobbyClientHandler = new LobbyClientHandler();
        networkManager.registerClientHandler(lobbyClientHandler);

        // Register RoomConfig handler for both host and clients
        RoomConfigClientHandler roomConfigHandler = new RoomConfigClientHandler();
        networkManager.registerClientHandler(roomConfigHandler);

        if (isHost) {
            lobbyServerHandler = new LobbyServerHandler();
            networkManager.registerServerHandler(lobbyServerHandler);
        } else {
            startGameClientHandler = new StartGameClientHandler();
            networkManager.registerClientHandler(startGameClientHandler);
        }

        networkManager.ensureJoinHandshake();
    }

    /**
     * Alterna el modo espectador para el jugador local.
     */
    private void toggleSpectator() {
        int localPlayerId = NetworkManager.getInstance().getMyId();
        boolean isCurrentlySpectator = spectators.contains(localPlayerId);
        
        if (isCurrentlySpectator) {
            spectators.remove(localPlayerId);
            spectatorButton.setText("Modo Espectador");
        } else {
            spectators.add(localPlayerId);
            spectatorButton.setText("Modo Jugador");
        }
        
        // Broadcast spectator status
        Packets.SpectatorStatus packet = new Packets.SpectatorStatus();
        packet.playerId = localPlayerId;
        packet.isSpectator = !isCurrentlySpectator;
        NetworkManager.getInstance().sendPacket(packet);
        
        updatePlayersList();
        Gdx.app.log("LobbyScreen", "Spectator mode toggled: " + !isCurrentlySpectator);
    }

    /**
     * Maneja el evento de nuevo jugador conectado.
     *
     * @param packet paquete con información del jugador que se unió
     */
    private void onPlayerJoined(Packets.PlayerJoined packet) {
        Gdx.app.log("LobbyScreen", "Player " + packet.playerName + " connected!");
        updatePlayersList();

        if (isHost && startButton != null) {
            startButton.setDisabled(false);
        }
    }

    /**
     * Maneja el evento de jugador desconectado.
     *
     * @param packet paquete con información del jugador que salió
     */
    private void onPlayerLeft(Packets.PlayerLeft packet) {
        Gdx.app.log("LobbyScreen", "Player " + packet.playerId + " disconnected");
        spectators.remove(packet.playerId); // Remove from spectators if they were one
        updatePlayersList();

        if (isHost && startButton != null) {
            if (NetworkManager.getInstance().getPlayerCount() <= 1) {
                startButton.setDisabled(true);
            }
        }
    }
    
    /**
     * Maneja el cambio de estado de espectador de un jugador.
     *
     * @param packet paquete con información del cambio de estado
     */
    private void onSpectatorStatusChanged(Packets.SpectatorStatus packet) {
        if (packet.isSpectator) {
            spectators.add(packet.playerId);
        } else {
            spectators.remove(packet.playerId);
        }
        
        // Update button text if it's the local player
        int localPlayerId = NetworkManager.getInstance().getMyId();
        if (packet.playerId == localPlayerId && spectatorButton != null) {
            spectatorButton.setText(packet.isSpectator ? "Modo Jugador" : "Modo Espectador");
        }
        
        updatePlayersList();
        Gdx.app.log("LobbyScreen", "Player " + packet.playerId + " spectator status: " + packet.isSpectator);
    }

    /**
     * Maneja el evento de inicio de juego enviado por el anfitrión.
     * Solo usado por clientes.
     *
     * @param packet paquete de inicio de juego
     */
    private void onGameStart(Packets.StartGame packet) {
        if (packet.minigameType != null && !packet.minigameType.isEmpty()) {
            try {
                MinigameType type = MinigameType.valueOf(packet.minigameType);
                int roundNumber = packet.currentRound > 0 ? packet.currentRound : 1;
                int configuredRounds = packet.totalRounds > 0 ? packet.totalRounds : (rounds > 0 ? rounds : 1);
                
                // Initialize GameFlowManager on clients with spectator information
                to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
                if (!flowManager.isInitialized()) {
                    flowManager.initialize(configuredRounds);
                    flowManager.setSpectators(spectators);
                    Gdx.app.log("LobbyScreen", "Client initialized GameFlowManager with " + spectators.size() + " spectators");
                }
                
                // Check if local player is a spectator
                int localPlayerId = NetworkManager.getInstance().getMyId();
                if (spectators.contains(localPlayerId)) {
                    game.setScreen(new SpectatorScreen(game, type, roundNumber, configuredRounds));
                } else {
                    game.setScreen(new GameScreen(game, type, roundNumber, configuredRounds));
                }
                dispose();
            } catch (IllegalArgumentException e) {
                Gdx.app.error("LobbyScreen", "Unknown minigame type: " + packet.minigameType);
            }
        } else {
            Gdx.app.error("LobbyScreen", "Received StartGame packet without minigame type!");
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

            String role;
            if (spectators.contains(playerId)) {
                role = "Espectador";
            } else if (playerId == 0) {
                role = isHost ? "Creador" : "Anfitrión";
            } else {
                role = "Jugador";
            }

            Table playerItem = new PlayerListItem(skin)
                    .playerName(playerName)
                    .role(role)
                    .build();

            playersContainer.add(playerItem).fillX().expandX().padBottom(UIStyles.Spacing.SMALL).row();
        }
    }

    /**
     * Inicia el juego y notifica a todos los jugadores conectados.
     * Solo puede ser invocado por el host.
     */
    private void startGame() {
        if (!isHost) {
            Gdx.app.error("LobbyScreen", "Non-host tried to start game!");
            return;
        }

        if (rounds < 2) {
            Gdx.app.error("LobbyScreen", "Cannot start game: rounds not configured!");
            return;
        }

        // Initialize GameFlowManager with rounds configuration
        to.mpm.minigames.manager.GameFlowManager flowManager = to.mpm.minigames.manager.GameFlowManager.getInstance();
        flowManager.initialize(rounds);
        flowManager.setSpectators(spectators);
        flowManager.startRound();

        // Broadcast room config with spectator information to all clients
        to.mpm.minigames.manager.ManagerPackets.RoomConfig configPacket = 
            new to.mpm.minigames.manager.ManagerPackets.RoomConfig(rounds, new ArrayList<>(spectators));
        NetworkManager.getInstance().broadcastFromHost(configPacket);

        // Select first minigame randomly (count only non-spectator players)
        int activePlayerCount = NetworkManager.getInstance().getPlayerCount() - spectators.size();
        MinigameType selectedGame = to.mpm.minigames.selection.RandomGameSelection.selectGame(activePlayerCount);

        Gdx.app.log("LobbyScreen", "Selected game: " + selectedGame.getDisplayName() + " for " + activePlayerCount + " players (" + spectators.size() + " spectators)");

        Packets.StartGame packet = new Packets.StartGame();
        packet.minigameType = selectedGame.name();
        packet.currentRound = flowManager.getCurrentRound();
        packet.totalRounds = flowManager.getTotalRounds();
        NetworkManager.getInstance().broadcastFromHost(packet);

        // Host transitions to appropriate screen
        int localPlayerId = NetworkManager.getInstance().getMyId();
        if (spectators.contains(localPlayerId)) {
            game.setScreen(new SpectatorScreen(game, selectedGame, flowManager.getCurrentRound(), flowManager.getTotalRounds()));
        } else {
            game.setScreen(new GameScreen(game, selectedGame, flowManager.getCurrentRound(), flowManager.getTotalRounds()));
        }
        dispose();
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
        NetworkManager nm = NetworkManager.getInstance();
        if (lobbyClientHandler != null) {
            nm.unregisterClientHandler(lobbyClientHandler);
            lobbyClientHandler = null;
        }
        if (startGameClientHandler != null) {
            nm.unregisterClientHandler(startGameClientHandler);
            startGameClientHandler = null;
        }
        if (lobbyServerHandler != null) {
            nm.unregisterServerHandler(lobbyServerHandler);
            lobbyServerHandler = null;
        }
        stage.dispose();
    }

    /**
     * Manejador de paquetes de la sala para clientes.
     */
    private final class LobbyClientHandler implements ClientPacketHandler {
        @Override
        public List<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(Packets.PlayerJoined.class, Packets.PlayerLeft.class, Packets.SpectatorStatus.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerJoined joined) {
                onPlayerJoined(joined);
            } else if (packet instanceof Packets.PlayerLeft left) {
                onPlayerLeft(left);
            } else if (packet instanceof Packets.SpectatorStatus spectatorStatus) {
                onSpectatorStatusChanged(spectatorStatus);
            }
        }
    }

    /**
     * Manejador de paquetes para configuración de sala.
     * Recibido tanto por host como clientes.
     */
    private final class RoomConfigClientHandler implements ClientPacketHandler {
        @Override
        public List<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(to.mpm.minigames.manager.ManagerPackets.RoomConfig.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof to.mpm.minigames.manager.ManagerPackets.RoomConfig roomConfig) {
                rounds = roomConfig.rounds;
                // Sync spectator list from host
                if (roomConfig.spectatorIds != null) {
                    spectators.clear();
                    spectators.addAll(roomConfig.spectatorIds);
                    updatePlayersList();
                }
                Gdx.app.log("LobbyScreen", "Room configured with " + rounds + " rounds and " + spectators.size() + " spectators");
            }
        }
    }

    /**
     * Manejador de paquetes para iniciar el juego (clientes).
     */
    private final class StartGameClientHandler implements ClientPacketHandler {
        @Override
        public List<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(Packets.StartGame.class);
        }

        @Override
        public void handle(ClientPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.StartGame startGame) {
                onGameStart(startGame);
            }
        }
    }

    /**
     * Manejador de paquetes de la sala para el servidor.
     */
    private static final class LobbyServerHandler implements ServerPacketHandler {

        @Override
        public List<Class<? extends NetworkPacket>> receivablePackets() {
            return List.of(Packets.PlayerJoinRequest.class, ServerEvents.ClientDisconnected.class, Packets.SpectatorStatus.class);
        }

        @Override
        public void handle(ServerPacketContext context, NetworkPacket packet) {
            if (packet instanceof Packets.PlayerJoinRequest joinRequest) {
                handleJoin(context, joinRequest);
            } else if (packet instanceof ServerEvents.ClientDisconnected disconnected) {
                handleDisconnect(context, disconnected);
            } else if (packet instanceof Packets.SpectatorStatus spectatorStatus) {
                // Relay spectator status to all clients
                context.broadcast(spectatorStatus);
            }
        }

        /**
         * Maneja una solicitud de unión de un jugador.
         *
         * @param context contexto del paquete del servidor
         * @param request paquete de solicitud de unión
         */
        private void handleJoin(ServerPacketContext context, Packets.PlayerJoinRequest request) {
            int newPlayerId = context.getServer().allocatePlayerId();
            context.getServer().bindConnectionToPlayer(context.getConnection(), newPlayerId, request.playerName);

            Packets.PlayerJoined selfPacket = new Packets.PlayerJoined();
            selfPacket.playerId = newPlayerId;
            selfPacket.playerName = request.playerName;
            selfPacket.correlationId = request.correlationId;
            context.reply(selfPacket);

            Packets.PlayerJoined broadcastPacket = new Packets.PlayerJoined();
            broadcastPacket.playerId = newPlayerId;
            broadcastPacket.playerName = request.playerName;
            broadcastPacket.existingPlayer = false;
            context.broadcastExceptSender(broadcastPacket);

            context.getServer().getConnectedPlayers().forEach((playerId, playerName) -> {
                if (playerId == newPlayerId) {
                    return;
                }
                Packets.PlayerJoined existing = new Packets.PlayerJoined();
                existing.playerId = playerId;
                existing.playerName = playerName;
                existing.existingPlayer = true;
                context.reply(existing);
            });

            Gdx.app.log("LobbyServer", "Player joined: " + request.playerName + " (ID: " + newPlayerId + ")");
        }

        /**
         * Maneja la desconexión de un jugador.
         *
         * @param context contexto del paquete del servidor
         * @param event   evento de desconexión del cliente
         */
        private void handleDisconnect(ServerPacketContext context, ServerEvents.ClientDisconnected event) {
            if (event.playerId < 0) {
                return;
            }
            Packets.PlayerLeft left = new Packets.PlayerLeft();
            left.playerId = event.playerId;
            context.broadcast(left);
            Gdx.app.log("LobbyServer", "Player disconnected: " + event.playerName + " (ID: " + event.playerId + ")");
        }
    }
}
