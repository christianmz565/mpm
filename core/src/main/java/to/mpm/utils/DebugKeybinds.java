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
        if (Gdx.input.isKeyPressed(Input.Keys.F1)) {
            if (!f1Pressed) {
                f1Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Main Menu");
                Screen currentScreen = game.getScreen();
                game.setScreen(new MainMenuScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f1Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
            if (!f2Pressed) {
                f2Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Create Room Screen");
                Screen currentScreen = game.getScreen();
                game.setScreen(new CreateRoomScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f2Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            if (!f3Pressed) {
                f3Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Join Lobby Screen");
                Screen currentScreen = game.getScreen();
                game.setScreen(new JoinLobbyScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f3Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F4)) {
            if (!f4Pressed) {
                f4Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Host Lobby Screen");
                Screen currentScreen = game.getScreen();
                game.setScreen(new HostLobbyScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f4Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F5)) {
            if (!f5Pressed) {
                f5Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Game Screen (Ball Movement)");
                Screen currentScreen = game.getScreen();
                game.setScreen(new GameScreen(game, MinigameType.BALL_MOVEMENT));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f5Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F6)) {
            if (!f6Pressed) {
                f6Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Spectator Screen");
                Screen currentScreen = game.getScreen();
                game.setScreen(new SpectatorScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f6Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F7)) {
            if (!f7Pressed) {
                f7Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Scoreboard Screen");
                Screen currentScreen = game.getScreen();
                game.setScreen(new ScoreboardScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f7Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F8)) {
            if (!f8Pressed) {
                f8Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Results Screen");
                Screen currentScreen = game.getScreen();
                game.setScreen(new ResultsScreen(game));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f8Pressed = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F9)) {
            if (!f9Pressed) {
                f9Pressed = true;
                Gdx.app.log("DebugKeybinds", "Switching to Minigame Selection Screen (debug)");
                Screen currentScreen = game.getScreen();
                game.setScreen(new MinigameSelectionScreen(game, true));
                if (currentScreen != null && !(currentScreen instanceof SettingsScreen)) {
                    currentScreen.dispose();
                }
            }
        } else {
            f9Pressed = false;
        }

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
