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
- [Java JDK 21+](https://adoptium.net/)
- [Gradle](https://gradle.org/) 
- [Just](https://github.com/casey/just) (opcional, para automatización de tareas)

### Documentación (opcional)
*Doxygen recientemente tuvo un problema con cambios hechos en el kernel de LaTeX, por lo que cualquier versión <1.15 va a fallar con versiones de LaTeX del año 2025*
- [Doxygen 1.15+](https://www.doxygen.nl/index.html) (para generar documentación)
- [Graphviz](https://graphviz.org/download/) (para generar diagramas)
- [Python 3+](https://www.python.org/) (para servir la documentación localmente)
- [LaTeX](https://www.latex-project.org/get/) (para generar documentación en PDF)

## Comandos útiles con Just (por si no te gusta el CLI de gradle)
*todo: alguien puede decirme si powershell funciona con gradlew?*
- `just build`: construye los fuentes y archivos de cada proyecto.
- `just run`: inicia la aplicación.
- `just clean`: elimina las carpetas `build`, que almacenan las clases compiladas
- `just docs`: genera la documentación del proyecto.
- `just docs-serve`: genera y sirve la documentación del proyecto en un servidor local.
- `just docs-pdf`: genera la documentación del proyecto en formato PDF.

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