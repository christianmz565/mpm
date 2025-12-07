package to.mpm.minigames.manager;

import to.mpm.network.NetworkPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paquetes de red manejados por el GameFlowManager.
 * <p>
 * Maneja la configuración de la sala, transiciones de rondas y resultados.
 */
public class ManagerPackets {

    /**
     * Paquete enviado por el host para configurar la sala.
     */
    public static class RoomConfig extends NetworkPacket {
        /** Número total de rondas a jugar. */
        public int rounds;
        /** Lista de IDs de jugadores marcados como espectadores. */
        public List<Integer> spectatorIds;

        public RoomConfig() {
        }

        /**
         * Constructor con parámetros.
         * 
         * @param rounds número de rondas
         */
        public RoomConfig(int rounds) {
            this.rounds = rounds;
            this.spectatorIds = new ArrayList<>();
        }

        /**
         * Constructor con parámetros.
         * 
         * @param rounds       número de rondas
         * @param spectatorIds lista de IDs de jugadores espectadores
         */
        public RoomConfig(int rounds, List<Integer> spectatorIds) {
            this.rounds = rounds;
            this.spectatorIds = spectatorIds;
        }
    }

    /**
     * Paquete enviado por el host para mostrar el marcador después de que termina
     * una ronda.
     * <p>
     * Contiene las puntuaciones acumuladas y la información de la ronda.
     */
    public static class ShowScoreboard extends NetworkPacket {
        /** La ronda que acaba de terminar. */
        public int currentRound;
        /** Total de rondas en el juego. */
        public int totalRounds;
        /** Mapa de playerId -> puntuación acumulada. */
        public Map<Integer, Integer> allPlayerScores;

        public ShowScoreboard() {
            this.allPlayerScores = new HashMap<>();
        }

        /**
         * Constructor con parámetros.
         * 
         * @param currentRound    ronda que acaba de terminar
         * @param totalRounds     total de rondas en el juego
         * @param allPlayerScores mapa de playerId -> puntuación acumulada
         */
        public ShowScoreboard(int currentRound, int totalRounds, Map<Integer, Integer> allPlayerScores) {
            this.currentRound = currentRound;
            this.totalRounds = totalRounds;
            this.allPlayerScores = allPlayerScores != null ? new HashMap<>(allPlayerScores) : new HashMap<>();
        }
    }

    /**
     * Paquete enviado por el host para iniciar la siguiente ronda.
     * <p>
     * Incluye la lista de jugadores participantes (para filtrado de la final).
     */
    public static class StartNextRound extends NetworkPacket {
        /** La ronda que está a punto de comenzar. */
        public int roundNumber;
        /** Minijuego elegido para esta ronda. */
        public String minigameType;
        /** Jugadores permitidos para jugar (null = todos). */
        public List<Integer> participatingPlayerIds;

        public StartNextRound() {
        }

        /**
         * Constructor con parámetros.
         * 
         * @param roundNumber            número de ronda que comienza
         * @param minigameType           tipo de minijuego
         * @param participatingPlayerIds lista de IDs de jugadores participantes
         */
        public StartNextRound(int roundNumber, String minigameType, List<Integer> participatingPlayerIds) {
            this.roundNumber = roundNumber;
            this.minigameType = minigameType;
            this.participatingPlayerIds = participatingPlayerIds;
        }
    }

    /**
     * Paquete enviado por el host para mostrar la pantalla de resultados finales.
     * <p>
     * Contiene las puntuaciones finales acumuladas de todos los jugadores.
     */
    public static class ShowResults extends NetworkPacket {
        /** Mapa de playerId -> puntuación final acumulada. */
        public Map<Integer, Integer> finalScores;

        public ShowResults() {
            this.finalScores = new HashMap<>();
        }

        /**
         * Constructor con parámetros.
         * 
         * @param finalScores mapa de playerId -> puntuación final acumulada
         */
        public ShowResults(Map<Integer, Integer> finalScores) {
            this.finalScores = finalScores != null ? new HashMap<>(finalScores) : new HashMap<>();
        }
    }

    /**
     * Paquete enviado por el host para devolver a todos al lobby.
     * <p>
     * Enviado después de que la pantalla de resultados finaliza.
     */
    public static class ReturnToLobby extends NetworkPacket {
        public ReturnToLobby() {
        }
    }
}
