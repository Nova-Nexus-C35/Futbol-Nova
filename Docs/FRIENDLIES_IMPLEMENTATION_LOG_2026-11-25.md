# Registro de implementación — Fútbol Nova Amistosos

Fecha: 2026-11-25  
Repositorio: `Nova-Nexus-C35/Futbol-Nova`  
Rama: `rebuild/libgdx-android-2026-11-22`  
Alcance: prioridad Amistosos; no modificar `main` ni fusionar PR #2.

## Cambio aplicado

### Controles táctiles visibles

Archivo: `app/src/main/java/com/novanexus/futbolnova/FutbolNovaGame.java`

- Añadido un joystick virtual visible en la zona izquierda.
- Añadidos botones visibles TIRO, PASE y ROBO en la zona derecha.
- La presentación usa las mismas zonas de pantalla que el input ya existente.
- El radio del joystick se limita a la altura/ancho disponibles para no dibujar un control fuera de pantalla.
- El indicador visual corrige la orientación Y entre coordenadas táctiles Android y coordenadas de render.
- La capa de controles solo aparece mientras el reloj permite acciones de juego.
- No se incorporó código, arte ni dependencia de terceros.

## Investigación pública

- `libgdx/gdx-ai`: Java/libGDX, steering, formaciones, pathfinding, árboles de comportamiento y FSM; LICENSE Apache-2.0 revisada. Candidato para una evaluación aislada futura, no dependencia adoptada.
- `ssenegas/kickoff`: Java/libGDX, referencia de arquitectura; licencia no verificada, sin reutilización.
- `erengaygusuz/fotbiler-football`: referencia de motor 3D/IA/reglas; licencia y procedencia pendientes, sin reutilización.

## Verificación

- Escritura de código confirmada por GitHub y lectura posterior del mismo archivo en la rama activa.
- Confirmados en el archivo import Matrix4, llamada a drawTouchControls, render de joystick/botones, ocultación fuera del juego activo y corrección del sentido vertical.
- CI de GitHub Actions para el commit `e4424a0dc3f3d1c99d08faa1be2e5cb5d355e77e`: en ejecución al redactar esta nota. La ejecución anterior del primer commit de la misma secuencia fue cancelada automáticamente por el nuevo push; no se toma como fallo de compilación.
- No se ha instalado ni probado el APK en el Android físico del propietario. No se afirma aceptación de controles ni jugabilidad completa.

## Próximas tareas P0

1. Confirmar CI del HEAD actual.
2. Validar controles en Android; ajustar tamaño/zonas y multitáctil según prueba real.
3. Auditar la pantalla de prepartido y selección de clubes/plantillas.
4. Corregir y probar la sincronización balón-jugador, posesión y movimiento del jugador controlado.
5. Validar reinicios, porteros, goles, pausa, descanso, final y segundo amistoso.
6. Mejorar IA y reglas con escenarios deterministas y pruebas unitarias.
7. Entregar APK debug verificable para instalación en el teléfono.

## Límites

Este cambio no completa el modo Amistosos. No modifica `main`, no cierra ni fusiona PR #2 y no declara jugabilidad verificada en dispositivo.


## Segundo bloque aplicado — validación de coordenadas

Archivo: `app/src/main/java/com/novanexus/futbolnova/MatchSimulation.java`

- `update` ahora rechaza frames cuando delta, coordenadas del balón o coordenadas del futbolista controlado no son valores finitos.
- El rechazo sucede antes de mover al futbolista, recalcular la IA o aplicar separación, evitando que NaN/infinito contamine las 22 posiciones y objetivos.

Prueba: `app/src/test/java/com/novanexus/futbolnova/MatchSimulationTest.java`

- Se añadió una prueba que inyecta NaN e infinitos en las coordenadas de balón y controladas, y verifica que posiciones y objetivos previos no cambian.

## Investigación complementaria

