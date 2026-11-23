# Objetivo visual y de jugabilidad — Fútbol Nova 3D moderno

## Decisión confirmada del propietario (2026-11-23)

Fútbol Nova debe desarrollarse como un videojuego de fútbol tridimensional (3D), con una presentación, controles y experiencia de simulación que aspiren al nivel de los juegos FIFA modernos. No se acepta como objetivo final un juego 2D, una vista isométrica simplificada ni una escena 3D de figuras geométricas sin animación y detalle.

La referencia FIFA describe el nivel y la dirección de calidad deseados; no autoriza copiar código, modelos, marcas, escudos, equipaciones, música, interfaces ni otros recursos protegidos de EA/FIFA. Todos los recursos externos deberán tener licencia comprobada y atribución cuando corresponda. Se priorizarán modelos, animaciones y recursos originales o con licencia compatible.

## Criterios visuales y de cámara

1. Campo 3D completo, césped con variación visual, líneas integradas, porterías con redes que reaccionen al balón, túneles, banquillos, graderíos por sectores, público y entorno de estadio.
2. Futbolistas 3D con anatomía y proporciones coherentes, mallas de calidad progresiva, equipaciones detalladas, dorsales, botas, caras/cabello diversos y materiales PBR o aproximación compatible con el dispositivo.
3. Esqueleto articulado y animaciones conectadas a la simulación: correr, acelerar, frenar, girar, caminar, pasar, disparar, cabecear, saltar, barrerse, caer, levantarse, celebrar y atajar.
4. Cámara de retransmisión 3D dinámica que mantenga legibilidad del campo y balón, encuadre jugadas, goles y repeticiones sin mareos ni saltos bruscos. Añadir opciones de cámara si el rendimiento lo permite.
5. Iluminación coherente, sombras proyectadas, materiales y efectos de ambiente; optimizar para Android ARM64 y ofrecer escalado de calidad/FPS.
6. Balón 3D con contacto pie-balón creíble, rotación visible, rebotes, colisiones con jugadores y marco, y red con respuesta visual.
7. HUD integrado y legible: marcador, reloj, radar/minimapa, indicador del jugador, stamina, botones táctiles configurables y menús de partido con acabado moderno.
8. Audio de ambiente y estadio, público dinámico, árbitro, golpeo del balón, postes/red y música opcional, siempre con recursos licenciados o originales.

## Criterios de jugabilidad

- Simulación 11 contra 11 con roles y tácticas, desmarques, coberturas, presión coordinada, marcaje, transición ataque/defensa y decisiones contextuales.
- Control táctil multitáctil: joystick de movimiento y botones separados de pase, tiro, centro, sprint, cambio de jugador y entrada; sensibilidad y disposición configurables.
- Animaciones y movimiento deben estar sincronizados con el estado real del jugador y el balón; evitar que el jugador patine o que el balón se despegue visualmente del pie.
- Reglas completas y reinicios ejecutados por jugadores, con árbitro, faltas, ventaja, tarjetas, fueras de juego, penaltis, sustituciones y final de partido.
- La calidad visual no debe ocultar la legibilidad ni reducir la respuesta de controles.

## Plan técnico por etapas

### Etapa A — Base 3D sólida
- Mantener Java 17 + libGDX como runtime Android actual.
- Separar simulación, renderizado, animación, cámara, UI y audio en sistemas independientes.
- Definir presupuesto de rendimiento para Android 9 ARM64: FPS objetivo configurable, memoria, tamaño de recursos y tiempos de carga.
- Implementar cámara de retransmisión con suavizado, seguimiento de jugada y encuadre de zonas relevantes.
- Reemplazar progresivamente geometría procedural low-poly por modelos 3D articulados originales/licenciados; no eliminar el fallback procedural hasta validar el nuevo contenido.

### Etapa B — Futbolistas y animación
- Importación de modelos riggeados y animaciones compatibles con libGDX.
- Máquina de estados de animación conectada a velocidad, orientación, posesión y acciones.
- Contacto pie-balón y sincronización de animación con física.
- Variación de caras, cuerpos, equipaciones, dorsales y porteros.

