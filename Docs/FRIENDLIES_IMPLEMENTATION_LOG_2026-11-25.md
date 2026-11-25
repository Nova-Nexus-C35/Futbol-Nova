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
