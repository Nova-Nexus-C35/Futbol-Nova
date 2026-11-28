# Archivo histórico de datos de clubes originales

Estos archivos se conservan como instantáneas inmutables de la fuente histórica de Cumba35/Nova-Workspace.

- `original_club_players_2026_workspace_legacy.json`: perfiles históricos de los clubes Nova C35 y FC Río de Argenti.
- `original_clubs_2026_workspace_legacy.json`: definición histórica de ambos clubes y sus referencias de plantilla.

**Importante:** estos archivos NO forman parte del catálogo activo del runtime nativo y no deben cargarse automáticamente. Se conservan para comparar, recuperar atributos y resolver diferencias sin perder la fuente original.

La configuración activa está definida en `data/original_clubs_2026.json` y `data/players/nova_original_club_rosters_2026.json`. Los archivos históricos contienen IDs y perfiles que no son equivalentes uno a uno a los nuevos IDs ficticios `FN-P-*`; no se deben fusionar por nombre ni renumerar sin una decisión explícita.
