# Prioridad de desarrollo: modo Amistosos — 2026-11-25

## Decisión confirmada del propietario

La prioridad principal de Fútbol Nova pasa a ser terminar y validar el **modo Amistosos** para que el propietario pueda probar el videojuego y su funcionamiento real en Android. Los demás modos (Manager, Leyenda, Entrenador, ligas, copas y online) quedan fuera del alcance inmediato, salvo que una dependencia compartida sea necesaria para el amistoso.

No se considera terminado el modo por el mero hecho de compilar el APK. La meta es una sesión de amistoso completa, repetible y estable en un dispositivo Android físico.

## Rama de trabajo

- Repositorio: `Nova-Nexus-C35/Futbol-Nova`.
- Rama activa examinada: `rebuild/libgdx-android-2026-11-22`.
- Stack: Java 17 + libGDX 1.14.2; Android ARM64; funcionamiento offline-first.
- No fusionar ni rebasar ramas como parte de esta priorización. PR #2 permanece abierto y su descripción exige reconciliar divergencias con `main` antes de cualquier merge.

## Estado de base verificado en código

La revisión del código actual confirma que ya existen componentes aprovechables:

- `TeamSelection`: selección cíclica de dos equipos entre plantillas con al menos once jugadores.
- `FutbolNovaGame`: carga de plantillas jugables y construcción de alineaciones iniciales; controles táctiles por zonas, cambio de futbolista, pausa, selección de equipos y flujo de descanso/final.
- `MatchClock`: fases PRE_MATCH, FIRST_HALF, HALF_TIME, SECOND_HALF y FULL_TIME; duración base de 45 minutos por tiempo y tiempo añadido. El runtime acelera el reloj (30 segundos de partido por segundo real), por lo que el encuentro reglamentario dura aproximadamente tres minutos reales sin tiempo añadido.
- `MatchSimulation`: simulación de los 22 futbolistas, control de jugador, banquillo y sustituciones.
- `MatchStatistics`, `MatchReport` y `MatchReportStore`: estadísticas, acta e historial local.
- `MatchSaveStore` y `MatchRuntimeSnapshot`: checkpoint local y recuperación de partido.
- La compilación Android ARM64 y las pruebas automatizadas pasaron en el workflow consultado del 2026-11-24. Esto no demuestra que el juego se haya instalado o jugado correctamente en un teléfono físico.

Estos elementos se consideran **implementados en alguna medida**, no aceptados como completos hasta superar las pruebas de extremo a extremo.

## Alcance P0 — Amistoso jugable

### A. Preparación del partido

- [ ] Pantalla clara de Amistosos como entrada al modo.
- [ ] Selector visual de equipo local y visitante, con escudos/nombres/colores disponibles y bloqueo para no elegir el mismo club dos veces.
- [ ] Confirmar que el selector carga plantillas reales del catálogo, no nombres genéricos ni equipos de reserva silenciosos.
- [ ] Mostrar alineación titular, posiciones, dorsal y banquillo antes del inicio.
- [ ] Seleccionar duración de cada tiempo y velocidad del reloj. Mantener una opción corta de prueba para sesiones rápidas.
- [ ] Elegir lado/campo y saque inicial si la simulación lo permite.
- [ ] Botón visible de iniciar y opción de volver sin perder datos.

### B. Partido y controles Android

- [ ] HUD táctil legible y adaptable a diferentes resoluciones y relaciones de aspecto.
- [ ] Joystick virtual visible con zona muerta, límites y respuesta consistente.
- [ ] Botones táctiles separados y visibles para pase, tiro, sprint, regate/protección y entrada/robo; acciones contextuales cuando corresponda.
- [ ] Multitáctil probado: mover y ejecutar una acción simultáneamente sin que un dedo robe el control del otro.
- [ ] Cambio de futbolista con indicador visual del jugador activo y selección contextual del compañero adecuado.
- [ ] Control de balón y respuesta de pase/tiro coherentes; evitar que el jugador controlado se desplace separado del futbolista visual.
- [ ] IA capaz de mantener un partido reconocible: avanzar, apoyar, defender, pasar, disparar cuando haya oportunidad y proteger su portería.
- [ ] Porteros capaces de intervenir ante tiros a puerta y resolver rebotes de forma razonable.
- [ ] Cámara estable, sin perder balón ni jugadores importantes.
- [ ] Pausa, reanudación y salida con confirmación; al volver de otra aplicación el partido debe permanecer pausado.

