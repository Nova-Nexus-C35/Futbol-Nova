# Registro de implementación — Fútbol Nova Amistosos

Fecha: 2026-11-25  
Repositorio: `Nova-Nexus-C35/Futbol-Nova`  
Rama: `rebuild/libgdx-android-2026-11-22`  
Alcance: prioridad Amistosos; no modificar `main` ni fusionar PR #2.

## Cambio aplicado

### Controles táctiles visibles

Archivo: `app/src/main/java/com/novanexus/futbolnova/FutbolNovaGame.java`

- Añadido un joystick virtual visible en la zona izquierda.
- Añadidos botones visibles TIRO, PASE y ROBO en la zona derecha.
- La presentación usa las mismas zonas de pantalla que el input ya existente.
- El radio del joystick se limita a la altura/ancho disponibles para no dibujar un control fuera de pantalla.
- El indicador visual corrige la orientación Y entre coordenadas táctiles Android y coordenadas de render.
- La capa de controles solo aparece mientras el reloj permite acciones de juego.
- No se incorporó código, arte ni dependencia de terceros.

## Investigación pública

- `libgdx/gdx-ai`: Java/libGDX, steering, formaciones, pathfinding, árboles de comportamiento y FSM; LICENSE Apache-2.0 revisada. Candidato para una evaluación aislada futura, no dependencia adoptada.
- `ssenegas/kickoff`: Java/libGDX, referencia de arquitectura; licencia no verificada, sin reutilización.
- `erengaygusuz/fotbiler-football`: referencia de motor 3D/IA/reglas; licencia y procedencia pendientes, sin reutilización.

## Verificación

- Escritura de código confirmada por GitHub y lectura posterior del mismo archivo en la rama activa.
- Confirmados en el archivo import Matrix4, llamada a drawTouchControls, render de joystick/botones, ocultación fuera del juego activo y corrección del sentido vertical.
- CI de GitHub Actions para el commit `e4424a0dc3f3d1c99d08faa1be2e5cb5d355e77e`: en ejecución al redactar esta nota. La ejecución anterior del primer commit de la misma secuencia fue cancelada automáticamente por el nuevo push; no se toma como fallo de compilación.
- No se ha instalado ni probado el APK en el Android físico del propietario. No se afirma aceptación de controles ni jugabilidad completa.

## Próximas tareas P0

1. Confirmar CI del HEAD actual.
2. Validar controles en Android; ajustar tamaño/zonas y multitáctil según prueba real.
3. Auditar la pantalla de prepartido y selección de clubes/plantillas.
4. Corregir y probar la sincronización balón-jugador, posesión y movimiento del jugador controlado.
5. Validar reinicios, porteros, goles, pausa, descanso, final y segundo amistoso.
6. Mejorar IA y reglas con escenarios deterministas y pruebas unitarias.
7. Entregar APK debug verificable para instalación en el teléfono.

## Límites

Este cambio no completa el modo Amistosos. No modifica `main`, no cierra ni fusiona PR #2 y no declara jugabilidad verificada en dispositivo.


## Segundo bloque aplicado — validación de coordenadas

Archivo: `app/src/main/java/com/novanexus/futbolnova/MatchSimulation.java`

- `update` ahora rechaza frames cuando delta, coordenadas del balón o coordenadas del futbolista controlado no son valores finitos.
- El rechazo sucede antes de mover al futbolista, recalcular la IA o aplicar separación, evitando que NaN/infinito contamine las 22 posiciones y objetivos.

Prueba: `app/src/test/java/com/novanexus/futbolnova/MatchSimulationTest.java`

- Se añadió una prueba que inyecta NaN e infinitos en las coordenadas de balón y controladas, y verifica que posiciones y objetivos previos no cambian.

## Investigación complementaria

- `ssenegas/kickoff`: Java/libGDX, MIT verificada; referencia de arquitectura/entrada, sin código reutilizado.
- `Geomancer86/Futtoboru`: Java/libGDX, GPL-3.0 verificada; se excluye código por licencia/enfoque.
- `blurz17/blurz-native-football-`: C++/SDL2/ODE/JNI, README describe controles táctiles Android; LICENSE declara GPLv3, API informa NOASSERTION. Solo se toma como referencia conceptual, no se integra.
- `libgdx/libgdx`: Apache-2.0, framework base mantenido.

## Estado de CI y APK del bloque anterior

- La ejecución de Android Actions `45688820438`, intento 2, para el commit `2c576181b0a240fa9f0345eeb3734670dcfdccc0` terminó SUCCESS: tests, build ARM64, verificación del runtime nativo y subida del artefacto.
- Artefacto `futbol-nova-android-arm64-debug`, ZIP de 3,497,242 bytes, con `app-debug.apk` de 9,259,789 bytes. Expira el 2026-12-09. Este APK corresponde al bloque anterior de controles visibles; no contiene todavía el nuevo guard de coordenadas.
- El nuevo commit de validación de coordenadas requiere su propia ejecución de CI antes de considerar su APK listo.
- Ningún APK ha sido instalado ni probado físicamente por el propietario en esta sesión.


## Auditoría de sincronización y posesión — 2026-11-25

Archivos inspeccionados desde la rama activa:
- app/src/main/java/com/novanexus/futbolnova/FutbolNovaGame.java
- app/src/main/java/com/novanexus/futbolnova/MatchSimulation.java
- app/src/main/java/com/novanexus/futbolnova/MatchGameplay.java
- app/src/test/java/com/novanexus/futbolnova/MatchGameplayTest.java

