# Fútbol Nova

Base inicial reconstruida desde cero como prototipo Unity 3D orientado a Android.

## Estado actual
Esta rama contiene una primera base jugable de prueba, no un juego terminado ni una adaptación validada de todos los sistemas investigados. El código nuevo es una implementación limpia de Fútbol Nova; los repositorios públicos estudiados se usan como referencias arquitectónicas y funcionales. No se ha copiado código ni assets de terceros.

## Tecnología
- Unity 6; proyecto configurado inicialmente para 6000.3.24f1.
- C#.
- Android ARM64 como destino previsto.
- Escena de partido generada proceduralmente, sin assets externos.

## Primera vertical slice
- Campo 3D, porterías, balón y dos equipos de 11 jugadores generados en tiempo de ejecución.
- Movimiento del jugador seleccionado, sprint, pase y tiro.
- IA inicial de compañeros y rivales.
- Marcador y reloj de partido.
- Teclado para pruebas en editor y controles táctiles simples.
- Reinicio después de gol.

## Cómo probar
1. Abrir esta carpeta desde Unity Hub con Unity 6000.3.24f1 o Unity 6 compatible.
2. En Unity, ejecutar el menú `Futbol Nova > Generate Bootstrap Scene`; crea `Assets/Scenes/Main.unity` y registra la escena en Build Settings.
3. Pulsar Play; el bootstrap construye el campo y el partido.
4. Para Android, usar el flujo de CI descrito abajo o cambiar a Android en Build Profiles y seleccionar ARM64.

## Controles
- WASD / flechas: mover.
- Shift: sprint.
- J: pase.
- K: tiro.
- En pantalla: pad direccional, sprint, pase y tiro.

## Pruebas y compilación automatizadas
- `Assets/Tests/EditMode/MatchStateTests.cs` contiene pruebas EditMode del reloj, pausa, finalización y marcador.
- `Assets/Scripts/FutbolNova.Runtime.asmdef` y `Assets/Tests/EditMode/FutbolNova.EditModeTests.asmdef` separan el código ejecutable y las pruebas.
- El flujo manual `.github/workflows/unity-ci.yml` ejecuta las pruebas y, si se solicita, genera un APK Android.
- Para ejecutar el flujo, configura el secreto de repositorio `UNITY_LICENSE` con una licencia válida de Unity y usa Actions > Unity validation and Android build. Marca `build_android` para solicitar el APK.
- El script `Assets/Editor/FutbolNovaBuild.cs` genera la escena de arranque y configura arquitectura Android ARM64.
- La CI está configurada, pero todavía no se ha ejecutado; no hay resultados de pruebas ni APK verificado.

## Arquitectura inicial
- `Core`: estado y reglas básicas de partido.
- `Gameplay`: coordinación del partido (`MatchManager`), construcción procedural (`MatchSceneFactory`), jugadores, balón, goles e IA inicial.
- `Input`: entrada teclado/táctil.
- `Bootstrap`: generación del mundo de prueba.

## Pendiente
Animaciones y modelos reales, estadio, audio, cámara broadcast avanzada, física refinada, porteros, faltas, fuera de juego, árbitro, sustituciones, IA táctica, menús, plantillas autorizadas, modos Manager/Leyenda/Entrenador, guardado, torneos, cantera, economía, módulos, Nova Nexus y pruebas en teléfonos Android.

## Procedencia y licencias
Este primer código de Fútbol Nova se ha escrito como implementación nueva. No incorpora código ni assets de los repositorios investigados. Sus descripciones públicas se usaron como referencia para definir capacidades y arquitectura. Antes de importar cualquier componente externo se comprobarán licencia, dependencias, avisos y compatibilidad.

La licencia MIT provisional cubre únicamente los archivos originales de este repositorio. No concede derechos sobre marcas, nombres, escudos, uniformes, retratos ni otros derechos de terceros.

## Configurar CI y licencia Unity
Consulta [`Docs/UNITY_CI_SETUP.md`](Docs/UNITY_CI_SETUP.md) para activar la licencia de Unity, ejecutar las seis pruebas EditMode y solicitar el APK Android ARM64. El workflow de solicitud puede generar un `.alf` desde GitHub Actions, pero Unity puede no aceptar activación manual Personal; no se garantiza una licencia hasta obtener el `.ulf` válido.
