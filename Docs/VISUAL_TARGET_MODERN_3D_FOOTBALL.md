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


## Progreso de implementación — zoom contextual de cámara (2026-11-23)

- BroadcastCamera ahora amplía suavemente el campo de visión durante transiciones rápidas del balón: parte de 49 grados y aumenta progresivamente hasta un máximo de 57 grados.
- El zoom se suaviza con la misma respuesta temporal del seguimiento para evitar cambios bruscos; velocidad y delta se limitan y se validan antes de usarse.
- Se añadieron pruebas unitarias para comprobar que el campo de visión se amplía progresivamente y respeta su límite.
- La cámara sigue siendo una primera versión de retransmisión: todavía faltan selección de plano según contexto, seguimiento de posesión, repetición y ajustes de usuario.


## Progreso de implementación — ciclo de carrera articulado (2026-11-23)

- Se añadió PlayerAnimation, una función determinista que calcula balanceo alterno de extremidades y oscilación vertical suave del torso según velocidad y fase individual.
- PlayerVisual ahora articula brazos y piernas alrededor de pivotes aproximados de hombros y caderas, en vez de girar cada pieza sobre su propio centro; los pies acompañan el ciclo y el torso/cabeza absorben un pequeño movimiento vertical.
- Se añadieron pruebas unitarias para reposo, alternancia y límites de amplitud, además de entradas no finitas.
- Alcance real: es una animación procedural de locomoción para las mallas low-poly actuales. No sustituye modelos esqueléticos, clips de animación profesionales ni acciones específicas (pase, tiro, cabezazo, barrida, caída, celebración o atajada); esas siguen en la hoja de ruta.


## Progreso de implementación — articulación procedural de futbolistas (2026-11-23)

- PlayerVisual ahora calcula una oscilación de carrera y un pequeño movimiento vertical del torso/cabeza según la velocidad, mediante PlayerAnimation.
- Brazos y piernas se colocan usando pivotes aproximados de hombros y caderas, en lugar de rotar cada pieza únicamente sobre su propio centro; botas acompañan la fase de pierna.
- PlayerAnimation mantiene funciones deterministas y limita la amplitud de zancada; entradas no finitas vuelven a una pose estable.
- Se añadieron pruebas de reposo, alternancia positiva/negativa de zancada, amplitud máxima y entradas inválidas.
- Limitación: siguen siendo mallas low-poly separadas y animación procedural básica. No hay esqueleto/skin, clips capturados, cinemática inversa, contacto pie-balón sincronizado ni animaciones de pase/tiro/caída/portero. No se considera animación de calidad moderna terminada.



## Confirmación explícita del propietario — realismo visual tipo FIFA (2026-11-25)

El propietario confirmó que desea un resultado **totalmente realista, comparable visualmente con FIFA/EA SPORTS FC**, y rechazó expresamente la apariencia mostrada en la captura del prototipo Android del 2026-11-25.

### La captura actual no es el objetivo aceptable

La imagen revisada muestra un estadio oscuro de geometría simple, jugadores low-poly muy pequeños, cámara excesivamente alejada y textos del HUD superpuestos. Sirve únicamente como evidencia del estado técnico actual; no debe presentarse como diseño final ni como nivel visual aprobado.

### Estándar de calidad que debe perseguirse

- Futbolistas humanos 3D de alta calidad, con anatomía, rostros, cabello, manos, botas y equipaciones detalladas; variedad corporal y facial.
- Modelos articulados con esqueleto y animaciones de calidad deportiva: locomoción, aceleración, frenado, cambios de dirección, regates, pases, tiros, cabezazos, entradas, caídas, levantarse, atajadas y celebraciones.
- Animación y contacto físico sincronizados con la simulación: pies apoyados, balón unido al gesto de golpeo, colisiones coherentes y ausencia de deslizamiento visual.
- Campo con césped de apariencia natural, textura y variación, iluminación de día/noche, sombras, porterías y redes reactivas.
- Estadio reconocible y detallado: gradas, público, banquillos, túneles, vallas LED, iluminación y ambiente de partido.
- Cámara de retransmisión deportiva que encuadre la jugada y permita reconocer jugadores, balón y espacios tácticos; evitar la vista excesivamente lejana de la captura.
- HUD moderno, limpio y adaptable: marcador, reloj, nombres/dorsales, indicador de jugador, radar y controles táctiles bien separados; prohibidos los textos amontonados o superpuestos.
- Audio de estadio y efectos de partido con recursos originales o licencias compatibles.

### Orden de trabajo y límites reales

1. Mantener el modo Amistosos como prioridad funcional: no sacrificar controles, estabilidad ni simulación para producir una captura bonita.
2. Diseñar una canalización de modelos 3D riggeados, materiales, texturas y clips de animación compatibles con el runtime actual.
3. Sustituir progresivamente los futbolistas procedurales por modelos de calidad, manteniendo un fallback ligero para dispositivos limitados.
4. Elevar la calidad del césped, estadio, público, iluminación, sombras, cámara y HUD.
5. Perfilar y ofrecer niveles gráficos ajustables para Android ARM64; comprobar FPS sostenidos, memoria, temperatura y batería en el teléfono objetivo.
6. Validar cada mejora en una compilación real ejecutada en Android. Una compilación CI o una escena procedural no certifica realismo ni rendimiento físico.

La referencia FIFA/EA SPORTS FC expresa el estándar visual y de experiencia deseado, no una promesa de paridad técnica inmediata ni autorización para copiar sus marcas, interfaces, código o recursos protegidos. Usar identidad propia y recursos originales o debidamente licenciados.

**Estado:** objetivo de realismo tipo FIFA confirmado por el propietario. La captura del 2026-11-25 queda clasificada como prototipo de baja fidelidad, no aceptado como resultado visual.
