# Auditoría funcional de Fútbol Nova — 2026-11-23

## Alcance y fuente de verdad

- Repositorio activo: `Nova-Nexus-C35/Futbol-Nova`.
- Rama auditada: `rebuild/libgdx-android-2026-11-22`.
- Commit auditado: `d8ef652158ef9190744304dc26d73931506dd75c`.
- Stack vigente: Java 17, libGDX 1.14.2, Android ARM64, minSdk 28, funcionamiento offline-first.
- La fuente Godot/GDScript queda preservada en Nova-Workspace como referencia histórica. No continuar añadiendo funciones al runtime Godot ni reactivar Unity: el desarrollo activo es Java/libGDX.
- `main` del repositorio de implementación permanece intacta.

## Estado comprobado de CI

GitHub Actions run #49 (`45071936387`), commit `d8ef652`, terminó en **success** el 2026-11-22. Pasaron la suite de pruebas Java, `assembleDebug`, la comprobación de que el APK incluye `lib/arm64-v8a/libgdx.so` y la subida del artefacto.

- Artefacto: `futbol-nova-android-arm64-debug`.
- ID: `14773548221`.
- Tamaño del ZIP del artefacto: 3,447,723 bytes.
- SHA-256 del ZIP: `4a115f743c9c76a82a29af64998cfbb1a76e7cae4e670eb420a6c5d71083fdff`.
- Expiración registrada: 2026-12-06.
- Esto valida compilación y pruebas automatizadas, no instalación, rendimiento ni jugabilidad en el teléfono físico Android 9 del propietario. El APK es debug, no release de publicación.

## Lo que ya existe en el runtime Java

1. Escena Android 3D en libGDX, orientación horizontal, campo procedural, líneas, porterías/redes geométricas, cuatro bloques de gradas, iluminación básica y cámara en perspectiva que sigue el balón.
2. Veintidós actores en formación 4-3-3. La IA actualiza posiciones, presiona con el jugador más cercano, mantiene límites de campo y aplica resistencia/fatiga simple.
3. Representación procedural humanoide low-poly con torso, cabeza, pelo, brazos, piernas, botas y oscilación básica de carrera.
4. Física del balón con gravedad, arrastre, Magnus, spin, rebote y fricción del suelo.
5. Lógica inicial de posesión, selección de receptor adelantado, comprobación de línea de pase, filtro de posición adelantada en la selección de pase, pase, disparo del jugador controlado y entrada/tackle básico.
6. La IA puede pasar o despejar; puede intentar entradas con enfriamiento.
7. Detección geométrica de gol y clasificación de salidas para saque inicial, banda, córner y saque de puerta. Los reinicios actuales se resuelven principalmente impulsando el balón después de una espera.
8. Marcador y cronómetro simple.
9. Lectura/resumen de algunos JSON originales mediante `OriginalProjectData`; los catálogos no están todavía conectados a la selección real de equipos y a las 22 identidades del partido.
10. Pruebas unitarias para MatchState, MatchSimulation, MatchGameplay, MatchRules, BallFlightModel y OriginalProjectData.

## Bloqueos para que el amistoso sea un partido completo

### P0 — Control y ciclo real de partido

- El partido se crea con dos IDs fijos (`nova_home` y `nova_away`) y alineaciones genéricas 4-3-3; todavía no se eligen clubes ni se cargan sus plantillas reales.
- El usuario controla siempre el delantero local del índice fijo. No hay cambio de futbolista, elección de jugador inicial ni control de equipo/individual.
- El input táctil actual usa `isTouched/justTouched` con una sola posición global; no implementa joystick multitáctil real, botones independientes con pointer IDs, calibración, reasignación ni asistencia de control.
- `MatchState` solo acumula segundos y goles. Faltan duración configurable, dos períodos, descanso, tiempo añadido, pausa/reanudación visible, final del partido, prórroga y tanda de penaltis.
- No existe una máquina de estados integral que gobierne preparación, saque, juego activo, interrupciones, reinicios, descanso y final. Los reinicios actuales no colocan las alineaciones en posiciones reglamentarias ni implementan la ejecución por el jugador que debe sacar.
- Falta pantalla de pausa, menú de partido, confirmación de salida y reinicio limpio para jugar un segundo amistoso.

