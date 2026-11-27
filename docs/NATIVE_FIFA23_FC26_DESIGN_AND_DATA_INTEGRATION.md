# Fútbol Nova — diseño integrado FIFA 23 + FC 26 para el motor nativo

**Fecha:** 2026-11-19  
**Rama de implementación:** `work/native-3d-engine`  
**Estado:** especificación de migración y contrato de integración; no declarar pantallas/modos/modelos terminados por estar documentados.

## 1. Decisión visual y de producto

Fútbol Nova combinará:
- **FIFA 23 como referencia principal de estructura y navegación:** jerarquía de hubs, pantallas, preparación de partidos, configuración, pausa, HUD y presentación de carrera. Las capturas archivadas en `assets/references/` tienen prioridad para los apartados que representan.
- **EA SPORTS FC 26 como referencia complementaria de presentación y simulación:** interfaz más contextual, lectura de roles, progresión individual, controles responsivos, animación corporal volumétrica, regate más preciso, protección del balón, posicionamiento inteligente y porteros más consistentes.
- **Identidad propia NOVA:** paleta azul noche/azul petróleo, cian, verde lima de cancha, blanco frío, gris acero y ámbar; tipografía, iconografía, clubes, futbolistas, equipaciones, estadios, sonidos y animaciones originales.

No copiar código, pantallas exactas, marcas, escudos, nombres exclusivos, modelos, texturas, sonidos ni assets de FIFA/FC. La referencia sirve para patrones de experiencia, no para extraer recursos protegidos.

## 2. Referencias visuales existentes que deben usarse

Archivos reales que ya están en el repositorio:
- `assets/references/main-menu-settings-reference.jpg`: jerarquía y navegación de Configuración.
- `assets/references/pause-menu-reference.jpg`: pausa durante el partido.
- `assets/references/kit-selection-reference.jpg`: selector HOME/AWAY y modelos de cuerpo completo.
- `assets/references/legend-mode-home-reference.jpg`: central de Modo Leyenda.
- `assets/references/ultimate-team-main-menu-reference.jpg`: archivo histórico EXCLUIDO. No usar.

Referencias todavía pendientes de recuperar: original de alta resolución de Pantalla 1; captura de Manager; ajustes de partido; ajustes de mando; imagen azul HOME/PLAY/CLUB. No simular que están archivadas ni inventar detalles visuales de ellas.

Las tres capturas recientes del usuario son diagnóstico del estado actual: campo con cámara demasiado cenital y jugadores minúsculos, splash de Godot visible y selector de clubes que tapa información/desborda la ventana. Son evidencia de defectos a corregir, no referencias del diseño final.

## 3. Arquitectura de menú que se conserva

La navegación raíz tiene exactamente cuatro pantallas y no se debe reemplazar por otra estructura:
1. **Inicio:** Partido amistoso local 11v11 y Ayuda en el paquete base.
2. **Online:** escaparate del módulo multijugador; no fingir conectividad.
3. **Modos:** catálogo de módulos descargables: Leyenda, Entrenador, Ligas y Copas, Torneos de Selecciones, TAS y Centro de Entrenamiento.
4. **Configuración:** Edición, Módulos, Música, Juego, Datos, Controles, Audio, Gráficos, Accesibilidad, Administración e Información.

Navegación horizontal bidireccional y circular, conserva estado, cuatro accesos persistentes y carrusel horizontal de tarjetas. La pantalla Inicio no abre modos que no estén instalados. El paquete base debe jugar amistosos offline sin módulos. Los modos adicionales solo se habilitan después de empaquetar, verificar, instalar y validar el módulo.

**Exclusiones expresas:** no Ultimate Team, no modo equivalente de cartas/colección, sobres, monedas UT ni mercado de cartas. No crear fútbol femenino: el alcance documentado es fútbol masculino.

## 4. Qué se toma de FC 26 para el motor y la experiencia

Los conceptos se implementan como sistemas originales de Fútbol Nova:
- Dos perfiles de partido configurables: **Auténtico** (ritmo táctico, colocación responsable, transiciones y física con mayor peso) y **Competitivo** (respuesta rápida, control directo y ritmo más ágil). El amistoso permite elegir; cada modo puede fijar un perfil por reglas.
- Movimiento con aceleración/desaceleración, cambios de dirección, altura y pie dominante; el control del usuario no debe quedar atrapado en animaciones largas.
- Regate dinámico: intervalos de toque de balón dependientes del perfil, velocidad, altura, control, agilidad, equilibrio y pie dominante.
- Protección del balón y duelos físicos dependientes de fuerza, equilibrio, orientación corporal, resistencia y ángulo de contacto.
- Posicionamiento de IA: líneas defensivas, cobertura, marcaje, apoyo, desmarques, anchura, presión y retorno.
- Porteros con posicionamiento, lectura, reacción, manejo, desvíos y recuperación coherentes con sus atributos.
- Animación corporal por estados y transiciones: quieto, trote, carrera, sprint, giro, freno, primer toque, conducción, pase, centro, tiro, cabeceo, salto, entrada, caída, levantarse, atajada y celebración.
- Roles y especializaciones originales: definen tendencias y bonificaciones limitadas; nunca sustituyen atributos ni se suman dos veces al GRL.
- Progresión de jugador con arquetipo/estilo de desarrollo, experiencia, atributos, habilidades y objetivos dentro de Leyenda; no usar cartas coleccionables.

