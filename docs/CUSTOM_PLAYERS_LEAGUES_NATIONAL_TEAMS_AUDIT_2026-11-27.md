# Auditoría de jugadores personalizados, ligas y selecciones — Fútbol Nova

Fecha: 2026-11-27  
Rama: `work/futbol-nova-completion-audit-2026-11-27`  
Fuente histórica: Nova-Nexus-C35/Nova-Workspace, commit `0e92d59bf4b0ae365ffb9ea69ac32bf3cbb00db3`.

## Resumen

- 3 jugadores personalizados: Anier Cumbá, Máximo Padilla y Raymond Valdez.
- 4 clubes originales recuperados; C35 y RDA tienen 22 jugadores cada uno y quedan disponibles para amistosos. Génesis y Sangre Galáctica siguen en catálogo.
- 41 perfiles ficticios generados completan las dos plantillas: 20 en C35 y 21 en RDA.
- Diez ligas base: 725 clubes y 15.950 jugadores ficticios en el catálogo. Con los dos clubes originales de exhibición: 727 clubes y 15.994 jugadores registrados.
- Directorio nacional: 211 asociaciones, 16 torneos catalogados y cero paquetes de selecciones publicados.

## 1. Jugadores personalizados

### Anier Cumbá — NOVA-PLAYER-0035
Nombre completo Anier Alejandro Cumbá Ibar; dorsal 35; capitán; MCO/MC; diestro; nacido 2001-04-14; 171 cm, 65 kg; GRL 92, POT 97; especialización Velocista. Club activo: Albor Madrid (`FN-ESP-REAL-MADRID`); club original de exhibición: Nova C35 (`FN-NOVA-C35`). Selección preferida actual Cuba (`CUB`); el archivo histórico decía BRA, pero se conserva la confirmación posterior del propietario para CUB. Se separan atributos base, confirmados y efectivos con bonificación: no sumar Velocista dos veces. Apariencia guardada: pelo muy corto, bigote y perilla; botas rojas/doradas/negras; medias por encima de la rodilla; espinilleras; camiseta interior ajustada de compresión y manga larga; brazalete según equipo; sin muñequeras, cinta de pelo ni aretes. Guantes del mismo modelo que Raymond, con paleta propia negra/roja/dorada. Historial: traspaso narrativo de Albor Madrid a Real Madrid el 2025-06-23; títulos de Liga de Campeones del Caribe 2023-24 y 2024-25 con Santiago de Cuba, más participaciones anteriores desde 2021-22. Sin partidos registrados; contadores en cero.

### Máximo Padilla — NOVA-PLAYER-0036
Nombre completo Máximo Padilla Robert; dorsal 30; MCD/MC; edad guardada 29; 180 cm, 75 kg; GRL 94, POT 97; nacionalidad y país de nacimiento CUB. Club activo: Litoral Barcelona (`FN-ESP-BARCELONA`); club original: Nova C35. Selección preferida Cuba; selección favorita Argentina; club favorito FC Barcelona. GRL/POT confirmados por el propietario, pero `confirmed_attributes` está vacío, por lo que no se presentan los atributos individuales como confirmados uno por uno. Foto de referencia del propietario archivada; bloque 3D provisional pendiente de revisión. Medias por encima de la rodilla y camiseta interior de compresión de manga larga. Sin partidos registrados.

### Raymond Valdez — NOVA-PLAYER-0037
Dorsal 5; DC/MCO; nacido 2007-05-26; edad registrada 19; 171 cm, 63 kg; GRL 91, POT 96; club FC Río de Argenti (`FN-NOVA-RDA`). Nacionalidad `UNK`; no se inventa país ni selección preferida. Sus 35 atributos siguen como `provisional_generated_pending_owner_review`. Perfil creativo/regateador; las referencias Messi/Neymar son inspiración general, no autorización para copiar modelos, clips o mocap. Habilidades: Regate corto básica (`FN-SK-L1-172`), Regate explosivo (`FN-SK-L2-003`) y Giro en el área legendaria (`FN-SK-L3-187`). Celebración especificada `raymond_messi_inspired_skyward_point`, clip 3D final pendiente. Referencia facial localizada en Dropbox, pero no marcada como inspeccionada visualmente. Sin partidos registrados.

Anier y Máximo conservan sus clubes activos y agregan Nova C35 como afiliación histórica. Raymond conserva RDA. Las preferencias nacionales no crean convocatorias. La fuente completa de atributos, historial, honores, estadísticas y referencias está en [custom_players.json](../data/custom_players.json).

## 2. Cuatro clubes originales

| Club | ID | Estadio de diseño | Plantilla | Identidad/equipaciones |
|---|---|---|---:|---|
| Nova C35 | `FN-NOVA-C35` | Estadio del Fénix · 105.000 | 22/22 jugable | Navy/cian/plata; local con banda diagonal cian y micro-líneas plateadas; kits en propuesta. |
| FC Río de Argenti | `FN-NOVA-RDA` | Estadio Río de Argenti · 105.000 | 22/22 jugable | Referencia local del propietario preservada; paleta pendiente de cotejo visual. Visitante blanca/río plateado, alternativa navy/plata y portero ámbar en propuesta. |
| Nova Génesis | `FN-NOVA-GEN` | Arena Nova Génesis · 105.000 | 0/22, catálogo | Local blanca/navy con acentos rojo/verde, dirección v2 aprobada; otros kits en propuesta. |
| Sangre Galáctica | `FN-NOVA-SAG` | Estadio Sangre de Estrellas · 105.000 | 0/22, catálogo | Morado profundo/rojo, estrellas doradas y números blancos; kits en propuesta. |

