# Investigación técnica externa — realismo 3D y fútbol — 2026-11-25

## Objetivo

Mejorar Fútbol Nova hacia el estándar visual y de experiencia confirmado por el propietario (simulador 3D de fútbol con aspiración tipo FIFA/EA SPORTS FC), manteniendo Java 17, libGDX 1.14.2, Android ARM64, Android 9 / API 28, funcionamiento offline-first y prioridad absoluta al modo Amistosos.

## Repositorios públicos revisados

### 1. mgsx-dev/gdx-gltf — candidato principal para pipeline 3D
https://github.com/mgsx-dev/gdx-gltf

- Licencia Apache-2.0, comprobada en LICENSE.
- El README documenta glTF 2.0, múltiples animaciones, interpolación de keyframes, shape keys/morph targets, mapas normal, metallic/roughness, IBL/PBR, skybox y sombras.
- Integración libGDX mediante JitPack y artefacto com.github.mgsx-dev:gdx-gltf.
- El módulo gltf/build.gradle declara libGDX 1.9.11 y Java 7; el README indica que gdx-gltf 2.x requiere libGDX 1.9.11+. Esto sugiere compatibilidad de API, pero NO prueba compatibilidad binaria/Android con libGDX 1.14.2, Gradle actual, minSdk 28 ni GPU del teléfono.
- Decisión: candidato prioritario, pero no añadir todavía al APK principal. Crear primero prueba aislada de GLB pequeño, skinning/animación, PBR, liberación de recursos y rendimiento ARM64.

### 2. UXCentury/open-bone — no resuelve futbolistas 3D
https://github.com/UXCentury/open-bone

- Licencia MIT, comprobada en LICENSE.
- Runtime Java con esqueleto/skins, mezcla de animaciones y renderer libGDX; su README describe animación esquelética 2D estilo Spine/Rive.
- Decisión: no integrarlo para futbolistas 3D. Puede estudiarse para animación 2D de menús, pero no sumar dependencia ahora.

### 3. rblaurent/football-tackle-lab — referencia de animación, con reserva de derechos
https://github.com/rblaurent/football-tackle-lab

- El README describe personajes humanoides skinned, transiciones de movimiento, aproximación al balón, golpeo con contacto real, tackles, caídas, recuperación y celebraciones.
- El mismo README explica que parte de mallas/clips procede de recursos extraídos de juegos comerciales y que la licencia del código no concede derechos sobre esos datos.
- Decisión: estudiar ideas generales de estados/transiciones y contacto pie-balón; NO copiar ni redistribuir modelos, clips, datos extraídos ni assets. Crear recursos propios o usar assets con licencia explícita.

### 4. blurz17/blurz-native-football- — referencia Android, no integrar código
https://github.com/blurz17/blurz-native-football-

- README presenta juego Android ARM64 3D con SDL2, C++, OpenGL ES, ODE, JNI, HUD táctil, cámara y pipeline glTF/FBX.
- La revisión previa del proyecto registró GPLv3 / NOASSERTION en el API consultado.
- Stack distinto del runtime Java/libGDX actual; no incorporar GPLv3 sin decisión explícita de licencia.
- Decisión: referencia conceptual para controles, perfiles gráficos, cámara y telemetría; no copiar código, modelos ni dependencias.

### 5. awest813/League-Soccer — referencia de diseño de simulación
https://github.com/awest813/League-Soccer

- README presenta motor 3D, 22 jugadores, física del balón, IA táctica y árbitro.
- No se verificó una licencia de reutilización en esta revisión.
- Decisión: estudiar organización funcional y cobertura de reglas; no copiar ni integrar hasta comprobar licencia, procedencia y compatibilidad.

### 6. FlavioMili/Player12 — referencia de simulación compartida
https://github.com/FlavioMili/Player12

- README describe simulación compartida de movimiento, física del balón, fatiga, presión, entradas, porteros y reglas; stack C++23/SDL3/SQLite no es compatible directamente con Android Java/libGDX.
- No se verificó licencia de reutilización en esta revisión.
- Decisión: referencia conceptual de separación entre simulación y representación; no integrar código.

### 7. kylemath/soccerSim y Tobilion/footysim — referencias de lógica, no runtime Android
- https://github.com/kylemath/soccerSim: README describe IA contextual, física, formaciones y pruebas; Python/Flask, no se integra en el APK.
- https://github.com/Tobilion/footysim: motor headless 11v11 tick-driven, agentes, tácticas, física y estadísticas en Python. Útil para contrastar decisiones deterministas y pruebas; no es dependencia de producción Android.
- No se incorporó código ni datos.

## Decisiones de arquitectura