- `ssenegas/kickoff`: Java/libGDX, MIT verificada; referencia de arquitectura/entrada, sin código reutilizado.
- `Geomancer86/Futtoboru`: Java/libGDX, GPL-3.0 verificada; se excluye código por licencia/enfoque.
- `blurz17/blurz-native-football-`: C++/SDL2/ODE/JNI, README describe controles táctiles Android; LICENSE declara GPLv3, API informa NOASSERTION. Solo se toma como referencia conceptual, no se integra.
- `libgdx/libgdx`: Apache-2.0, framework base mantenido.

## Estado de CI y APK del bloque anterior

- La ejecución de Android Actions `45688820438`, intento 2, para el commit `2c576181b0a240fa9f0345eeb3734670dcfdccc0` terminó SUCCESS: tests, build ARM64, verificación del runtime nativo y subida del artefacto.
- Artefacto `futbol-nova-android-arm64-debug`, ZIP de 3,497,242 bytes, con `app-debug.apk` de 9,259,789 bytes. Expira el 2026-12-09. Este APK corresponde al bloque anterior de controles visibles; no contiene todavía el nuevo guard de coordenadas.
- El nuevo commit de validación de coordenadas requiere su propia ejecución de CI antes de considerar su APK listo.
- Ningún APK ha sido instalado ni probado físicamente por el propietario en esta sesión.


## Auditoría de sincronización y posesión — 2026-11-25

Archivos inspeccionados desde la rama activa:
- app/src/main/java/com/novanexus/futbolnova/FutbolNovaGame.java
- app/src/main/java/com/novanexus/futbolnova/MatchSimulation.java
- app/src/main/java/com/novanexus/futbolnova/MatchGameplay.java
- app/src/test/java/com/novanexus/futbolnova/MatchGameplayTest.java

Hallazgos:
- El movimiento táctil actualiza controlledPosition; MatchSimulation.update copia esa posición al futbolista controlado.
- El cambio de jugador, el cambio de lado en el descanso, la reconstrucción de alineaciones, la sustitución del jugador controlado y la expulsión del jugador controlado sincronizan la posición de control en los puntos inspeccionados.
- Mientras existe un poseedor válido, updateGameplay ancla el balón delante de ese futbolista. Los tiros/pases liberan el índice de posesión y establecen la velocidad del balón; los reinicios también limpian la posesión.
- La adquisición de posesión no rechazaba explícitamente NaN/infinito ni velocidad negativa. Se endureció MatchGameplay.acquirePossession para validar todas las coordenadas, altura y velocidad antes de calcular distancias.

Prueba añadida:
- MatchGameplayTest.possessionRejectsNonFiniteOrNegativeBallState cubre NaN, infinito, velocidad negativa y altura negativa. Se conserva la prueba existente para posesión de jugadores activos y excluye expulsados.

Investigación pública:
- atas76/openengine: README y LICENSE leídos; licencia MIT. Motor Java de partidos con enfoque contextual/data-driven. No se integra: el POM usa Java 18 y dependencias Maven (Spring/JDBC/SQLite/Jackson/Commons CSV), no un módulo Android/libGDX listo.
- modelence/open-soccer: referencia 11v11 en TypeScript/Canvas para separación de motor/render/tipos; no se integra en Java Android.
- No se copió código, assets ni dependencias de esos repositorios.

## Verificación posterior a esta actualización

- La ejecución CI 45689356818 terminó SUCCESS para el commit anterior 8235ca389d4a6d8c4973700706f69aaf04863fe4: pruebas, build Android ARM64 y verificación del runtime nativo completados.
- Los cambios nuevos de adquisición de posesión y sus pruebas se escribieron después de esa ejecución. Necesitan una ejecución CI propia; no atribuirles todavía el resultado SUCCESS anterior.
- Pendiente de pruebas de integración para continuidad en pases/tiros/robos/sustituciones/reinicios y de prueba física en Android.


## Segunda pasada de continuidad de posesión — 2026-11-25

