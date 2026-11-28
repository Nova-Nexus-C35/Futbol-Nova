# Fútbol Nova — migración al motor nativo (27-11-2026)

## Decisión aplicada

El runtime de distribución pasa a ser Android nativo: Java 17, C++17, JNI y OpenGL ES 3.0. No se utilizarán Godot, Unity ni otro motor de juegos. La compilación se realiza en GitHub Actions con Android SDK/NDK, CMake y Gradle.

## Integración de esta etapa

- Se recuperó el prototipo nativo de la rama `work/native-3d-engine` y se incorporó a la línea principal mediante esta rama de migración.
- Se conservan las cuatro pantallas principales: Inicio, Online, Modos y Configuración.
- Se integra el selector de equipos y el lector offline de JSON canónicos.
- Se mantienen las reglas de elegibilidad: 22 jugadores exactos, IDs únicos y perfiles personalizados vinculados solo por ID.
- El motor OpenGL ES contiene un campo 3D, actores procedurales, balón, cámara, controles táctiles, marcador y reloj inicial.
- Se añade una validación Python de las fuentes JSON antes de compilar.
- El workflow Android nativo se activa en cambios de `native-android/**` y `data/**`, y publica una APK ARM64 como artefacto.
- El antiguo workflow de exportación Godot se desactiva. Los archivos Godot quedan como archivo histórico de consulta, no como runtime ni como ruta de compilación.

## Limitaciones que no deben ocultarse

Este cambio no significa que el juego esté terminado. El prototipo nativo todavía necesita completar y validar física del balón, porteros, reglas arbitrales, saques, fuera de juego, IA táctica, animaciones humanas, modelos 3D finales, guardados, estadísticas y modos de carrera/competiciones. La APK solo se considera compilada cuando el nuevo workflow termina correctamente; su funcionamiento real exige instalación y prueba en un teléfono.

## Próximas prioridades

1. Ejecutar CI nativa y corregir cualquier error real de Gradle, NDK, CMake, Java o C++.
2. Probar carga de plantillas, selección de dos clubes distintos y arranque del partido.
3. Separar el estado de partido del renderizado y completar goles, reloj, pausas y reinicios.
4. Mejorar control de jugador, selección dinámica, pases con receptor y tiros.
5. Implementar porteros e IA por roles.
6. Portar de forma incremental los contratos, simulación de temporadas, ligas, copas y carreras existentes.
7. Añadir pruebas de regresión nativas para cada sistema antes de activarlo.

## Criterio de conservación

No borrar todavía `project.godot`, `scenes/` ni `scripts/`: contienen contratos, reglas y sistemas que sirven como especificación para la migración. No se compilan ni se incluyen en la APK nativa. Los JSON de `data/` siguen siendo la fuente canónica y no deben duplicarse.
