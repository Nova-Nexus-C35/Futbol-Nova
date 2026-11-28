# Política de migración de Fútbol Nova a código nativo

Fecha: 2026-11-19.

## Decisión de arquitectura

El juego ejecutable de Fútbol Nova se construye con código propio:
- Android: Java 17.
- Simulación y renderizado: C++17.
- Gráficos: OpenGL ES 3.0.
- Datos y catálogos offline: JSON versionado en `data/`.

No se permite que el APK dependa de Godot, Unity, Unreal, WebView como motor de juego, ni de otro motor externo.

## Archivos históricos

El proyecto contiene archivos GDScript, escenas y `project.godot` de la implementación anterior. Se conservan temporalmente como material histórico para consultar reglas, contratos, catálogos y comportamiento ya diseñado. No forman parte del proyecto Gradle, no se compilan y no se empaquetan en el APK nativo.

No se deben añadir nuevas funciones al runtime Godot ni generar nuevas versiones instalables desde él.

## Migración segura

1. Identificar el contrato y los datos que usa cada sistema histórico.
2. Implementar el equivalente en Java/C++ dentro de `native-android/`.
3. Reutilizar los JSON canónicos de `data/`, sin duplicar ni inventar plantillas.
4. Añadir pruebas o validaciones automatizadas para reglas, estados y persistencia.
5. Activar la función solo cuando su implementación nativa pase las pruebas.
6. Retirar los archivos históricos únicamente cuando el reemplazo nativo esté completo y validado; no borrar reglas o datos útiles antes de migrarlos.

## Estado

La nueva aplicación Android nativa ya tiene shell, carga de datos offline, selector de plantillas verificadas y un prototipo 3D de amistoso. No equivale todavía a la migración completa de todos los sistemas del juego. Carrera, manager, ligas/copas, TAS, módulos, estadísticas completas, guardado, árbitro, porteros y simulación táctica siguen necesitando sus implementaciones nativas y pruebas.
