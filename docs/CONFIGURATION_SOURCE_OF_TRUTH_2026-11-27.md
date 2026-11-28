# Fútbol Nova — reconciliación maestra de fuentes y configuración
**Fecha:** 2026-11-27  
**Estado:** reconciliación documental y preservación de fuentes completada; validación funcional del juego todavía pendiente.

## 1. Jerarquía oficial de fuentes

| Ámbito | Fuente que manda | Uso |
|---|---|---|
| Runtime Android | `Nova-Nexus-C35/Futbol-Nova`, rama de migración nativa | Código compilable y ejecución Android |
| Datos activos del runtime | `data/` de este repositorio | JSON canónicos consumidos por Java/C++ |
| Contratos y decisiones de diseño históricas | `Cumba35/Nova-Workspace/proyectos/juegos/futbol-nova/` | Fuente de requisitos, sistemas Godot históricos, reglas, investigación y datos anteriores |
| Copias históricas de plantillas originales | `data/archive/` en este repositorio | Preservación; nunca cargar automáticamente |
| Recursos y copias de trabajo | Dropbox `/Nova Workspace Recovery/` y `/Apps/Futbol Nova/` | Documentos de recuperación, APK antiguas y referencias gráficas |

La rama de integración actual es `work/futbol-nova-completion-audit-2026-11-27`. El PR #5 continúa siendo el vehículo de revisión hacia `main`. No se fusiona ni se reemplaza `main` como parte de esta reconciliación.

## 2. Decisión técnica vigente

El runtime de distribución es Android nativo:
- Java 17 para aplicación, navegación, pantallas, menús y controles.
- C++17 y JNI para simulación y motor.
- OpenGL ES 3.0 para renderizado.
- Android SDK 35, NDK 26.3.11579264, CMake 3.22.1 y Gradle 8.7.
- ABI de distribución: `arm64-v8a`.
- `minSdk=28`, `targetSdk=28`.
- Identificador: `com.nova.futbolnova`.
- Versión documentada: `0.2.0-native`, versionCode 2.

Godot 4.4.1 es la plataforma del proyecto histórico en Nova-Workspace, no el runtime oficial actual. Se conservan `project.godot`, `scenes/`, `scripts/`, `tests/` y sus documentos como material de consulta y portabilidad. No borrar esos archivos hasta que sus reglas y sistemas hayan sido migrados y validados. Unity, Unreal y otros motores no forman parte de la ruta oficial.

## 3. Datos deportivos activos y límites de elegibilidad

- Base cubana: 52 clubes y 1.144 perfiles ficticios, distribuidos en Primera (16/352), Segunda (16/352) y Tercera (20/440).
- Catálogo internacional: 10 ligas base documentadas, 725 clubes y 15.950 perfiles generados; catálogo de selección de 100 ligas.
- Selecciones: 211 asociaciones catalogadas y 16 torneos nacionales listados. Esto no significa que los paquetes de selecciones estén completos o jugables.
- Los datos de catálogo no habilitan automáticamente un equipo para partido.
- Un roster elegible requiere exactamente 22 jugadores, IDs estables únicos y no vacíos, y atributos válidos.
- Los perfiles personalizados solo se aplican mediante coincidencia del ID estable; nunca por nombre.
- Estadísticas sin actas verificadas empiezan en cero; medias y porcentajes sin denominador permanecen `null`.

## 4. Clubes originales y jugadores

Clubes activos catalogados:
- `FN-NOVA-C35` — Nova C35.
- `FN-NOVA-RDA` — FC Río de Argenti.
- Nova Génesis — catalogado, sin plantilla jugable.
- Sangre Galáctica — catalogado, sin plantilla jugable.

La rama nativa mantiene dos plantillas de exhibición de 22:
- Nova C35: Anier Cumbá (`NOVA-PLAYER-0035`), Máximo Padilla (`NOVA-PLAYER-0036`) y 20 perfiles ficticios `FN-P-C35-2026-*`.
- FC Río de Argenti: Raymond Valdez (`NOVA-PLAYER-0037`) y 21 perfiles ficticios `FN-P-RDA-2026-*`.

Preferencias que no se deben alterar:
- Anier Cumbá permanece en Albor Madrid en la temporada actual.
- Máximo Padilla permanece en Litoral Barcelona en la temporada actual.
- Ambos mantienen Nova C35 como afiliación histórica.
- Raymond Valdez mantiene FC Río de Argenti y nacionalidad `UNK`; no inventar selección nacional.
- El amistoso predeterminado documentado sigue siendo Albor Madrid vs Litoral Barcelona. No cambiarlo por Nova C35 vs RDA sin aprobación del propietario.

## 5. Diferencia detectada entre las plantillas históricas y la nueva plantilla activa

La fuente de Nova-Workspace `data/original_club_players_2026.json` contiene 42 registros históricos (incluye a Raymond); la lista de jugadores de los dos clubes también referencia a Anier y Máximo, cuyos perfiles se mantenían por separado. La plantilla activa nativa contiene 44 registros y usa IDs nuevos `FN-P-*` para sus perfiles generados.