### P1 — Motor de fútbol e IA

- La IA no decide entre conducir, regatear, proteger, pasar, centrar, tirar, despejar y temporizar según contexto. El flujo actual de posesión IA tiende a pase adelantado o despeje; el disparo está conectado al jugador humano.
- Faltan apoyos, desmarques, coberturas, basculación coordinada, líneas defensivas, presión táctica, marcaje, transiciones y ajustes de dificultad.
- El portero únicamente sigue lateralmente la posición del balón y adelanta algo su posición. No hay predicción de trayectoria, estirada, blocaje, desvío, salida, achique, rebotes ni recuperación.
- No hay colisiones completas balón-jugador ni colisiones físicas con postes y larguero. La detección de gol/salida es geométrica y no sustituye esas colisiones.
- El fuera de juego solo se filtra al buscar receptor de pase; falta sancionarlo en el partido vivo y contemplar participación activa.
- Falta árbitro y reglamento vivo: faltas, ventaja, tiros libres, penalti por infracción, tarjetas, expulsiones, mano si se define, lesiones y disciplina.
- Faltan centros, pases al espacio configurables, cambios de orientación, entradas deslizantes, despejes controlados, balón dividido y segundas jugadas.
- Las reglas de saque clasifican la salida, pero faltan colocación de jugadores, barrera, elección de ejecutor, animación y reanudación reglamentaria.

### P2 — Datos, resultado y persistencia

- Los JSON originales se leen como resumen; todavía no alimentan el selector, los nombres, dorsales, posiciones, atributos, uniformes ni alineaciones del partido.
- Faltan sustituciones, banquillo, formaciones configurables, tácticas y roles seleccionables.
- No hay registro de eventos del encuentro ni estadísticas por equipo/jugador: posesión, tiros, tiros a puerta, pases, precisión, regates, entradas, faltas y paradas.
- No hay resumen postpartido ni historial de amistosos.
- No hay guardado/carga del partido, preferencias de control ni recuperación ante cierre inesperado.
- La simulación no está desacoplada todavía de la escena/render lo suficiente para probar una partida completa determinista de extremo a extremo.

### P3 — Presentación 3D y audio

- El campo y las gradas son primitivas geométricas; faltan estadio reconocible, túneles, tribunas detalladas, público, banquillos, iluminación avanzada y sombras.
- Los jugadores son low-poly procedurales, sin esqueleto/skin, rostros, texturas detalladas, dorsales visibles ni equipaciones completas.
- Faltan animaciones de carrera con ciclos completos, giro, aceleración, control, pase, tiro, entrada, salto, caída, portero, celebración y transición entre estados.
- El balón necesita colisiones de postes/larguero, redes con deformación o respuesta y mejor contacto con el pie.
- Falta HUD de partido completo, radar, indicadores de jugador/posesión, energía, tarjetas, sustituciones y panel de estadísticas.
- No hay audio de ambiente, público, silbato, golpeo, poste, redes ni música opcional.

### P4 — Validación Android y publicación

- Instalar y jugar la APK en el teléfono Android 9 ARM64 del propietario sigue pendiente.
- Faltan pruebas manuales de multitáctil, orientación, pausa al cambiar de aplicación, llamadas/notificaciones, memoria, FPS sostenidos, temperatura y consumo de batería.
- La CI prueba clases Java y empaquetado, pero no demuestra una sesión 11v11 completa ni aceptación visual/táctil en dispositivo.
- La entrega actual es APK debug; falta pipeline de release, firma protegida, versionado, comprobación de integridad y notas de publicación.
- Deben revisarse derechos de marcas, escudos, nombres, equipaciones, imágenes, jugadores reales, música y cada asset antes de publicar. Los perfiles ficticios y recursos originales deben permanecer claramente identificados.

## Orden recomendado de implementación