Cambios de código:
- MatchGameplay.acquirePossession ahora omite slots nulos y jugadores inactivos o con coordenadas X/Z no finitas, evitando que una entrada dañada rompa el cálculo de distancias.
- MatchGameplayTest añade cobertura para listas con slots nulos/coordenadas inválidas y para el contrato de posesión después de una entrada exitosa, sustitución que conserva el índice de alineación y balón demasiado rápido que luego vuelve a velocidad controlable.
- La prueba de entrada establece orientaciones opuestas correctas para distinguir una entrada limpia de una falta.

Archivos:
- app/src/main/java/com/novanexus/futbolnova/MatchGameplay.java
- app/src/test/java/com/novanexus/futbolnova/MatchGameplayTest.java

Alcance:
- Son pruebas de reglas/contratos del motor de simulación; no son una prueba end-to-end de los métodos privados de FutbolNovaGame.
- El ciclo completo de pase, tiro, robo, sustitución, reanudación y gol todavía necesita pruebas de integración que conduzcan el estado del partido.
- El workflow .github/workflows/android-build.yml está configurado para ejecutarse al hacer push a esta rama. Este commit documental final también dispara una ejecución para validar todos los cambios anteriores de esta tanda.
- No se ha instalado ni probado el APK en un teléfono real.


## Cierre de corrección de pruebas y CI — 2026-11-25

