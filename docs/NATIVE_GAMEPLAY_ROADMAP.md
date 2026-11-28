# Hoja de ruta técnica — Fútbol Nova nativo

Este documento convierte la auditoría de 2026-11-27 en tareas verificables. El runtime oficial es Java 17 + C++17 + OpenGL ES 3.0. Unity y Godot no se utilizarán como motores de ejecución.

## Convenciones de estado

- `implemented`: existe código conectado y una prueba lo verifica.
- `prototype`: existe una primera implementación, aún incompleta.
- `planned`: no existe implementación funcional.
- `blocked`: depende de una decisión, recurso o sistema anterior.

No cambiar el estado a `implemented` por crear una pantalla, un JSON o una descripción.

## P0 — Base estable

- [ ] Integrar perfiles personalizados por ID estable y `club_id`, conservando todas las preferencias confirmadas. Anier (`NOVA-PLAYER-0035`) pertenece a Albor Madrid (`FN-ESP-REAL-MADRID`) y Máximo (`NOVA-PLAYER-0036`) a Litoral Barcelona (`FN-ESP-BARCELONA`); ambos tienen preferencia nacional CUB registrada. No asignarlos a clubes/selecciones por nombre, nacionalidad o preferencia solamente.
- [x] Configurar el amistoso predeterminado como Nova C35 (`FN-NOVA-C35`) vs FC Río de Argenti (`FN-NOVA-RDA`): ambos rosters de exhibición contienen 22 jugadores y están habilitados en el runtime nativo. La pareja Albor Madrid vs Litoral Barcelona queda pendiente como trabajo separado hasta completar sus plantillas españolas de 22 jugadores; no sustituirla silenciosamente ni alterar los clubes activos de Anier y Máximo.
- [ ] Definir el contrato de atributos entre JSON, Java y C++ y comprobar especialmente las seis estadísticas de portero.
- [ ] Conectar el catálogo de habilidades/técnicas a acciones de juego reales o mantenerlas claramente desactivadas hasta su integración.



- [ ] Validación estricta de todos los catálogos canónicos.
- [ ] Pruebas de selección de dos equipos distintos.
- [ ] Pruebas de atributos y perfiles personalizados por ID estable.
- [ ] Pruebas de reloj, pausa, descanso y segundo tiempo.
- [ ] Pruebas de gol, reinicio y cambio de lado.
- [ ] Pruebas de colisión con jugadores, porteros, postes y larguero.
- [ ] Recuperación ante pausa/resume y pérdida de contexto OpenGL.
- [ ] Compilación CI ARM64 y registro del artefacto.

## P1 — Reglas del fútbol

- [ ] Máquina de estados de reinicio: saque inicial, banda, córner, saque de meta, tiro libre y penalti.
- [ ] Fuera de juego con posición, participación e interferencia.
- [ ] Faltas y ventaja.
- [ ] Tarjetas amarilla/roja y expulsiones.
- [ ] Árbitro con posicionamiento y señales.
- [ ] Sustituciones y banquillo.
- [ ] Estadísticas de partido y por futbolista.

## P1 — Simulación e IA

- [ ] Formaciones configurables.
- [ ] Roles y responsabilidades por posición.
- [ ] Fases: posesión, transición ofensiva, transición defensiva y defensa organizada.
- [ ] Presión, cobertura, marcaje, desmarque y apoyo.
- [ ] Decisión contextual de pase, conducción, tiro y despeje.
- [ ] Dificultad sin bonificaciones injustas.
- [ ] Fatiga, resistencia y sustitución por rendimiento.
- [ ] Portero con anticipación, blocaje, rechace y despeje contextual.

## P2 — Animación y presentación

- [ ] Esqueleto y modelos humanos originales.
- [ ] Equipaciones y variación visual por equipo.
- [ ] Animaciones locomotoras y deportivas.
- [ ] Transiciones de animación y prevención de deslizamiento.
- [ ] Estadios y elementos de ambiente.
- [ ] Cámaras seleccionables.
- [ ] Audio ambiental y efectos.
- [ ] Presets gráficos y medición de FPS/memoria.

## P3 — Guardado y modos

- [ ] Guardado local versionado.
- [ ] Carga y validación de partidas.
- [ ] Calendario y clasificación.
- [ ] Ligas y copas.
- [ ] Manager: plantilla, fichajes, economía, contratos, entrenamiento y temporadas.
- [ ] Leyenda: carrera individual y progreso.
- [ ] Migraciones de datos guardados entre versiones.

## P4 — Online

- [ ] Definir protocolo y autoridad de simulación.
- [ ] Salas y emparejamiento.
- [ ] Sincronización y tolerancia a latencia.
- [ ] Reconexión y abandono.
- [ ] Seguridad y validación de acciones.
- [ ] Pruebas multijugador antes de publicar el módulo.

## Reglas de integración en GitHub

1. Todo cambio se realiza en una rama de trabajo descriptiva.
2. Antes de editar un archivo existente, leer su versión actual y conservar su SHA.
3. Guardar cada dato en su ubicación canónica:
   - Datos deportivos y manifiestos: `data/`.
   - Código Android Java: `native-android/app/src/main/java/`.
   - Código C++/OpenGL/JNI: `native-android/app/src/main/cpp/`.
   - Pruebas y validadores: `tools/` o la carpeta de pruebas correspondiente.
   - Documentación técnica y decisiones: `docs/`.
   - Compilación y automatización: `.github/workflows/`.
4. No duplicar datos canónicos en assets o catálogos alternativos; el empaquetado debe consumir `data/`.
5. No sobrescribir ni borrar la referencia histórica; cualquier depuración requiere verificar dependencias y conservar trazabilidad.
6. Cada cambio debe tener un commit con mensaje que describa la función o corrección.
7. Ejecutar validación de datos y compilación CI después de cambios de código/datos.
8. No declarar una función terminada hasta tener pruebas y evidencia.
9. No afirmar que una APK fue probada en teléfono si solo se compiló en GitHub Actions.
10. Mantener README, manifiesto y esta hoja de ruta sincronizados con el estado real.

## Fuente de verdad

- Código y datos: este repositorio GitHub.
- Contrato del runtime: `data/native_runtime_manifest.json`.
- Estado y tareas: este documento y `docs/REFERENCE_COMPLETION_AUDIT.md`.
- Compilación: `.github/workflows/futbol-nova-native-android.yml`.