1. **Partido mínimo completo:** máquina de estados y reloj reglamentario; saque inicial y reinicios; control multitáctil real; cambio de jugador; posesión estable; pase, tiro, conducción y tackles.
2. **Porteros y colisiones:** colisión jugador-balón, postes/larguero, predicción de trayectoria, blocaje/desvío, rebotes y recuperación.
3. **Reglamento:** árbitro, faltas, fuera de juego sancionado, tiros libres, penaltis, tarjetas, descanso, tiempo añadido y final.
4. **IA táctica:** roles, apoyos, desmarques, cobertura, presión, conducción, pase/tiro según probabilidad y dificultad.
5. **Clubes y plantillas:** cargar catálogo JSON, elegir local/visitante, alinear titulares y suplentes, dorsales y equipaciones.
6. **Estadísticas y persistencia:** eventos verificables, estadísticas por jugador/equipo, resumen final, historial y guardados versionados.
7. **Calidad 3D/audio:** personajes riggeados, animaciones, estadio, cámara/HUD y audio, siempre con presupuesto de rendimiento móvil.
8. **Aceptación en Android 9:** sesión larga real, pruebas táctiles, rendimiento térmico/memoria, segundo partido y recuperación de estado.
9. **Después del amistoso:** portar Manager, Leyenda, Entrenador, ligas, torneos, economía, cantera y módulos opcionales por capas; no antes de estabilizar el motor de partido.

## Investigación de repositorios públicos

La investigación se usa para comparar algoritmos, arquitectura y pruebas. No se copió código ni assets en esta auditoría.

- **awest813/League-Soccer** — https://github.com/awest813/League-Soccer. README y LICENSE declaran Apache-2.0; motor C++17 con 22 jugadores, IA táctica, árbitro, faltas, set pieces, estadísticas, replay, guardados y pruebas de integración. Es la referencia más completa para diseñar contratos, eventos, reglas y casos de prueba. No es una dependencia directa: usa CMake, SDL, OpenGL, OpenAL, Boost y SQLite para escritorio, no Java/libGDX Android. Revisar avisos y procedencia de componentes heredados antes de portar código.
- **blurz17/blurz-native-football-** — https://github.com/blurz17/blurz-native-football-. Proyecto Android nativo C++/SDL/JNI/OpenGL ES con joystick 360°, HUD táctil, cambio de jugador y cámara móvil; resulta útil para estudiar distribución de controles y arquitectura móvil. No se confirmó una licencia reutilizable en esta revisión: no copiar código ni assets.
- **aliyavuzz/Godotball** — https://github.com/aliyavuzz/Godotball. El README describe física de balón, 22 jugadores, árbitro y AI sobre Godot 4.4.1/GDExtension C++; aunque el README declara Apache-2.0, la cadena de procedencia y el archivo de licencia del repositorio deben verificarse antes de cualquier reutilización. No es compatible directamente con el runtime Java/libGDX.
- **namelesssyf/soccer-box-game** — https://github.com/namelesssyf/soccer-box-game. Godot 4.7.1, física, portero IA, telemetría y servidor autoritativo; útil como referencia de telemetría/estados online, pero su diseño arcade y su stack no son la base de Fútbol Nova. Licencia no verificada; no copiar.
- **Adem-maker600/football-3d-game** — https://github.com/Adem-maker600/football-3d-game. Pequeña muestra Java/LWJGL de 3 contra 3 con cámara y física simple; solo sirve como ejemplo didáctico, no como motor 11v11. Licencia y mantenimiento no verificados; no copiar.
- **ssenegas/kickoff** — https://github.com/ssenegas/kickoff. Proyecto Java/libGDX de estilo Kick Off 2D; puede orientar input y separación de lógica, pero no satisface la meta 3D y su antigüedad exige revisar dependencias/licencia antes de usar algo.

### Decisión técnica de reutilización

Mantener Java 17 + libGDX y el motor de simulación propio. Primero portar conceptos y casos de prueba: máquina de estados, eventos, árbitro, roles de IA, físicas, replay y controles táctiles. No añadir otro motor ni una extensión C++/GDExtension por defecto. Cualquier fragmento externo futuro exige lectura de la licencia real, cadena de origen, dependencias, atribución, compatibilidad ARM64 y prueba de rendimiento antes de integrarse.

## Criterio de finalización del amistoso

No declarar el amistoso funcional hasta que una prueba reproducible y una sesión en el Android objetivo permitan: seleccionar dos clubes con plantillas válidas; iniciar 11v11; controlar y cambiar futbolistas; pasar, conducir, tirar y defender; ver al portero intervenir; ejecutar faltas y reinicios; completar dos períodos y, cuando corresponda, prórroga/penaltis; generar marcador, eventos, estadísticas y resumen; pausar/reanudar/salir; y jugar un segundo partido sin residuos del anterior.

