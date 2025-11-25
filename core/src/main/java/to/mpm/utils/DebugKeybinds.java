package to.mpm.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import to.mpm.Main;
import to.mpm.minigames.MinigameType;
import to.mpm.screens.*;

/**
 * Keybinds de debug
 * 
 * Keybinds:
 * - F1: Menú Principal
 * - F2: Pantalla de Crear Sala
 * - F3: Pantalla de Unirse a Sala
 * - F4: Pantalla de Anfitrión de Sala (requiere crear un juego primero)
 * - F5: Pantalla de Juego (requiere configuración de red)
 * - F6: Pantalla de Espectador
 * - F7: Pantalla de Marcador
 * - F8: Pantalla de Resultados
 * - F9: Pantalla de Selección de Minijuego (debug)
 * - ESC: Alternar superposición de configuración
 */
public class DebugKeybinds {
    private final Main game;
    private boolean f1Pressed = false;
    private boolean f2Pressed = false;
    private boolean f3Pressed = false;
    private boolean f4Pressed = false;
    private boolean f5Pressed = false;
    private boolean f6Pressed = false;
    private boolean f7Pressed = false;
    private boolean f8Pressed = false;
    private boolean f9Pressed = false;
    private boolean escPressed = false;

    public DebugKeybinds(Main game) {
        this.game = game;
    }

    public void update() {
        f1Pressed = handleKey(f1Pressed, Input.Keys.F1, "Switching to Main Menu",
                () -> switchScreen(new MainMenuScreen(game)));

        f2Pressed = handleKey(f2Pressed, Input.Keys.F2, "Switching to Create Room Screen",
                () -> switchScreen(new CreateRoomScreen(game)));

        f3Pressed = handleKey(f3Pressed, Input.Keys.F3, "Switching to Join Lobby Screen",
                () -> switchScreen(new JoinLobbyScreen(game)));

        f4Pressed = handleKey(f4Pressed, Input.Keys.F4, "Switching to Host Lobby Screen",
                () -> switchScreen(new LobbyScreen(game, true)));

        f5Pressed = handleKey(f5Pressed, Input.Keys.F5, "Switching to Game Screen (Ball Movement)",
            () -> switchScreen(new GameScreen(game, MinigameType.BALL_MOVEMENT, 1, 3)));

        f6Pressed = handleKey(f6Pressed, Input.Keys.F6, "Switching to Spectator Screen",
                () -> switchScreen(new SpectatorScreen(game, to.mpm.minigames.MinigameType.BALL_MOVEMENT, 1, 5)));

        f7Pressed = handleKey(f7Pressed, Input.Keys.F7, "Switching to Scoreboard Screen",
                () -> {
                    java.util.Map<Integer, Integer> dummyScores = new java.util.HashMap<>();
                    dummyScores.put(0, 1000);
                    dummyScores.put(1, 800);
                    dummyScores.put(2, 600);
                    switchScreen(new ScoreboardScreen(game, dummyScores, 1, 3, 0));
                });

        f8Pressed = handleKey(f8Pressed, Input.Keys.F8, "Switching to Results Screen",
                () -> {
                    java.util.Map<Integer, Integer> dummyScores = new java.util.HashMap<>();
                    dummyScores.put(0, 1500);
                    dummyScores.put(1, 1200);
                    dummyScores.put(2, 900);
                    switchScreen(new ResultsScreen(game, dummyScores));
                });

        f9Pressed = handleKey(f9Pressed, Input.Keys.F9, "Switching to Minigame Selection Screen (debug)",
                () -> switchScreen(new MinigameSelectionScreen(game, true)));

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            if (!escPressed) {
                escPressed = true;
                Gdx.app.log("DebugKeybinds", "Toggling Settings overlay");
                game.toggleSettings();
            }
        } else {
            escPressed = false;
        }
    }

    private boolean handleKey(boolean wasPressed, int keyCode, String logMessage, Runnable action) {
        if (Gdx.input.isKeyPressed(keyCode)) {
            if (!wasPressed) {
                Gdx.app.log("DebugKeybinds", logMessage);
                action.run();
            }
            return true;
        }
        return false;
    }

    private void switchScreen(Screen nextScreen) {
        Screen currentScreen = game.getScreen();
        game.setScreen(nextScreen);
        if (currentScreen != null) {
            currentScreen.dispose();
        }
    }

    public static void printHelp() {
        Gdx.app.log("DebugKeybinds", "=== Debug Keybinds ===");
        Gdx.app.log("DebugKeybinds", "F1: Main Menu");
        Gdx.app.log("DebugKeybinds", "F2: Create Room Screen");
        Gdx.app.log("DebugKeybinds", "F3: Join Lobby Screen");
        Gdx.app.log("DebugKeybinds", "F4: Host Lobby Screen (starts server)");
        Gdx.app.log("DebugKeybinds", "F5: Game Screen (Ball Movement)");
        Gdx.app.log("DebugKeybinds", "F6: Spectator Screen");
        Gdx.app.log("DebugKeybinds", "F7: Scoreboard Screen");
        Gdx.app.log("DebugKeybinds", "F8: Results Screen");
        Gdx.app.log("DebugKeybinds", "F9: Minigame Selection Screen (debug)");
        Gdx.app.log("DebugKeybinds", "ESC: Toggle Settings overlay");
        Gdx.app.log("DebugKeybinds", "=====================");
    }
}
