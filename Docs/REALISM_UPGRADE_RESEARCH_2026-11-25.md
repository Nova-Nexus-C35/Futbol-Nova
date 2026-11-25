# Investigación técnica externa — realismo 3D y fútbol — 2026-11-25

## Objetivo

Mejorar Fútbol Nova hacia el estándar visual y de experiencia confirmado por el propietario (simulador 3D de fútbol con aspiración tipo FIFA/EA SPORTS FC), manteniendo Java 17, libGDX 1.14.2, Android ARM64, Android 9 / API 28, funcionamiento offline-first y prioridad absoluta al modo Amistosos.

## Repositorios públicos revisados

### 1. mgsx-dev/gdx-gltf — candidato principal para pipeline 3D
https://github.com/mgsx-dev/gdx-gltf

- Licencia Apache-2.0, comprobada en LICENSE.
- El README documenta glTF 2.0, múltiples animaciones, interpolación de keyframes, shape keys/morph targets, mapas normal, metallic/roughness, IBL/PBR, skybox y sombras.
- Integración libGDX mediante JitPack y artefacto com.github.mgsx-dev.gdx-gltf:gltf.
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

- [ ] Crear módulo/escena aislada de prueba para gdx-gltf 2.1.0 con GLB propio o CC0 claramente acreditado.
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