1. Mantener Java 17 + libGDX 1.14.2; no migrar a Unity/Godot ni cambiar a C++ sin autorización expresa.
2. Separar progresivamente simulación, representación de jugadores, animación, cámara, HUD y audio.
3. Adoptar glTF 2.0/GLB como formato objetivo si la prueba de gdx-gltf valida compatibilidad y rendimiento.
4. Mantener fallback procedural ligero y perfiles de calidad; no eliminarlo hasta validar modelos nuevos en Android 9 ARM64.
5. Modelos, texturas, uniformes, escudos, sonidos y animaciones deben ser originales o tener licencia explícita, verificable y registrada.
6. No usar extracción de assets comerciales aunque un repositorio público los incluya.

## Próximos trabajos priorizados

- [ ] Crear módulo/escena aislada de prueba para gdx-gltf 2.3.0 con GLB propio o CC0 claramente acreditado.
- [ ] Verificar skinning, clips, PBR, IBL, sombras, pérdida de contexto GL y dispose.
- [ ] Medir APK, memoria, FPS, temperatura y tiempos de carga en Android objetivo.
- [ ] Definir formato de jugador: esqueleto, nombres de huesos, escala, retargeting, estados y transiciones.
- [ ] Crear/obtener futbolista humanoide con licencia clara; registrar origen, licencia, atribución y restricciones.
- [ ] Implementar renderer desacoplado de MatchSimulation.
- [ ] Sustituir gradualmente PlayerVisual low-poly, manteniendo fallback.
- [ ] Rediseñar cámara/HUD y validar con capturas en dispositivo.
- [ ] Añadir pruebas de integración entre estado de partido, animación y contacto balón-pie.

## Cambios propios aplicados en esta revisión

FutbolNovaGame.drawHud() ahora calcula escala adaptativa por resolución, separa marcador/jugador/estado, compacta estadísticas en tres líneas con separación vertical y elimina una instrucción redundante. Conserva tarjetas, jugadores, tiros, tiros a puerta, paradas, entradas, pases, fueras de juego y posesión. No cambia reglas, IA, controles ni simulación.

## Verificación pendiente

- [ ] Ejecutar CI Android y pruebas unitarias tras el cambio HUD.
- [ ] Revisar HUD en distintas relaciones de aspecto y densidades; CI no prueba legibilidad en teléfono.
- [ ] Validar la prueba gdx-gltf antes de incorporar dependencia al runtime.
- [ ] Ejecutar prueba física completa de Amistosos en Android.


## Prueba concreta de pipeline GLB — 2026-11-25

### gdx-gltf 2.3.0 incorporado como renderer opcional

- Se verificó el tag público 2.3.0 de mgsx-dev/gdx-gltf y su módulo gltf/build.gradle.
- La dependencia se añadió al módulo Android y JitPack al repositorio de dependencias.
- El renderer nuevo GltfPlayerSquad carga un GLB local una sola vez, crea instancias de escena por jugador, selecciona clips por nombre (Idle/Stand, Walk/Jog, Run/Sprint), aplica posición/orientación desde MatchSimulation y renderiza mediante SceneManager PBR.
- FutbolNovaGame intenta cargar models/players/active.glb; si el archivo no existe o falla, mantiene PlayerVisual procedural sin bloquear el partido.
- El renderer es opcional y no descarga assets desde Internet.

### Fixture de prueba CC0

Se añadió app/src/test/resources/gltf/fixtures/quaternius-player.glb desde kendrekaran/striker-3d/assets/player.glb (blob original SHA 2faab1ed245bbe2b28153151d3b301913a216f69). Se comprobó su cabecera GLB 2.0, tamaño 498,212 bytes, 45 nodos, 1 skin con 31 articulaciones y 11 clips, incluyendo Man_Idle, Man_Run y Man_Walk. El archivo de licencia fuente confirma CC0 1.0 Universal y se conserva junto al fixture.

Repositorio fuente: https://github.com/kendrekaran/striker-3d
Licencia fuente: https://github.com/kendrekaran/striker-3d/blob/main/assets/LICENSE-quaternius.txt

**Límite importante:** el modelo Quaternius es low-poly. Solo sirve como fixture de integración y NO es el futbolista realista final. No se copia a assets de producción ni se activa por defecto.

### Verificación pendiente después de la integración

