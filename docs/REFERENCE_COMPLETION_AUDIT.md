# Auditoría de finalización — Fútbol Nova

Fecha de auditoría: 2026-11-27
Repositorio: https://github.com/Nova-Nexus-C35/Futbol-Nova
Rama de trabajo: `work/futbol-nova-completion-audit-2026-11-27`
Base inspeccionada: `main`, commit `130d784e18589ad40cf260a1459c0b1582c63d47`

## Alcance y criterio

Esta auditoría inspecciona los archivos que forman la ruta nativa Android y los contratos de datos referenciados por el manifiesto. No equivale a una prueba física en un teléfono. Un sistema se considera implementado solo cuando existe código conectado al runtime; una entrada de catálogo, una pantalla informativa o una descripción no cuentan como funcionalidad terminada.

## Arquitectura confirmada

- Android nativo con Java 17.
- Simulación y renderizado en C++17.
- OpenGL ES 3.0.
- Android SDK 35, NDK 26.3.11579264, CMake 3.22.1 y Gradle 8.7 en CI.
- ABI configurada: `arm64-v8a`.
- Motor externo: ninguno. Unity y Godot quedan excluidos del runtime.
- Datos JSON canónicos en `data/`; el runtime lee el manifiesto `data/native_runtime_manifest.json`.
- Workflow de compilación: `.github/workflows/futbol-nova-native-android.yml`.

## Funciones que sí existen en el código inspeccionado

### Aplicación y navegación
- Pantallas Inicio, Online, Modos y Configuración.
- Selección de equipo local y visitante a partir de plantillas verificadas.
- Duración configurable por tiempo: 3, 5, 6, 8, 10, 15 o 20 minutos.
- Inicio, pausa, reanudación, descanso, segundo tiempo, final y revancha.
- Controles táctiles de movimiento, cambio de jugador, tiro, pase, regate/acción y presión.
- Marcador y reloj de partido.

### Prototipo de partido 3D
- Campo, líneas, porterías, estructuras de grada y balón renderizados con OpenGL ES.
- 22 actores procedurales con animación básica de carrera y celebración.
- Cámara que sigue al jugador controlado y al balón.
- Movimiento táctico básico de los actores no controlados.
- Movimiento básico de porteros, intentos de despeje/parada y colisiones del balón con jugadores y marco de portería.
- Detección básica de gol y secuencia de celebración/reinicio.
- Física simplificada de balón con gravedad, rebote, velocidad y límites del campo.

### Datos

La inspección de los tres archivos de plantillas cubanas confirmó **52 equipos y 1.144 futbolistas**: primera división 16/352, segunda división 16/352 y tercera división 20/440. Los 1.144 IDs son únicos entre las tres divisiones; cada jugador tiene 35 atributos enteros entre 0 y 99 y una posición registrada. Los tres archivos marcan los datos como ficticios.

El catálogo `data/custom_players.json` contiene dos perfiles adicionales. Sus IDs (`NOVA-PLAYER-0035` y `NOVA-PLAYER-0036`) no coinciden con los IDs de las plantillas cubanas, por lo que el método actual `applyTeam` no los aplica a las alineaciones cubanas. Se mantienen como perfiles catalogados, no como jugadores ya integrados en un club/plantilla nativa.

- Lector Android para datos JSON canónicos.
- Se rechazan plantillas con un número distinto de 22 jugadores o IDs vacíos/duplicados.
- Se cargan plantillas cubanas de primera, segunda y tercera división.
- Se aplican algunos atributos del perfil del jugador al runtime.
- Se muestran catálogos de habilidades, técnicas, balones, clubes, módulos y jugadores personalizados.

## Configuración personalizada recuperada y protegida — 2026-11-27

Se revisaron las fichas y decisiones guardadas antes de continuar el desarrollo:

