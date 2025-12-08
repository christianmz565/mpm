
# MicroPatosMania 🦆

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![LibGDX](https://img.shields.io/badge/libGDX-E44D35?style=for-the-badge&logo=libgdx&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

**MicroPatosMania** es una colección de microjuegos competitivos multijugador diseñados para red de área local (LAN). Los juegos fomentan la interacción rápida, sencilla y entretenida entre varios jugadores simultáneamente.

---

## 📑 Entregables y Documentación (Rúbrica)

Acceso directo a los documentos requeridos para la evaluación:

| Documento | Descripción | Enlace |
| :--- | :--- | :---: |
| **📹 Video de Demostración** | Demostración de funcionalidad en YouTube/Drive. | [Ver Video](https://drive.google.com/file/d/1gVrajtwt88dHXRcPIsAJDocWJn-eexTq/view?usp=sharing) |
| **📄 Memoria Descriptiva** | Arquitectura detallada, diagramas y decisiones técnicas. | [Ver PDF](./MEMORIA_DESCRIPTIVA.pdf) |
| **📖 Manual de Usuario** | Guía de instalación, interfaz y cómo jugar. | [Ver PDF](./MANUAL_DE_USUARIO.pdf) |

---

## 🛠️ Arquitectura y Patrones de Diseño

El proyecto utiliza una arquitectura **Cliente-Servidor** autoritativa implementada con **KryoNet**. Para garantizar la escalabilidad y mantenibilidad, se han aplicado los siguientes patrones de diseño (detallados en la Memoria Descriptiva):

### 1. Factory Pattern (Creacional)
Centraliza la creación de los distintos minijuegos, permitiendo instanciar el juego correcto basado en una enumeración sin acoplar la lógica de la pantalla principal.
*   **Ubicación:** [`core/src/main/java/to/mpm/minigames/MinigameFactory.java`](core/src/main/java/to/mpm/minigames/MinigameFactory.java)
*   **Uso:** `MinigameFactory.createMinigame(type, id)` decide si crear un `SumoMinigame`, `CatchThemAllMinigame`, etc.

### 2. Singleton Pattern (Creacional)
Garantiza que existan instancias únicas de los gestores principales del sistema para evitar conflictos de conexión y recursos.
*   **Ubicación:** [`core/src/main/java/to/mpm/network/NetworkManager.java`](core/src/main/java/to/mpm/network/NetworkManager.java)
*   **Uso:** `NetworkManager.getInstance()` gestiona la conexión única del cliente/servidor en toda la aplicación.

### 3. Strategy Pattern (Comportamiento)
Permite intercambiar la lógica de juego (reglas, renderizado, input) dinámicamente sin cambiar la pantalla de juego (`GameScreen`).
*   **Interfaz:** [`core/src/main/java/to/mpm/minigames/Minigame.java`](core/src/main/java/to/mpm/minigames/Minigame.java)
*   **Implementaciones:** Carpetas dentro de `minigames/` (ej. `SumoMinigame`, `BallMovementMinigame`).

### 4. Observer Pattern (Comportamiento)
Utilizado en el sistema de red para que múltiples componentes reaccionen a la llegada de paquetes sin acoplamiento fuerte.
*   **Ubicación:** [`core/src/main/java/to/mpm/network/handlers/`](core/src/main/java/to/mpm/network/handlers/)
*   **Uso:** Los `ClientPacketHandler` y `ServerPacketHandler` observan eventos de red específicos.

---

## 🛡️ Manejo de Excepciones y Buenas Prácticas

El proyecto sigue estándares de la industria para asegurar robustez y legibilidad:

### Manejo de Excepciones
*   **Red (KryoNet):** Se capturan `IOException` en `NetworkManager` para manejar fallos de conexión (puertos ocupados, timeouts) sin cerrar el juego abruptamente, notificando al usuario mediante logs `Gdx.app.error`.
*   **Factory Segura:** En `MinigameFactory`, el bloque `default` del switch lanza o loguea un error controlado si se intenta crear un minijuego inexistente, evitando `NullPointerException` en tiempo de ejecución.
*   **Validación de Paquetes:** Los handlers verifican `instanceof` antes de castear paquetes de red para asegurar la integridad de los datos.

### Buenas Prácticas
*   **Gestión de Recursos:** Uso estricto de métodos `dispose()` en todas las clases que implementan `Disposable` (Texturas, Stages, Fonts) para evitar fugas de memoria (Memory Leaks).
*   **Separación de Responsabilidades (SoC):**
    *   `core`: Lógica pura del juego.
    *   `lwjgl3`: Lanzador de escritorio.
    *   `assets`: Recursos estáticos separados del código.
*   **Código Limpio:** Uso de constantes estáticas para configuraciones (ej. `NetworkConfig`), Enums para tipos de juegos, e interfaces para abstracción.

---


## 🎮 Minijuegos Incluidos

1.  **Catch Them All:** Competencia PvE para atrapar patos.
2.  **Sumo (Pond Push):** PvP de físicas donde debes empujar a los rivales fuera del estanque.
3.  **Duck Shooter:** Shooter competitivo de precisión.
4.  **The Finale:** El desafío final.

---

## 🚀 Instalación y Ejecución

Para ver las instrucciones detalladas sobre:
*   Requisitos del sistema.
*   Comandos de instalación y compilación.
*   Cómo ejecutar el juego (Modo Cliente y Host).

👉 **Por favor, consulte el [Manual de Usuario](./MANUAL_DE_USUARIO.pdf) incluido en este repositorio.**

---

## 👥 Equipo de Desarrollo

*   **CACERES RUIZ, Johann Andre**
*   **GUTIERREZ CCAMA, Juan Diego**
*   **JARA MAMANI, Mariel Alisson**
*   **MESTAS ZEGARRA, Christian Raul**
*   **NOA CAMINO, Yenaro Joel**
*   **VALDIVIA SEGOVIA, Ryan Fabian**

---
*Universidad Nacional de San Agustín de Arequipa - Tecnología de Objetos 2025*