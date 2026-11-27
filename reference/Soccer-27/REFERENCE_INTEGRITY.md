# Verificación de integridad de la copia

Fecha: 2026-11-27.

## Resultado

Se comparó el árbol Git de origen `Wilberforcedev/Soccer-26-game` en el commit `f741b5d5ae3d531db367e1105023a7b39a1ffe3d` con los archivos de `reference/Soccer-27/` en este repositorio.

- Archivos fuente: 134
- Archivos de referencia coincidentes por SHA: 134
- Archivos fuente ausentes: 0
- Archivos con SHA diferente: 0
- Tamaño total declarado por el árbol fuente: 7341376 bytes
- Resultado: copia de referencia completa a nivel de archivos y contenido Git.

Los SHA Git de los 134 blobs coinciden exactamente; por tanto, también se verifican los contenidos binarios y JSON, no solo los nombres de archivo.

## Separación de trabajo

- `reference/Soccer-27/`: instantánea de referencia, no editar.
- `vendor/Soccer-27`: submódulo Git del proyecto original.
- El código y los recursos propios de NOVA deben crearse en una carpeta separada para mantener trazabilidad y facilitar futuras actualizaciones.

## Precaución legal

La coincidencia de contenido no implica autorización de redistribución. El README fuente declara Apache-2.0, pero debe verificarse el texto de licencia y cada licencia de terceros antes de distribuir o comercializar una versión derivada.
