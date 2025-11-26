package to.mpm.minigames.manager;

import com.badlogic.gdx.Gdx;
import to.mpm.network.NetworkManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maneja el flujo del juego, incluyendo rondas, puntuaciones acumuladas y
 * la lógica para determinar qué jugadores participan en la ronda final.
 * <p>
 * Implementa el patrón singleton para asegurar una única instancia.
 */
public class GameFlowManager {
    private static GameFlowManager instance; //!< instancia singleton
    private int currentRound; //!< ronda actual (desde 1)
    private int totalRounds; //!< total de rondas configuradas
    private final Map<Integer, Integer> accumulatedScores; //!< puntuaciones acumuladas por jugador (playerId -> score)
    private final Set<Integer> spectatorIds; //!< jugadores marcados como espectadores
    private boolean initialized; //!< indica si se ha llamado a initialize()

    /**
     * Constructor privado para el singleton.
     */
    private GameFlowManager() {
        this.accumulatedScores = new HashMap<>();
        this.spectatorIds = new HashSet<>();
        this.initialized = false;
    }

    /**
     * Obtiene la instancia.
     * 
     * @return la instancia de GameFlowManager
     */
    public static GameFlowManager getInstance() {
        if (instance == null) {
            instance = new GameFlowManager();
        }
        return instance;
    }

    /**
     * Inicializa el flujo del juego para una nueva sesión.
     * <p>
     * Debe ser llamado por el host al iniciar el juego.
     * <p>
     * Registra las clases de paquetes de red necesarias.
     * 
     * @param rounds número total de rondas a jugar (debe ser >= 2)
     */
    public void initialize(int rounds) {
        if (rounds < 2) {
            Gdx.app.error("GameFlowManager", "Cannot initialize game flow: rounds must be at least 2");
            return;
        }

        this.totalRounds = rounds;
        this.currentRound = 0;
        this.accumulatedScores.clear();
        this.spectatorIds.clear();
        this.initialized = true;

        NetworkManager.getInstance().registerAdditionalClasses(
                ManagerPackets.RoomConfig.class,
                ManagerPackets.ShowScoreboard.class,
                ManagerPackets.StartNextRound.class,
                ManagerPackets.ShowResults.class,
                ManagerPackets.ReturnToLobby.class,
                HashMap.class,
                ArrayList.class);

        Gdx.app.log("GameFlowManager", "Initialized with " + rounds + " rounds");
    }

    /**
     * Inicia una nueva ronda.
     */
    public void startRound() {
        if (!initialized) {
            Gdx.app.error("GameFlowManager", "Cannot start round: not initialized!");
            return;
        }
        currentRound++;
        Gdx.app.log("GameFlowManager", "Starting round " + currentRound + "/" + totalRounds);
    }

    /**
     * Termina la ronda actual y fusiona las puntuaciones.
     * <p>
     * Debe ser llamado por el host cuando un minijuego termina.
     * 
     * @param roundScores las puntuaciones del minijuego que acaba de terminar
     *                    (playerId -> score)
     */
    public void endRound(Map<Integer, Integer> roundScores) {
        if (!initialized) {
            Gdx.app.error("GameFlowManager", "Cannot end round: not initialized!");
            return;
        }

        if (roundScores != null) {
            for (Map.Entry<Integer, Integer> entry : roundScores.entrySet()) {
                int playerId = entry.getKey();
                int score = entry.getValue();
                if (!spectatorIds.contains(playerId)) {
                    accumulatedScores.merge(playerId, score, Integer::sum);
                }
            }
        }

        Gdx.app.log("GameFlowManager", "Round " + currentRound + " ended. Scores updated.");
    }

    /**
     * Verifica si la final debe jugarse a continuación.
     * 
     * @return true si la siguiente ronda debe ser la final
     */
    public boolean shouldPlayFinale() {
        return initialized && currentRound == totalRounds;
    }

