# Fútbol Nova — estado y continuidad

## Repositorios conocidos
- Origen: Cumba35/Futbol-Nova
- Rama: work/friendly-match-completion-2026-11-20
- Espejo: Cumba35/Nova-Workspace
- Rama espejo: work/futbol-nova-friendly-match-completion-2026-11-20
- Ruta espejo: proyectos/juegos/futbol-nova/
Estos datos proceden del estado conservado al 2026-11-21; comprobar antes de nuevas escrituras.

## Prioridad del propietario
Completar un modo amistoso jugable de principio a fin. El propietario trabaja desde Android y necesita un APK instalable en su teléfono.

## CI #591 — 2026-11-21
- Run ID: 44942050664
- URL: https://github.com/Cumba35/Futbol-Nova/actions/runs/44942050664
- Commit: 46c9f1a67250aad6bfaa6744f3d704cbee98789c
- Job: 136961188239
- Resultado registrado: 61 pasos correctos.
- Godot 4.4.1; exportación Android ARM64.
- Pruebas mencionadas: importación Godot, entrada táctil, base 11 contra 11, amistoso en vivo con goles y períodos, disparos de IA, posicionamiento del portero, smoke test de escena Android, regresiones y validación de módulos opcionales.

## Artefactos
- APK/ZIP CI: futbol-nova-android-debug
- Artifact ID: 14722277336
- Tamaño: 29,752,378 bytes (aprox. 29.75 MB)
- SHA-256: 7b0b7964226f985ad914935e4aac5886149fe3aaa9f5aab946cd455687d13887
- Expiración comunicada: 2026-12-05 19:52 UTC
- ZIP contiene APK Android ARM64 de depuración.
- Fuentes: futbol-nova-godot-source, Artifact ID 14721828251.

## Límites de verificación
CI confirmó compilación/exportación y pruebas automatizadas; no confirma instalación ni funcionamiento en el teléfono físico. Instalar y probar el APK en Android sigue pendiente. No publicar release ni fusionar PR sin autorización.

## Dropbox conocido
Se había observado /Aplicaciones/Fútbol Nova/FutbolNova-debug.apk, tamaño 29,961,953 bytes (19-11-2026); no asumir que sea idéntico al artefacto CI #591. El propietario aprobó guardar el ZIP CI como Futbol-Nova-Android-ARM64.zip en esa carpeta, pero la subida no consta como completada.

## Documentación anterior
Se intentaron actualizar documentos en origen y espejo, pero la lectura posterior fue bloqueada por error 403 de cuenta suspendida. No considerarlos verificados aquí.

## Próximos pasos
1. Recuperar artefacto #591 mientras siga vigente.
2. Instalarlo en Android y registrar resultados físicos.
3. Corregir errores detectados.
4. Mantener el amistoso como prioridad.
5. Releer ramas y documentos de origen/espejo antes de modificar.
6. Registrar nuevas ejecuciones, commits, artefactos y pruebas distinguiendo CI de prueba física.

## Manifiesto de clubes y recursos gráficos

El manifiesto Paper de Dropbox del 2026-11-20 fue leído y transcrito en [ACTIVOS_Y_CLUBES.md](ACTIVOS_Y_CLUBES.md). Contiene el diseño de Nova Génesis (GEN), referencias de FC Río de Argenti (RDA), Nova C35, y la información sobre propuestas históricas y archivos pendientes.

Los binarios permanecen en Dropbox; esta documentación no significa que estén sincronizados con GitHub. No declarar las imágenes pendientes como cargadas ni modificar los diseños sin releer el manifiesto y los archivos de referencia.


## Perfil 3D del jugador y APK adicional
- [JUGADOR_3D_ANIER_CUMBA.md](JUGADOR_3D_ANIER_CUMBA.md) conserva el perfil NOVA-PLAYER-0035 y el estado del blockout GLB (sin rig, animación ni condición jugable).
- La copia FutbolNova-debug.apk de la biblioteca tiene SHA-256 distinto al artefacto CI #591; no asumir que son el mismo binario. Ver el documento del jugador para ambos hashes y la advertencia de comparación.


## Ejecución CI #590 — referencia histórica parcial
Una conversación del 2026-11-21 menciona que la ejecución #590 validó controles y cambio de futbolista, control individual y de equipo, IA de ambos equipos, movimiento, tiros/goles, porterías y estadio. No se recuperaron el run ID, commit, job, conteo de pruebas ni artefactos exactos; por tanto, no se presenta como una ejecución auditada aquí. La ejecución #591 conserva los identificadores y resultados registrados en la sección anterior.


## APK almacenado en Library — comprobación separada
Se materializó el archivo `FutbolNova-debug.apk` de Library (29.961.953 bytes). SHA-256 calculado: `797c2a7e37ea581e20ef92bab16557eac039b42a620e282596c9ce2e4a91328e`. Este hash no coincide con el artefacto CI #591 (`7b0b7964226f985ad914935e4aac5886149fe3aaa9f5aab946cd455687d13887`). No se puede afirmar que sea la misma compilación. No se instaló ni se probó físicamente.


## Snapshot adicional comunicado en conversación — 2026-11-21

En una conversación posterior al estado histórico de CI #591 se comunicó el siguiente punto de trabajo:
- Ramas: `work/friendly-match-completion-2026-11-20` y `work/futbol-nova-friendly-match-completion-2026-11-20`.
- PR #10 abierto.
- Copa de Cuba: usar códigos `CUB-` como referencia; país como alternativa; mantener exactamente 52 clubes.
- Commits citados: `9fd73bfe`, `ba77fb64`, `0f166850`, `bc41da6c`.
- Workflow #585 falló; #587 seguía ejecutándose en el momento del mensaje.
- APK final y validación pendientes.

**Estado histórico sin reconciliar:** esta información se conserva como snapshot de conversación y no sustituye la referencia anterior a CI #591 aprobada. Antes de continuar el desarrollo, consultar las ramas, PR #10 y ejecuciones de Actions en vivo para determinar qué commits y workflows son los más recientes. No fusionar ni publicar APK sin autorización expresa.
