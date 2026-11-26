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



## Actualización de presentación y estrategia de realismo — 2026-11-25

- [x] Revisar el HUD que aparece en la captura del propietario: había demasiada información distribuida en líneas inferiores muy próximas y ayudas redundantes.
- [x] Aplicar escala adaptativa de fuente y reorganizar el HUD en marcador, jugador, estado y tres líneas compactas de estadísticas. Se conservan los indicadores principales; el cambio no altera gameplay.
- [ ] Validar visualmente el HUD en Android físico y en distintas relaciones de aspecto.
- [x] Investigar repositorios públicos para modelos glTF/PBR, animación esquelética y simulación futbolística; decisiones y licencias registradas en Docs/REALISM_UPGRADE_RESEARCH_2026-11-25.md.
- [ ] Crear prueba aislada de gdx-gltf 2.3.0 antes de incorporar la dependencia al APK.
- [ ] Sustituir gradualmente los futbolistas low-poly por modelos humanos 3D riggeados, originales o con licencia explícita, manteniendo fallback.
- [ ] Implementar estados y transiciones de animación conectados a la simulación (carrera, frenado, giro, pase, tiro, entrada, caída, recuperación, portero y celebración).
- [ ] Mejorar materiales PBR, césped, estadio, iluminación, sombras, público y redes con presupuestos de rendimiento Android.
- [ ] Rediseñar y validar cámara broadcast/HUD con capturas reales de dispositivo.
- [ ] Añadir pruebas de integración jugador–animación–balón y perfilado de FPS, memoria, temperatura y batería.

**Nota de alcance:** el objetivo tipo FIFA es un objetivo de calidad, no una afirmación de paridad inmediata. La compilación CI del HUD aún debe completarse; no se marca como validado en dispositivo.


## Pipeline de jugadores GLB opcional — 2026-11-25

- [x] Añadir gdx-gltf 2.3.0 y repositorio JitPack al proyecto Android.
- [x] Crear GltfPlayerSquad: carga local de GLB, escenas por jugador, transformaciones desde MatchSimulation, selección de Idle/Walk/Run y render PBR.
- [x] Conectar FutbolNovaGame al renderer GLB opcional con fallback automático a PlayerVisual cuando falta el modelo o falla la carga.
- [x] Incorporar fixture GLB CC0 de Quaternius solo en recursos de test; conservar aviso de licencia y probar cabecera/skin/clips.
- [x] Documentar contrato del asset activo en app/src/main/assets/models/players/README.md.
- [ ] Ejecutar CI; confirmar resolución JitPack, compilación Android y pruebas de fixture.
- [ ] Proporcionar un GLB humano realista de producción en app/src/main/assets/models/players/active.glb; el fixture low-poly no es aceptable como arte final.
- [ ] Verificar que el modelo real se ajusta a la cámara, escala, orientación, 24 huesos por draw y rendimiento del Android objetivo.
- [ ] Implementar mapeo de animaciones futbolísticas (dribbling, control, pase, tiro, tackle, caída, portero y celebraciones) y sincronización balón-pie.
- [ ] Activar renderer GLB por defecto solo después de validación en dispositivo y pruebas de estabilidad.


## Futbolistas humanos de detalle alto — 2026-11-26

- [x] Investigar Ara 3D Gen y verificar código MIT, datos MakeHuman CC0 y condiciones CMU.
- [x] Crear especificaciones reproducibles para el preset The Footballer: uniforme azul local y rojo visitante.
- [x] Crear workflow CI para generar dos GLB de detalle alto con clips futbolísticos seleccionados y guardarlos en la rama de desarrollo.
- [x] Adaptar GltfPlayerSquad para cargar dos assets distintos, mantener locomoción, reproducir acciones y ocultar jugadores inactivos.
- [x] Conectar controles TIRO/PASE/ROBO con animaciones de tiro, pase y entrada.
- [ ] Ejecutar el workflow y comprobar que genera ambos GLB; inspeccionar tamaño, nodos, huesos y nombres de clips.
- [ ] Ejecutar Android CI después de la generación y comprobar empaquetado de ambos GLB en APK ARM64.
- [ ] Probar en Android físico: escala, orientación, materiales, uniformes, rendimiento, memoria, temperatura y estabilidad.
- [ ] Añadir acciones de recepción, control, cabezazo, portero, caída y celebración a los eventos reales de gameplay.
- [ ] Ajustar el punto de contacto pie-balón y sincronizar el fotograma de impacto con la física del balón.
- [ ] No retirar el fallback procedural ni declarar realismo final hasta completar validación física.


## Corrección de empaquetado GLB — 2026-11-26