    /**
     * Determina qué jugadores deben participar en la final.
     * <p>
     * Devuelve el 30% superior de jugadores (mínimo 2).
     * <p>
     * Si hay un empate, incluye aleatoriamente a uno.
     * 
     * @return lista de IDs de jugadores elegibles para la final, ordenados por
     *         puntuación descendente
     */
    public List<Integer> getFinalePlayerIds() {
        if (accumulatedScores.isEmpty()) {
            Gdx.app.log("GameFlowManager", "No scores available for finale filtering");
            return new ArrayList<>();
        }

        List<Map.Entry<Integer, Integer>> sortedPlayers = accumulatedScores.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());

        int totalPlayers = sortedPlayers.size();
        int finaleCount = Math.max(2, (int) Math.ceil(totalPlayers * 0.3));

        if (totalPlayers == 1) {
            Gdx.app.log("GameFlowManager", "Only 1 player remaining, skipping finale");
            return Arrays.asList(sortedPlayers.get(0).getKey());
        }

        ArrayList<Integer> finalePlayerIds = new ArrayList<>();
        for (int i = 0; i < finaleCount; i++) {
            finalePlayerIds.add(sortedPlayers.get(i).getKey());
        }
        return finalePlayerIds;
    }

    /**
     * Revisa si el juego ha terminado.
     * 
     * @return true si el juego ha terminado
     */
    public boolean isGameComplete() {
        return initialized && currentRound >= totalRounds;
    }

    /**
     * Obtiene las puntuaciones acumuladas de todos los jugadores.
     * 
     * @return mapa de playerId a puntuación total
     */
    public Map<Integer, Integer> getTotalScores() {
        return new HashMap<>(accumulatedScores);
    }

    /**
     * Obtiene la ronda actual.
     * 
     * @return número de la ronda actual
     */
    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Obtiene el número total de rondas.
     * 
     * @return número total de rondas
     */
    public int getTotalRounds() {
        return totalRounds;
    }

    /**
     * Elimina a un jugador de las puntuaciones acumuladas.
     * <p>
     * Debe llamarse cuando un jugador se desconecta.
     * 
     * @param playerId el jugador a eliminar
     */
    public void removePlayer(int playerId) {
        accumulatedScores.remove(playerId);
        Gdx.app.log("GameFlowManager", "Removed player " + playerId + " from game flow");
    }

    /**
     * Restablece el gestor del flujo del juego a su estado inicial.
     * <p>
     * Debe llamarse al regresar al lobby o al iniciar un nuevo juego.
     */
    public void reset() {
        this.currentRound = 0;
        this.totalRounds = 0;
        this.accumulatedScores.clear();
        this.spectatorIds.clear();
        this.initialized = false;
        Gdx.app.log("GameFlowManager", "Game flow reset");
    }

    /**
     * Establece los IDs de los jugadores espectadores.
     * 
     * @param spectators conjunto de IDs de jugadores marcados como espectadores
     */
    public void setSpectators(Set<Integer> spectators) {
        this.spectatorIds.clear();
        if (spectators != null) {
            this.spectatorIds.addAll(spectators);
        }
        Gdx.app.log("GameFlowManager", "Registered " + this.spectatorIds.size() + " spectators");
    }

    /**
     * Obtiene el número de jugadores activos (no espectadores).
     * 
     * @return número de jugadores activos
     */
    public int getActivePlayerCount() {
        int totalPlayers = NetworkManager.getInstance().getPlayerCount();
        return totalPlayers - spectatorIds.size();
    }

    /**
     * Verifica si un jugador es espectador.
     * 
     * @param playerId el ID del jugador a verificar
     * @return true si el jugador es espectador
     */
    public boolean isSpectator(int playerId) {
        return spectatorIds.contains(playerId);
    }

    /**
     * Verifica si el gestor está inicializado.
     * 
     * @return true si está inicializado
     */
    public boolean isInitialized() {
        return initialized;
    }
}
