# Investigación de repositorios públicos para Fútbol Nova

Fecha de revisión: 2026-11-23  
Repositorio objetivo: `Nova-Nexus-C35/Futbol-Nova`  
Rama de trabajo: `rebuild/libgdx-android-2026-11-22`

## Regla de reutilización

Buscar repositorios públicos es una actividad permanente del desarrollo. Cada hallazgo se evalúa por separado: licencia real en el repositorio, procedencia/forks, dependencias, lenguaje, compatibilidad con Java 17 + libGDX 1.14.2 + Android ARM64/minSdk 28, coste de memoria/FPS y pruebas. Un README que menciona una licencia no sustituye la lectura del archivo LICENSE. No se copian assets, código ni marcas sin verificar los derechos y obligaciones correspondientes. Las ideas arquitectónicas pueden inspirar implementaciones propias sin copiar expresión protegida.

## Hallazgos revisados

### Referencias directamente relevantes para el runtime 3D

- [libgdx/libgdx](https://github.com/libgdx/libgdx) — Framework Java multiplataforma con render OpenGL/OpenGL ES y soporte Android/3D. El README consultado declara Apache-2.0. Es la base técnica ya adoptada; mantener sus APIs y convenciones como primera referencia.
- [mgsx-dev/gdx-gltf](https://github.com/mgsx-dev/gdx-gltf) — Cargador glTF 2.0 y shaders PBR para libGDX. El README describe carga de cámaras/luces, morph targets, múltiples animaciones, interpolación de keyframes y materiales PBR; el archivo LICENSE declara Apache-2.0. Es un candidato fuerte para la futura importación de futbolistas riggeados y animaciones. **No se añadió como dependencia**: el build consultado declara libGDX 1.9.11, muy anterior a la versión 1.14.2 del juego; hay que validar compatibilidad, API, shaders GLES, tamaño y rendimiento en Android antes de integrarlo.
- [AmanRajAryan/Libgdx-gdx-gltf-instancing](https://github.com/AmanRajAryan/Libgdx-gdx-gltf-instancing) — Ejemplo público relacionado con instanciación y gdx-gltf. Solo referencia de investigación; revisar fuente, licencia y vigencia antes de reutilizar cualquier parte.

### Referencias para lógica futbolística y reglas

- [haeretici/soccer-js](https://github.com/haeretici/soccer-js) — Motor isométrico 2D JavaScript con IA de equipo, formaciones, roles, apoyos, marcaje, pases, centros, reglas de partido, saques, sustituciones y fuera de juego demorado. El README declara MIT. No es tecnología de render adecuada para el objetivo 3D; sirve como referencia para diseñar escenarios deterministas y pruebas de reglas. No se importó código.
- [modelence/open-soccer](https://github.com/modelence/open-soccer) — Juego 11v11 con cámara broadcast, controles, reinicios y reglas; arquitectura React/TypeScript/Canvas, no Java/libGDX. Su README permite estudiar la separación entre motor, cámara, render y controles. No se encontró un archivo LICENSE en la ruta raíz consultada; no reutilizar código ni assets hasta verificar licencia.
- [awest813/League-Soccer](https://github.com/awest813/League-Soccer) — Repositorio consultado como fork de `vi3itor/GameplayFootball`, con fuente upstream identificada como `BazkieBumpercar/GameplayFootball`; el repositorio consultado declara Apache-2.0. El árbol contiene un motor C++17 y arquitectura de simulación 3D/partido. Es una referencia conceptual para eventos, árbitro, estados, tácticas, estadísticas y pruebas; su cadena de procedencia y componentes deben revisarse antes de portar código. No es una dependencia directa Android/Java.
- [Tobilion/footysim](https://github.com/Tobilion/footysim) — Motor espacial 11v11 Python, con ticks deterministas, agentes, decisiones, física, xG, ratings y estadísticas por jugador. Útil para comparar contratos de simulación y telemetría; no es runtime móvil ni se importó código. Verificar licencia antes de reutilizar fuente.

### Ejemplo 3D pequeño

- [Adem-maker600/football-3d-game](https://github.com/Adem-maker600/football-3d-game) — Muestra Java/LWJGL de fútbol 3v3 con IA básica, balón, cámara y colisiones del campo. El README declara MIT, pero su alcance es didáctico y no resuelve 11v11 ni animación moderna. No se ha incorporado código.

## Decisiones para Fútbol Nova

1. Mantener el runtime confirmado Java 17 + libGDX 1.14.2 + Android ARM64. No migrar a Unity, Godot, Canvas/JavaScript, C++ ni otro motor sin autorización explícita.
2. Mantener la simulación propia en Java, desacoplándola progresivamente del render. Adoptar de las referencias patrones y casos de prueba: estados de partido, eventos, decisiones por jugador, fuera de juego, portero, balón dividido, estadísticas y replay.
3. Investigar gdx-gltf como vía prioritaria para sustituir gradualmente los humanoides procedurales por modelos glTF riggeados originales o con licencia compatible. Antes de añadirlo, probar una rama/escena aislada con la versión actual de libGDX y un modelo de prueba con licencia comprobada.
4. No incluir assets de demostración de terceros como recursos de producción. Cada modelo, textura, sonido, kit, escudo, nombre y likeness debe tener procedencia y licencia registradas.
5. Repetir esta búsqueda cuando se trabaje en un subsistema nuevo y actualizar este documento con fecha, repositorios revisados, licencias y decisión de adopción.

## Corrección de CI relacionada con libGDX

La ejecución Android CI #160 (commit previo `09aead75766875fe71818b72dbd567a23bff8c39`) ejecutó 56 pruebas y falló en las cuatro pruebas de `BroadcastCameraTest` con `java.lang.UnsatisfiedLinkError`. La causa es que las pruebas JVM ejercitan utilidades matemáticas/cámara de libGDX que requieren las bibliotecas nativas de escritorio; el proyecto solo declaraba nativos ARM64 para empaquetar la aplicación.

Se añadió `testRuntimeOnly "com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-desktop"` a `app/build.gradle`. Es una dependencia exclusiva del runtime de pruebas; no sustituye ni se empaqueta en lugar de los nativos ARM64 de Android. La corrección queda pendiente de confirmación por la siguiente ejecución CI asociada al nuevo HEAD.