**Estado final de esta auditoría:** compilación ARM64 y suite unitaria aprobadas en CI #49; prototipo 3D ejecutable a nivel de build; amistoso completo, prueba física Android y sistemas de producción todavía pendientes.


## Registro de implementación posterior a la auditoría

### Cambios realizados en la rama de desarrollo

- Se añadió `MatchClock.java`: máquina de estados pura para prepartido, primera parte, descanso, segunda parte y final; duración configurable, tiempo añadido por período, pausa/reanudación y reinicio para una nueva sesión.
- Se añadió `MatchClockTest.java`: pruebas para inicio explícito, descanso, segunda parte, final, tiempo añadido, pausa, deltas inválidos, reinicio y duración inválida.
- Se sustituyó la lectura de un único toque global por seguimiento de hasta 20 punteros Android independientes en `FutbolNovaGame.updateTouchControls`. La intención es permitir mover con el dedo izquierdo mientras se toca disparo/pase/entrada con otro dedo.
- Se corrigió el signo del eje Z del joystick para conservar la dirección de avance previa.

### Verificación pendiente

- Ambos archivos del reloj y el archivo del runtime fueron releídos desde GitHub después de escribirlos.
- La ejecución CI más reciente observada durante esta implementación es la run #54 (`45250324823`), commit `c9bf647c`, que estaba **in_progress** al registrar esta actualización. Las runs #51–#53 fueron canceladas por pushes posteriores; no se consideran validación.
- El reloj es todavía un componente probado de forma aislada: falta conectarlo a la interfaz y al ciclo real del partido con una política de duración/velocidad elegida, descanso visible y botón de continuar. No declarar terminados los períodos del juego por la mera existencia de esta clase.
- El control multitáctil requiere validación manual en un Android real; la compilación CI solo valida compatibilidad de código y empaquetado.
- El cambio de control no implementa todavía selección/cambio de futbolista, controles configurables ni joystick dibujado en pantalla.

### Siguiente bloque de trabajo

1. Conectar MatchClock al ciclo de partido sin congelar el juego en el descanso: interfaz de pausa/continuar y final, duración de partido configurable.
2. Hacer que el jugador visual y el estado simulado usen una única fuente de posición para evitar desincronización del futbolista controlado.
3. Implementar selección del jugador controlado y actualización de posesión/control sin depender del índice fijo del delantero.
4. Añadir pruebas de integración del ciclo de partido y controles/reglas; después continuar con porteros, colisiones, árbitro y equipos reales.

### Implementación: integración del reloj y flujo de partido (2026-11-23)

- Integrada `MatchClock` en `FutbolNovaGame`: el partido comienza explícitamente desde PRE_MATCH, cambia a HALF_TIME al terminar el primer período, requiere una acción explícita para iniciar el segundo y pasa a FULL_TIME al finalizarlo.
- La simulación del balón, jugadores y decisiones de juego queda detenida cuando el partido está antes del saque inicial, en descanso, pausado o finalizado; el render 3D continúa activo.
- Controles táctiles de la franja superior: izquierda para iniciar/continuar desde descanso; derecha para pausar/reanudar. El HUD comunica las fases y acciones disponibles.
- Escala arcade declarada: 30 segundos de partido por segundo real. Cada parte reglamentaria de 45 minutos dura aproximadamente 90 segundos reales; partido completo aproximadamente 3 minutos, sin contar pausas. Es una constante de configuración, no tiempo real reglamentario.
- Verificación de escritura: `FutbolNovaGame.java` releído desde GitHub después de la escritura; blob SHA `ac27615bce6eec262490a31324ad7ca898ff7dad`.
- CI de la tanda anterior: run #55, job `android`, conclusión `success`. Esta ejecución no valida todavía la nueva integración; el nuevo run debe completarse antes de marcarla como compilada.
- Pendiente de prueba física: comprobar detección de toques en la franja superior, pause/resume, descanso y final en Android 9 ARM64, además de multitáctil simultáneo. No afirmar aceptación táctil hasta probar en dispositivo.


