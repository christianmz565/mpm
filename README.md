# MicroPatosMania

## Descripción
todo

## Estructura del Proyecto
todo

## Integrantes
- CACERES RUIZ, Johann Andre
- GUTIERREZ CCAMA, Juan Diego
- JARA MAMANI, Mariel Alisson
- MESTAS ZEGARRA, Christian Raul
- NOA CAMINO, Yenaro Joel

## Dependencias
- Java Development Kit (JDK) 21+
- Gradle 7.0+

## Gradle

Este proyecto usa [Gradle](https://gradle.org/) para gestionar las dependencias.
El wrapper de Gradle fue incluido, por lo que puedes ejecutar tareas de Gradle usando los comandos `gradlew.bat` o `./gradlew`.
Tareas y flags útiles de Gradle:

- `--continue`: al usar este flag, los errores no detendrán la ejecución de las tareas.
- `--daemon`: gracias a este flag, el daemon de Gradle será usado para ejecutar las tareas elegidas.
- `--offline`: al usar este flag, se usarán los archivos de dependencias en caché.
- `--refresh-dependencies`: este flag fuerza la validación de todas las dependencias. Útil para versiones snapshot.
- `build`: construye los fuentes y archivos de cada proyecto.
- `cleanEclipse`: elimina los datos del proyecto Eclipse.
- `cleanIdea`: elimina los datos del proyecto IntelliJ.
- `clean`: elimina las carpetas `build`, que almacenan las clases compiladas y los archivos construidos.
- `eclipse`: genera los datos del proyecto Eclipse.
- `idea`: genera los datos del proyecto IntelliJ.
- `lwjgl3:jar`: construye el archivo jar ejecutable de la aplicación, que se puede encontrar en `lwjgl3/build/libs`.
- `lwjgl3:run`: inicia la aplicación.
- `test`: ejecuta las pruebas unitarias (si las hay).