# Auditoría de finalización — Fútbol Nova

Fecha de auditoría: 2026-11-27
Repositorio: https://github.com/Nova-Nexus-C35/Futbol-Nova
Rama de trabajo: `work/futbol-nova-completion-audit-2026-11-27`
Base inspeccionada: `main`, commit `130d784e18589ad40cf260a1459c0b1582c63d47`

## Alcance y criterio

Esta auditoría inspecciona los archivos que forman la ruta nativa Android y los contratos de datos referenciados por el manifiesto. No equivale a una prueba física en un teléfono. Un sistema se considera implementado solo cuando existe código conectado al runtime; una entrada de catálogo, una pantalla informativa o una descripción no cuentan como funcionalidad terminada.

## Arquitectura confirmada

- Android nativo con Java 17.
- Simulación y renderizado en C++17.
- OpenGL ES 3.0.
- Android SDK 35, NDK 26.3.11579264, CMake 3.22.1 y Gradle 8.7 en CI.
- ABI configurada: `arm64-v8a`.
- Motor externo: ninguno. Unity y Godot quedan excluidos del runtime.
- Datos JSON canónicos en `data/`; el runtime lee el manifiesto `data/native_runtime_manifest.json`.
- Workflow de compilación: `.github/workflows/futbol-nova-native-android.yml`.

## Funciones que sí existen en el código inspeccionado

### Aplicación y navegación
- Pantallas Inicio, Online, Modos y Configuración.
- Selección de equipo local y visitante a partir de plantillas verificadas.
- Duración configurable por tiempo: 3, 5, 6, 8, 10, 15 o 20 minutos.
- Inicio, pausa, reanudación, descanso, segundo tiempo, final y revancha.
- Controles táctiles de movimiento, cambio de jugador, tiro, pase, regate/acción y presión.
- Marcador y reloj de partido.

### Prototipo de partido 3D
- Campo, líneas, porterías, estructuras de grada y balón renderizados con OpenGL ES.
- 22 actores procedurales con animación básica de carrera y celebración.
- Cámara que sigue al jugador controlado y al balón.
- Movimiento táctico básico de los actores no controlados.
- Movimiento básico de porteros, intentos de despeje/parada y colisiones del balón con jugadores y marco de portería.
- Detección básica de gol y secuencia de celebración/reinicio.
- Física simplificada de balón con gravedad, rebote, velocidad y límites del campo.

### Datos

La inspección de los tres archivos de plantillas cubanas confirmó **52 equipos y 1.144 futbolistas**: primera división 16/352, segunda división 16/352 y tercera división 20/440. Los 1.144 IDs son únicos entre las tres divisiones; cada jugador tiene 35 atributos enteros entre 0 y 99 y una posición registrada. Los tres archivos marcan los datos como ficticios.

El catálogo `data/custom_players.json` contiene dos perfiles adicionales. Sus IDs (`NOVA-PLAYER-0035` y `NOVA-PLAYER-0036`) no coinciden con los IDs de las plantillas cubanas, por lo que el método actual `applyTeam` no los aplica a las alineaciones cubanas. Se mantienen como perfiles catalogados, no como jugadores ya integrados en un club/plantilla nativa.

- Lector Android para datos JSON canónicos.
- Se rechazan plantillas con un número distinto de 22 jugadores o IDs vacíos/duplicados.
- Se cargan plantillas cubanas de primera, segunda y tercera división.
- Se aplican algunos atributos del perfil del jugador al runtime.
- Se muestran catálogos de habilidades, técnicas, balones, clubes, módulos y jugadores personalizados.

## Configuración personalizada recuperada y protegida — 2026-11-27

Se revisaron las fichas y decisiones guardadas antes de continuar el desarrollo:

