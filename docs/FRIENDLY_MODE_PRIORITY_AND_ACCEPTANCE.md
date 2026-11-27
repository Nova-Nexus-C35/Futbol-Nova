# Prioridad de desarrollo: modo Amistosos

Fecha: 2026-11-19. Esta prioridad rige el siguiente ciclo de trabajo de Fútbol Nova.

## Decisión
El modo Amistosos es el único modo de juego prioritario hasta que pueda probarse un partido completo y estable en un teléfono Android. No se ampliarán Carrera, Manager, Ligas, Copas, TAS ni Online durante este ciclo, salvo que una dependencia sea imprescindible para el amistoso.

## Flujo de usuario que debe quedar operativo
1. Abrir Fútbol Nova sin conexión.
2. Entrar a Amistosos.
3. Elegir equipo local y visitante de las plantillas validadas.
4. Elegir estadio, duración por tiempo y perfil de partido (Auténtico/Competitivo).
5. Verificar alineaciones y formaciones.
6. Iniciar el partido 3D 11 contra 11.
7. Controlar al futbolista seleccionado con joystick y botones táctiles.
8. Jugar con balón, compañeros, rivales y porteros activos.
9. Ver reloj, marcador, eventos y pausas.
10. Marcar goles, reanudar desde el centro y completar los dos tiempos.
11. Mostrar resultado final y permitir revancha o volver al menú.

## Orden de implementación
### P0 — Bloqueadores de una prueba jugable
- Corregir orientación de cámara, controles, dirección de ataque y posesión.
- Implementar porteros con movimiento y colisiones reales.
- Completar pase, tiro, regate, robo, cambio de jugador y control defensivo.
- Mejorar IA de los 20 futbolistas sin control: apoyos, desmarques, marcaje, presión, repliegue y decisiones básicas.
- Reiniciar correctamente posiciones, saque y reloj tras goles y al descanso.
- Resolver goles, tiros fuera, saques de banda, esquina y saque de puerta.
- Evitar que el balón atraviese jugadores, postes o travesaño.
- Asegurar que el partido se puede pausar, reanudar, abandonar y reiniciar sin bloquear el renderizado.
- Integrar perfiles personalizados en sus clubes y selecciones por ID, para que estén disponibles al elegir plantillas y disputar amistosos.

### P1 — Partido de prueba completo
- Dos tiempos configurables con descanso y final.
- Selección de estadio funcional, aunque inicialmente haya pocos estadios.
- Formaciones y alineaciones visibles antes del inicio.
- Perfil Auténtico/Competitivo con reglas diferenciadas verificables.
- HUD: reloj, marcador, jugador controlado, energía y mensajes de eventos.
- Sustituciones básicas y cambio de futbolista.
- Estadísticas finales: posesión, tiros, tiros a puerta, faltas y pases.
- Guardado local del resultado y opción de revancha.

### P2 — Calidad visual y controles
- Sustituir blockout de primitivas por modelos 3D humanos originales de NOVA.
- Animaciones esqueléticas: quieto, correr, sprint, pase, tiro, cabeceo, entrada, caída y celebración.
- Cámara de retransmisión alternativa y ajustes de cámara.
- Mejorar iluminación, césped, redes, público, audio y rendimiento en móviles modestos.
- Opciones de accesibilidad y sensibilidad de controles.

## Criterios de aceptación para declarar Amistosos listo para pruebas
- Arranca sin conexión en Android ARM64 y no depende de Godot, Unity ni servicios remotos.
- Permite seleccionar dos plantillas distintas, ambas con 22 IDs únicos.
- Las plantillas muestran los perfiles personalizados correspondientes a su club/selección, sin duplicados ni asignaciones arbitrarias.
- El mismo ID y perfil personalizado se conserva desde la pantalla de plantilla hasta el actor del partido 3D.
- Renderiza 22 jugadores y dos porteros en el campo.
- Joystick y botones responden correctamente en orientación horizontal y distintos tamaños.
- El jugador controlado puede correr, girar, pasar, tirar y regatear con balón cercano.
- Los compañeros y rivales se mueven con decisiones básicas; los porteros defienden ambas porterías.
- Los goles solo cuentan cuando el balón cruza completamente la línea dentro de los postes y bajo el travesaño.
- Reinicia saque, posiciones y reloj de forma correcta.
- Completa dos tiempos, presenta el resultado y permite jugar otra vez.
- No se bloquea ni pierde el contexto al pausar, reanudar o salir.
- La APK de CI contiene la biblioteca nativa ARM64 y se instala y prueba físicamente.

