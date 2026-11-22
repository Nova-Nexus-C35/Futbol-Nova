# Fútbol Nova — evaluación tecnológica y decisión provisional

Fecha: 2026-11-22
Repositorio: Nova-Nexus-C35/Futbol-Nova
Rama: rebuild/libgdx-android-2026-11-22

## Requisito confirmado por el usuario

No usar Unity ni Godot. Priorizar soluciones sin programas externos propietarios, licencias de pago ni costes obligatorios. El usuario trabaja desde Android y no dispone de PC. Investigar repositorios públicos de GitHub de otros autores para aprender y mejorar el proyecto, sin copiar código o recursos sin permiso. Mantener la posibilidad de generar APK Android ARM64 y funcionar offline.

## Decisión de esta fase

Se crea una prueba de viabilidad con **Java 17 + Android SDK + Gradle + libGDX 1.14.2**. libGDX es un framework de código abierto, no un editor de escenas ni un servicio de pago. El proyecto no necesita Unity Hub, Godot Editor ni activación. Gradle descarga las dependencias y GitHub Actions proporciona Java/Gradle/Android SDK en un runner hospedado.

La decisión es provisional hasta que el primer workflow termine con éxito y el APK pueda instalarse y ejecutarse en un teléfono real. No se presenta todavía como motor definitivo de todo el juego.

## Repositorios públicos estudiados

### 1. libGDX — framework candidato de render y entrada
- URL: https://github.com/libgdx/libgdx
- Uso declarado: framework Java multiplataforma, incluye Android y gráficos 2D/3D sobre OpenGL.
- Licencia del repositorio: Apache-2.0.
- Versión estable consultada: 1.14.2.
- Uso en Fútbol Nova: dependencia Maven fijada en 1.14.2. No se copiaron archivos fuente ni assets del juego de terceros.
- Valor: API de render 3D, cámara, modelos, input táctil, ciclo de vida y empaquetado Android.
- Riesgos: tamaño y compatibilidad de bibliotecas nativas; revisar dependencias transitivas, consumo de memoria, rendimiento en teléfonos modestos y mantenimiento de la versión.

### 2. League-Soccer — referencia de lógica futbolística y gestión
- URL: https://github.com/awest813/League-Soccer
- Tecnología declarada: C++17, CMake, SDL, OpenGL, OpenAL, ODE y SQLite.
- Licencia raíz verificada: Apache-2.0.
- Sistemas descritos: simulación 22 jugadores, física del balón, IA táctica, árbitro, fuera de juego, faltas, fatiga, sustituciones, lesiones, estadísticas, replay, ligas, cantera, scouting, contratos y finanzas.
- Plataforma declarada en README: Linux, macOS y Windows; no declara Android.
- Uso en Fútbol Nova: referencia de diseño, requisitos, modelos de dominio y pruebas. No copiar código por ahora ni asumir portabilidad Android.

### 3. Android Native Football — referencia nativa Android
- URL: https://github.com/blurz17/blurz-native-football-
- Tecnología declarada: C++, SDL2, JNI, OpenGL ES, ODE, CMake y NDK; Android ARM64.
- Licencia raíz verificada: GPL-3.0.
- Sistemas descritos: entrenamiento, HUD táctil, joystick 360°, cámara tercera persona, física y sincronización de hilos.
- Uso en Fútbol Nova: estudiar patrones arquitectónicos y controles. No copiar código ni assets en esta fase; una obra derivada distribuida puede activar obligaciones GPLv3 y las dependencias/recursos tienen licencias separadas.

### 4. Player12 — referencia de gestión
- URL: https://github.com/FlavioMili/Player12
- Tecnología declarada: C++23, SDL3, Dear ImGui y SQLite.
- Licencia raíz consultada: GPL-3.0.
- Funciones descritas: mundo futbolístico, ligas, copas, promociones, gestión de plantilla, cantera, fichajes, finanzas y partidos 2D/3D.
- Plataforma declarada: Linux/macOS y Windows experimental; no Android.
- Uso: referencia conceptual y de organización de sistemas. No incorporar código GPL al proyecto Android.

### 5. The3Deer Android 3D Engine — alternativa secundaria
- URL: https://github.com/the3deer/android-3D-engine
- Tecnología declarada: Kotlin, OpenGL ES 2/3, animación esquelética, luces, colisiones y loaders OBJ/STL/DAE/glTF/FBX.
- Licencia raíz verificada: MIT. README declara dependencias con licencias separadas (incluye LGPL para un parser STL y MIT/ISC para otros componentes).
- Compatibilidad declarada: Android 8+.
- Uso: alternativa a evaluar si libGDX presenta limitaciones. No incorporado a la build inicial para evitar añadir dependencias antes de necesitarlas.

