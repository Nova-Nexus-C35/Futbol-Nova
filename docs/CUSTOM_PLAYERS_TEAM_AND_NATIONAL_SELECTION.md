# Jugadores personalizados en clubes y selecciones — contrato de integración

Fecha: 2026-11-19
Estado: requisito confirmado; integración visual y de convocatorias pendiente.
Alcance inmediato: modo Amistosos 3D.

## Objetivo
Los perfiles personalizados del catálogo canónico deben aparecer en los equipos y selecciones a los que pertenecen. Deben conservar identidad, atributos y apariencia al elegir el equipo, consultar su plantilla e iniciar un amistoso. No basta con cargar sus estadísticas de forma aislada.

## Fuentes y claves de pertenencia
- Fuente de perfiles: `data/custom_players.json`.
- Identidad primaria: `id` estable del jugador. Nunca identificarlo solo por nombre.
- Club: `club_id` es la clave de pertenencia al club. `club_name` es solo etiqueta de presentación.
- Selección: usar la pertenencia nacional explícita del perfil y el catálogo/plantilla de selecciones. `preferred_national_team` expresa la selección preferida y debe resolverse contra un ID de selección válido; no inventar una convocatoria a partir del texto del nombre.
- `nationality` y `birth_country` son datos biográficos, no autorización automática para duplicar al jugador en todas las selecciones de ese país.
- `favorite_club` y `favorite_national_team` son preferencias, no contratos ni pertenencias.

## Reglas de integración
1. Al cargar un club o selección, buscar perfiles personalizados por ID y por la clave de pertenencia correspondiente.
2. Si el ID ya está en la plantilla canónica, actualizar/enriquecer esa misma plaza con el perfil personalizado; nunca crear un duplicado.
3. Si el perfil pertenece al equipo pero su ID aún no aparece en la plantilla, incorporarlo al listado de plantilla de ese equipo mediante una operación determinista y auditable. No sustituir silenciosamente a otro futbolista ni perderlo: el sistema de alineaciones debe decidir titulares y reservas.
4. Un jugador puede pertenecer a un club y, de forma independiente, a una selección. Al cambiar de camiseta/equipo, conservar identidad, atributos y rasgos personales; cambiar solo los elementos dependientes de la equipación.
5. La selección nacional debe mostrar a sus jugadores personalizados en su plantilla y permitir convocarlos al amistoso. La preferencia nacional no debe confundirse con nacionalidad o país de nacimiento.
6. Si faltan datos, la clave no resuelve a un equipo válido o hay conflicto de IDs, mostrar advertencia y conservar el perfil en el catálogo; no asignarlo arbitrariamente.
7. El modo Amistosos debe cargar el mismo registro de jugador que usan el selector y la pantalla de plantilla. No mantener copias divergentes para el menú y el motor.

## Presentación 3D
- Transmitir al motor el ID del jugador y un perfil visual estructurado, además de sus atributos.
- Aplicar rasgos personales persistentes: modelo/rostro disponible, cabello, vello facial, accesorios y número/nombre de camiseta.
- Aplicar la equipación del equipo seleccionado (colores, diseño y brazalete) sin borrar los rasgos personales.
- Los elementos personales que sean invariantes (por ejemplo, botas propias o guantes) se conservan; los elementos definidos como dependientes de la equipación (medias, camiseta interior y brazalete) se adaptan a la camiseta actual.
- Si el modelo 3D de alta fidelidad aún no existe, mostrar el blockout disponible con los rasgos que el motor soporte. No declarar el modelo final implementado.

## Flujo de aceptación
- Un jugador personalizado aparece en la plantilla del club indicado por `club_id`.
- El mismo jugador aparece en la plantilla de su selección configurada cuando el ID de selección está resuelto.
- Aparece una sola vez por plantilla, con su ID estable.
- El selector de Amistosos muestra el perfil y su valoración/posición.
- Al iniciar el partido, el actor 3D recibe el perfil y los atributos correctos.
- Al cambiar entre club y selección, la identidad personal se conserva y la equipación cambia.
- El comportamiento funciona offline y no depende de Godot, Unity ni servicios remotos.

## Ejemplos de datos existentes que requieren resolver
- `NOVA-PLAYER-0035` (Anier Cumbá): `club_id=FN-ESP-REAL-MADRID`, `nationality=CUB`, `preferred_national_team=BRA`. Debe aparecer en el club identificado por su ID y en la selección preferida solo si `BRA` resuelve al ID oficial configurado. No añadirlo automáticamente a Cuba solo por nacionalidad.
- Otros perfiles personalizados deben seguir la misma regla por sus propios IDs y campos; no codificar excepciones por nombre.

## Estado técnico auditado
`CanonicalDataRepository` ya indexa perfiles por ID y aplica algunos atributos cuando encuentra ese mismo ID en una plantilla cargada. Sin embargo, no incorpora aún perfiles ausentes según `club_id`/selección, no resuelve la lista de selecciones nacionales al selector de Amistosos y no envía al renderer el nombre, número, ID ni los rasgos visuales/equipación. Por tanto, este requisito NO está completado.

## Siguiente implementación
1. Normalizar IDs de clubes y selecciones en el catálogo canónico.
2. Construir un resolvedor de pertenencia de perfiles personalizados para plantillas de clubes y selecciones, con detección de duplicados/conflictos.
3. Ampliar el modelo de plantilla y la pantalla de selección/alineación para incluir los perfiles resueltos.
4. Enviar perfiles identificados por ID al motor nativo y aplicar kit/rasgos visuales por jugador.
5. Añadir pruebas unitarias de pertenencia, duplicados, preferencia nacional y cambio de equipación; después compilar y probar el amistoso en Android.

No marcar como terminado hasta que se verifique el flujo de plantilla a partido 3D.

## Asignaciones nacionales confirmadas por el propietario — 2026-11-19

- `NOVA-PLAYER-0035` (Anier Cumbá): club `FN-ESP-REAL-MADRID`; selección Cuba (`CUB`, asociación `FIFA-MA-052`). Se corrigió `preferred_national_team` de `BRA` a `CUB` conforme a la confirmación del propietario.
- `NOVA-PLAYER-0036` (Máximo Padilla): club `FN-ESP-BARCELONA`; selección Cuba (`CUB`, asociación `FIFA-MA-052`).
- Raymond: selección Brasil (`BRA`, asociación `FIFA-MA-028`). Su perfil no aparece en `data/custom_players.json` de esta rama y no se localizó su ID estable; la asignación está confirmada pero pendiente de enlazar al perfil canónico. No inventar ni duplicar un registro.
- Estas asignaciones representan el canon del juego confirmado por el propietario, no convocatorias oficiales reales.

El manifiesto `data/national_teams_manifest.json` declara cero paquetes publicados y que las plantillas nacionales aún no se han construido. Por ello, las asignaciones de Anier y Máximo ya están guardadas en los perfiles y se muestran en la pantalla Inicio, pero Cuba y Brasil todavía no son equipos nacionales seleccionables ni se ha probado que los jugadores aparezcan en una plantilla nacional 22v22 dentro del amistoso. La integración jugable sigue pendiente.