### Implementación: cambio manual de futbolista (2026-11-23)

- `MatchSimulation` ya no depende de un índice fijo para controlar al jugador: permite recorrer cíclicamente los diez futbolistas de campo del equipo local y mantiene al portero bajo IA.
- El cambio actualiza la posición de control táctil al futbolista seleccionado; la simulación excluye dinámicamente al jugador seleccionado de la IA.
- Se conectaron las acciones de tiro, pase y entrada al índice dinámico del jugador controlado.
- El sector superior central del HUD cambia el futbolista; la zona superior izquierda conserva inicio/continuación y la derecha pausa/reanudación.
- Se añadió prueba unitaria del ciclo y de la exclusión del portero. Pendiente comprobar usabilidad y multitáctil en Android físico.
- Corrección posterior a la revisión: tiro, pase y entrada usan ahora el índice dinámico seleccionado también para identificar al pasador y asignar la posesión tras una entrada exitosa. Se releyó `FutbolNovaGame.java` desde GitHub y se confirmó que la única referencia al índice inicial fijo restante se utiliza exclusivamente para colocar el jugador seleccionado inicial al crear los modelos.


### Implementación: plantillas canónicas y nombres de equipos (2026-11-23)

- Se conectó el lector de datos a `cuba_players.json`: extrae ID, nombre, posición, valoración general y atributos de los futbolistas sin sustituirlos por identidades inventadas.
- `MatchSimulation` puede formar dos XI iniciales 4-3-3 desde las plantillas reales incluidas, seleccionando jugadores por posición preferida y usando valoración como desempate; si una posición no está disponible, selecciona el mejor jugador aún no usado.
- La escena inicia con las dos primeras plantillas que existen realmente en el catálogo (Tercera División), muestra nombres de clubes y del futbolista controlado. El catálogo disponible contiene 20 grupos de plantilla de 22 jugadores; las ligas de Primera y Segunda tienen clubes en el catálogo de competiciones, pero todavía no tienen plantillas correspondientes en `cuba_players.json`, por lo que no se simulan como si tuvieran jugadores cargados.
- Los dorsales 1–11 de esta alineación son números visuales asignados por orden de campo, ya que el JSON de plantilla no incluye dorsales individuales.
- Se añadieron pruebas para el parseo de identidades/atributos y para crear el partido desde dos plantillas.
- Se corrigió además la posición inicial del control para usar el índice dinámico seleccionado y no el delantero fijo original.
- Pendiente: selector de clubes/plantillas en interfaz, y completar las plantillas ausentes de Primera/Segunda sin fabricar datos. Esta implementación no acredita todavía una prueba en dispositivo.

- En el paso al segundo tiempo se invierten los extremos, se actualiza la posición del futbolista seleccionado, se repone el balón en el centro y el saque inicial se orienta según el nuevo sentido de ataque. Las reglas de gol y las velocidades de tiro/despeje reciben la orientación actual.


### Implementación: selección de equipos y nuevo amistoso (2026-11-23)

- En la pantalla previa al saque inicial, la franja superior izquierda recorre los equipos con plantilla disponible como local; la zona central recorre el visitante; la derecha inicia el encuentro. No permite seleccionar el mismo club en ambos lados.
- El marcador muestra los nombres de los dos clubes. Durante el partido se muestra el nombre del jugador activo.
- Tras el pitido final, la zona superior izquierda reinicia marcador, reloj, balón, posesión, enfriamientos y alineaciones para un nuevo amistoso.
- Añadido reset explícito de MatchState y prueba unitaria. El catálogo seleccionable se limita a los 20 grupos de plantilla disponibles en el JSON actual.

### Implementación: selector de equipos y corrección de disparo tras el descanso (2026-11-23)