## Límites de alcance
No se considera terminado por mostrar una escena 3D o por compilar. La meta es jugar y completar el amistoso de principio a fin. Las pantallas y modos restantes quedan congelados hasta superar los criterios anteriores. Las referencias FIFA 23 y FC 26 orientan estructura y sensación; arte, código, nombres y recursos de Fútbol Nova serán originales.

## Requisito añadido — jugadores personalizados (2026-11-19)
La integración de pertenencia y visualización se especifica en `docs/CUSTOM_PLAYERS_TEAM_AND_NATIONAL_SELECTION.md`. La tarea incluye clubes y selecciones, resolución por IDs estables, deduplicación y conservación de rasgos personales al cambiar la equipación. Está pendiente de implementación y prueba; no se considera satisfecha por la aplicación parcial de atributos existente.

## Avance implementado — 2026-11-19 (pendiente de CI y Android físico)

- Añadido cambio de futbolista mediante un control táctil dedicado; la selección recorre compañeros de campo según su proximidad al balón.
- El pase intenta encontrar un compañero adelantado y evita, con una comprobación geométrica inicial, las líneas de pase ocupadas por rivales.
- La IA de cada equipo asigna un perseguidor al balón, mientras el resto mantiene apoyos básicos de formación.
- El joystick y la presión conservan el ID del dedo que los activó; Java envía los movimientos de todos los punteros activos para que el multi-touch no dependa del dedo 0.
- Reinicio del estado de entrada al iniciar el motor.

No se da por validado hasta que termine el workflow nativo Android y se compruebe el funcionamiento real en un dispositivo. Siguen pendientes el robo/entrada, el control de posesión de los otros 20 jugadores, dos tiempos, descanso, saques y resultado final.

## Progreso del flujo de partido — 2026-11-19

Se incorporó selección de duración por tiempo (3–20 minutos), descanso automático, inversión de campo y sentido de ataque al comenzar la segunda mitad, y finalización automática con resultado y opción de revancha. Las transiciones usan la cola del hilo OpenGL para evitar modificar el estado del partido desde el hilo de interfaz mientras el motor renderiza.

La fase sigue pendiente de compilación CI del último commit y prueba en teléfono. No se declara terminado el amistoso hasta comprobar los dos tiempos, el cambio de lado, goles de ambos equipos, pausa, salida y revancha.

## Ajuste de física de posesión y comportamiento IA — 2026-11-19

Se corrigió un defecto estático por el que el control de balón podía cancelar inmediatamente un pase o tiro. El motor conserva ahora una ventana corta de balón liberado y permite que la trayectoria avance. Los jugadores IA pueden intentar un pase, conducir el balón o tirar al acercarse al área. No se considera validado hasta CI y prueba física; faltan entradas/robos, marcaje táctico, decisiones de equipo y más reglas.


## Secuencia 3D de gol con salto manual — 2026-11-20

- Al marcar el equipo local o visitante, el motor conserva el marcador y activa una secuencia 3D sin reiniciar instantáneamente las posiciones.
- Fase 1 (3,4 s): los diez jugadores de campo del equipo anotador convergen hacia la zona de celebración con brazos elevados y pequeños saltos; los diez jugadores de campo que recibieron el gol regresan caminando a sus posiciones de saque en torno al centro del campo. Los porteros permanecen en sus áreas.
- Fase 2 (2,8 s): los jugadores de campo terminan de colocarse en sus posiciones de saque. Después se recolocan ambos porteros y el balón para el saque central.
- El botón táctil «SALTAR» permanece disponible durante toda la secuencia. Al pulsarlo, el motor finaliza la animación inmediatamente y ejecuta el reinicio completo del saque.
- La secuencia no pausa ni modifica el cronómetro del partido; depende del evento de gol y no del formato con que la interfaz presente el reloj. El control de salto funciona durante el juego normal en ambos sentidos de marcador.
- Se mantiene la lógica de pausa: si el usuario pausa el partido, la animación también se detiene y puede saltarse al reanudar o desde el control visible.
- Implementación en `native_engine.cpp` y `MainActivity.java`; commits: `e68c4190e3817deac6793ff9ddca2c5e0effc4a2` y `7b1401a7e327b1e515ad1461ab7b943fe8ecd11e`.
- Verificación estática: se releen ambos archivos desde GitHub y se comprueba la activación tras gol local/visitante, la animación de celebración, el método JNI de salto, el botón Android y su visibilidad según el estado nativo.
- Pendiente: compilación CI de estos commits y prueba física en Android. Debe comprobarse gol local, gol visitante, salto al inicio/mitad/final de la secuencia, reloj avanzando, pausa/reanudación y saque correcto.
