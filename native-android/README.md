# Fútbol Nova — Android nativo

Este módulo es la implementación nativa propia de Fútbol Nova. No usa Godot, Unity, Unreal ni otro motor de juegos.

## Tecnologías
- Android Java 17: ciclo de vida, menús, selector de equipos, diálogo de pausa y controles táctiles.
- C++17: escena, movimiento, balón, formación y lógica de simulación inicial.
- OpenGL ES 3.0: renderizado directo en Android.
- Android NDK 26.3.11579264 + CMake 3.22.1: compilación de la biblioteca nativa.

## Datos
Los JSON canónicos de `../data/` se empaquetan como assets offline. `data/native_runtime_manifest.json` declara las fuentes y reglas de elegibilidad. `CanonicalDataRepository` valida las plantillas y no activa equipos con IDs repetidos o menos/más de 22 jugadores.

## Construir APK ARM64
Desde esta carpeta, con Android SDK/NDK y Gradle instalados:

```sh
gradle --no-daemon :app:assembleDebug
```

APK:
`app/build/outputs/apk/debug/app-debug.apk`

ABI:
`arm64-v8a`

## Estadio local
La pantalla de partido previo obtiene automáticamente el estadio desde la ficha canónica del equipo local. Nova C35 selecciona Estadio del Fénix (`nova_c35`). La paleta de gradas, cubierta y acentos se envía al renderer nativo por JNI y se aplica a la geometría procedural del estadio. Al cambiar el equipo local, cambia también el estadio asociado.

## Estado
Es un shell funcional de navegación y un prototipo de partido 3D. Los actores son blockouts procedurales; la simulación todavía no contiene el conjunto completo de reglas, árbitro, porteros, goles/saques, marcador, reloj, repeticiones, animaciones esqueléticas ni todos los modos. No considerarlo una versión terminada hasta pasar pruebas de partido 11v11 en dispositivo Android.