- [x] Identificar el fallo real de CI: `:app:mergeDebugJavaResource` por tres copias del recurso `net/mgsx/gltf/shaders/default.vs.glsl` procedentes de módulos gdx-gltf.
- [x] Añadir `packaging.resources.pickFirsts` acotado a `net/mgsx/gltf/shaders/**` en `app/build.gradle`.
- [ ] Ejecutar Android CI y confirmar que se resuelve el merge sin ocultar recursos distintos.
- [ ] Verificar que `home.glb`, `away.glb` y `libgdx.so` estén dentro del APK ARM64.
- [ ] Ejecutar pruebas de carga GLB/animaciones y probar en dispositivo.


## Estándar obligatorio de simulador 3D tipo FIFA — 2026-11-26

No aceptar como entrega final una cancha 3D con personajes simples ni un prototipo de visualización. La meta requiere partido 3D jugable completo.

- [x] Confirmar objetivo visual y jugable: simulador de fútbol 3D tipo FIFA/EA SPORTS FC.
- [x] Mantener pipeline de dos modelos humanoides GLB diferenciados por equipo y animaciones de fútbol solicitadas.
- [x] Endurecer el renderer GLB contra coordenadas/velocidades no finitas y delta inválido (commit f338f2815e2fa4af7845bd7ef3e48015bcac481f).
- [ ] Confirmar CI del commit f338f2815e2fa4af7845bd7ef3e48015bcac481f (run 45767673346).
- [ ] Ejecutar workflow de generación Ara y comprobar ambos GLB reales, tamaños, licencias, skins, huesos, nombres/IDs y cantidad de clips.
- [ ] Validar escala, orientación, root motion, rig y retargeting; comprobar que 22 instancias se ven correctamente y que los suplentes/expulsados se ocultan y reactivan.
- [ ] Integrar y probar clips de control/recepción, regate, pase, tiro, cabezazo, entrada, caída, portero y celebración con transición suave.
- [ ] Sincronizar el fotograma de contacto del pie/cabeza con la física y trayectoria del balón.
- [ ] Añadir articulación y locomoción de piernas/brazos reales; no aceptar la animación procedural de cilindros como resultado final.
- [ ] Mejorar césped y materiales, líneas 3D, redes, estadio, público, luces, sombras y atmósfera.
- [ ] Validar cámara broadcast dinámica y replays.
- [ ] Completar IA táctica 11v11, porteros, árbitro, reglas, sustituciones y continuidad del partido.
- [ ] Medir FPS, memoria, temperatura, consumo y estabilidad en Android ARM64 real; preparar niveles gráficos adaptables.
- [ ] Completar pruebas end-to-end de amistoso y aceptación física antes de declarar Amistosos terminado.

Criterio de aceptación final: un partido completo 11v11 en 3D, con controles táctiles, animaciones humanas de fútbol, balón con físicas y presentación de estadio/cámara de simulador moderno. La comparación con FIFA es el estándar de objetivo, no una afirmación de paridad gráfica ya conseguida.

### Integración parcial de acciones futbolísticas — 2026-11-26

- El renderer GLB ya recibía Idle/Walk/Run y las acciones del jugador controlado (tiro, pase y entrada).
- Se amplió la integración en FutbolNovaGame: el receptor ejecuta `soccer_receive` al completar el pase; los jugadores de IA ejecutan `soccer_shoot`, `soccer_pass` y `standing_tackle` en sus eventos; el portero usa `keeper_catch` o `keeper_dive_left/right`, con fallback a `keeper_punch`.
- Los nombres solicitados por el generador se comprobaron en `.github/workflows/generate-football-players.yml`; el selector busca coincidencias parciales sin asumir mayúsculas exactas.
- Esto cierra una primera conexión de eventos, no el sistema completo: aún faltan control/dribbling continuo, cabecera, despeje, saque de banda, caídas, celebraciones, sincronización temporal del contacto pie-balón y pruebas visuales de cada clip.
## Integración ampliada de animaciones de partido — 2026-11-26

- [x] Sustituir el temporizador manual de acciones por `AnimationController.action(..., loopCount=1, ...)`: las acciones GLB son de una sola ejecución y regresan a la locomoción en cola.
- [x] Aplicar transiciones `animate(..., loopCount=-1, ...)` al cambiar entre Idle/Walk/Run.
- [x] Crear `PlayerAnimationPolicy` y pruebas JUnit para bandas de locomoción, tackle de pie/deslizante, control aéreo y acciones de portero.
- [x] Seleccionar slide tackle cuando la velocidad de aproximación alcanza el umbral configurado; de lo contrario, standing tackle.
- [x] Seleccionar chest control/header según altura de balón al ganar una posesión aérea o completar una recepción.
- [x] Conectar throw-in con el jugador de campo activo más cercano del equipo beneficiado.
- [x] Activar celebración de hasta seis jugadores de campo del equipo que marca.
- [x] Fortalecer ambos workflows para rechazar GLB que no contengan Idle/Walk/Run, tiro, pase, recepción, header, chest control, standing/slide tackle, catch/punch/dive de portero, throw-in y celebración.
- [ ] Crear clip específico de dribbling/conducción: no aparece en el catálogo deportivo Ara revisado; usar content pack propio o fuente con licencia compatible.
- [ ] Medir el instante de contacto de cada animación y sincronizar pie-balón, root motion, velocidad y posición; no asumir que la duración aproximada equivale al contacto.
- [ ] Probar todas las transiciones y las 22 instancias en Android ARM64 físico, con FPS, memoria, temperatura y estabilidad de contexto GL.
- [ ] Revisar visualmente escala, orientación, uniformes, calidad de piel/materiales, sombras y lectura de animaciones en cámara broadcast.
### Ajuste de control aéreo basado en límites reales de posesión — 2026-11-26

