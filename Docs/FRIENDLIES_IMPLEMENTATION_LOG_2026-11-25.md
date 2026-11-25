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
