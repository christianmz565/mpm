package to.mpm.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import to.mpm.Main;
import to.mpm.minigames.MinigameType;
import to.mpm.screens.*;

/**
 * Atajos de teclado para depuración.
 * <p>
 * Permite navegar rápidamente entre pantallas durante el desarrollo.
 * <p>
 * Atajos disponibles:
 * <ul>
 *   <li>F1: Menú Principal</li>
 *   <li>F2: Pantalla de Crear Sala</li>
 *   <li>F3: Pantalla de Unirse a Sala</li>
 *   <li>F4: Pantalla de Anfitrión de Sala</li>
 *   <li>F5: Pantalla de Juego</li>
 *   <li>F6: Pantalla de Espectador</li>
 *   <li>F7: Pantalla de Marcador</li>
 *   <li>F8: Pantalla de Resultados</li>
 *   <li>F9: Pantalla de Selección de Minijuego</li>
 *   <li>ESC: Alternar superposición de configuración</li>
 * </ul>
 */
public class DebugKeybinds {
    /** Instancia del juego principal. */
    private final Main game;
    /** Estado de la tecla F1. */
    private boolean f1Pressed = false;
    /** Estado de la tecla F2. */
    private boolean f2Pressed = false;
    /** Estado de la tecla F3. */
    private boolean f3Pressed = false;
    /** Estado de la tecla F4. */
    private boolean f4Pressed = false;
    /** Estado de la tecla F5. */
    private boolean f5Pressed = false;
    /** Estado de la tecla F6. */
    private boolean f6Pressed = false;
    /** Estado de la tecla F7. */
    private boolean f7Pressed = false;
    /** Estado de la tecla F8. */
    private boolean f8Pressed = false;
    /** Estado de la tecla F9. */
    private boolean f9Pressed = false;
    /** Estado de la tecla ESC. */
    private boolean escPressed = false;

    /**
     * Construye una nueva instancia de DebugKeybinds.
     *
     * @param game instancia del juego principal
     */
    public DebugKeybinds(Main game) {
        this.game = game;
    }

    /**
     * Actualiza el estado de los atajos de teclado.
     * <p>
     * Debe llamarse cada frame para detectar pulsaciones.
     */
    public void update() {
        f1Pressed = handleKey(f1Pressed, Input.Keys.F1, "Cambiando a Menú Principal",
                () -> switchScreen(new MainMenuScreen(game)));

        f2Pressed = handleKey(f2Pressed, Input.Keys.F2, "Cambiando a Pantalla de Crear Sala",
                () -> switchScreen(new CreateRoomScreen(game)));

        f3Pressed = handleKey(f3Pressed, Input.Keys.F3, "Cambiando a Pantalla de Unirse a Sala",
                () -> switchScreen(new JoinLobbyScreen(game)));

        f4Pressed = handleKey(f4Pressed, Input.Keys.F4, "Cambiando a Pantalla de Sala de Anfitrión",
                () -> switchScreen(new LobbyScreen(game, true)));

        f5Pressed = handleKey(f5Pressed, Input.Keys.F5, "Cambiando a Pantalla de Juego (Atrapa a Todos)",
                () -> switchScreen(new GameScreen(game, MinigameType.CATCH_THEM_ALL, 1, 3)));

        f6Pressed = handleKey(f6Pressed, Input.Keys.F6, "Cambiando a Pantalla de Espectador",
                () -> switchScreen(new SpectatorScreen(game, to.mpm.minigames.MinigameType.CATCH_THEM_ALL, 1, 5)));

        f7Pressed = handleKey(f7Pressed, Input.Keys.F7, "Cambiando a Pantalla de Marcador",
                () -> {
                    java.util.Map<Integer, Integer> dummyScores = new java.util.HashMap<>();
                    dummyScores.put(0, 1000);
                    dummyScores.put(1, 800);
                    dummyScores.put(2, 600);
                    switchScreen(new ScoreboardScreen(game, dummyScores, 1, 3, 0));
                });

        f8Pressed = handleKey(f8Pressed, Input.Keys.F8, "Cambiando a Pantalla de Resultados",
                () -> {
                    java.util.Map<Integer, Integer> dummyScores = new java.util.HashMap<>();
                    dummyScores.put(0, 1500);
                    dummyScores.put(1, 1200);
                    dummyScores.put(2, 900);
                    switchScreen(new ResultsScreen(game, dummyScores));
                });

        f9Pressed = handleKey(f9Pressed, Input.Keys.F9, "Cambiando a Pantalla de Selección de Minijuego",
                () -> switchScreen(new MinigameSelectionScreen(game, true)));

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            if (!escPressed) {
                escPressed = true;
                Gdx.app.log("DebugKeybinds", "Alternando superposición de Ajustes");
                game.toggleSettings();
            }
        } else {
            escPressed = false;
        }
    }

    /**
     * Maneja la pulsación de una tecla de función.
     *
     * @param wasPressed estado anterior de la tecla
     * @param keyCode    código de la tecla
     * @param logMessage mensaje a registrar
     * @param action     acción a ejecutar
     * @return nuevo estado de la tecla
     */
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

    /**
     * Cambia a la pantalla especificada y libera la anterior.
     *
     * @param nextScreen nueva pantalla a mostrar
     */
    private void switchScreen(Screen nextScreen) {
        Screen currentScreen = game.getScreen();
        game.setScreen(nextScreen);
        if (currentScreen != null) {
            currentScreen.dispose();
        }
    }

    /**
     * Imprime la ayuda de atajos de teclado en la consola.
     */
    public static void printHelp() {
        Gdx.app.log("DebugKeybinds", "=== Atajos de Depuración ===");
        Gdx.app.log("DebugKeybinds", "F1: Menú Principal");
        Gdx.app.log("DebugKeybinds", "F2: Pantalla de Crear Sala");
        Gdx.app.log("DebugKeybinds", "F3: Pantalla de Unirse a Sala");
        Gdx.app.log("DebugKeybinds", "F4: Pantalla de Sala de Anfitrión");
        Gdx.app.log("DebugKeybinds", "F5: Pantalla de Juego (Atrapa a Todos)");
        Gdx.app.log("DebugKeybinds", "F6: Pantalla de Espectador");
        Gdx.app.log("DebugKeybinds", "F7: Pantalla de Marcador");
        Gdx.app.log("DebugKeybinds", "F8: Pantalla de Resultados");
        Gdx.app.log("DebugKeybinds", "F9: Pantalla de Selección de Minijuego");
        Gdx.app.log("DebugKeybinds", "ESC: Alternar superposición de Ajustes");
        Gdx.app.log("DebugKeybinds", "============================");
    }
}