### C. Ciclo y reglas mínimas del amistoso

- [ ] Saque inicial ejecutado por un jugador correctamente situado.
- [ ] Saques de banda, esquina, puerta y tiros libres con posiciones y ejecutores comprensibles; no limitarse a lanzar automáticamente el balón desde el punto.
- [ ] Gol, actualización del marcador y reanudación desde el centro.
- [ ] Dos tiempos y descanso; cambio de lado y saque del segundo tiempo.
- [ ] Final del encuentro sin seguir simulando acciones.
- [ ] Faltas, fuera de juego, penaltis y tarjetas: implementar y probar las decisiones mínimas antes de marcar el reglamento como completo.
- [ ] Sustituciones con límites y actualización correcta del jugador controlado si sale el titular.
- [ ] No permitir entradas de gameplay durante pausa, descanso o final.

### D. Cierre y repetición

- [ ] Pantalla final con equipos, resultado, goleadores y estadísticas principales.
- [ ] Guardar acta e historial sin duplicar el mismo encuentro.
- [ ] Nuevo amistoso desde el resultado, con reinicio de balón, reloj, posesión, estadísticas, tarjetas, sustituciones y checkpoints.
- [ ] Cargar una partida interrumpida sin corrupción ni duplicación de goles/eventos.
- [ ] Prueba de dos amistosos consecutivos en una misma ejecución de la app.

## Orden de implementación

1. **Prueba de extremo a extremo del estado actual:** instalar el APK de la rama activa en Android ARM64 y documentar qué ocurre desde abrir el juego hasta terminar un partido. No dar por hecho que la CI prueba la jugabilidad.
2. **Corregir bloqueos de flujo:** pantalla de amistosos, selección de equipos y plantillas, prepartido, pausa, descanso, final y reinicio.
3. **Controles táctiles:** joystick y botones visibles, multitáctil, cambio de futbolista, calibración por resolución y respuesta en dispositivo.
4. **Jugabilidad del amistoso:** posesión, IA de ambos equipos, porteros, balón, cámara y reinicios reglamentarios.
5. **Resultado y persistencia:** acta, estadísticas, historial, recuperación y dos partidos consecutivos.
6. **Pulido mínimo de presentación:** HUD, indicadores, equipaciones legibles, animaciones esenciales y audio básico; evitar invertir primero en decoración de estadio.
7. **Validación de aceptación:** sesión completa offline en Android físico, estabilidad, rendimiento, temperatura, memoria, interrupciones y controles.

## Criterios de aceptación: “Amistoso funcional para pruebas”

El modo solo pasa a esta condición cuando se demuestre todo lo siguiente en un Android físico:

1. Abrir la app y entrar en Amistosos sin conexión a Internet.
2. Elegir dos clubes diferentes con sus plantillas disponibles.
3. Iniciar un partido 11 contra 11.
4. Mover al futbolista, pasar, tirar, defender y cambiar de jugador con controles táctiles.
5. La IA rival y la propia reaccionan durante el encuentro; los porteros pueden detener tiros.
6. Marcar al menos un gol y comprobar marcador y reanudación.
7. Pausar y reanudar; superar el descanso y terminar el segundo tiempo.
8. Consultar el resumen, estadísticas e historial.
9. Iniciar un segundo amistoso y confirmar que no hereda estado del anterior.
10. Cerrar/interrumpir la app durante un partido y comprobar la recuperación pausada.
11. Completar una sesión sin cierres, bloqueos de controles ni errores críticos.