### Etapa C — Estadio, materiales y presentación
- Césped y líneas de calidad, redes dinámicas, graderíos detallados, público, banquillos, túneles, iluminación y sombras.
- HUD moderno, radar, repeticiones de gol, transiciones, presentación previa y resumen postpartido.
- Audio ambiental y efectos con derechos verificados.

### Etapa D — Simulación de nivel moderno
- IA táctica, regates, protección, centros, desmarques, coberturas, porteros avanzados y árbitro.
- Reglas y ciclo completo de partido, plantillas, formaciones, cambios y estadísticas.
- Dificultad configurable y comportamiento consistente para los dos equipos.

### Etapa E — Optimización y aceptación
- Perfiles de calidad para dispositivos Android ARM64.
- Pruebas físicas en el teléfono objetivo: FPS, calor, batería, memoria, multitáctil, pausa/reanudación, orientación y estabilidad.
- CI automatizada para pruebas y APK; pruebas físicas no se marcarán como aprobadas por CI.
- No declarar terminado el juego ni equipararlo visualmente a FIFA moderno hasta cumplir los criterios y realizar una evaluación visual/jugable en dispositivo.

## Estado real observado al registrar este objetivo

- Existe una escena 3D inicial en libGDX: campo procedural, gradas geométricas, porterías, modelos humanoides low-poly, balón esférico, iluminación básica y cámara en perspectiva que sigue el balón.
- No existen todavía modelos de futbolistas de calidad moderna, esqueletos/animaciones completas, estadio detallado, césped/materiales avanzados, público, sombras de producción, audio, repeticiones ni cámara de retransmisión completa.
- El código y la auditoría deben seguir distinguiendo prototipo 3D existente de la meta visual FIFA-moderna confirmada.
- Restricción técnica actual: desarrollo Java 17/libGDX 1.14.2, Android ARM64 y minSdk 28. Cualquier migración de motor requiere decisión explícita del propietario; no se cambiará silenciosamente el runtime.


## Progreso de implementación — cámara de retransmisión 3D (2026-11-23)

- Se añadió BroadcastCamera, una cámara elevada en tres cuartos desde la banda, con seguimiento suavizado del balón y encuadre de la jugada.
- La altura aumenta moderadamente durante transiciones rápidas; el punto de enfoque se limita a las dimensiones reglamentarias del campo y se protege frente a deltas/velocidades no finitos.
- FutbolNovaGame usa ahora este controlador en el render del partido, sustituyendo el seguimiento anterior más rígido.
- Se añadieron pruebas para perspectiva elevada, seguimiento y límites de campo, y protección frente a delta inválido.
- Esta es una primera cámara de retransmisión funcional, no un sistema completo de cámaras FIFA: faltan cambios de plano por contexto, seguimiento de posesión, zoom dinámico horizontal, repeticiones y opciones configurables.


## Progreso de implementación — césped y graderíos por niveles (2026-11-23)

- El campo procedural ahora usa franjas alternas de césped con dos materiales verdes, conservando las marcas reglamentarias y la geometría de juego existente.
- Los cuatro bloques sólidos de graderío se sustituyeron por seis niveles escalonados alrededor del campo, con dos tonos de asientos para dar profundidad a la silueta del estadio.
- Se reutilizan mallas compartidas e instancias para limitar el coste de memoria y llamadas de creación de modelos.
- Siguen pendientes asientos individuales, público, barandillas, cubierta, túneles, banquillos, iluminación de estadio y sombras avanzadas. El estadio continúa siendo procedural de prototipo.


## Progreso de implementación — detalles de campo y redes (2026-11-23)

- Se añadieron las áreas de meta (seis yardas), puntos central y de penalti, y arcos de esquina para completar la lectura reglamentaria del terreno.
- Las porterías ahora muestran una cuadrícula de red en el fondo, techo y laterales, no solo el marco.
- Las redes son todavía líneas estáticas; falta deformación/animación de red al recibir un gol y colisión física precisa con postes y travesaño.
- CI #142 falló por una secuencia de escape literal en una declaración de FutbolNovaGame.java. Se corrigió en un commit posterior y se está solicitando una validación nueva antes de dar por compilado el lote.
