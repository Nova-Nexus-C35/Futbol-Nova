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
