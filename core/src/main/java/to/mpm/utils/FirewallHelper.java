package to.mpm.utils;

import com.badlogic.gdx.Gdx;

import javax.swing.*;
import java.io.IOException;

/**
 * Clase de apoyo para solicitar permisos de firewall en diferentes plataformas
 */
public class FirewallHelper {

    /**
     * Solicita permiso de firewall para el puerto especificado
     *
     * @param port Número de puerto
     */
    public static void requestFirewallPermission(int port) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            requestWindowsFirewall(port);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            requestLinuxFirewall(port);
        } else if (os.contains("mac")) {
            requestMacFirewall(port);
        }
    }

    /**
     * Solicita permiso de firewall en Windows
     *
     * @param port Número de puerto
     */
    private static void requestWindowsFirewall(int port) {
        SwingUtilities.invokeLater(() -> {
            String message = "Necesitas permitir el acceso a la red para el juego.\n" +
                "El servidor se está ejecutando en el puerto " + port + ".\n" +
                "Cuando aparezca el aviso del firewall de Windows, selecciona 'Permitir acceso'.";

            JOptionPane.showMessageDialog(
                null,
                message,
                "Permiso de Firewall",
                JOptionPane.INFORMATION_MESSAGE
            );
        });

        // TODO: check whether this actually achieves something or if we just need to try a connection for windows to ask for permission
        // TODO: prevent duplicate rules?
        try {
            String appName = "MicroPatosMania";
            String javaPath = System.getProperty("java.home") + "\\bin\\javaw.exe";

            String command = String.format(
                "netsh advfirewall firewall add rule name=\"%s\" dir=in action=allow program=\"%s\" enable=yes",
                appName, javaPath
            );

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            pb.start();

        } catch (IOException e) {
            Gdx.app.log("FirewallHelper", "Could not add firewall rule (expected without admin): " + e.getMessage());
        }
    }

    /**
     * Muestra notificación de firewall en Linux
     *
     * @param port Número de puerto
     */
    private static void requestLinuxFirewall(int port) {
        SwingUtilities.invokeLater(() -> {
            String message = "Necesitas permitir el acceso a la red para el juego.\n" +
                "El servidor se está ejecutando en el puerto " + port + ".\n" +
                "Revisa la documentación de tu distribución para permitir conexiones entrantes.\n";

            JOptionPane.showMessageDialog(
                null,
                message,
                "Permiso de Firewall",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    /**
     * Muestra notificación de firewall en macOS
     *
     * @param port Número de puerto
     */
    // TODO: uh we cant actually test this but sure maybe it works
    private static void requestMacFirewall(int port) {
        SwingUtilities.invokeLater(() -> {
            String message = "Necesitas permitir el acceso a la red para el juego.\n" +
                "El servidor se está ejecutando en el puerto " + port + ".\n" +
                "Cuando aparezca el aviso del firewall de macOS, selecciona 'Permitir acceso'.";

            JOptionPane.showMessageDialog(
                null,
                message,
                "Permiso de Firewall",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
}