- Se incorporó `TeamSelection` como modelo puro para elegir local y visitante entre plantillas realmente jugables. Filtra plantillas sin 11 jugadores, IDs vacíos y clubes duplicados; nunca permite enfrentar un club consigo mismo.
- El selector está conectado a la pantalla previa: toque superior izquierdo cambia el local, centro cambia el visitante y derecha inicia el encuentro. Al cambiar equipos se reconstruyen las alineaciones y se limpia el estado temporal del balón/posesión para no arrastrar el partido anterior.
- Se añadieron tres pruebas unitarias del selector: selección inicial distinta, ciclos sin duplicar rival y rechazo de una sola plantilla.
- Se corrigió la posición inicial del disparo para que el balón salga desde el lado correcto del futbolista según la dirección de ataque actual, incluido el segundo tiempo.
- Archivos de selector, runtime, prueba y esta auditoría fueron releídos desde GitHub después de sus escrituras. La ejecución #82 fue cancelada por nuevos commits; no cuenta como validación. Debe completarse la ejecución CI correspondiente al último commit antes de declarar aprobada esta tanda.
- Limitaciones que permanecen: las plantillas disponibles no cubren todos los clubes de Primera y Segunda; los dorsales reales, banquillo y suplentes todavía no están modelados; las equipaciones continúan siendo colores genéricos; y no se ha probado en el teléfono físico.

### Implementación: primera intervención funcional del portero y orientación tras el descanso (2026-11-23)

- La IA de movimiento ahora calcula el avance del jugador más cercano al balón usando la dirección real de ataque de cada equipo; antes estaba fija a los sentidos del primer tiempo.
- El punto de contacto del balón en los pases del usuario y de la IA ahora queda delante del poseedor según el sentido de ataque actual, también tras cambiar de campo.
- Se añadió una comprobación geométrica determinista para que el portero solo intente intervenir si el balón va hacia su propia portería, está a distancia alcanzable y no supera la altura configurada.
- La escena conecta esa comprobación con dos respuestas: blocaje/posesión si la velocidad es moderada, o despeje/parada con rebote si el disparo llega rápido. El balón se devuelve hacia el terreno de juego y se registra el último toque del equipo del portero.
- Se añadió una prueba unitaria de alcance del portero (dirección, altura y distancia). La intervención es todavía una primera aproximación geométrica: no incluye animación de estirada, manos, predicción avanzada ni colisión física del cuerpo del portero.
- Los archivos `MatchSimulation.java`, `MatchGameplay.java`, `FutbolNovaGame.java`, `MatchGameplayTest.java` y esta auditoría fueron releídos desde GitHub después de las escrituras.
- CI de esta tanda aún no validada: los commits consecutivos cancelaron las ejecuciones intermedias. La última ejecución asociada al siguiente commit debe finalizar en verde antes de declarar compilación aprobada.

### Implementación: estadísticas básicas y registro de eventos (2026-11-23)

- Se añadió `MatchStatistics`, independiente de la escena, con contadores locales/visitantes para tiros, pases intentados, entradas, faltas, paradas y goles.
- Mantiene un registro de los últimos 256 eventos para evitar crecimiento de memoria sin límite; los contadores del partido siguen siendo acumulativos.
- Se conectaron al juego los eventos de tiro, pase intentado, entrada exitosa, falta detectada, parada del portero y gol.
- El HUD muestra contadores agregados por equipo. El estado se reinicia al comenzar un amistoso nuevo, no en el descanso.
- Se añadieron pruebas para separación por equipo, eventos, límite de historial y reset.
- Estas estadísticas son una primera instrumentación: aún no calculan posesión porcentual, precisión de pase, tiros a puerta, regates ni estadísticas individuales; tampoco hay pantalla de resumen postpartido ni persistencia histórica.


### Implementación: tiros libres y penaltis tras entradas sancionadas (2026-11-23)

- Las infracciones detectadas por la entrada del jugador o por la presión de la IA ya no dejan el balón en juego sin sanción: generan un reinicio a favor del equipo perjudicado.
- Se añadió clasificación geométrica de falta directa o penalti según la zona de la infracción, el área de 16,5 m y la anchura reglamentaria aproximada del área.
- Los tiros libres y penaltis ejecutan una reanudación automática orientada hacia la portería rival; los penaltis colocan el balón en el punto de 11 m.
- Se añadieron pruebas para faltas dentro/fuera de ambas áreas y para la inversión de campos.
- Limitación: aún no hay árbitro visible, barrera, elección del lanzador, animación, tarjetas ni ventaja; la ejecución automática es provisional y requiere ajuste de jugabilidad.


