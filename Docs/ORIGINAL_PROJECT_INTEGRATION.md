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

## Orden de portabilidad

1. Dominio deportivo: perfiles, IDs, equipos, alineaciones 4-3-3, contrato y reloj.
2. Partido 3D: movimiento, posesión, balón, pases, tiros, entradas, porteros y cámara; traducir los sistemas originales a Java/libGDX.
3. Reglas: fases, interrupciones, sustituciones, goles, reinicios, faltas y fuera de juego.
4. Temporadas: calendarios, tablas, competiciones y simulación determinista.
5. Carreras: Manager, Leyenda, contratos, economía, cantera y progresión.
6. Módulos: manifiestos y paquetes opcionales, conservando el juego base offline.

## Pendiente de validación

- Ejecutar las pruebas Java y la suite completa en GitHub Actions.
- Compilar el APK ARM64 y probar lectura de assets en Android 9.
- Portar los subsistemas uno a uno con pruebas de regresión.
- Revisar identidad de clubes/plantillas, licencias y procedencia de assets gráficos.

La importación actual es parcial: no se han copiado las 321 entradas completas del proyecto Godot. Se preserva el repositorio fuente y se migran datos y lógica compatible selectivamente.