Hallazgos:
- El movimiento táctil actualiza controlledPosition; MatchSimulation.update copia esa posición al futbolista controlado.
- El cambio de jugador, el cambio de lado en el descanso, la reconstrucción de alineaciones, la sustitución del jugador controlado y la expulsión del jugador controlado sincronizan la posición de control en los puntos inspeccionados.
- Mientras existe un poseedor válido, updateGameplay ancla el balón delante de ese futbolista. Los tiros/pases liberan el índice de posesión y establecen la velocidad del balón; los reinicios también limpian la posesión.
- La adquisición de posesión no rechazaba explícitamente NaN/infinito ni velocidad negativa. Se endureció MatchGameplay.acquirePossession para validar todas las coordenadas, altura y velocidad antes de calcular distancias.

Prueba añadida:
- MatchGameplayTest.possessionRejectsNonFiniteOrNegativeBallState cubre NaN, infinito, velocidad negativa y altura negativa. Se conserva la prueba existente para posesión de jugadores activos y excluye expulsados.

Investigación pública:
- atas76/openengine: README y LICENSE leídos; licencia MIT. Motor Java de partidos con enfoque contextual/data-driven. No se integra: el POM usa Java 18 y dependencias Maven (Spring/JDBC/SQLite/Jackson/Commons CSV), no un módulo Android/libGDX listo.
- modelence/open-soccer: referencia 11v11 en TypeScript/Canvas para separación de motor/render/tipos; no se integra en Java Android.
- No se copió código, assets ni dependencias de esos repositorios.

## Verificación posterior a esta actualización

- La ejecución CI 45689356818 terminó SUCCESS para el commit anterior 8235ca389d4a6d8c4973700706f69aaf04863fe4: pruebas, build Android ARM64 y verificación del runtime nativo completados.
- Los cambios nuevos de adquisición de posesión y sus pruebas se escribieron después de esa ejecución. Necesitan una ejecución CI propia; no atribuirles todavía el resultado SUCCESS anterior.
- Pendiente de pruebas de integración para continuidad en pases/tiros/robos/sustituciones/reinicios y de prueba física en Android.


## Segunda pasada de continuidad de posesión — 2026-11-25

Cambios de código:
- MatchGameplay.acquirePossession ahora omite slots nulos y jugadores inactivos o con coordenadas X/Z no finitas, evitando que una entrada dañada rompa el cálculo de distancias.
- MatchGameplayTest añade cobertura para listas con slots nulos/coordenadas inválidas y para el contrato de posesión después de una entrada exitosa, sustitución que conserva el índice de alineación y balón demasiado rápido que luego vuelve a velocidad controlable.
- La prueba de entrada establece orientaciones opuestas correctas para distinguir una entrada limpia de una falta.

Archivos:
- app/src/main/java/com/novanexus/futbolnova/MatchGameplay.java
- app/src/test/java/com/novanexus/futbolnova/MatchGameplayTest.java

Alcance:
- Son pruebas de reglas/contratos del motor de simulación; no son una prueba end-to-end de los métodos privados de FutbolNovaGame.
- El ciclo completo de pase, tiro, robo, sustitución, reanudación y gol todavía necesita pruebas de integración que conduzcan el estado del partido.
- El workflow .github/workflows/android-build.yml está configurado para ejecutarse al hacer push a esta rama. Este commit documental final también dispara una ejecución para validar todos los cambios anteriores de esta tanda.
- No se ha instalado ni probado el APK en un teléfono real.


## Cierre de corrección de pruebas y CI — 2026-11-25

- La ejecución 45691048752 identificó dos errores de las pruebas recién añadidas: se intentaba modificar la vista no modificable de getPlayers(), y el amistoso creado con start("home","away") no tiene suplentes para probar una sustitución.
- Se corrigieron las pruebas usando una copia mutable únicamente para simular slots nulos y dos plantillas de 14 futbolistas para disponer de suplentes reales en el escenario de sustitución. La vista de simulación se conserva para verificar que el reemplazo mantiene el índice estable.
- Commit verificado: 2ea93695f8769fce3eea7326a01704d308f4a603.
- CI final: [45691426001](https://github.com/Nova-Nexus-C35/Futbol-Nova/actions/runs/45691426001) — SUCCESS. Pasaron testDebugUnitTest y assembleDebug; el paso de verificación encontró lib/arm64-v8a/libgdx.so y el artefacto se subió correctamente.
- Artefacto: futbol-nova-android-arm64-debug, ID 15059376049, ZIP 3,497,329 bytes, SHA-256 8bfb2563f2c2491801aec06da0d71a9e6f951b6982d1e576c4f1f60a655661c3. APK dentro: 9,259,789 bytes. ZIP y APK se inspeccionaron; el APK contiene AndroidManifest.xml y lib/arm64-v8a/libgdx.so.
- Este CI valida la compilación y pruebas unitarias automatizadas. No demuestra el funcionamiento táctil ni el partido completo en un teléfono físico.
- Pendiente real: pruebas end-to-end de FutbolNovaGame para pase/tiro/robo/reanudación/gol, y prueba del APK en Android. No se marca Amistosos como terminado hasta completar ambas.
