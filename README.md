# Fútbol Nova

Repositorio independiente del videojuego **Fútbol Nova**. Contiene la copia del proyecto publicada anteriormente dentro de Cumba35/Web-Publica-Nova/futbol-nova.

## Desarrollo y compilación

El desarrollo se administra mediante **ChatGPT y GitHub**. No es necesario instalar Godot, Android Studio, Unity ni otro editor en el teléfono. Las pruebas y exportaciones se ejecutan en GitHub Actions. El flujo de pruebas y APK Android está en [Fútbol Nova — pruebas y APK](https://github.com/Cumba35/Futbol-Nova/actions/workflows/futbol-nova-android-apk.yml).

- Proyecto Godot: project.godot
- Documentación del juego: GAME_README.md y docs/
- Datos, clubes y jugadores: data/
- Escenas y scripts: scenes/ y scripts/
- Pruebas: tests/
- Investigación de repositorios públicos y política de reutilización: [docs/THIRD_PARTY_RESEARCH.md](docs/THIRD_PARTY_RESEARCH.md).

## Repositorio maestro NOVA

La copia original y el historial principal del proyecto se conservan en el repositorio privado Cumba35/Nova-Workspace, en proyectos/juegos/futbol-nova/.

Este repositorio es la distribución independiente de Fútbol Nova. Los cambios no se sincronizan automáticamente hacia Nova Workspace: se revisan primero para evitar sobrescribir el proyecto maestro.

## Origen de esta importación

La importación copia el contenido de Cumba35/Web-Publica-Nova/futbol-nova/ desde la rama main. El repositorio público de origen no se elimina ni se modifica.


## Investigación técnica

Las alternativas públicas, sus licencias, compatibilidad Android y decisiones de reutilización se registran en [la auditoría de repositorios públicos](docs/PUBLIC_GITHUB_RESEARCH_2026-11-18.md).


## Estado de validación — 18-11-2026

- Se corrigió la referencia global `ManagerCareer` que impedía compilar la prueba headless del perfil de Máximo Padilla; detalle en [CI_44450991327_FIX.md](docs/CI_44450991327_FIX.md).
- La corrección está sincronizada con `Cumba35/Nova-Workspace`; la nueva ejecución CI `44453253855` debe terminar antes de declarar aprobada la corrección o la APK.
- Godot 4.4.1, funcionamiento offline y Android ARM64 se mantienen como requisitos.


## Estado CI — 19-11-2026

La ejecución 44453253855 detectó una segunda referencia global a FootballDatabase en el servicio de carrera durante compilación headless. Se corrigieron las llamadas de guardado y carga mediante búsqueda de /root/FootballDatabase; la corrección está sincronizada con Nova Workspace. CI nueva 44507868982 pendiente. Ver docs/CI_44450991327_FIX.md. No declarar APK validada antes de completar la CI.


## Corrección CI — 19-11-2026

Se corrigió el aislamiento headless de la prueba de Máximo Padilla: la física se desactiva antes de añadir el nodo al árbol. Los detalles y la sincronización están en [CI_44507868982_DIAGNOSIS.md](docs/CI_44507868982_DIAGNOSIS.md). La nueva ejecución debe pasar las regresiones y generar el APK antes de declarar la versión validada.


## Prioridad de desarrollo — amistoso 11v11 (19-11-2026)

- El objetivo inmediato es obtener un APK arm64 instalable y validar un partido amistoso completo en Android; no se priorizan otros modos hasta completar esta prueba.
- La ejecución GitHub Actions 330 superó las pruebas de base 11v11, amistoso en vivo con goles y períodos, física del balón, escena Android, jugadores personalizados y la regresión de base compartida. En la última consulta continuaba en `test_tas_competition_engine.gd`; todavía no había llegado a exportar el APK.
- Se corrigió la prueba aislada de `FootballDatabase` y se añadieron verificaciones multitáctiles para el joystick y los botones de acción. La prueba de amistoso también mantiene aislada la posesión humana de los 21 actores IA mientras valida el pase y el tiro.
- El workflow fue reorganizado en el commit `dec8c02bea1e17599649222da5c73b3541d38d39`: exporta y publica el APK después de las pruebas principales del partido y de la escena Android, antes de las regresiones extendidas.
- La revisión de referencias públicas y licencias está en [FRIENDLY_MODE_REFERENCE_AUDIT.md](docs/FRIENDLY_MODE_REFERENCE_AUDIT.md). No se han añadido dependencias externas al motor.
- Los cambios de pruebas, documentación y estado también se sincronizaron en la ruta canónica `Cumba35/Nova-Workspace/proyectos/juegos/futbol-nova/`. Las ejecuciones 490–492 de ese repositorio terminaron sin runner asignado (`runner_id=0`), sin ejecutar pruebas; no son fallos funcionales confirmados.
- La nueva ejecución 332 está en cola detrás de la ejecución 330. No declarar el APK terminado hasta que exista el artefacto `futbol-nova-android-debug` y se pueda instalar y jugar en un dispositivo Android.


## Actualización de mantenimiento — 19-11-2026

La ejecución 334 (`44549563484`) superó las pruebas principales del amistoso local 11v11 y la escena Android, pero no pudo exportar el APK porque Godot no tenía configuradas sus rutas globales de Android SDK y Java. Se corrigió el workflow para generar `~/.config/godot/editor_settings-4.4.tres` con ambas rutas y el keystore temporal. La corrección está en el commit `fb024313eb48571695cc912e12da9eee00e28751` de Futbol-Nova. La ejecución 335 (`44551233214`) está en curso; no declarar APK disponible hasta verificar exportación, artefacto ARM64 e instalación real. Diagnóstico completo: `docs/CI_44549563484_DIAGNOSIS.md`.


## Seguimiento de desarrollo — 19-11-2026

- [Estado de desarrollo y prioridades](docs/DEVELOPMENT_STATUS_2026-11-19.md)
- [Investigación complementaria de repositorios públicos](docs/PUBLIC_GITHUB_RESEARCH_2026-11-19.md)
- [Diagnóstico de exportación Android CI 44551233214](docs/CI_44551233214_DIAGNOSIS.md)
- La exportación Android ahora ejecuta Godot con `--verbose` para registrar el motivo exacto de cualquier error de configuración del preset.
- La documentación nueva está sincronizada con la ruta maestra de Nova Workspace y sus SHA de contenido fueron verificados.


## Prioridad de amistosos — 20-11-2026

- Estado actual y resultados CI: [docs/DEVELOPMENT_STATUS_2026-11-20.md](docs/DEVELOPMENT_STATUS_2026-11-20.md)
- Auditoría de mejoras y pruebas del amistoso: [docs/FRIENDLY_MODE_REFERENCE_AUDIT.md](docs/FRIENDLY_MODE_REFERENCE_AUDIT.md)
- Hoja de ruta del motor de partido: [docs/MATCH_ENGINE_COMPLETION_ROADMAP.md](docs/MATCH_ENGINE_COMPLETION_ROADMAP.md)
- Se añadió decisión de tiro para la IA y la prueba dedicada `tests/test_friendly_ai_shooting.gd`; su resultado debe confirmarse en GitHub Actions.
- Se corrigió la elevación del tiro y se limitó el posicionamiento lateral del portero; pruebas: `tests/test_friendly_goalkeeper_positioning.gd` y `tests/test_friendly_ai_shooting.gd`.
- La ejecución más reciente con estas pruebas es [44676705137](https://github.com/Cumba35/Futbol-Nova/actions/runs/44676705137), pendiente de finalizar.