FC 26 distingue oficialmente perfiles Competitive y Authentic, y describe mejoras de regate, posicionamiento de IA, movimiento explosivo, protección del balón, animaciones volumétricas y decisiones de porteros. Fútbol Nova adopta estos principios de diseño, no su implementación propietaria. Fuentes:  
- https://www.ea.com/games/ea-sports-fc/fc-26/features/fc-26-gameplay  
- https://www.ea.com/games/ea-sports-fc/fc-26/news/pitch-notes-fc26-gameplay-deep-dive  
- https://www.ea.com/games/ea-sports-fc/fc-26/news/pitch-notes-fc26-career-mode-deep-dive

## 5. Modos y pantallas según datos canónicos existentes

La lista de modos no se inventa ni se reemplaza:
- **Amistoso local:** único modo jugable del paquete base; 11 contra 11, clubes/plantillas offline, estadio, equipaciones, balón y ajustes previos permitidos.
- **Leyenda:** carrera individual con jugador creado o personalizado; central, Mi Pro, Temporada, Personalizar, progresión, personalidad, notificaciones, mi carrera y centro de plantilla.
- **Entrenador:** carrera de club con plantilla, calendario, tabla, objetivos de directiva, economía, academia, fichajes y evaluación.
- **Ligas y Copas:** competiciones nacionales/continentales, temporadas, rondas, clasificación y reglamentos.
- **Torneos de selecciones:** competiciones internacionales con selección nacional.
- **TAS:** Torneo Americano de Supercampeones y clasificatorias, según su módulo.
- **Centro de Entrenamiento:** tutoriales, práctica libre, tiros libres, penaltis, ejercicios, desafíos y evaluación.
- **Online:** módulo aparte; internet y zona Wi-Fi local deben mostrarse como conexiones distintas.

El catálogo y los manifiestos existentes siguen siendo fuente canónica: `data/game_modules_manifest.json`, `data/optional_modules_catalog.json`, `data/league_content_manifest.json` y `data/national_teams_manifest.json`. Los estados `planned`, `in_development` y `testing` no se presentan como disponibles ni descargables.

## 6. Datos de clubes, ligas y jugadores ya enviados

No crear listas ficticias nuevas cuando ya hay datos canónicos. Integrar por ID estable:
- Diez ligas base: Inglaterra, España, Brasil, Italia, Alemania, Francia, Portugal, Argentina, Cuba y Estados Unidos.
- Catálogo documentado: 725 clubes y 15.950 perfiles ficticios (22 por club), con estadísticas de carrera/temporada y procedencia.
- Cuba: archivos de primera y segunda división y catálogo cubano.
- Equipos personalizados y nombres de juego originales: por ejemplo `FN-ESP-REAL-MADRID` se muestra como **Albor Madrid** y `FN-ESP-BARCELONA` como **Litoral Barcelona**; no cambiar IDs ni sustituir nombres sin una decisión del propietario.
- Plantillas deben validarse con 11 jugadores únicos como mínimo antes de habilitar el saque inicial.
- Solo perfiles de jugador con estado y procedencia verificados se consideran datos disponibles. No convertir perfiles ficticios en jugadores reales.

### Jugadores personalizados que deben conservarse

**Anier Cumbá — `NOVA-PLAYER-0035`**
- Nombre en camiseta CUMBÁ, dorsal 35, capitán, Cuba, MCO/MC, diestro, 171 cm/65 kg, GRL 92, POT 97.
- Perfil velocista: aceleración y velocidad efectiva 98; pase corto 96, pase largo 95, visión 96, control 96, resistencia 96, regate 91.
- Apariencia 3D: cabello muy corto, bigote y perilla, camiseta fuera del pantalón, medias largas sobre rodilla, espinilleras obligatorias, camiseta interior de compresión manga larga, botas rojas con detalles dorados/negros, guantes negros con detalles rojos/dorados, brazalete de capitán de la equipación; sin muñequeras, cinta de pelo ni aretes.
- La cara de alta fidelidad está pendiente; el blockout procedural no debe presentarse como modelo final.

