# Fútbol Nova

Native Android football game project. The current development line uses Java 17, Android SDK/Gradle and libGDX 1.14.2 (Apache-2.0). It does not use Unity, Godot, a proprietary editor, a paid engine licence, or bundled third-party game assets.

## Current prototype

- Android application, landscape orientation, offline-first, ARM64 target.
- Procedural 3D pitch, 22 temporary player placeholders, ball, perspective camera and simple touch input.
- Basic ball movement, goal detection, score and match clock.
- Match clock and score are separated into a plain Java class with unit tests.
- Geometry is generated in code; no external models, kits, logos, sounds or images are bundled.

This is an early technical vertical slice, not a complete football game. Player AI, proper collisions, goalkeeper behavior, animation, referee/rules, team tactics, career modes, saves and real-world assets remain to be implemented.

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