## Fuera de alcance hasta aceptar el amistoso

Manager, Leyenda, Entrenador, temporadas, ligas y copas completas, mercado de fichajes, multijugador online, retos diarios y progresión de carrera. No se deben iniciar estas funciones antes de que el amistoso supere los criterios de aceptación, excepto trabajo compartido estrictamente necesario.

## Registro de verificación

- 2026-11-25: decisión de priorizar Amistosos confirmada por el propietario.
- 2026-11-25: código de selección, reloj, controles, estadísticas, guardado e historial inspeccionado en la rama indicada.
- Pendiente: auditoría interactiva en dispositivo Android; esta nota no afirma que el modo esté terminado ni que se haya probado físicamente.


## Avance de implementación — 2026-11-25

- [x] Dibujar joystick virtual y botones visibles TIRO/PASE/ROBO durante el juego activo, usando las mismas zonas de pantalla que consume el input.
- [x] Ajustar el radio del joystick a las dimensiones reales de pantalla y corregir el sentido visual vertical del indicador.
- [ ] Probar en Android físico la legibilidad, el alcance con pulgares, el movimiento diagonal y el multitáctil movimiento+acción.
- [ ] Confirmar por CI la compilación y todas las pruebas tras el cambio.

Este avance solo cubre la capa visual inicial de controles. No marca como completados el joystick analógico calibrado, la reasignación, la asistencia contextual, ni la aceptación del partido en dispositivo.


## Avance adicional de robustez — 2026-11-25

- [x] Proteger el motor de simulación frente a coordenadas NaN/infinito del balón o del jugador controlado; el update descarta el frame inválido antes de modificar posiciones o decisiones de IA.
- [x] Añadir prueba de regresión que comprueba que posiciones y objetivos de jugadores permanecen intactos ante coordenadas no finitas.
- [ ] Validar con escenarios de partido reales que el balón nunca genera coordenadas no finitas y revisar telemetría si ocurre.


## Auditoría jugador–balón–posesión — 2026-11-25

- [x] Revisar sincronización del futbolista controlado al cambiar de jugador, iniciar segundo tiempo, reconstruir alineaciones, sustituir o expulsar; los puntos de cambio inspeccionados actualizan la posición de control.
- [x] Revisar el anclaje del balón al poseedor y la liberación en tiro/pase/robo/reinicio; la lógica existente mantiene la posesión en el índice del jugador y ancla el balón durante el turno del poseedor.
- [x] Rechazar estados no finitos o físicamente inválidos en la adquisición de posesión y cubrirlos con pruebas.
- [ ] Añadir pruebas de integración para continuidad del balón y del índice de poseedor en pase, tiro, robo, sustitución y reinicio.
- [ ] Ejecutar CI para el nuevo commit que contiene esta defensa y sus pruebas.
- [ ] Validar el comportamiento real en Android; la inspección de código y CI no sustituyen la prueba táctil en el teléfono.

La auditoría no certifica todavía la simulación completa: los puntos pendientes deben cerrarse antes de declarar Amistosos listo.


## Continuidad de posesión: regresiones de nivel simulación — 2026-11-25

- [x] La búsqueda de poseedor ignora entradas nulas y jugadores con coordenadas X/Z no finitas.
- [x] Añadir cobertura de regresión para adquisición tras una entrada exitosa, sustitución en un índice estable y rechazo/re-adquisición del balón según velocidad.
- [ ] Completar pruebas de integración del ciclo completo de balón dentro de FutbolNovaGame (pase, tiro, robo, sustitución, reanudación y gol).
- [x] CI Android para los cambios de esta sección: ejecución 45691426001 SUCCESS (123 pruebas, APK ARM64 y runtime nativo verificado).
- [ ] Prueba táctil y de partido real en Android.

Las pruebas añadidas ejercitan las reglas y los contratos del motor, pero no invocan directamente los métodos privados de renderizado/control de FutbolNovaGame; por eso no se marcan como integración end-to-end.