- [ ] CI resuelve JitPack y compila GltfPlayerSquad con libGDX 1.14.2.
- [ ] Pruebas JUnit verifican GLB 2.0, skin, clips Idle/Run y presencia del aviso CC0.
- [ ] Conseguir o crear un modelo humano de mayor fidelidad con licencia verificable y clips futbolísticos (control de balón, pase, tiro, entrada, caída y portero).
- [ ] Probar active.glb real en Android ARM64; validar shader PBR, límite de huesos por draw, texturas, orientación, escala, FPS, memoria, temperatura y context loss.
- [ ] Añadir retargeting o perfiles de clips para acciones futbolísticas; la primera versión solo mapea locomoción por nombres.
- [ ] Añadir animación de portero y sincronización pie-balón antes de activar el renderer como predeterminado.


### Corrección de coordenadas JitPack

La página de JitPack consultada para el tag 2.3.0 publica como coordenada recomendada `com.github.mgsx-dev:gdx-gltf:2.3.0`. Se corrigió app/build.gradle para usar esa coordenada oficial, en lugar de la coordenada histórica de submódulo documentada en el README del repositorio. La compilación CI del commit actualizado debe confirmar que el artefacto se resuelve realmente.


## Ara 3D Gen — pipeline realista de futbolistas — 2026-11-26

Repositorio público: https://github.com/olatunde85/3d-gen
Sitio y documentación: https://ara3dgen.com/ y https://ara3dgen.com/docs/cli/

Hallazgos comprobados en el README y documentación pública:
- Código MIT; los datos base MakeHuman se publican bajo CC0 1.0.
- Exporta GLB riggeado con detalle low/medium/high y permite seleccionar clips de animación por ID.
- El preset exacto `The Footballer` existe y ya define camiseta, pantalón corto y medias deportivas.
- El catálogo incluye animaciones deportivas con IDs como `soccer_shoot`, `soccer_pass`, `soccer_receive`, `soccer_header`, `soccer_chest_control`, `standing_tackle`, `slide_tackle`, `keeper_ready`, `keeper_catch`, `keeper_dive_left/right`, `throw_in` y `siu_celebration`.
- El CLI documenta export reproducible desde JSON y generación de modelos en CI, con versión de herramienta fijada y cache de datos.
- Los movimientos CMU pueden usarse en productos comerciales, pero sus datos no se pueden revender directamente, ni siquiera convertidos. Se debe conservar el crédito CMU en los avisos de distribución.

Decisión aplicada en Fútbol Nova:
- Dos especificaciones versionables: `Assets/Players/characters/home.json` (azul) y `away.json` (rojo), ambas basadas en `The Footballer`.
- Nuevo workflow `.github/workflows/generate-football-players.yml`: fija Ara CLI 0.18.1, genera GLB de detalle alto con locomoción y clips futbolísticos, valida GLB 2.0/skin/animaciones y guarda `home.glb` y `away.glb` en la rama de desarrollo.
- `GltfPlayerSquad` carga los dos assets y asigna los primeros 11 al equipo local y los siguientes 11 al visitante; mantiene locomoción y permite disparar clips de acción con bloqueo temporal para que no se cancelen al frame siguiente.
- `FutbolNovaGame` conecta TIRO, PASE y ROBO con `soccer_shoot`, `soccer_pass` y `standing_tackle`; los jugadores inactivos se ocultan conservando índices estables.

Referencias: https://github.com/olatunde85/3d-gen/blob/main/docs/animations.md ; https://github.com/olatunde85/3d-gen/blob/main/docs/cli.md ; https://github.com/olatunde85/3d-gen/blob/main/scripts/ASSETS_LICENSE.txt

**Límite:** los GLB de producción aún dependen de que el workflow se ejecute correctamente. Hasta que existan en la rama, pase CI Android y se prueben en dispositivo, el renderer conservará el fallback procedural y no se declara alcanzado el nivel visual FIFA.


### Actualización de versión Ara CLI

La versión 0.11.0 falló en CI con `Unknown animation "soccer_shoot"`. Se comprobó el paquete publicado en el repositorio oficial: `packages/cli/package.json` declara 0.18.1 y su changelog tiene fecha 2026-11-25. Ambos workflows se fijaron ahora en 0.18.1, que corresponde al catálogo deportivo actual. La validación CI queda pendiente.


### Corrección de dependencia gdx-gltf tras Android CI — 2026-11-26

La primera compilación con Ara GLB encontró clases duplicadas porque `com.github.mgsx-dev:gdx-gltf:2.3.0` resuelve el proyecto agregado, que incluye los módulos `desktop` e `ibl-composer` junto al runtime de libGDX. Se revisaron `README.md`, `settings.gradle` y `gltf/build.gradle` del tag público 2.3.0: el README oficial recomienda la dependencia del submódulo `com.github.mgsx-dev.gdx-gltf:gltf:2.3.0`. Se corrigió `app/build.gradle` a esa coordenada core, que depende de libGDX pero no añade los módulos desktop/IBL. Recompilación pendiente.
