# Investigación de repositorios públicos para Fútbol Nova

Fecha de revisión: 2026-11-23  
Repositorio objetivo: `Nova-Nexus-C35/Futbol-Nova`  
Rama de trabajo: `rebuild/libgdx-android-2026-11-22`

## Regla de reutilización

Buscar repositorios públicos es una actividad permanente del desarrollo. Cada hallazgo se evalúa por separado: licencia real en el repositorio, procedencia/forks, dependencias, lenguaje, compatibilidad con Java 17 + libGDX 1.14.2 + Android ARM64/minSdk 28, coste de memoria/FPS y pruebas. Un README que menciona una licencia no sustituye la lectura del archivo LICENSE. No se copian assets, código ni marcas sin verificar los derechos y obligaciones correspondientes. Las ideas arquitectónicas pueden inspirar implementaciones propias sin copiar expresión protegida.

## Hallazgos revisados

### Referencias directamente relevantes para el runtime 3D

- [libgdx/libgdx](https://github.com/libgdx/libgdx) — Framework Java multiplataforma con render OpenGL/OpenGL ES y soporte Android/3D. El README consultado declara Apache-2.0. Es la base técnica ya adoptada; mantener sus APIs y convenciones como primera referencia.
- [mgsx-dev/gdx-gltf](https://github.com/mgsx-dev/gdx-gltf) — Cargador glTF 2.0 y shaders PBR para libGDX. El README describe carga de cámaras/luces, morph targets, múltiples animaciones, interpolación de keyframes y materiales PBR; el archivo LICENSE declara Apache-2.0. Es un candidato fuerte para la futura importación de futbolistas riggeados y animaciones. **No se añadió como dependencia**: el build consultado declara libGDX 1.9.11, muy anterior a la versión 1.14.2 del juego; hay que validar compatibilidad, API, shaders GLES, tamaño y rendimiento en Android antes de integrarlo.
- [AmanRajAryan/Libgdx-gdx-gltf-instancing](https://github.com/AmanRajAryan/Libgdx-gdx-gltf-instancing) — Ejemplo público relacionado con instanciación y gdx-gltf. Solo referencia de investigación; revisar fuente, licencia y vigencia antes de reutilizar cualquier parte.

### Referencias para lógica futbolística y reglas

- [haeretici/soccer-js](https://github.com/haeretici/soccer-js) — Motor isométrico 2D JavaScript con IA de equipo, formaciones, roles, apoyos, marcaje, pases, centros, reglas de partido, saques, sustituciones y fuera de juego demorado. El README declara MIT. No es tecnología de render adecuada para el objetivo 3D; sirve como referencia para diseñar escenarios deterministas y pruebas de reglas. No se importó código.
- [modelence/open-soccer](https://github.com/modelence/open-soccer) — Juego 11v11 con cámara broadcast, controles, reinicios y reglas; arquitectura React/TypeScript/Canvas, no Java/libGDX. Su README permite estudiar la separación entre motor, cámara, render y controles. No se encontró un archivo LICENSE en la ruta raíz consultada; no reutilizar código ni assets hasta verificar licencia.
- [awest813/League-Soccer](https://github.com/awest813/League-Soccer) — Repositorio consultado como fork de `vi3itor/GameplayFootball`, con fuente upstream identificada como `BazkieBumpercar/GameplayFootball`; el repositorio consultado declara Apache-2.0. El árbol contiene un motor C++17 y arquitectura de simulación 3D/partido. Es una referencia conceptual para eventos, árbitro, estados, tácticas, estadísticas y pruebas; su cadena de procedencia y componentes deben revisarse antes de portar código. No es una dependencia directa Android/Java.
- [Tobilion/footysim](https://github.com/Tobilion/footysim) — Motor espacial 11v11 Python, con ticks deterministas, agentes, decisiones, física, xG, ratings y estadísticas por jugador. Útil para comparar contratos de simulación y telemetría; no es runtime móvil ni se importó código. Verificar licencia antes de reutilizar fuente.

### Ejemplo 3D pequeño

- [Adem-maker600/football-3d-game](https://github.com/Adem-maker600/football-3d-game) — Muestra Java/LWJGL de fútbol 3v3 con IA básica, balón, cámara y colisiones del campo. El README declara MIT, pero su alcance es didáctico y no resuelve 11v11 ni animación moderna. No se ha incorporado código.

## Decisiones para Fútbol Nova

1. Mantener el runtime confirmado Java 17 + libGDX 1.14.2 + Android ARM64. No migrar a Unity, Godot, Canvas/JavaScript, C++ ni otro motor sin autorización explícita.
2. Mantener la simulación propia en Java, desacoplándola progresivamente del render. Adoptar de las referencias patrones y casos de prueba: estados de partido, eventos, decisiones por jugador, fuera de juego, portero, balón dividido, estadísticas y replay.
3. Investigar gdx-gltf como vía prioritaria para sustituir gradualmente los humanoides procedurales por modelos glTF riggeados originales o con licencia compatible. Antes de añadirlo, probar una rama/escena aislada con la versión actual de libGDX y un modelo de prueba con licencia comprobada.
4. No incluir assets de demostración de terceros como recursos de producción. Cada modelo, textura, sonido, kit, escudo, nombre y likeness debe tener procedencia y licencia registradas.
5. Repetir esta búsqueda cuando se trabaje en un subsistema nuevo y actualizar este documento con fecha, repositorios revisados, licencias y decisión de adopción.

## Corrección de CI relacionada con libGDX

La ejecución Android CI #160 (commit previo `09aead75766875fe71818b72dbd567a23bff8c39`) ejecutó 56 pruebas y falló en las cuatro pruebas de `BroadcastCameraTest` con `java.lang.UnsatisfiedLinkError`. La causa es que las pruebas JVM ejercitan utilidades matemáticas/cámara de libGDX que requieren las bibliotecas nativas de escritorio; el proyecto solo declaraba nativos ARM64 para empaquetar la aplicación.

Se añadió `testRuntimeOnly "com.badlogicgames.gdx:gdx-platform:${gdxVersion}:natives-desktop"` a `app/build.gradle`. Es una dependencia exclusiva del runtime de pruebas; no sustituye ni se empaqueta en lugar de los nativos ARM64 de Android. La corrección queda pendiente de confirmación por la siguiente ejecución CI asociada al nuevo HEAD.


## Ampliación de investigación — 2026-11-23

Se amplió la búsqueda a repositorios públicos encontrados en GitHub. Se registran como referencias de estudio, no como dependencias ni como código incorporado:

- [Pirhan/soccer-game-ai](https://github.com/Pirhan/soccer-game-ai) — proyecto Java 5v5 con IA para regate, tiro, pase y formaciones; GitHub identifica licencia MIT. Es la referencia de lenguaje más próxima para estudiar descomposición de decisiones ofensivas, pero el alcance 5v5 y sus bibliotecas antiguas (Slick/LWJGL y librerías incluidas) no lo hacen integrable directamente en Android/libGDX. Revisar LICENSE y dependencias de cada archivo antes de portar cualquier fragmento.
- [brenosss/touchlines](https://github.com/brenosss/touchlines) — motor de partido 2D con árbol de decisiones pass/carry/shoot/dribble/through-ball, posicionamiento de portero, fuera de juego, tácticas y pruebas deterministas. GitHub muestra AGPL-3.0. Se utilizará solo como referencia conceptual mientras no exista una decisión jurídica explícita sobre obligaciones AGPL; no copiar ni enlazar código al runtime.
- [santisiri/fobal-simulator](https://github.com/santisiri/fobal-simulator) — simulación retro-isométrica 2D con motor headless, protocolo versionado, servidor, IA utilitaria, roles, desmarques, marcaje y estadísticas. No se confirmó licencia en esta revisión; solo estudio de arquitectura y escenarios, sin reutilizar código/assets.
- [FlavioMili/Player12](https://github.com/FlavioMili/Player12) — simulador/manager de fútbol con partidos, táctica, replay, estadísticas y guardado; está desarrollado en C++23/SDL3 y GitHub muestra GPL-3.0. No es compatible directamente con Java/libGDX ni se portará código por defecto; puede orientar requisitos de persistencia y modos de gestión futuros.

### Prioridades extraídas, sin adopción de código

1. Crear decisiones ofensivas separadas y testeables: pase, conducción, regate, tiro, centro y protección.
2. Mantener una simulación determinista/headless para pruebas de partido, independiente del render 3D.
3. Añadir roles y responsabilidades para evitar que todos los jugadores persigan el balón; usar escenarios reproducibles para presión, marcaje y transición.
4. Diseñar eventos/versionado de partido para replay, estadísticas e historial.
5. Tratar la licencia AGPL de TouchLines y GPL de Player12 como restricciones reales: no copiar ni mezclar código sin evaluación legal y cumplimiento explícito.

La búsqueda se repetirá al iniciar cada subsistema relevante. Toda futura integración deberá tener una ficha con origen, commit/versión, licencia del archivo concreto, dependencias, atribución, compatibilidad Android ARM64 y pruebas.


## Investigación adicional: porteros y motores 3D (2026-11-23)

- [Adem-maker600/football-3d-game](https://github.com/Adem-maker600/football-3d-game) — juego 3v3 en Java/OpenGL con física de balón, IA y cámara dinámica; el README declara MIT. Sirve como ejemplo pequeño para comparar organización de física/cámara, pero usa LWJGL/Maven y no es una base de integración Android/libGDX.
- [erengaygusuz/fotbiler-football](https://github.com/erengaygusuz/fotbiler-football) — motor 3D de fútbol con IA táctica, árbitro, offside, jugadas a balón parado, replay y estadísticas; GitHub identifica Apache-2.0 y lenguaje C++. Referencia de arquitectura y matriz de funcionalidades; no portar código C++ al runtime Java/libGDX sin un módulo JNI deliberado y pruebas Android.
- [telgarin01/last-save-reborn](https://github.com/telgarin01/last-save-reborn) — simulador 3D centrado en porteros, penaltis y modo carrera. No se verificó licencia en esta revisión; solo observación conceptual, sin copiar código ni assets.
- [Chenguku/Reactive-Goalkeeper](https://github.com/Chenguku/Reactive-Goalkeeper) — predicción de trayectoria y decisión de estirada con presupuesto de reacción; su README describe un pipeline Python/OpenCV y plataformas macOS/QNX. No es una dependencia adecuada para Android/libGDX; inspira únicamente el principio de predecir trayectoria y decidir dentro de una ventana temporal.

### Implementación propia inspirada en el principio de predicción

Se añadió en MatchGameplay una función Java independiente que estima el tiempo hasta el plano de intervención del portero, proyecta X/Y usando la gravedad compartida con BallFlightModel y rechaza trayectorias fuera de los postes, por encima del larguero, fuera del alcance lateral o que no se dirigen a la portería. FutbolNovaGame usa esa predicción para resolver la parada cerca del plano de gol. No se copió código de los repositorios estudiados.


## Revisión ampliada de arquitectura táctica y render Android — 2026-11-23

- [awest813/League-Soccer](https://github.com/awest813/League-Soccer) — motor C++17 con 22 jugadores, física de balón, árbitro, offside, faltas, set pieces, replay, estadísticas y carrera. El repositorio declara Apache-2.0. Referencia de arquitectura y QA; no se integra directamente por su stack C++/desktop.
- [UoS-CIDA-Lab/MESSI](https://github.com/UoS-CIDA-Lab/MESSI) — simulador 11v11 JAX para investigación multiagente con dos tiempos, fuera de juego, faltas, tarjetas, set pieces, sustituciones y física 3D. Stack JAX/Python: solo referencia para contratos deterministas; licencia no verificada aquí.
- [DeltaBitsSystem/FootballEngine](https://github.com/DeltaBitsSystem/FootballEngine) — motor táctico F# WIP con percepción espacial, roles, influencia de zonas, presión y decisiones contextuales. Licencia no verificada; referencia conceptual solamente.
- [the3deer/android-3D-engine](https://github.com/the3deer/android-3D-engine) — motor Android OpenGL ES con soporte descrito para glTF, skinning, animaciones y PBR. No se adopta; verificar LICENSE e integración libGDX/GLES antes de considerarlo.
- [berabera54152-cell/3D-Soccer-Game-Engine](https://github.com/berabera54152-cell/3D-Soccer-Game-Engine) — ejemplo MIT en Three.js/JavaScript con control, IA y física híbrida. Útil para comparar estados; no compatible directamente con Java/libGDX.

### Cambio propio basado en esta investigación

Se añadió una selección de carril de conducción para el portador IA: evalúa tres trayectorias adelantadas (centro y dos diagonales), mide la distancia al rival más cercano y mantiene el destino dentro del campo. FutbolNovaGame actualiza el objetivo del jugador mientras conserva la posesión y antes de decidir el pase. Es una implementación Java propia, no una copia de los repositorios citados.

### Criterios para la próxima fase de IA

1. Ampliar la elección contextual a regate, protección, pase corto/largo, pase filtrado, centro y tiro, usando atributos y presión.
2. Añadir apoyo y desmarque de compañeros con límites de ocupación de espacio; separar fase ofensiva y defensiva.
3. Validar cada decisión con escenarios deterministas y pruebas de invariantes, no solo con capturas del render.
4. Evitar dependencias JAX/F#/C++/JS dentro del runtime Android. Portar únicamente algoritmos originales tras revisión técnica y legal.


## Revisión adicional de IA colectiva y disciplina de licencias — 2026-11-23

- [Nano-233/football-kaleidoscope](https://github.com/Nano-233/football-kaleidoscope) — simulador sintético de partidos en Python con licencia MIT. Su documentación de motor separa decisiones del portador (pase, conducción, tiro, despeje) del movimiento sin balón (estructura, marcaje, presión, desmarques y apoyos), y resuelve balones sueltos por llegada física. Referencia útil para separar responsabilidades y validar decisiones geométricas; no se integra porque el runtime es Python/NumPy/SciPy, no Java/libGDX.
- [chike2510/touchline](https://github.com/chike2510/touchline) — tipos de dominio TypeScript para atributos técnicos, mentales y físicos, roles, perfiles tácticos y árbitro (incluye intensidad y ventaja). La API de GitHub no declara licencia para el repositorio en esta revisión; por tanto, no copiar ni portar su código. Se toma solo como lista de conceptos que Fútbol Nova debe modelar mediante diseño propio.
- [osagberg/FinalWhistle-Original](https://github.com/osagberg/FinalWhistle-Original) — auditorías de fidelidad futbolística que identifican vacíos como forma, fatiga, lesiones, sustituciones y arbitraje. Su LICENSE declara “All Rights Reserved” y prohíbe reutilizar/copiar el código sin permiso escrito. Se excluye de toda reutilización; solo se registra el riesgo/temas pendientes a nivel de alto nivel.

### Incremento propio de IA colectiva

Se sustituyó el movimiento de apoyo genérico por un objetivo sin balón sensible al rol y a la fase estimada del equipo: el delantero busca profundidad, los extremos conservan amplitud, mediocampistas ofrecen líneas de apoyo y la defensa compacta su bloque hacia la posición del balón. El jugador más cercano de cada equipo mantiene la responsabilidad de presionar/atacar el balón. La dirección se adapta al cambio de campo del descanso. Implementación Java original; no se copió código de los proyectos consultados.

### Límites

La posesión colectiva se estima comparando la distancia del jugador propio y rival más cercanos al balón; no es todavía un estado de posesión autoritativo. Faltan asignación de marcas individuales, coberturas coordinadas, líneas defensivas basadas en amenazas, atributos/rasgos individuales y decisiones probabilísticas de pase/regate/tiro. Las pruebas cubren geometría de forma y sentido de ataque, no validación de comportamiento visual en dispositivo.


## Referencias nuevas para decisiones contextuales — 2026-11-23

- [kylemath/soccerSim](https://github.com/kylemath/soccerSim) — simulador Python modular con jugador/agente, formaciones, física y pruebas; el README declara MIT. Referencia para separar motor, decisiones y harness de calibración; no se integra por su runtime Python/Flask.
- [Tobilion/footysim](https://github.com/Tobilion/footysim) — motor espacial 11v11 con decisiones por jugador, cálculo de carriles, presión, marcaje, pases, tiros, xG, fatiga y microestadísticas. Se consultó su descripción de arquitectura; la licencia no se verificó en esta revisión, por lo que no se reutiliza código.
- [davidklemme/abpfiff](https://github.com/davidklemme/abpfiff) — motor modular con cálculo espacial de líneas de pase, movimiento por rol, decisiones de pase/tiro/regate y predicción de recepción. Su licencia PolyForm Noncommercial 1.0.0 restringe el uso comercial; se excluye de reutilización directa y solo se estudian conceptos generales.

### Aplicación propia

Se añadió selección de objetivo de tiro de la IA orientada al poste contrario: el cálculo considera la posición horizontal del portero rival y la presión defensiva cercana, y limita el punto de mira al interior de la portería. La integración reutiliza la física de tiro existente; no se incorporó código ni dependencia de estos proyectos.
