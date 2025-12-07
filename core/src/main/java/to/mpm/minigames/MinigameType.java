package to.mpm.minigames;

/**
 * Enumeración de todos los minijuegos disponibles.
 * <p>
 * Define los tipos de minijuegos con sus nombres, descripciones y límites de
 * jugadores.
 */
public enum MinigameType {
        /** Minijuego de atrapar patos que caen. */
        CATCH_THEM_ALL(
                        "Catch Them All",
                        "¡Atrapa los patos que caen con tu cesta! Los patos buenos te dan puntos, " +
                                        "pero cuidado con los patos malos.",
                        "A/D o Flechas Izquierda/Derecha para moverse",
                        2, 6),
        /** Minijuego de empujar patos fuera de la plataforma. */
        SUMO(
                        "Pond Push",
                        "¡Empuja a los otros jugadores fuera de la plataforma! " +
                                        "Choca con otros jugadores para tirarlos al agua.",
                        "WASD o Flechas para moverse",
                        2, 6),
        /** Minijuego de esquivar lluvia de objetos. */
        DODGE_RAIN(
                        "Dodge Rain",
                        "¡Esquiva la lluvia de objetos! Sobrevive el mayor tiempo posible.",
                        "A/D o Flechas Izquierda/Derecha para moverse",
                        2, 6),
        /** Minijuego final del torneo. */
        THE_FINALE(
                        "The Finale",
                        "¡El enfrentamiento final! " +
                                        "Dispara a otros patos para eliminarlos. ¡El último pato en pie es el campeón!",
                        "WASD para moverse, Ratón para apuntar, ESPACIO para disparar",
                        2, 99);

        /** Nombre del minijuego para mostrar. */
        private final String displayName;
        /** Descripción del minijuego. */
        private final String description;
        /** Descripción de los controles del minijuego. */
        private final String controls;
        /** Número mínimo de jugadores requeridos. */
        private final int minPlayers;
        /** Número máximo de jugadores permitidos. */
        private final int maxPlayers;

        /**
         * Constructor parametrizado para crear un tipo de minijuego.
         * 
         * @param displayName nombre del minijuego para mostrar en la UI
         * @param description descripción breve del minijuego
         * @param controls    descripción de los controles del minijuego
         * @param minPlayers  número mínimo de jugadores requeridos
         * @param maxPlayers  número máximo de jugadores permitidos
         */
        MinigameType(String displayName, String description, String controls, int minPlayers, int maxPlayers) {
                this.displayName = displayName;
                this.description = description;
                this.controls = controls;
                this.minPlayers = minPlayers;
                this.maxPlayers = maxPlayers;
        }

        /**
         * Obtiene el nombre del minijuego para mostrar en la interfaz.
         * 
         * @return nombre del minijuego
         */
        public String getDisplayName() {
                return displayName;
        }

        /**
         * Obtiene la descripción del minijuego.
         * 
         * @return descripción del minijuego
         */
        public String getDescription() {
                return description;
        }

        /**
         * Obtiene el número mínimo de jugadores requeridos para el minijuego.
         * 
         * @return número mínimo de jugadores
         */
        public int getMinPlayers() {
                return minPlayers;
        }

        /**
         * Obtiene el número máximo de jugadores permitidos en el minijuego.
         * 
         * @return número máximo de jugadores
         */
        public int getMaxPlayers() {
                return maxPlayers;
        }

        /**
         * Obtiene la descripción de los controles del minijuego.
         * 
         * @return descripción de los controles
         */
        public String getControls() {
                return controls;
        }
}