- `MatchGameplay.acquirePossession` limita actualmente la altura del balón a 1.1 m. Por eso los umbrales de chest control/header se ajustaron a 0.65 m y 0.98 m, con límite superior explícito de 1.1 m.
- No se activa `soccer_jump_header` ni se permite control por encima de la altura de posesión hasta implementar salto/alcance vertical y colisión aérea reales. Así se evita una animación que visualmente toca un balón que la simulación no puede poseer.
### Corrección del mapeo de nombres de animación — 2026-11-26

- El exportador puede guardar IDs (`soccer_shoot`) mientras el juego solicita etiquetas (`Shoot (football)`). Se añadió normalización y alias de prefijos en `PlayerAnimationPolicy.clipNameMatches`; el mapeo ya no depende de coincidencia literal de toda la etiqueta.
- Se quitó `stand` como fallback de Idle para evitar que una animación `Standing tackle` se confunda con una pose de pie.
### Soporte de clip de dribbling cuando exista en el asset — 2026-11-26

- [x] Pasar el índice de poseedor real desde `FutbolNovaGame` a `GltfPlayerSquad.updatePlayer`.
- [x] Si el futbolista tiene posesión y se desplaza, priorizar un clip looping `dribble`/`soccer_dribble`/`ball carry`; si el GLB no lo incluye, volver a locomoción walk/run sin romper el partido.
- [x] Añadir prueba de reconocimiento de nombres de clip de dribbling.
- [ ] Crear e incorporar un clip de conducción original/licenciado y medir contacto pie-balón; el GLB actual no se debe anunciar como si ya incluyera ese clip.
## Cierre de integración de animaciones y CI — 2026-11-26

- [x] Workflow Android separado de la generación manual de GLB; valida assets versionados y evita regenerarlos en cada push.
- [x] CI 444: pruebas unitarias, APK ARM64, `libgdx.so`, `home.glb` y `away.glb` empaquetados; release publicado. Resultado: SUCCESS.
- [x] Renderer recibe el estado de posesión y busca un bucle `dribble`/`soccer_dribble`/`ball carry` si el asset lo ofrece; fallback walk/run cuando no existe.
- [x] Investigar alternativa pública `pronane/c_games/arena-fc`: clip `soccer_dribble`, pero rig de 18 huesos y GLB de 43,6 MB; no importar sin optimización/retarget y revisión de licencia.
- [ ] Añadir validación automática de que cada GLB liga una malla a un skin de 18–25 articulaciones y que los clips requeridos contienen canales reproducibles; workflow preparado, pendiente CI de esta validación.
- [ ] Obtener y retargetear un clip de dribbling real, con marcadores de contacto y ritmo de pie, para el rig Ara activo.
- [ ] Sincronizar contacto pie-balón, desplazamiento raíz y control físico; el balón actualmente se ancla por delante del poseedor y no hay una prueba visual de contacto.
- [ ] Prueba física Android: carga de 22 instancias, escala/orientación, transiciones, cámara, multitáctil, FPS, memoria, temperatura, pérdida/restauración GL y partido completo offline.
- [ ] Completar el checklist P0 del modo Amistosos: prepartido/plantillas, controles, reglas, pausa/descanso/final, historial, segundo amistoso y recuperación en teléfono.

**Límite de aceptación:** CI 444 prueba compilación, tests y empaquetado, no ejecución real en el teléfono. El modo Amistosos y el estándar FIFA-like siguen sin certificarse hasta pasar la prueba física.
### Corrección del límite de joints GLB según la documentación Ara — 2026-11-26

- [x] Corregir la validación inicial que asumía 25 joints totales: el rig `game_lite` tiene 25 huesos de cuerpo, y Ara añade `WeaponR`, `jaw` y ambos ojos (29 joints totales en el GLB actual).
- [x] Ajustar los dos workflows a 25–29 joints; validar mesh+skin y que cada clip requerido tenga canales reproducibles.
- [ ] Confirmar CI posterior a esta corrección y mantener el límite de 29 salvo que la documentación/licencia del rig cambie.