**Máximo Padilla — `NOVA-PLAYER-0036`**
- MCD/MC, Cuba, GRL 94, POT 97; 180 cm/75 kg son medidas provisionales del blockout, no biografía confirmada.
- Pivote único recuperador: intercepciones 97, conciencia defensiva 97, entradas 97, fuerza 96, agresividad 95, resistencia 94, pase corto 88 y largo 92.
- Apariencia 3D con medias altas, espinilleras y camiseta interior de compresión de manga larga; referencia fotográfica guardada en el repositorio.
- No modificar GRL/POT confirmados ni atribuirle rasgos no documentados.

La fuente estructurada de ambos perfiles es `data/custom_players.json`; fichas detalladas: `docs/ANIER_CUMBA_PLAYER_IMPLEMENTATION.md` y `docs/MAXIMO_PADILLA_PLAYER.md`. Las estadísticas, atributos, bonificaciones y GRL deben seguir el contrato de datos: no duplicar bonificaciones.

## 7. Preparación de amistoso y partido

Conservar decisiones ya registradas:
- Predeterminado: Albor Madrid vs Litoral Barcelona, IDs estables.
- Estadios offline: Estadio Nova Central (42.000), Arena Bahía (28.500), Parque Aurora (19.000).
- Selector local/visitante independiente, once titular sin IDs repetidos, 4-3-3 como prioridad inicial y validación de plantillas.
- Selector de equipación HOME/AWAY independiente: deslizar sobre el panel de cada equipo solo modifica ese lado; la vista previa 3D se actualiza inmediatamente.
- Selector de balón con apariencia y reglas; no cambiar física por apariencia.
- Los amistosos permiten duración por tiempo de 3, 5, 6, 8, 10, 15 o 20 minutos, siempre dos tiempos; el predeterminado es 5 minutos reales por tiempo.
- Cámara configurable: Transmisión, Táctica, Dinámica, Cercana, De jugador y Cinematográfica; altura, distancia/zoom, ángulo y seguimiento.
- Controles táctiles: joystick progresivo a la izquierda; pase, disparo, centro, técnicas; defensa con presión, cambio de jugador y barrida; gestos configurables.
- Pausa: Reanudar, Repetición (solo si existe), Entrenador/Tácticas, Datos, Configuración y Abandonar con confirmación. Pausa congela reloj, IA, balón y animaciones del partido y reanuda el mismo estado.

## 8. Requisitos de la implementación nativa

- Android Java/Kotlin para ciclo de vida, pantallas, accesibilidad y entrada.
- C++17 propio para partido, balón, colisiones, IA, animación y estado.
- OpenGL ES 3.0 para renderizado directo; shaders, mallas, cámara, materiales, luces y sombras implementados por el proyecto.
- Ningún motor de juego externo, incluido Godot, Unity, Unreal u otro.
- Android SDK/NDK/CMake son herramientas de plataforma/compilación, no motores de juego.
- Datos deportivos en JSON versionado, cargados por IDs estables y validados; guardados locales versionados.
- La base debe ser offline-first; no requerir conexión para amistosos.
- APK objetivo Android ARM64.

### Orden de implementación

1. Hacer que el proyecto nativo compile y arranque en ARM64.
2. Crear cargador de catálogos y perfiles JSON; validar clubes, jugadores y equipos.
3. Completar malla y rig humano procedural; importar solo recursos propios/licenciados.
4. Animación locomotora y transición de estados con control directo.
5. Física del balón, contacto pie-balón, colisiones, porterías, goles, saques y faltas.
6. IA 11v11, roles, porteros, tácticas y árbitro.
7. Cámara de retransmisión y cámaras configurables.
8. HUD, pausa, selector de equipos, equipaciones, balón y estadio.
9. Pantallas raíz y hubs de modos conforme a la distribución base/módulos.
10. Pruebas unitarias, de regresión, APK ARM64, instalación y partido completo en Android.

## 9. Criterios de aceptación

- No aparece splash ni biblioteca de Godot/otro motor.
- El partido se presenta en 3D con futbolistas de escala humana, cámara seleccionable y campo/estadio completos.
- Los datos de equipos y jugadores provienen de los JSON canónicos; los IDs no se regeneran.
- Anier y Máximo mantienen exactamente los datos y rasgos confirmados.
- El amistoso funciona offline 11v11 y no requiere instalar módulos.
- No se muestran modos como jugables si no existe un paquete instalado y validado.
- No aparece Ultimate Team ni contenido equivalente.
- Menús adaptados a móvil, sin desbordes, sin selectores que tapen el contenido y con controles táctiles utilizables.
- No afirmar éxito hasta que la compilación y las pruebas en dispositivo real hayan pasado.