### 6. GLARE — alternativa sin bibliotecas externas
- URL: https://github.com/david-palm/GLARE
- Tecnología declarada: Java + OpenGL ES 3; el README dice expresamente que no usa bibliotecas externas.
- Licencia raíz verificada: MIT.
- Estructura revisada: renderer, shaders, buffers/meshes, cámara, escena, materiales, luces, eventos, capas y matemáticas 3D.
- Limitaciones: demo de alcance reducido centrada en un cubo/textura; README indica que aún no tiene renderizado de texto. Últimos commits visibles: enero de 2023. build.gradle usa Android Gradle Plugin 7.2.1, por lo que no es una base moderna lista para compilar sin actualización.
- Valor: referencia de cómo separar un renderer Android OpenGL ES propio sin depender de un framework externo.
- Decisión: no se integra ahora como motor completo. Si libGDX no cumple el criterio del usuario, evaluar una implementación propia sobre Android OpenGL ES, usando ideas generales de renderer/mesh/cámara y sin copiar código sin necesidad.

### 6. NationSoccer — referencia histórica
- URL: https://github.com/delight-im/NationSoccer
- Proyecto Android 1v1 antiguo, marcado como no mantenido.
- README describe GPL-3.0 para código Java y licencias separadas para dependencias/medios.
- No apto como base de un juego 3D 11v11 moderno. Solo referencia histórica.

## Política de propiedad intelectual

- No copiar código de repositorios GPL-3.0 ni mezclarlo por accidente con una distribución cuya licencia sea incompatible.
- No copiar modelos, retratos, uniformes, escudos, nombres comerciales, música o sonidos sin derechos expresos.
- Las licencias de código no autorizan automáticamente los assets.
- Mantener avisos y textos de licencia de las dependencias efectivamente empaquetadas.
- No se decide todavía la licencia global del código original de Fútbol Nova.

## Arquitectura objetivo

1. **Football.Domain (Java puro):** partido, reloj, marcador, jugadores, equipos, atributos, contratos, finanzas y reglas; sin API de render.
2. **Football.Simulation (Java puro):** movimiento, posesión, balón, fatiga, colisiones simplificadas, IA, árbitro y simulación de partidos.
3. **Football.Render3D (libGDX):** campo, estadio, modelos, animación, cámara, luces y efectos.
4. **Football.Input:** interfaz táctil con joystick y acciones; desacoplada de la simulación.
5. **Football.UI:** HUD, menús, estadísticas y navegación.
6. **Football.Career:** Manager, Leyenda y Entrenador; calendario, ligas, copa, cantera, contratos, fichajes y economía.
7. **Football.Persistence:** guardado versionado, transacciones, recuperación y migraciones.
8. **Football.Assets:** recursos originales o con licencias verificadas, con manifiestos y hashes.
9. **Football.Online (opcional/futuro):** Nova Nexus y sincronización. El modo offline no dependerá de red.

## Prueba inicial implementada en esta rama

- Proyecto Gradle Android con Java 17, minSdk 28, targetSdk 35 y ABI arm64-v8a.
- libGDX 1.14.2 y backend Android.
- Geometría procedural propia: campo, 22 jugadores temporales, balón y líneas de campo.
- Cámara de perspectiva y luz.
- Entrada táctil preliminar: lado izquierdo para movimiento; lado derecho para pase/tiro.
- Estado de partido Java puro y pruebas unitarias de reloj, pausa y marcador.
- Workflow GitHub Actions para ejecutar pruebas y producir APK debug ARM64 sin licencia de motor.

## Validación pendiente

- Primer workflow CI: verificar descarga de dependencias, compilación, tests y empaquetado de las bibliotecas nativas ARM64.
- Corregir errores reales de Gradle/Java/Android manifest que aparezcan.
- Verificar que el APK existe como artefacto descargable.
- Instalarlo y probarlo en dispositivo Android físico; comprobar pantalla, orientación, input, estabilidad, temperatura, memoria y FPS.
- Medir tamaño del APK y eliminar dependencias no necesarias.
- La geometría actual es un placeholder: no hay modelos animados, porteros funcionales, física completa, IA de equipo ni reglas de partido.
- No fusionar a main hasta superar CI y una prueba real en Android.

## Reglas permanentes de mejora

Investigar repositorios públicos para cada subsistema importante (render, física, IA, árbitro, carrera, persistencia, input, animación, accesibilidad y build). Registrar URL, commit/tag revisado, licencia, plataforma, partes útiles, riesgos y decisión de reutilización. Preferir ideas y diseños compatibles; cualquier copia de código requiere auditoría de licencia y atribución. Actualizar este documento y el estado del proyecto después de cada decisión importante.