- Fuente estructurada única: `data/custom_players.json`.
- Anier Cumbá (`NOVA-PLAYER-0035)): club `FN-ESP-REAL-MADRID` (nombre de juego: Albor Madrid), MCO/MC, GRL 92, POT 97, dorsal 35, capitán y especialización Velocista. Se conserva la separación entre `base_attributes` / `confirmed_attributes` y atributos efectivos con bonificación; no volver a sumar Velocista al GRL.
- Máximo Padilla (`NOVA-PLAYER-0036`): club `FN-ESP-BARCELONA` (nombre de juego: Litoral Barcelona), MCD/MC, GRL 94, POT 97, dorsal 30. Sus medidas de blockout siguen siendo provisionales y no se convierten en biografía confirmada.
- La confirmación del propietario del 2026-11-19 asigna a Anier y Máximo a Cuba (`CUB`). El JSON de Anier aún indicaba `BRA`; se corrigió a `CUB` en `data/custom_players.json` y se verificó la escritura. Esta preferencia no equivale a una convocatoria oficial.
- Se mantienen las preferencias visuales individuales registradas en las fichas; no se sustituyen ni se inventan rasgos.
- Los IDs de Anier y Máximo están enlazados en `data/spain_clubs_2026_27.json` a sus clubes por ID estable. Ambos clubes siguen con `status=draft_review_required`; esa referencia no es una plantilla completa ni los habilita para jugar.
- `data/national_teams_manifest.json` declara cero paquetes de selecciones publicados y plantillas nacionales aún no construidas. La preferencia CUB no crea una selección jugable ni una convocatoria.
- El runtime Android actual solo carga las tres fuentes de plantillas cubanas (52 equipos). No carga todavía las plantillas españolas de Albor Madrid y Litoral Barcelona. Por tanto, los perfiles personalizados siguen visibles en el catálogo, pero no se deben insertar artificialmente en una plantilla cubana ni anunciar como jugadores ya integrados en el amistoso nativo.

El validador `tools/validate_native_data.py` ahora comprueba que cada perfil personalizado apunte al club correcto mediante `club_id` y que el club contenga su ID estable en `player_ids`. También conserva la distinción entre preferencia nacional y convocatoria. La compilación/ejecución CI de este cambio queda pendiente de resultado.

## Pendiente confirmado

| Área | Estado real | Criterio para darla por terminada |
|---|---|---|
| Modelos de futbolistas | Procedurales y geométricos | Modelos originales con proporciones, equipaciones, rostro/silueta y LOD |
| Animaciones | Ciclos básicos | Locomoción, giro, aceleración, frenada, pase, tiro, salto, choque, caída, portero y transiciones sin cortes |
| Física del balón | Simplificada | Pruebas repetibles de fricción, efecto, bote, aire, contacto, postes, larguero y red |
| IA de campo | Movimiento y decisiones heurísticas básicas | Roles, formaciones, presión, cobertura, desmarques, posesión, transición, fatiga y dificultad |
| Porteros | Movimiento y colisión básicos | Posicionamiento, anticipación, blocaje, despeje, rechace dirigido, mano cambiada y animaciones |
| Reglas y árbitro | No implementados como sistema completo | Saques, banda, córner, saque de meta, faltas, tarjetas, penaltis, ventaja, fuera de juego y árbitro |
| Sustituciones | No implementadas | Banquillo, límites configurables, cambios y actualización de roles/atributos |
| Estadísticas | Marcador y reloj básicos | Posesión, tiros, tiros a puerta, pases, precisión, faltas, tarjetas, distancia y rendimiento individual |
| Guardado | No hay persistencia de carrera/partido | Guardado versionado, carga, migración de esquema y recuperación ante archivo inválido |
| Manager | Pantalla de catálogo, no modo funcional | Plantilla, calendario, tabla, economía, contratos, fichajes, entrenamientos y temporadas |
| Leyenda | No implementado | Carrera individual, progreso, objetivos, contratos, selección nacional y estadísticas |
| Ligas y copas | Datos/catalogación parcial | Calendarios, jornadas, clasificación, eliminatorias, desempates, ascensos y descensos |
| Online | Pantalla informativa | Protocolo, salas, sincronización, autoridad de partido, reconexión, seguridad y pruebas de red |
| Audio | No conectado como sistema de partido | Público, ambiente, silbato, balón, impactos, interfaz y mezcla configurable |
| Gráficos/cámaras | Renderizado base | Calidad escalable, cámaras seleccionables, sombras, iluminación, estadio y rendimiento |
| Prueba en dispositivo | No acreditada por esta inspección | Instalar y jugar en Android ARM64 real; documentar modelo, versión Android, FPS, fallos y consumo |

## Riesgos técnicos que deben resolverse

9. Los dos perfiles de `custom_players.json` no coinciden con IDs de las plantillas cubanas; debe definirse su club/competición y completar una plantilla válida antes de presentarlos como seleccionables en el partido.
10. Los catálogos de habilidades y técnicas están empaquetados, pero el motor C++ no los ejecuta todavía como un sistema de habilidades; el botón de regate/acción no equivale a integrar las 300 habilidades del catálogo.
11. La UI de Online y los módulos opcionales son informativos; no existe todavía una implementación de red ni un instalador genérico de módulos.



1. El runtime debe fallar de forma visible y recuperable si falta un JSON obligatorio; no debe continuar con catálogos parcialmente cargados como si fueran válidos.
2. La lista de equipos jugables debe excluir equipos repetidos y evitar que un equipo sea seleccionado simultáneamente en ambos lados.
3. Los atributos del jugador se aplican parcialmente: deben definirse y probarse los índices del vector de atributos, incluyendo atributos específicos de portero.
4. La simulación y la interfaz deben mantener una única fuente de verdad para el reloj, el estado del partido y los eventos.
5. La pausa/reanudación del ciclo de vida Android debe conservar o reiniciar el partido según una política explícita, sin dejar callbacks activos.
6. Los recursos OpenGL deben liberarse y reconstruirse correctamente al perderse el contexto gráfico.
7. Las reglas deben ser deterministas y comprobables con pruebas automatizadas; no se deben añadir reglas de fútbol mediante simples mensajes de interfaz.
8. El catálogo de módulos no debe mostrar como descargable/instalado un módulo que no tenga paquete, hash, versión y verificación de integridad.

## Orden de ejecución

### P0 — Integridad y estabilidad del partido base
- Validación estricta de datos y atributos.
- Pruebas de reloj, pausa, descanso, segundo tiempo, goles, reinicios y límites.
- Pruebas de colisión, portería y portero.
- Ciclo de vida Android y recuperación de contexto OpenGL.
- Compilación ARM64 reproducible.

### P1 — Partido completo
- Sistema de estados de balón y reinicios reglamentarios.
- Árbitro y reglas: banda, córner, saque de meta, faltas, tarjetas, penaltis y fuera de juego.
- Sustituciones y banquillo.
- IA táctica y dificultad.
- Estadísticas del encuentro.

### P2 — Presentación
- Modelos humanos originales y equipaciones.
- Animaciones de partido.
- Estadios, iluminación, cámaras, audio y ajustes de rendimiento.

### P3 — Modos y persistencia
- Guardado/carga.
- Manager.
- Leyenda.
- Ligas, copas y temporadas.

### P4 — Red
- Multijugador local/online después de estabilizar el simulador determinista.
- Pruebas de sincronización, reconexión y seguridad.

## Política de datos y licencias

- `data/` es la fuente canónica de datos de juego; no crear copias divergentes dentro de `native-android/`.
- Mantener IDs estables de clubes, competiciones y futbolistas.
- Los futbolistas y clubes ficticios deben seguir marcados como ficticios.
- No convertir estimaciones internas en datos reales ni atribuirlas a fuentes externas.
- No copiar código, escudos, equipaciones, modelos, música, nombres protegidos o recursos de terceros sin licencia compatible.
- La referencia visual se usa para estudiar estructura y usabilidad, no para copiar sus recursos.
- Ultimate Team permanece excluido.

## Verificación

La validación de datos existente está en `tools/validate_native_data.py`. El workflow actual compila y comprueba la APK y la biblioteca ARM64. La compilación CI no sustituye una prueba de juego en dispositivo real.

No marcar un apartado como terminado hasta adjuntar evidencia: prueba automatizada, resultado CI o prueba manual reproducible.


## Raymond Valdez y clubes originales — recuperación desde fuentes históricas — 2026-11-27

### Perfil canónico recuperado

La búsqueda ampliada encontró el perfil completo de Raymond en el repositorio histórico `Nova-Nexus-C35/Nova-Workspace`, commit `0e92d59bf4b0ae365ffb9ea69ac32bf3cbb00db3`:

- Fuente estructurada: `proyectos/juegos/futbol-nova/data/custom_players.json`; ID `NOVA-PLAYER-0037`.
- Documento de perfil y plantillas: `docs/ORIGINAL_CLUB_ROSTERS_AND_RAYMOND_2026-11-20.md`.
- Implementación: `docs/ORIGINAL_CLUBS_FRIENDLY_IMPLEMENTATION_2026-11-20.md`.
- Perfil: Raymond Valdez; dorsal 5; DC, secundaria MCO; nacimiento 2007-05-26; edad registrada 19; 171 cm; 63 kg; GRL 91; POT 96.
- Nacionalidad: `UNK` (no confirmada; no inferir).
- Club histórico: `FN-NOVA-RDA` — FC Río de Argenti.
- Atributos individuales: existen en `custom_players.json` como `attributes` y `base_attributes`, pero están marcados `provisional_generated_pending_owner_review`; no presentarlos como confirmados.
- Estadísticas de carrera/temporada: sin partidos registrados; los contadores empiezan en cero.
- Perfil de juego: delantero creativo/regateador, con rasgos generales inspirados por el propietario en Messi y Neymar; no copiar modelos, clips ni capturas de movimiento.
- Celebración original: `raymond_messi_inspired_skyward_point`; secuencia descrita en el documento de plantillas, pendiente de clip 3D final.
- Habilidades registradas: Regate corto básica (`FN-SK-L1-172`), Regate explosivo (`FN-SK-L2-003`) y Giro en el área legendaria (`FN-SK-L3-187`).
- Referencia facial del propietario: `/Nova/Workspace/Recursos gráficos/Ficha de Jugador - Raymond Valdez.png` (PNG, 2.49 MB; Dropbox, modificado 2026-11-15).
- Guantes: el perfil conserva la referencia visual del propietario; la actualización compartida del 2026-11-16 confirma que Anier utiliza el mismo modelo de guantes que Raymond, manteniendo Anier su propia paleta negra/roja/dorada. No trasladar automáticamente esa paleta a Raymond.

### Equipos originales, estadios y equipaciones recuperados

Fuente estructurada: `data/original_clubs_2026.json` y fichas individuales del mismo repositorio histórico. Los cuatro equipos son originales/ficticios de Fútbol Nova:

| Club | ID | Estadio (capacidad de diseño) | Identidad y primera equipación |
|---|---|---|---|
| Nova C35 | `FN-NOVA-C35` | Estadio del Fénix (105.000) | Azul marino/cian, plata y blanco; camiseta local azul marino con banda diagonal cian y micro-líneas plateadas; escudo conceptual C35 con alas de fénix. |
| FC Río de Argenti | `FN-NOVA-RDA` | Estadio Río de Argenti (105.000) | Plata, azul río, azul marino y blanco; la camiseta local tiene una referencia original del propietario en Dropbox que debe tratarse como fuente visual de verdad. Escudo conceptual con río y monograma RDA. |
| Nova Génesis | `FN-NOVA-GEN` | Arena Nova Génesis (105.000) | Blanco/azul marino con acentos rojos y verdes; camiseta local v2 aprobada visualmente, paneles geométricos/texturizados y líneas angulares; letras GEN entrelazadas. |
| Sangre Galáctica | `FN-NOVA-SAG` | Estadio Sangre de Estrellas (105.000) | Morado/rojo dominantes, amarillo/blanco secundarios; camiseta local propuesta en morado profundo con diagonal roja, detalles de estrellas doradas y números blancos. |

Detalles de equipaciones alternativas documentadas:
- Nova C35: visitante blanca con hombros navy y paneles cian; alternativa cian con bloques navy; portero amarillo señal con paneles navy y ribetes cian.
- FC Río de Argenti: visitante blanca con motivo de río plateado y acentos azul río; alternativa navy con línea diagonal plateada; portero ámbar con paneles navy y plata.
- Nova Génesis: visitante navy con paneles angulares blancos y acentos rojos/verdes; alternativa roja; portero verde eléctrico.
- Sangre Galáctica: visitante blanca con hombros morados y degradado rojo; alternativa roja con textura estelar morada; portero dorado.

### Archivos gráficos localizados en Dropbox

- Raymond: `/Nova/Workspace/Recursos gráficos/Ficha de Jugador - Raymond Valdez.png`.
- Nova C35: `/Apps/Futbol Nova/Assets/Clubs/Nova C35/Kits/nova_c35_home_kit_reference.jpg`.
- FC Río de Argenti: `/Apps/Futbol Nova/Assets/Clubs/RDA/Kits/rda_home_kit_reference.jpg`.
- Nova Génesis: carpeta `/Apps/Futbol Nova/Assets/Clubs/Nova Génesis/`; existe una propuesta histórica PNG y el manifiesto identifica una propuesta local v2 aprobada como adjunto Paper, pendiente de incorporación binaria.
- Sangre Galáctica: no se encontró un archivo binario específico en la carpeta de activos revisada; su kit permanece como propuesta descrita en JSON.

### Autoría, estudios y derechos

No se encontró un estudio externo de diseño, fabricante de equipaciones ni patrocinador confirmado para estos cuatro clubes. Las fichas describen identidades y conceptos originales del proyecto; no atribuirlos a Nike, Adidas, Puma ni a un estudio externo sin fuente. El documento maestro exige auditoría de derechos por activo y diseños originales cuando no exista licencia. Los conceptos marcados como propuesta no deben presentarse como kit final aprobado.

### Límite de migración al runtime actual

El perfil y los clubes anteriores proceden del repositorio histórico `Nova-Workspace`. La rama activa de este repositorio `Futbol-Nova` usa actualmente IDs de clubes españoles para Anier y Máximo y el amistoso predeterminado Albor Madrid vs Litoral Barcelona; no contiene aún el catálogo `FN-NOVA-RDA`/Nova C35 ni importa estas plantillas al runtime Android nativo. Por eso, el perfil recuperado queda identificado con su club histórico correcto y no se reasigna a Albor, Litoral ni a un club cubano/español por conveniencia. La migración de Raymond y de los cuatro clubes originales requiere una decisión explícita de alcance y la integración/validación de sus catálogos en el runtime actual.


## Actualización autorizada — cuatro clubes originales y auditoría ampliada — 2026-11-27

La autorización posterior del propietario permitió integrar los cuatro clubes originales y recuperar Raymond. La nota anterior que indicaba que faltaba autorización queda supersedida por esta actualización.

- Catálogo añadido: `data/original_clubs_2026.json` (Nova C35, FC Río de Argenti, Nova Génesis y Sangre Galáctica).
- Plantillas añadidas: `data/players/nova_original_club_rosters_2026.json`. Nova C35 y FC Río de Argenti cargan 22 jugadores cada uno; Nova Génesis y Sangre Galáctica siguen sin roster y no son jugables.
- Perfiles personalizados: `data/custom_players.json` contiene Anier, Máximo y Raymond. Anier y Máximo conservan sus clubes activos actuales y registran Nova C35 como afiliación histórica; Raymond conserva FC Río de Argenti.
- El runtime nativo y la pantalla Inicio muestran los cuatro clubes y permiten elegir los dos con plantilla completa. El amistoso predeterminado Albor Madrid vs Litoral Barcelona se mantiene.
- El validador comprueba las cuatro fichas de club, las dos plantillas originales, 22 jugadores por equipo, 35 atributos y vínculos actuales e históricos.
- La investigación completa queda en `docs/CUSTOM_PLAYERS_LEAGUES_NATIONAL_TEAMS_AUDIT_2026-11-27.md`. El catálogo de 100 ligas y las 211 asociaciones no se presentan como ligas ni selecciones jugables.
- Queda pendiente CI/compilación y la integración visual 3D de escudos, equipaciones y estadios. No se fusiona PR #5 sin autorización.
