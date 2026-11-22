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
2. Crear una escena vacía y guardarla como `Assets/Scenes/Main.unity`.
3. Añadir un GameObject vacío llamado `FutbolNovaBootstrap`.
4. Añadir el componente `FutbolNovaBootstrap` desde `Assets/Scripts/Bootstrap`.
5. Pulsar Play; el bootstrap construye el campo y el partido.
6. Para Android, cambiar a Android en Build Profiles, seleccionar ARM64 y probar en dispositivo. La configuración final y el rendimiento aún no están validados.

## Controles
- WASD / flechas: mover.
- Shift: sprint.
- J: pase.
- K: tiro.
- En pantalla: pad direccional, sprint, pase y tiro.

## Arquitectura inicial
- `Core`: estado y reglas básicas de partido.
- `Gameplay`: jugadores, balón, goles e IA inicial.
- `Input`: entrada teclado/táctil.
- `Bootstrap`: generación del mundo de prueba.

## Pendiente
Animaciones y modelos reales, estadio, audio, cámara broadcast avanzada, física refinada, porteros, faltas, fuera de juego, árbitro, sustituciones, IA táctica, menús, plantillas autorizadas, modos Manager/Leyenda/Entrenador, guardado, torneos, cantera, economía, módulos, Nova Nexus, pruebas automatizadas, CI y pruebas en teléfonos Android.

## Procedencia y licencias
Este primer código de Fútbol Nova se ha escrito como implementación nueva. No incorpora código ni assets de los repositorios investigados. Sus descripciones públicas se usaron como referencia para definir capacidades y arquitectura. Antes de importar cualquier componente externo se comprobarán licencia, dependencias, avisos y compatibilidad.

La licencia MIT provisional cubre únicamente los archivos originales de este repositorio. No concede derechos sobre marcas, nombres, escudos, uniformes, retratos ni otros derechos de terceros.
