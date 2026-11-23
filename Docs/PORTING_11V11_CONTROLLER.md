# Portabilidad del controlador 11v11 — primera fase

Fecha: 2026-11-22.

## Fuente revisada

- Repositorio original: `Nova-Nexus-C35/Nova-Workspace`, rama `main`.
- Controlador: `Projects/Games/futbol-nova/scripts/gameplay/match_11v11_controller.gd`.
- SHA fuente: `0b8a44c755ab6bb81a0e91fa24472fc5b6274a43`.
- Formación fuente: 4-3-3, 11 jugadores por equipo, portero incluido.
- Decisiones tácticas limitadas a intervalos de 0,20 s; movimiento interpolado por frame; separación cinemática para móvil.
- El jugador controlado originalmente ocupa el puesto ST (índice 9 de la alineación local).
- También se revisaron `ball_flight_model.gd`, `match_rules.gd`, `match_clock_state_machine.gd` y `goalkeeper_reaction_model.gd`; esos subsistemas aún no están completamente portados.

## Implementación Java incorporada

- `MatchSimulation.java` crea 22 futbolistas con IDs únicos, roles y posiciones de la formación 4-3-3 original.
- Mantiene 11 jugadores locales y 11 visitantes, con la orientación opuesta del campo.
- El jugador local ST es el controlado por el usuario.
- La IA reposiciona a los jugadores según el desplazamiento del balón, el rol y la cercanía; el jugador más cercano de cada equipo puede presionar.
- Los porteros siguen lateralmente el balón y adelantan su posición de forma limitada cuando el balón entra en el tercio defensivo.
- Movimiento con velocidad diferenciada para porteros, fatiga/recuperación ligera y separación cinemática.
- Límites de movimiento dentro del terreno.
- Se portaron las condiciones base de control del balón y evaluación geométrica de entradas/tackles.
- `FutbolNovaGame` instancia la alineación desde `MatchSimulation` y actualiza las 22 posiciones cada frame.
- La vista dejó de usar bloques: `PlayerVisual` dibuja humanoides low-poly con partes articuladas y animación procedural básica.
- `MatchGameplay` conecta adquisición de posesión, receptores adelantados, líneas de pase, filtro de fuera de juego, pases, disparos y entradas.
- La IA puede pasar/despejar y realizar entradas con enfriamiento; el juego ejecuta saques iniciales, saques de banda, córners y saques de puerta.
- Se añadieron porterías, redes geométricas, gradas perimetrales y cámara de seguimiento del balón.
- La velocidad máxima del futbolista se mantiene separada de la velocidad instantánea usada para la animación.

## Pruebas nuevas

`MatchSimulationTest` comprueba 22 jugadores/11 por lado, posiciones de porteros, rechazo de equipos inválidos, presión del jugador cercano, límites del campo, deltas no válidos y umbrales de control/tackle.

## Alcance y limitaciones

Esta portabilidad continúa siendo parcial. Aunque ya existen posesión, pases, tiros, tackles básicos, IA de pase/tackle y reinicios principales, aún faltan árbitro y faltas completas, fuera de juego sancionado en vivo, cambios de jugador, sustituciones, reloj por períodos y reacciones/animaciones especializadas del portero. Los modelos low-poly son una base procedural, no personajes finales con esqueletos, texturas, rostros y clips de animación.

El modelo aerodinámico del balón ya está conectado, pero faltan colisiones con postes/larguero y validación de física más completa. El siguiente bloque debe mejorar el pipeline de modelos/animaciones, cerrar reglas de partido, optimizar el estadio/cámara y validar rendimiento y controles en Android 9.
