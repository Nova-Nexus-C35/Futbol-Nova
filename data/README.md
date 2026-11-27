# Fútbol Nova — catálogo original de datos

Esta carpeta conserva una copia íntegra del catálogo de datos fuente de Nova Workspace:

- Fuente canónica: `Nova-Nexus-C35/Nova-Workspace`, rama `main`, ruta `Projects/Games/futbol-nova/data/`.
- Destino de preservación: `Futbol-Nova/data/original/`.
- Archivos: **75** (JSON, CSV y README de subcatálogos).
- Los contenidos se copian sin transformar; sus SHA de blob Git deben coincidir con los de la fuente.

## Contenido

- Catálogos de ligas, clubes y competiciones nacionales, continentales e internacionales.
- Directorios globales, cobertura, niveles y estado de importación de clubes.
- Plantillas y perfiles ficticios por país/temporada, además de datos cubanos.
- Habilidades, técnicas gestuales, economía, balones, cantera y desafíos.
- Esquemas de base de datos, paquetes descargables, selecciones y módulos opcionales.
- Auditorías históricas y políticas de identidad de clubes.

## Importante: preservación no equivale a integración

Estos archivos son la copia de fuente para conservar y portar los datos por fases. No se empaquetan automáticamente en el APK ni significa que cada liga, jugador, módulo o competición esté conectado al runtime Java/libGDX o sea jugable. El subconjunto de 15 JSON que ya existía en `app/src/main/assets/data/original/` sigue siendo el contenido empaquetado actualmente. Los demás datos quedan disponibles para futuras migraciones y paquetes opcionales, manteniendo ligero el APK base.

No editar esta copia de forma aislada: la fuente de verdad continúa siendo Nova Workspace. Si se actualizan datos, reconciliar ambos repositorios y verificar los SHA de cada archivo.