- La ejecución 45691048752 identificó dos errores de las pruebas recién añadidas: se intentaba modificar la vista no modificable de getPlayers(), y el amistoso creado con start("home","away") no tiene suplentes para probar una sustitución.
- Se corrigieron las pruebas usando una copia mutable únicamente para simular slots nulos y dos plantillas de 14 futbolistas para disponer de suplentes reales en el escenario de sustitución. La vista de simulación se conserva para verificar que el reemplazo mantiene el índice estable.
- Commit verificado: 2ea93695f8769fce3eea7326a01704d308f4a603.
- CI final: [45691426001](https://github.com/Nova-Nexus-C35/Futbol-Nova/actions/runs/45691426001) — SUCCESS. Pasaron testDebugUnitTest y assembleDebug; el paso de verificación encontró lib/arm64-v8a/libgdx.so y el artefacto se subió correctamente.
- Artefacto: futbol-nova-android-arm64-debug, ID 15059376049, ZIP 3,497,329 bytes, SHA-256 8bfb2563f2c2491801aec06da0d71a9e6f951b6982d1e576c4f1f60a655661c3. APK dentro: 9,259,789 bytes. ZIP y APK se inspeccionaron; el APK contiene AndroidManifest.xml y lib/arm64-v8a/libgdx.so.
- Este CI valida la compilación y pruebas unitarias automatizadas. No demuestra el funcionamiento táctil ni el partido completo en un teléfono físico.
- Pendiente real: pruebas end-to-end de FutbolNovaGame para pase/tiro/robo/reanudación/gol, y prueba del APK en Android. No se marca Amistosos como terminado hasta completar ambas.


## Primera integración opcional de jugadores GLB — 2026-11-25

Investigación y dependencias:
- Revisado mgsx-dev/gdx-gltf, tag 2.3.0, licencia Apache-2.0; documenta glTF 2.0, animaciones, PBR/IBL y SceneManager.
- Añadido JitPack y dependencia com.github.mgsx-dev.gdx-gltf:gltf:2.3.0 al módulo Android.
- Referencia complementaria: kendrekaran/striker-3d usa un GLB riggeado CC0 con AnimationMixer y fallback procedural. Solo se adoptó la idea arquitectónica de cargar un modelo y conservar fallback; no se copió su código JavaScript.

Cambios de código:
- Nuevo GltfPlayerSquad.java: carga un GLB local, comparte SceneAsset, crea escenas por jugador, mapea clips por nombres Idle/Stand, Walk/Jog y Run/Sprint, aplica transformaciones desde coordenadas de MatchSimulation y renderiza con SceneManager/PBR.
- FutbolNovaGame.java intenta cargar models/players/active.glb. Si falta o falla, sigue con PlayerVisual procedural. Mantiene los transforms del fallback actualizados y captura errores de render GLB para volver al renderer procedural.
- app/src/main/assets/models/players/README.md documenta formato, escala/orientación, offline, límites iniciales de huesos y fallback.

Fixture y pruebas:
- Añadido app/src/test/resources/gltf/fixtures/quaternius-player.glb desde kendrekaran/striker-3d/assets/player.glb; SHA de origen 2faab1ed245bbe2b28153151d3b301913a216f69.
- Aviso de licencia CC0 1.0 incluido junto al fixture. Inspección del GLB: versión 2, 498212 bytes, 45 nodos, 1 skin de 31 articulaciones y 11 clips (incluye Man_Idle, Man_Run, Man_Walk).
- Añadido GltfPlayerFixtureTest para verificar cabecera GLB, skin, clips y aviso CC0.
- El fixture Quaternius es low-poly y se mantiene exclusivamente en recursos de test. NO es el arte realista final y no se activa en el juego.

Verificación y límites:
- El workflow Android CI se disparó en la ejecución 45700930177 para el commit de código 53f2dec1840c002ea6a6cab0cb400d4962dccf14. En el momento de este registro seguía en ejecución; no se afirma compilación aprobada hasta ver resultado.
- Pendiente: resolver cualquier error de JitPack/API, pasar pruebas, instalar APK y probar en Android ARM64 físico.
- Pendiente: conseguir/crear un futbolista humano de alta fidelidad con licencia clara y clips específicos de fútbol; el fixture actual solo demuestra estructura y licencia.
- Pendiente: validar maxBones/Shader en GPU real, uniformes/equipaciones distintas, animaciones de balón-pie, portero, caídas y celebraciones.
- No se declara completada la integración visual ni el modo Amistosos.

- Revisión de coordenadas JitPack: la página del tag 2.3.0 indica com.github.mgsx-dev:gdx-gltf:2.3.0; app/build.gradle se corrigió a esa coordenada oficial. La resolución queda pendiente del siguiente CI.


## Generación de futbolistas de detalle alto y dos uniformes — 2026-11-26

Investigación pública:
- Ara 3D Gen: https://github.com/olatunde85/3d-gen (MIT); datos MakeHuman CC0 1.0 y movimiento CMU con sus condiciones de uso.
- Confirmado preset `The Footballer`, export GLB de detalle alto, JSON de parámetros y selección de clips deportivos por ID.

Archivos añadidos:
- `Assets/Players/characters/home.json`: futbolista local con camiseta azul, shorts claros y número 10.
- `Assets/Players/characters/away.json`: futbolista visitante con camiseta roja, shorts blancos y número 10.
- `.github/workflows/generate-football-players.yml`: CLI Ara 0.18.1 fijado, cache de datos, export GLB de detalle alto, validación GLB 2.0/skin/clips, commit de los dos modelos en esta rama y artifact de inspección.

Código actualizado:
- `GltfPlayerSquad.java`: dos SceneAsset independientes para home/away; crea 11 instancias por equipo, usa maxBones combinado, aplica transformaciones de MatchSimulation, mantiene locomoción, soporta playAction con bloqueo temporal y oculta jugadores inactivos.
- `FutbolNovaGame.java`: carga ambos GLB solo si están disponibles; si falta cualquiera conserva el fallback procedural. Conecta TIRO/PASE/ROBO a `soccer_shoot`, `soccer_pass` y `standing_tackle`; conserva el fallback actualizado para recuperación en runtime.
- README principal y contrato de assets documentan rutas, pipeline y licencias.

Estado de verificación:
- Los JSON, workflow y cambios Java fueron escritos en la rama indicada. Falta leerlos de nuevo y verificar los SHA de todos los cambios.
- Pendiente ejecutar workflow Ara, revisar artefactos GLB, compilar Android y probar en teléfono real.
- El workflow no toca `main`; el commit automático de los binarios se dirige únicamente a `rebuild/libgdx-android-2026-11-22`.
- No se afirma que los GLB finales ya existan ni que el realismo FIFA esté terminado.


### Corrección del generador Ara tras CI — 2026-11-26

- La primera ejecución del workflow falló de forma reproducible porque Ara CLI 0.11.0 no conocía el ID deportivo `soccer_shoot`; los clips deportivos ya están presentes en el código fuente público actual, pero no en aquella versión publicada del CLI.
- Se inspeccionó `packages/cli/package.json` y `packages/cli/CHANGELOG.md` del repositorio oficial: la versión actual es 0.18.1 (2026-11-25), que incluye la evolución reciente del catálogo y exportador.
- Se actualizó la versión fijada de ambos workflows a 0.18.1 y la clave de caché. Se mantiene la lista de IDs deportivos, el preset The Footballer y el rig game_lite.
- Pendiente: el siguiente CI debe demostrar que los IDs se reconocen y que se generan ambos GLB; no se marca como resuelto hasta ver el resultado.

- CI con GLB generados falló en `:app:checkDebugDuplicateClasses`: la coordenada agregada `com.github.mgsx-dev:gdx-gltf:2.3.0` incorporó `desktop` e `ibl-composer` y duplicó clases libGDX. Se consultó el README/build del tag 2.3.0 y se corrigió a `com.github.mgsx-dev.gdx-gltf:gltf:2.3.0`, el submódulo runtime recomendado oficialmente. Pendiente confirmar con nuevo CI.


### Corrección de empaquetado Android — 2026-11-26

- Se leyó el log completo de la ejecución `45701139242`: el fallo ocurrió en `:app:mergeDebugJavaResource`, con tres recursos duplicados `net/mgsx/gltf/shaders/default.vs.glsl` de `desktop`, `ibl-composer` y `gltf` 2.3.0.
- Se modificó `app/build.gradle` añadiendo `android.packaging.resources.pickFirsts += ['net/mgsx/gltf/shaders/**']`, limitado al directorio de shaders de gdx-gltf para no descartar recursos generales ni clases.
- Commit de corrección: `24f851c816a6d7fee271cea86564aaedcf59f56e`.
- Pendiente: ejecutar CI posterior a este commit; comprobar `testDebugUnitTest`, `assembleDebug`, los tres archivos empaquetados (runtime ARM64 + home/away GLB) y cualquier conflicto adicional. La ejecución anterior sigue siendo FALLIDA y no valida esta corrección.


## Endurecimiento del renderer GLB — 2026-11-26

Archivo: `app/src/main/java/com/novanexus/futbolnova/GltfPlayerSquad.java`

- `updatePlayer` rechaza índices inválidos, coordenadas X/Z no finitas, orientación no finita, velocidad no finita o negativa antes de modificar la transformación de una instancia.
- `render` convierte delta no finito en cero y mantiene el límite de 50 ms para evitar contaminar el reloj de animación.
- Se conserva el sistema de animaciones, el mapeo desde MatchSimulation y el fallback procedural controlado por FutbolNovaGame.
- Commit: `f338f2815e2fa4af7845bd7ef3e48015bcac481f`.
- Relectura desde GitHub al commit confirma ambos guardas.
- GitHub Actions Android CI: run `45767673346`, estado `queued` al registrar esta nota. No atribuir todavía éxito de compilación ni de pruebas.
- Rama activa confirmada: `rebuild/libgdx-android-2026-11-22`; HEAD `f338f2815e2fa4af7845bd7ef3e48015bcac481f`. `main` sigue en `5fca4bfe000b79a82551c6204ff7c34e34af1c41`.

## Alcance todavía no validado

- No se ha confirmado aún que el workflow Ara haya generado y guardado `home.glb` y `away.glb`.
- No se ha verificado la calidad visual, escala, orientación ni compatibilidad real de los modelos.
- No se ha validado el render GLB en GPU/Android físico, el rendimiento de 22 jugadores ni el tamaño de APK.
- Los clips de fútbol se solicitan en el generador, pero falta comprobar sus nombres exactos, duración, retargeting y conexión con eventos de gameplay.
- El proyecto sigue siendo una vertical slice 3D en desarrollo, no un simulador con paridad FIFA.


## Resultado verificado Android CI 410 — 2026-11-26

- Workflow run: `45767716572` — conclusión `success`.
- Commit probado: `4453b9eb225872bc9402bc0cb06c6f3a8b94e022`.
- Job Android: success. Pasos completados: generación Ara de modelos local/visitante, validación GLB y clips, commit de assets, Java 17, Android SDK, pruebas unitarias, APK ARM64, verificación del runtime nativo y empaquetado de modelos.
- Job publish-release: success.
- Artefacto modelos: `futbol-nova-realistic-player-models`, 17,532,787 bytes, SHA-256 `5be62fb6efd591db3b7edffdc161f743212613d0c1bee5476e4d72e3f06359f2`.
- Artefacto APK: `futbol-nova-android-arm64-debug`, 20,022,862 bytes, SHA-256 `ddaf5b934e659eded47856f5af59a866a101276473483b207f141121791e6fcb`.
- Release permanente: [debug-410](https://github.com/Nova-Nexus-C35/Futbol-Nova/releases/tag/debug-410), asset `app-debug.apk`, 26,643,671 bytes, SHA-256 `562b8191ae591b85d4245c7a7454907efa74a21c82b8f9f9732706b3e69443ed`.
- Recursos de modelos local/visitante presentes en `app/src/main/assets/models/players/home.glb` y `away.glb`.
- Descarga Android: https://github.com/Nova-Nexus-C35/Futbol-Nova/releases/download/debug-410/app-debug.apk

## Interpretación y límites

CI demuestra que las pruebas unitarias pasaron, que ambos GLB y clips fueron generados/validados por el workflow, que el runtime ARM64 y los modelos se empaquetaron y que el APK se publicó. Esto NO demuestra por sí solo que el aspecto sea de nivel FIFA, que los 22 jugadores se animen correctamente en un partido real, ni que el rendimiento sea estable en un teléfono físico. Se mantiene pendiente la prueba visual y de gameplay en dispositivo Android, el contacto sincronizado balón/pie, la animación futbolística completa, el estadio detallado y la aceptación end-to-end del modo Amistosos.

## Integración de acciones GLB con eventos de partido — 2026-11-26

- Commit de código: `aac31714f600ce86ac61517330755fed6b499ad8`.
- `FutbolNovaGame` dispara `soccer_receive` al completar una recepción de pase; `standing_tackle` en las recuperaciones de balón de la IA; `soccer_shoot` y `soccer_pass` para tiros y pases de la IA.
- Las paradas del portero activan `keeper_catch` o `keeper_dive_left/right`; si no existe el clip direccional, se intenta `keeper_punch`.
- Los identificadores se cotejaron con la lista `FOOTBALL_CLIPS` del workflow Ara. La búsqueda por fragmento tolera prefijos/sufijos que añada el exportador.
- Relectura de GitHub tras el commit confirmó las cinco conexiones nuevas.
- El workflow Android CI se disparó por los cambios documentales posteriores; pendiente esperar y revisar sus jobs. No afirmar todavía que el nuevo código compile.
- Pendiente: animación de conducción/control, cabeceras, despejes, saques, caídas y celebraciones; comprobar la duración real y transición de los clips; sincronización exacta del pie con el balón; prueba en GPU y teléfono Android físico. No se declara FIFA-like ni Amistosos terminado.
## Integración ampliada del controlador GLB y cobertura de acciones — 2026-11-26

- Añadido `PlayerAnimationPolicy.java` con decisiones puras para locomoción, tackle de pie/deslizante, chest control/header y acciones de portero. Pruebas `PlayerAnimationPolicyTest.java` cubren umbrales y valores no finitos.
- `GltfPlayerSquad.java` ahora usa `AnimationController.action` de libGDX para acciones de una ejecución, evita que una acción sea sustituida por locomoción mientras está activa y usa `animate` con transición de 0.16 s para cambios Idle/Walk/Run. Se eliminó el temporizador manual aproximado.
- `FutbolNovaGame.java` conecta slide tackle según velocidad del defensor, control de pecho/cabezazo según altura del balón, throw-in con el jugador de campo más cercano y celebración de hasta seis jugadores del equipo anotador. Se conserva la simulación y el control de posesión actuales.
- Ambos workflows ahora validan los clips esenciales para evitar publicar un GLB que solo tenga tiro/pase pero carezca de acciones requeridas.
- Investigación adicional: `kendrekaran/striker-3d` inspira la separación simulación/representación y acredita el fixture Quaternius con CC0; no se copió su código Three.js. La API oficial de libGDX confirma el uso de `action` y `animate`.
- Límite identificado: el catálogo Ara consultado no ofrece clip específico de dribbling. No se simula que Run/Walk sea dribbling; se registra como tarea para un content pack propio/licenciado.
- Pendiente de CI para los commits nuevos; el éxito del proceso 414 corresponde al código anterior a esta ampliación. No declarar la nueva compilación hasta que el run del último commit termine.
- Pendiente validar el contacto pie-balón por fase de animación, duración/retargeting real, rendimiento de 22 instancias y calidad visual en teléfono Android físico.
### Corrección de coherencia del control aéreo — 2026-11-26

- Revisión de `MatchGameplay.acquirePossession` detectó un límite vertical de 1.1 m; el umbral anterior de header 1.65 m no podía dispararse nunca.
- Se ajustó `PlayerAnimationPolicy`: chest control desde 0.65 m, header desde 0.98 m y rechazo explícito por encima de 1.1 m. Se añadieron pruebas de límite inferior/superior.
- `soccer_jump_header` queda pendiente hasta que exista salto del jugador y contacto aéreo físicamente sincronizado; no se finge esa capacidad.
### Corrección crítica del selector de clips GLB — 2026-11-26

- Se detectó que `findClip` comparaba la etiqueta completa. Las acciones del jugador controlado (`Shoot (football)`, `Pass (straight)`) podían no coincidir con IDs exportados `soccer_shoot`/`soccer_pass`.
- `PlayerAnimationPolicy.clipNameMatches` normaliza separadores, elimina descriptores entre paréntesis y reconoce prefijos `soccer`, `keeper` y `goalkeeper`. Se conecta a `GltfPlayerSquad.findClip`.
- Se añadieron pruebas para shoot/pass, dive, standing tackle, null y falso positivo `stand`; el fallback Idle ya no usa `stand`.
- Ajuste adicional del selector: se bloqueó el falso positivo del alias `stand` contra `Standing tackle`; el alias solo acepta una pose `Stand`/`Stand idle` real. El código y la prueba de regresión quedaron guardados.
### Separación de generación de assets y CI Android — 2026-11-26

- `.github/workflows/generate-football-players.yml` conserva la generación manual, validación de clips y commit de los GLB.
- `.github/workflows/android-build.yml` ya no vuelve a generar modelos en cada push: valida los dos GLB versionados y ejecuta pruebas/compilación ARM64. Esto reduce tiempo y evita que la generación de arte cancele repetidamente la compilación de código.
- La ejecución de Android CI del nuevo workflow debe terminar sobre el último commit antes de dar la compilación por válida.
### Soporte condicional de animación de conducción — 2026-11-26

- `GltfPlayerSquad.updatePlayer` ahora recibe `carryingBall`, y prioriza un clip looping de dribbling cuando el poseedor se desplaza. Si no hay clip compatible, usa walk/run; no finge que una carrera genérica sea una animación de control de balón.
- `FutbolNovaGame` pasa `possessionOwnerIndex == i` para cada jugador y se añadió prueba del normalizador para `Soccer dribble with ball`.
- Pendiente de contenido: los modelos actuales no se han certificado con un clip de dribbling específico; crear/retargetear uno propio y sincronizar el toque con el balón.