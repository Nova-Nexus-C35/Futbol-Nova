# Integración del proyecto original de Fútbol Nova

Fecha: 2026-11-22

## Fuente original localizada

- Repositorio: `Nova-Nexus-C35/Nova-Workspace`, rama `main`.
- Ruta: `Projects/Games/futbol-nova/`.
- Commit de referencia: `2f3283efcc840f870fcf930fecc942300f75e5ec`.
- Proyecto original: Godot 4.4.1 / GDScript; 321 entradas en el árbol Git, entre escenas, scripts, datos, módulos, pruebas y documentación.
- El README original describe fútbol 3D en tiempo real, control directo, física del balón, IA, gestión/carrera, ligas/copas, entrenamiento y desafíos diarios. El diseño de menú define Inicio, Online, Modos y Configuración; excluye Ultimate Team.

## Restricción tecnológica vigente

El propietario descartó Unity y Godot y eligió Java 17 + libGDX. Por tanto, el proyecto actual mantiene libGDX: no se copian `project.godot`, escenas `.tscn` ni GDScript al runtime Java. Los sistemas de gameplay se portarán por subsistemas y con pruebas, no se fingirá que copiar documentación equivale a integración.

## Primera importación de datos

Se copiaron sin transformación a `app/src/main/assets/data/original/`:

- `cuba_leagues.json`
- `cuba_players.json`
- `cuban_competition_rules.json`
- `custom_players.json`
- `club_commercial_economy.json`
- `youth_academy_rules.json`
- `player_statistics_schema.json`
- `daily_training_challenges.json`
- `international_club_competitions.json`
- `international_national_team_competitions.json`
- `league_content_manifest.json`
- `league_regions.json`
- `global_league_selection_100.json`
- `global_league_tiers.json`
- `national_teams_manifest.json`

Los JSON conservan el contenido fuente. Los catálogos no se consideran automáticamente completos, vigentes, publicados o jugables. Antes de distribuir el APK final, los datos de modos y ligas deberán separarse en paquetes opcionales para mantener ligera la instalación base.

## Adaptador Java

Se añadió `OriginalProjectData`, que lee el catálogo de ligas cubanas, el conjunto de jugadores cubanos y los jugadores personalizados desde assets, valida su estructura JSON básica y registra un resumen al iniciar. Se añadió una prueba unitaria del parser.

Esto demuestra lectura y análisis estructural de los datos, no que las ligas o carreras ya sean jugables.

## Portabilidad 11v11 — fase 1 completada en Java

Se incorporó `MatchSimulation.java`, primer port del controlador original de movimiento táctico. El juego crea 22 jugadores en formación 4-3-3, controla al delantero ST local, mueve la IA en decisiones de 0,20 s, ajusta posiciones por el balón y rol, mantiene a los porteros cerca de su área y aplica separación cinemática, fatiga ligera y límites del campo. Se añadieron pruebas unitarias para alineaciones, presión, límites, control del balón y evaluación de entradas. Detalles y límites: `Docs/PORTING_11V11_CONTROLLER.md`.

Esta fase no incluye todavía posesión real, pases/t tiros de IA, reglas completas, reloj por períodos ni modelos humanos animados.

## Portabilidad de física y reglas geométricas — fase 2

Se añadieron `BallFlightModel.java` y `MatchRules.java`, ports Java de los modelos originales de trayectoria y geometría de reglas. La física integra gravedad, arrastre y Magnus; la detección de gol comprueba el cruce completo de la línea y el espacio entre postes/bajo larguero. Se añadieron pruebas unitarias. Los saques, fuera de juego en vivo, rebotes con postes y árbitro aún no están conectados. Detalles: `Docs/PORTING_BALL_AND_RULES.md`.

## Orden de portabilidad

1. Dominio deportivo: perfiles, IDs, equipos, alineaciones 4-3-3, contrato y reloj.
2. Partido 3D: movimiento, posesión, balón, pases, tiros, entradas, porteros y cámara; traducir los sistemas originales a Java/libGDX.
3. Reglas: fases, interrupciones, sustituciones, goles, reinicios, faltas y fuera de juego.
4. Temporadas: calendarios, tablas, competiciones y simulación determinista.
5. Carreras: Manager, Leyenda, contratos, economía, cantera y progresión.
6. Módulos: manifiestos y paquetes opcionales, conservando el juego base offline.

## Validación automatizada — 2026-11-22

- GitHub Actions run #31 terminó con éxito: [ver ejecución](https://github.com/Nova-Nexus-C35/Futbol-Nova/actions/runs/45070366933).
- Las pruebas Java y la compilación ARM64 terminaron correctamente.
- La verificación confirmó `lib/arm64-v8a/libgdx.so` dentro del APK.
- Artefacto generado: `futbol-nova-android-arm64-debug` (3.441.295 bytes; disponible hasta el 06-12-2026).
- Esto valida compilación automatizada, no ejecución real en el Android 9 del propietario.

## Pendiente de validación

- Probar instalación, apertura, rendimiento y controles en Android 9 ARM64.
- Portar los subsistemas uno a uno con pruebas de regresión.
- Revisar identidad de clubes/plantillas, licencias y procedencia de assets gráficos.

La importación actual es parcial: no se han copiado las 321 entradas completas del proyecto Godot. Se preserva el repositorio fuente y se migran datos y lógica compatible selectivamente.
