# Plantillas iniciales de clubes personalizados — 2026-11-27

## Asignaciones confirmadas por el propietario

- Anier Cumbá → Nova C35.
- Máximo Padilla → Nova C35.
- Raymond → FC Río de Argenti (RDA).

## Integración realizada

- Se añadieron Nova C35 y FC Río de Argenti al principio del catálogo de plantillas que consume el selector de Amistosos.
- Cada equipo tiene una plantilla inicial de 22 jugadores para permitir titulares y suplentes.
- Anier Cumbá conserva su perfil existente y su GRL 92; su club asignado para esta plantilla pasa a ser Nova C35.
- Máximo Padilla y Raymond tienen fichas personalizadas iniciales y aparecen en sus clubes confirmados.
- Los demás espacios se completaron con jugadores genéricos claramente marcados como provisionales. No se reutilizaron futbolistas de otros clubes.
- Los datos espejo de `data/original` y `app/src/main/assets/data/original` se actualizaron de forma idéntica.

## Datos provisionales que requieren corrección

- Los 42 jugadores genéricos de relleno, sus posiciones, GRL 65, POT 70 y atributos son valores temporales generados para habilitar dos plantillas completas.
- Posición MC, GRL 65, POT 70 y atributos 65 de Máximo Padilla y Raymond son provisionales, no datos biográficos ni deportivos confirmados.
- Las posiciones, dorsales, atributos, edad, altura, peso, nacionalidad, apariencia, equipación y estadísticas de Máximo Padilla y Raymond siguen pendientes de revisión del propietario.
- No se han inventado estadísticas de partidos: los datos estadísticos reales siguen sin registrar.
- El ID de club interno es `FN-CUB-NOVA-C35` para Nova C35 y `FN-ARG-FC-RIO-DE-ARGENTI` para FC Río de Argenti.

## Alcance y limitaciones

Esta integración permite que el catálogo de datos ofrezca primero Nova C35 y FC Río de Argenti, y que ambos cumplan el mínimo de once jugadores que exige `TeamSelection`. No certifica que la presentación visual de escudos/equipaciones, la alineación táctica, los dorsales, las caras o las animaciones personalizadas estén terminadas ni probadas en Android físico.

## Archivos actualizados

- `data/original/cuba_players.json`
- `app/src/main/assets/data/original/cuba_players.json`
- `data/original/custom_players.json`
- `app/src/main/assets/data/original/custom_players.json`
- `app/src/test/java/com/novanexus/futbolnova/OriginalProjectDataTest.java`

Se añadió una prueba unitaria que comprueba el orden de los clubes y la presencia de Anier, Máximo y Raymond en sus plantillas.