### Implementación: saque inicial funcional y decisiones ofensivas de la IA (2026-11-23)

- El primer toque de iniciar partido ahora prepara el balón en el centro y ejecuta el saque inicial automático a favor del equipo local; antes el balón podía permanecer inmóvil en el círculo central.
- La IA ya intenta disparar cuando un poseedor llega a la zona ofensiva final con un ángulo central razonable. Los tiros de la CPU se registran en las estadísticas.
- Los pases de la IA y las entradas exitosas de los defensores de CPU también alimentan los contadores del equipo correspondiente.
- Se añadió una prueba unitaria para las condiciones de tiro de la IA.
- Pendiente: elección manual del ejecutor de saques, centros, regates, paredes y variación táctica; el comportamiento de tiro CPU es una regla geométrica inicial, no una IA avanzada.

### Estado consolidado de implementación y validación (2026-11-23)

- Ya están integrados en la rama: ciclo de reloj con dos tiempos y descanso; selector de equipos con plantillas cargadas; identidades/nombres de jugadores del catálogo; cambio de futbolista; controles táctiles con punteros independientes; intervención geométrica inicial del portero; registro básico de estadísticas/eventos; reinicios de falta directa/penalti; saque inicial automático; y disparos de CPU en zona ofensiva.
- La ejecución #102 (commit anterior a la última tanda de saque inicial/tiros CPU) fue cancelada por una actualización posterior; no se considera prueba aprobada.
- El commit actual de código es `0f3117849c8a124d65868e236ed03567f21ececb`. Debe ejecutarse y completarse CI para este estado antes de declarar validada la compilación.
- Sigue sin demostrarse una partida completa en el teléfono físico. La CI no puede certificar la experiencia multitáctil, el rendimiento térmico, el equilibrio del juego ni la calidad visual.
- Pendientes de implementación: árbitro/ventaja/tarjetas; fuera de juego sancionado durante juego vivo; colisiones jugador-balón y postes; portero con animaciones y predicción; centros/regates/conducción y decisiones tácticas amplias; banquillo/sustituciones; estadísticas individuales y resumen postpartido; guardados/historial; gráficos/audio de producción; plantillas completas para todas las divisiones; release firmado y aceptación física Android.

### Implementación: colisiones de balón con jugadores, postes y larguero (2026-11-23)

- Se añadió `BallCollisionModel` para responder al contacto rápido del balón con los cuerpos de los futbolistas y para colisiones geométricas con los dos postes y el larguero.
- Los contactos de baja velocidad no se desvían por el modelo corporal para permitir que el sistema de posesión capture el balón; los contactos rápidos se reflejan con amortiguación.
- Las colisiones del marco corrigen la posición del balón y aplican rebote antes de evaluar si cruzó completamente la línea de gol.
- Se añadieron pruebas para poste, larguero y contacto jugador-balón, incluyendo la conservación del control de balones lentos.
- Limitación: es una aproximación de cuerpos esféricos y segmentos; no equivale todavía a un sistema físico completo con articulaciones, redes deformables ni animaciones de impacto.
- Archivos de colisión, pruebas y runtime fueron releídos desde GitHub tras la escritura. La validación CI del último commit aún está pendiente.


### Implementación: sanción de fuera de juego en recepción (2026-11-23)

- La selección de pase ahora conserva una decisión explícita del receptor: prefiere opciones en posición legal y, si no existe ninguna opción legal viable, puede elegir la mejor opción adelantada y marcarla como fuera de juego.
- Si ese jugador participa recibiendo el pase, el juego detiene el balón y concede un tiro libre indirecto automático al rival desde la posición de la infracción.
- Se registra el fuera de juego por equipo y se añade al historial acotado de eventos. Si el balón lo intercepta otro jugador o sale del campo, se cancela la infracción pendiente.
- Se añadieron pruebas de decisión de pase y del contador de fuera de juego. La regla sigue siendo una aproximación: no cubre todas las situaciones de interferencia, rebotes ni excepciones complejas de reglamento.

- Se limpian las infracciones de fuera de juego pendientes al iniciar/reiniciar períodos y cuando interviene un portero; el HUD muestra también el contador de fueras de juego. La prueba de fuera de juego cubre ambos sentidos de ataque.
