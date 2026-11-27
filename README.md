# Fútbol Nova

Videojuego de fútbol NOVA para Android, con partido 3D local, datos deportivos offline y modos ampliables.

## Motor oficial: Android nativo

**Fútbol Nova deja de utilizar Godot como motor de ejecución.** El juego se reconstruye sobre un runtime propio:

- Java 17: aplicación Android, navegación, menús, selección de equipos y controles táctiles.
- C++17: simulación del partido y lógica de juego.
- OpenGL ES 3.0: renderizado 3D directo.
- Android SDK, NDK 26.3.11579264, CMake 3.22.1 y Gradle 8.7: compilación de la APK.
- JSON canónicos de `data/`: clubes, plantillas, perfiles, habilidades, técnicas, balones y módulos.

No se requiere Unity, Godot, Unreal ni una licencia de motor comercial. El desarrollo y la compilación se administran desde GitHub Actions.

## Proyecto Android

- Aplicación: `native-android/`
- Actividad y menús: `native-android/app/src/main/java/com/nova/futbolnova/MainActivity.java`
- Integración de datos: `native-android/app/src/main/java/com/nova/futbolnova/CanonicalDataRepository.java`
- Motor 3D: `native-android/app/src/main/cpp/native_engine.cpp`
- Contrato de datos: `data/native_runtime_manifest.json`
- Workflow: `.github/workflows/futbol-nova-native-android.yml`

La compilación genera el artefacto `futbol-nova-native-android-arm64` con la APK de depuración. En GitHub: **Actions → Fútbol Nova — Android nativo ARM64 → Run workflow**. Después de finalizar correctamente, abre la ejecución y descarga el artefacto.

## Datos deportivos

Los datos canónicos siguen en `data/` y se reutilizan desde el runtime nativo; no se duplican en otro catálogo. El lector nativo solo habilita plantillas que tengan exactamente 22 futbolistas con IDs únicos y no vacíos. Los perfiles personalizados solo se aplican cuando coincide el ID estable. Los catálogos de clubes no equivalen automáticamente a equipos jugables.

La validación previa a la compilación está en `tools/validate_native_data.py`.

## Estado real

La rama de migración incorpora un shell Android nativo, navegación de cuatro pantallas, selección de plantillas cubanas verificadas, controles táctiles y un prototipo de partido 3D con 22 actores procedurales, balón, cámara, marcador y reloj inicial. Es una base jugable en desarrollo, no un juego terminado.

Pendiente de completar y probar: modelos humanos y animaciones, porteros avanzados, árbitro, faltas, fueras de juego, saques, sustituciones, física completa, IA táctica, estadísticas, guardados nativos, Manager, Leyenda, ligas/copas y prueba física en teléfonos Android.

**No se declara una APK válida hasta que GitHub Actions compile, verifique el contenido ARM64 y se compruebe su instalación y funcionamiento en un dispositivo.**

## Referencias y archivos históricos

- `assets/references/` conserva capturas para estudiar jerarquía visual y usabilidad. Son referencias internas, no recursos para copiar al producto.
- `reference/` y `docs/` conservan investigación, contratos, reglas y documentación previa.
- `project.godot`, `scenes/` y `scripts/` son material histórico de la implementación anterior. Se mantienen temporalmente para portar sus reglas y sistemas; no se compilan, no se empaquetan y no son el motor oficial.
- Ultimate Team está excluido. La identidad, código, modelos, equipaciones, escudos, música y demás recursos del producto deben ser originales o contar con licencias compatibles.

## Auditoría y hoja de ruta

- [Auditoría de finalización](docs/REFERENCE_COMPLETION_AUDIT.md): funcionalidades verificadas, brechas y riesgos técnicos.
- [Hoja de ruta de juego nativo](docs/NATIVE_GAMEPLAY_ROADMAP.md): tareas ordenadas por prioridad y criterios de aceptación.
- Validación de datos: `python3 tools/validate_native_data.py` (fuentes JSON, IDs, plantillas, atributos y política de motor nativo).

La revisión de las plantillas cubanas confirma 52 equipos y 1.144 futbolistas ficticios con IDs únicos y 35 atributos por jugador. Los dos perfiles personalizados adicionales siguen catalogados, pero no están asociados a IDs de esas plantillas y no se aplican aún a sus alineaciones.

## Desarrollo desde GitHub

No hace falta PC ni instalar un motor en el teléfono. Los cambios se guardan en el repositorio y GitHub Actions compila la APK. El repositorio maestro NOVA y la sincronización de cambios se gestionan por separado para evitar sobrescribir datos.

