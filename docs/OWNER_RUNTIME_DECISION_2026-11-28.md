# Decisión del propietario — runtime nativo de Fútbol Nova

**Fecha:** 2026-11-28  
**Repositorio ejecutable:** `Nova-Nexus-C35/Futbol-Nova`  
**Rama de trabajo:** `work/futbol-nova-completion-audit-2026-11-27`

## Tecnología autorizada

Fútbol Nova continúa como aplicación Android nativa, sin Godot ni otro motor de videojuegos externo:

- Java 17 para la aplicación, pantallas, navegación y controles Android.
- C++17 para simulación y lógica del partido.
- OpenGL ES 3.0 para renderizado 3D.
- Gradle, Android SDK 35, NDK 26.3.11579264 y CMake 3.22.1 para compilar.
- ABI objetivo: `arm64-v8a`.
- Datos canónicos JSON en `data/`, empaquetados para funcionamiento offline.

Los archivos Godot existentes se conservan como referencia histórica y especificación para portar reglas. No se ejecutan, compilan ni empaquetan. No borrar ni sobrescribir el legado mientras haya reglas, datos, recursos o requisitos pendientes de portar.

## Configuración inicial del amistoso

- Local: **Nova C35**, ID `FN-NOVA-C35`.
- Visitante: **FC Río de Argenti**, ID `FN-NOVA-RDA`.
- Ambos equipos tienen 22 jugadores en `data/players/nova_original_club_rosters_2026.json`.
- Nova Génesis y Sangre Galáctica permanecen en catálogo hasta tener 22 perfiles verificados cada uno.
- Anier Cumbá y Máximo Padilla conservan sus clubes activos de temporada; la pertenencia a la plantilla de exhibición C35 no cambia esas asignaciones.

## CI y publicación

El pipeline oficial es `.github/workflows/futbol-nova-native-android.yml`: valida datos, compila con Gradle y comprueba la biblioteca ARM64 dentro del APK. El workflow histórico de Godot del repositorio de distribución se ha retirado para evitar ejecuciones accidentales. Una CI verde acredita la compilación automatizada, no la instalación ni una prueba física en el teléfono.

## Conservación y verificación

Los cambios se guardan en la rama nativa correspondiente y se sincronizan con Nova Workspace como documentación/requisitos, sin sobrescribir el proyecto histórico. Después de cada escritura, releer desde GitHub y comprobar el contenido. No declarar APK terminada hasta verificar el artefacto y, además, la instalación y un partido real en Android.