- Fuente estructurada única: `data/custom_players.json`.
- Anier Cumbá (`NOVA-PLAYER-0035)): club `FN-ESP-REAL-MADRID` (nombre de juego: Albor Madrid), MCO/MC, GRL 92, POT 97, dorsal 35, capitán y especialización Velocista. Se conserva la separación entre `base_attributes` / `confirmed_attributes` y atributos efectivos con bonificación; no volver a sumar Velocista al GRL.
- Máximo Padilla (`NOVA-PLAYER-0036`): club `FN-ESP-BARCELONA` (nombre de juego: Litoral Barcelona), MCD/MC, GRL 94, POT 97, dorsal 30. Sus medidas de blockout siguen siendo provisionales y no se convierten en biografía confirmada.
- La confirmación del propietario del 2026-11-19 asigna a Anier y Máximo a Cuba (`CUB`). El JSON de Anier aún indicaba `BRA`; se corrigió a `CUB` en `data/custom_players.json` y se verificó la escritura. Esta preferencia no equivale a una convocatoria oficial.
- Se mantienen las preferencias visuales individuales registradas en las fichas; no se sustituyen ni se inventan rasgos.
- Los IDs de Anier y Máximo están enlazados en `data/spain_clubs_2026_27.json` a sus clubes por ID estable. Ambos clubes siguen con `status=draft_review_required`; esa referencia no es una plantilla completa ni los habilita para jugar.
- `data/national_teams_manifest.json` declara cero paquetes de selecciones publicados y plantillas nacionales aún no construidas. La preferencia CUB no crea una selección jugable ni una convocatoria.
- El runtime Android actual solo carga las tres fuentes de plantillas cubanas (52 equipos). No carga todavía las plantillas españolas de Albor Madrid y Litoral Barcelona. Por tanto, los perfiles personalizados siguen visibles en el catálogo, pero no se deben insertar artificialmente en una plantilla cubana ni anunciar como jugadores ya integrados en el amistoso nativo.

El validador `tools/validate_native_data.py` ahora comprueba que cada perfil personalizado apunte al club correcto mediante `club_id` y que el club contenga su ID estable en `player_ids`. También conserva la distinción entre preferencia nacional y convocatoria. La compilación/ejecución CI de este cambio queda pendiente de resultado.

## Pendiente confirmado

| Área | Estado real | Criterio para darla por terminada |
|---|---|---|
| Modelos de futbolistas | Procedurales y geométricos | Modelos originales con proporciones, equipaciones, rostro/silueta y LOD |
| Animaciones | Ciclos básicos | Locomoción, giro, aceleración, frenada, pase, tiro, salto, choque, caída, portero y transiciones sin cortes |
| Física del balón | Simplificada | Pruebas repetibles de fricción, efecto, bote, aire, contacto, postes, larguero y red |
| IA de campo | Movimiento y decisiones heurísticas básicas | Roles, formaciones, presión, cobertura, desmarques, posesión, transición, fatiga y dificultad |
| Porteros | Movimiento y colisión básicos | Posicionamiento, anticipación, blocaje, despeje, rechace dirigido, mano cambiada y animaciones |
| Reglas y árbitro | No implementados como sistema completo | Saques, banda, córner, saque de meta, faltas, tarjetas, penaltis, ventaja, fuera de juego y árbitro |
| Sustituciones | No implementadas | Banquillo, límites configurables, cambios y actualización de roles/atributos |
| Estadísticas | Marcador y reloj básicos | Posesión, tiros, tiros a puerta, pases, precisión, faltas, tarjetas, distancia y rendimiento individual |
| Guardado | No hay persistencia de carrera/partido | Guardado versionado, carga, migración de esquema y recuperación ante archivo inválido |
| Manager | Pantalla de catálogo, no modo funcional | Plantilla, calendario, tabla, economía, contratos, fichajes, entrenamientos y temporadas |
| Leyenda | No implementado | Carrera individual, progreso, objetivos, contratos, selección nacional y estadísticas |
| Ligas y copas | Datos/catalogación parcial | Calendarios, jornadas, clasificación, eliminatorias, desempates, ascensos y descensos |
| Online | Pantalla informativa | Protocolo, salas, sincronización, autoridad de partido, reconexión, seguridad y pruebas de red |
| Audio | No conectado como sistema de partido | Público, ambiente, silbato, balón, impactos, interfaz y mezcla configurable |
| Gráficos/cámaras | Renderizado base | Calidad escalable, cámaras seleccionables, sombras, iluminación, estadio y rendimiento |
| Prueba en dispositivo | No acreditada por esta inspección | Instalar y jugar en Android ARM64 real; documentar modelo, versión Android, FPS, fallos y consumo |

## Riesgos técnicos que deben resolverse

9. Los dos perfiles de `custom_players.json` no coinciden con IDs de las plantillas cubanas; debe definirse su club/competición y completar una plantilla válida antes de presentarlos como seleccionables en el partido.
10. Los catálogos de habilidades y técnicas están empaquetados, pero el motor C++ no los ejecuta todavía como un sistema de habilidades; el botón de regate/acción no equivale a integrar las 300 habilidades del catálogo.
11. La UI de Online y los módulos opcionales son informativos; no existe todavía una implementación de red ni un instalador genérico de módulos.



1. El runtime debe fallar de forma visible y recuperable si falta un JSON obligatorio; no debe continuar con catálogos parcialmente cargados como si fueran válidos.
2. La lista de equipos jugables debe excluir equipos repetidos y evitar que un equipo sea seleccionado simultáneamente en ambos lados.
3. Los atributos del jugador se aplican parcialmente: deben definirse y probarse los índices del vector de atributos, incluyendo atributos específicos de portero.
4. La simulación y la interfaz deben mantener una única fuente de verdad para el reloj, el estado del partido y los eventos.
5. La pausa/reanudación del ciclo de vida Android debe conservar o reiniciar el partido según una política explícita, sin dejar callbacks activos.
6. Los recursos OpenGL deben liberarse y reconstruirse correctamente al perderse el contexto gráfico.
7. Las reglas deben ser deterministas y comprobables con pruebas automatizadas; no se deben añadir reglas de fútbol mediante simples mensajes de interfaz.
8. El catálogo de módulos no debe mostrar como descargable/instalado un módulo que no tenga paquete, hash, versión y verificación de integridad.

## Orden de ejecución

### P0 — Integridad y estabilidad del partido base
- Validación estricta de datos y atributos.
- Pruebas de reloj, pausa, descanso, segundo tiempo, goles, reinicios y límites.
- Pruebas de colisión, portería y portero.
- Ciclo de vida Android y recuperación de contexto OpenGL.
- Compilación ARM64 reproducible.

### P1 — Partido completo
- Sistema de estados de balón y reinicios reglamentarios.
- Árbitro y reglas: banda, córner, saque de meta, faltas, tarjetas, penaltis y fuera de juego.
- Sustituciones y banquillo.
- IA táctica y dificultad.
- Estadísticas del encuentro.

### P2 — Presentación
- Modelos humanos originales y equipaciones.
- Animaciones de partido.
- Estadios, iluminación, cámaras, audio y ajustes de rendimiento.

### P3 — Modos y persistencia
- Guardado/carga.
- Manager.
- Leyenda.
- Ligas, copas y temporadas.

### P4 — Red
- Multijugador local/online después de estabilizar el simulador determinista.
- Pruebas de sincronización, reconexión y seguridad.

## Política de datos y licencias

- `data/` es la fuente canónica de datos de juego; no crear copias divergentes dentro de `native-android/`.
- Mantener IDs estables de clubes, competiciones y futbolistas.
- Los futbolistas y clubes ficticios deben seguir marcados como ficticios.
- No convertir estimaciones internas en datos reales ni atribuirlas a fuentes externas.
- No copiar código, escudos, equipaciones, modelos, música, nombres protegidos o recursos de terceros sin licencia compatible.
- La referencia visual se usa para estudiar estructura y usabilidad, no para copiar sus recursos.
- Ultimate Team permanece excluido.

## Verificación

La validación de datos existente está en `tools/validate_native_data.py`. El workflow actual compila y comprueba la APK y la biblioteca ARM64. La compilación CI no sustituye una prueba de juego en dispositivo real.

No marcar un apartado como terminado hasta adjuntar evidencia: prueba automatizada, resultado CI o prueba manual reproducible.


## Raymond — perfil pendiente de recuperar, no omitir — 2026-11-27

- Raymond debe figurar en el inventario de jugadores personalizados pendientes; no debe quedar excluido por no aparecer todavía en `data/custom_players.json`.
- La configuración confirmada disponible está en `docs/PLAYER_APPEARANCE_UPDATE_2026-11-16.md`: comparte con Anier el modelo de guantes; ambos usan camiseta interior ajustada de manga larga y medias por encima de las rodillas.
- En Anier se conserva la paleta de guantes negra con detalles rojos y dorados. Esa paleta es de Anier y no debe copiarse a Raymond.
- La documentación consultada indica expresamente que no se encontró una ficha canónica independiente de Raymond. Por tanto, no se inventan ID, posición, GRL, POT, atributos, nacionalidad, club, dorsal ni rasgos visuales adicionales.
- Pendiente: recuperar/localizar la ficha canónica de Raymond y, cuando esté disponible, incorporarlo a los datos estructurados y validar su representación 3D. La ausencia actual de la ficha no significa que Raymond no forme parte de la configuración del proyecto.