Los 41 perfiles ficticios de las plantillas nuevas no son equivalentes por ID ni por nombre a los 41 perfiles ficticios de la fuente histórica. Por eso:
1. Se conserva la fuente original completa en `data/archive/original_club_players_2026_workspace_legacy.json`.
2. Se conserva la definición original de clubes en `data/archive/original_clubs_2026_workspace_legacy.json`.
3. Esas instantáneas no se cargan en el runtime ni se mezclan con los rosters activos.
4. La sustitución, recuperación de atributos, renumeración o fusión de perfiles históricos se deja como decisión pendiente; no se hace una equivalencia por nombre.
5. `data/custom_players.json` y `data/players/nova_original_club_rosters_2026.json` son las fuentes activas para la etapa nativa.

## 6. Mapa de archivos activos

| Archivo | Responsabilidad |
|---|---|
| `data/native_runtime_manifest.json` | Contrato de recursos/datos para runtime |
| `data/custom_players.json` | Perfiles personalizados 0035, 0036 y 0037 |
| `data/original_clubs_2026.json` | Catálogo y estado de los cuatro clubes originales |
| `data/players/nova_original_club_rosters_2026.json` | Dos rosters de exhibición activos |
| `data/game_modules_manifest.json` | Módulos de juego |
| `data/optional_modules_catalog.json` | Catálogo de módulos opcionales |
| `data/league_content_manifest.json` | Estado de contenido de ligas |
| `data/national_teams_manifest.json` | Catálogo/estado de selecciones |
| `data/global_database_import_status.json` | Estado de importación de base global |
| `data/global_schema_v1.json` y `data/player_statistics_schema.json` | Contratos de datos |
| `native-android/app/src/main/java/com/nova/futbolnova/MainActivity.java` | UI y flujo de navegación |
| `native-android/app/src/main/java/com/nova/futbolnova/CanonicalDataRepository.java` | Lectura de JSON canónicos |
| `native-android/app/src/main/cpp/native_engine.cpp` | Motor nativo |
| `tools/validate_native_data.py` | Validación previa a compilación |
| `.github/workflows/futbol-nova-native-android.yml` | Compilación y verificación APK ARM64 |

## 7. Material recuperado de Dropbox

Ubicaciones identificadas:
- `/Nova Workspace Recovery/`: ficha de recuperación, documento maestro de diseño, arquitectura/configuración/licencias, código Java anterior, base de datos y módulos, punto de entrada/controlador, esquemas/manifiestos, importación global y selección de ligas.
- `/Apps/Futbol Nova/Assets/Clubs/`: referencias de equipaciones/identidad para Nova C35, RDA y Nova Génesis.
- `/Aplicaciones/Fútbol Nova/`: APK históricas de noviembre, incluidas builds de depuración y pruebas de reloj/marcador/guardado.

Las APK antiguas no sustituyen la compilación nativa actual. Los recursos de Dropbox quedan como fuente de recuperación; todavía deben incorporarse selectivamente y verificarse licencia, formato y correspondencia con cada producto. No se copian referencias visuales como assets finales automáticamente.

## 8. CI y criterios de aceptación

El workflow activo es `.github/workflows/futbol-nova-native-android.yml`. En la consulta del 2026-11-27, los runs #46082171838, #46082056300, #46082051900 y #46081941810 terminaron con `success`; los runs #46083721458 y #46083716525 seguían `in_progress`. Un run exitoso verifica el pipeline de CI y el artefacto según sus pasos, no prueba instalación ni jugabilidad en un teléfono físico.

No marcar como terminado hasta comprobar:
1. CI más reciente concluido correctamente y APK/ABI ARM64 verificados.
2. Instalación real en Android.
3. Carga de datos y selección de equipos.
4. Inicio y finalización de amistoso 11 vs 11.
5. Controles táctiles, reloj, marcador, pausa y reinicio.
6. Pruebas de regresión y persistencia.

## 9. Pendientes de desarrollo (no se consideran resueltos por la reconciliación)

- Confirmar/recuperar atributos y perfiles históricos que el propietario decida conservar.
- Completar modelos humanos y animaciones.
- Física del balón y colisiones.
- Porteros y comportamiento por rol.
- Árbitro, faltas, fueras de juego, saques y balón parado.
- IA táctica y cambios/sustituciones.
- Estadísticas y guardados nativos.
- Portar progresivamente Manager, Leyenda, ligas, copas y competiciones.
- Integrar assets finales originales/licenciados.
- Prueba física en teléfono Android.

## 10. Regla de cambios

Todo cambio se escribe en el repositorio, rama y carpeta adecuados. Antes de editar un archivo existente, leer su versión actual y usar su SHA. Después de cada escritura, volver a leer desde GitHub y verificar el contenido. Mantener el historial; no borrar datos históricos para “limpiar” la configuración. Los conflictos de diseño se documentan y se resuelven uno por uno con el propietario.