Los estadios son especificaciones de diseño del videojuego, no recintos reales confirmados; propiedad y diseño final siguen pendientes. No consta estudio externo, fabricante ni patrocinador confirmado.

Plantillas: C35 = Anier + Máximo + 20 ficticios generados; RDA = Raymond + 21 ficticios generados. Total 44; los 41 generados conservan procedencia ficticia y estadísticas en cero. La fuente canónica es [nova_original_club_rosters_2026.json](../data/players/nova_original_club_rosters_2026.json), cuyos IDs coinciden con `known_player_ids` en [original_clubs_2026.json](../data/original_clubs_2026.json). El archivo histórico alternativo `original_club_players_2026.json` tiene IDs `NOVA-PLAYER-0038…0078` no referenciados por el roster canónico; no se importa para evitar duplicados.

## 3. Ligas

| País | Paquete base | Divisiones | Clubes | Jugadores ficticios |
|---|---|---:|---:|---:|
| Inglaterra | Premier League / Championship / League One | 3 | 68 | 1.496 |
| España | LaLiga y niveles inferiores | 3 | 82 | 1.804 |
| Brasil | Brasileirão Série A y niveles inferiores | 3 | 60 | 1.320 |
| Italia | Serie A y niveles inferiores | 3 | 100 | 2.200 |
| Alemania | Bundesliga y niveles inferiores | 3 | 56 | 1.232 |
| Francia | Ligue 1 y niveles inferiores | 3 | 54 | 1.188 |
| Portugal | Primeira Liga y niveles inferiores | 3 | 56 | 1.232 |
| Argentina | Liga Profesional y niveles inferiores | 3 | 125 | 2.750 |
| Cuba | Sistema Nacional de Ligas de Cuba — Fútbol Nova | 3 | 52 | 1.144 |
| Estados Unidos | MLS / USL Championship / USL League One | 3 | 72 | 1.584 |
| **Total** | | **30** | **725** | **15.950** |

Las diez bases tienen datos de temporada y plantillas ficticias guardadas, pero la validación del motor sigue pendiente; no se declaran jugables como ligas completas. El runtime carga las 52 plantillas cubanas y ahora dos plantillas originales de exhibición, no las demás ligas. Cuba tiene Primera (16 clubes, 30 jornadas), Segunda (16) y Tercera (20 en dos grupos de diez); España tiene 20/22/40 clubes; Inglaterra 20/24/24. MLS, USL Championship y USL League One son independientes, sin ascensos/descensos automáticos entre ellas.

El registro global contiene 100 ligas: diez bases y 90 entradas adicionales. Estados: 8 entradas base pendientes de auditoría; 1 MLS pendiente de auditoría; 88 entradas adicionales pendientes de membresía, clubes, plantillas, derechos y pruebas; 2 opcionales futuras (Países Bajos y Colombia); Cuba preservada como base. La clasificación externa es prioridad de investigación, no certificación oficial.

Diez paquetes adicionales (todos no descargables) son: Turquía/Süper Lig (18 clubes), Bélgica/Jupiler Pro League (18), Arabia Saudita/Saudi Pro League (18), Ecuador/LigaPro (16), Grecia/Super League (14), Egipto/Premier League (20), Chequia/First League (16), Japón/J1 League (20), Paraguay/Primera División (12) y Chipre/First Division (14). Faltan plantillas completas, revisión de membresía/procedencia/derechos, checksum y validación del motor.

Los módulos continentales reúnen seis confederaciones y 100 registros de ligas. Su manifiesto indica 212 registros de países frente a 211 asociaciones en el directorio; reconciliar la diferencia de un registro.

## 4. Selecciones nacionales

Directorio: 211 asociaciones; 211 marcadas verificadas en el snapshot del 2026-11-14. Distribución: AFC 46, CAF 54, Concacaf 35, CONMEBOL 10, OFC 11, UEFA 55. Paquetes publicados: 0; no hay plantillas absolutas verificadas ni selecciones jugables. Alcance inicial masculino absoluto; femenino y juvenil requieren paquetes separados.

Torneos catalogados (16): Copa Mundial de la FIFA; clasificatorias mundialistas AFC, CAF, Concacaf, CONMEBOL, OFC y UEFA; Campeonato Europeo de la UEFA; UEFA Nations League; Copa América; Copa Oro de la Concacaf; Concacaf Nations League; Copa Asiática de la AFC; Copa Africana de Naciones; Copa de Naciones de la OFC; Finalissima.

Todos permanecen como catálogo. Faltan paquetes de plantillas, identidades globales verificadas, convocatorias oficiales, elegibilidad FIFA/cambios de asociación, reglas por edición, calendario, motor de partidos y persistencia. Nacionalidad o preferencia no crea convocatoria: se exige convocatoria oficial y elegibilidad comprobada.

## 5. Integración en la rama actual

Se añadieron el catálogo de cuatro clubes y las dos plantillas completas; el manifiesto registra ambas fuentes; el lector nativo carga los dos equipos originales antes de las plantillas cubanas; Inicio muestra los cuatro clubes y el estado de cada plantilla; el validador comprueba IDs, 35 atributos, 22 jugadores y vínculos actuales/históricos. El amistoso predeterminado Albor Madrid vs Litoral Barcelona permanece sin cambios y sus plantillas españolas aún no están cargadas en runtime. Escudos, equipaciones y estadios siguen como datos de diseño; su representación 3D final requiere recursos y pruebas. Falta CI/compilación antes de declarar la migración terminada. PR #5 no se fusiona sin autorización.
