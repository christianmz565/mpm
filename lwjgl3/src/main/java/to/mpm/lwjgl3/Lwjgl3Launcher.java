package to.mpm.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import to.mpm.Main;

/**
 * Lanzador de la aplicación de escritorio (LWJGL3).
 * <p>
 * Punto de entrada principal para ejecutar el juego en plataformas de escritorio.
 */
public class Lwjgl3Launcher {
    /**
     * Método principal de la aplicación.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return;
        createApplication();
    }

    /**
     * Crea la instancia de la aplicación LWJGL3.
     *
     * @return instancia de la aplicación creada
     */
    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    /**
     * Obtiene la configuración predeterminada de la aplicación.
     *
     * @return configuración de LWJGL3 con valores predeterminados
     */
    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("MicroPatosMania");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(640, 480);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win") || osName.contains("mac")) {
            configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
        }

        return configuration;
    }
}