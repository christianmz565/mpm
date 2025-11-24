package to.mpm.minigames.manager;

import to.mpm.network.NetworkPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Network packets for game flow management.
 * Handles room configuration, round transitions, and results.
 */
public class ManagerPackets {

    /**
     * Packet sent by host to configure room settings.
     * Sent to all clients (including host as client) when room is created.
     */
    public static class RoomConfig extends NetworkPacket {
        public int rounds; //!< total number of rounds to play

        public RoomConfig() {
        }

        public RoomConfig(int rounds) {
            this.rounds = rounds;
        }
    }

    /**
     * Packet sent by host to show scoreboard after a round ends.
     * Contains accumulated scores and round information.
     */
    public static class ShowScoreboard extends NetworkPacket {
        public int currentRound; //!< the round that just finished (1-based)
        public int totalRounds; //!< total rounds in the game
        public Map<Integer, Integer> allPlayerScores; //!< playerId -> accumulated score

        public ShowScoreboard() {
            this.allPlayerScores = new HashMap<>();
        }

        public ShowScoreboard(int currentRound, int totalRounds, Map<Integer, Integer> allPlayerScores) {
            this.currentRound = currentRound;
            this.totalRounds = totalRounds;
            this.allPlayerScores = allPlayerScores != null ? new HashMap<>(allPlayerScores) : new HashMap<>();
        }
    }

    /**
     * Packet sent by host to start the next round.
     * Includes list of participating players (for finale filtering).
     */
    public static class StartNextRound extends NetworkPacket {
        public int roundNumber; //!< the round about to start (1-based)
        public String minigameType; //!< name of the MinigameType enum
        public List<Integer> participatingPlayerIds; //!< players allowed to play (null = all)

        public StartNextRound() {
        }

        public StartNextRound(int roundNumber, String minigameType, List<Integer> participatingPlayerIds) {
            this.roundNumber = roundNumber;
            this.minigameType = minigameType;
            this.participatingPlayerIds = participatingPlayerIds;
        }
    }

    /**
     * Packet sent by host to show final results screen.
     * Contains final accumulated scores for all players.
     */
    public static class ShowResults extends NetworkPacket {
        public Map<Integer, Integer> finalScores; //!< playerId -> final accumulated score

        public ShowResults() {
            this.finalScores = new HashMap<>();
        }

        public ShowResults(Map<Integer, Integer> finalScores) {
            this.finalScores = finalScores != null ? new HashMap<>(finalScores) : new HashMap<>();
        }
    }

    /**
     * Packet sent by host to return everyone to the lobby.
     * Sent after results screen completes.
     */
    public static class ReturnToLobby extends NetworkPacket {
        public ReturnToLobby() {
        }
    }
}
