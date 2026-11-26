# Fútbol Nova

Native Android football game project. The current development line uses Java 17, Android SDK/Gradle and libGDX 1.14.2 (Apache-2.0). It does not use Unity, Godot, a proprietary editor, a paid engine licence, or bundled third-party game assets.

## Current prototype

- Android application, landscape orientation, offline-first, ARM64 target.
- Procedural 3D pitch, 22 player placeholders, ball, perspective camera and touch input.
- Java 4-3-3 11v11 simulation ported from the original project: tactical repositioning, nearest-player pressure, goalkeeper positioning, stamina and player separation.
- Aerodynamic ball flight (gravity, drag and Magnus spin) and stricter goal-line geometry.
- Match clock, two-half flow, score, substitutions, basic discipline/advantage and match statistics are implemented in the current development branch; full referee decisions and regulation restarts remain incomplete.
- Versioned match snapshots and local JSON checkpoints restore simulation, benches, clock, score, statistics, cards, advantage and ball/gameplay state; periodic and lifecycle checkpoints are enabled.
- The procedural stadium includes stands, LED perimeter boards, floodlight towers, roof beams, pitch markings and goal nets. Players remain low-poly procedural humanoids with basic gait animation.
- Geometry is generated in code; no third-party player models, club kits, logos, sounds or images are bundled.

This remains an early 3D technical vertical slice, not a complete football game or a FIFA-equivalent production. Advanced player animations/models, realistic goalkeeper reactions and collisions, complete live referee/rules, tactical AI, licensed club assets, audience/audio, career modes and device acceptance/release signing remain to be implemented.

## Build without a PC

The repository includes GitHub Actions workflow `.github/workflows/android-build.yml`. A push to the development branch or a pull request to `main` runs unit tests and attempts to build an ARM64 debug APK. The workflow uploads the APK as an artifact if the build succeeds.

The user does not need Unity, Godot, Android Studio or a desktop editor to trigger the hosted build. GitHub Actions provisions Java, Gradle and Android SDK on its runner. GitHub Actions usage limits and availability still apply.

## Local build (optional)

With Java 17, Android SDK platform 35 and Gradle 8.9 installed:

```sh
gradle :app:testDebugUnitTest :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Repository structure

- `app/src/main/java`: Android entry point, 3D rendering prototype and plain Java match state.
- `app/src/test/java`: unit tests.
- `.github/workflows`: hosted Android build and test.
- `Docs/`: technology decisions, status and implementation roadmap.
- `THIRD_PARTY_NOTICES.md`: third-party framework and licence record.

## Development rules

- Keep `main` protected from unverified rewrites; work on feature branches.
- Read the current file before editing; preserve existing content.
- Verify every write by reading it back from GitHub.
- Do not claim build, test or device validation until the corresponding result exists.
- Study public GitHub repositories for architecture and techniques; do not copy code/assets unless the exact licence and obligations are compatible.
- Keep all features offline-capable unless an explicitly optional online feature is required.


## Integración del proyecto original

La fuente original está conservada en `Nova-Nexus-C35/Nova-Workspace/Projects/Games/futbol-nova/` y su versión histórica usa Godot 4.4.1. El proyecto Android actual mantiene Java 17 + libGDX por decisión del propietario. Los catálogos JSON originales se están migrando a `app/src/main/assets/data/original/` y se leen mediante `OriginalProjectData`. La portabilidad completa de gameplay sigue pendiente. Consulta `Docs/ORIGINAL_PROJECT_INTEGRATION.md`.


## Jugadores humanos 3D realistas

Las especificaciones reproducibles de los dos uniformes están en `Assets/Players/characters/home.json` y `away.json`. El workflow `.github/workflows/generate-football-players.yml` usa Ara 3D Gen CLI fijado a la versión 0.11.0 para generar modelos GLB riggeados de detalle alto con animaciones futbolísticas. Los modelos generados se guardan en `app/src/main/assets/models/players/home.glb` y `away.glb` en esta rama de desarrollo.

El renderer usa dos GLB distintos (azul local y rojo visitante), mantiene el fallback procedural, oculta jugadores inactivos y conecta los controles TIRO/PASE/ROBO a clips deportivos cuando están presentes. La generación requiere que GitHub Actions pueda descargar Ara y sus datos CC0; los modelos no se consideran validados hasta que el workflow termine, la compilación Android pase y se prueben en un dispositivo.

Procedencia/licencias: [Ara 3D Gen](https://github.com/olatunde85/3d-gen), código MIT; datos MakeHuman CC0; animaciones CMU con sus condiciones de uso comercial y prohibición de reventa directa de los datos de captura. Consultar el ZIP/export de Ara y `Docs/REALISM_UPGRADE_RESEARCH_2026-11-25.md` antes de redistribuir assets.
