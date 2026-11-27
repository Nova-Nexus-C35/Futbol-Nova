# Auditoría de integración — Soccer 27

Fecha de revisión: 2026-11-27.

## Repositorios y permisos

- Destino: `Nova-Nexus-C35/Futbol-Nova` (cuenta GitHub identificada como Primary).
- Fuente: `Wilberforcedev/Soccer-26-game`.
- Rama fuente consultada: `main`.
- Commit fuente fijado: `f741b5d5ae3d531db367e1105023a7b39a1ffe3d`.
- Árbol fuente consultado: `d60c691e964f42385cf7f6766369a931e12c3252`.
- La rama principal del destino contenía únicamente `README.md`; se conserva el repositorio y se prepara una rama de integración separada.

## Capacidades documentadas en la fuente

- Proyecto activo: Unity 3D en `unity/FC2026Unity`.
- Unity 6.3 LTS, editor `6000.3.24f1`.
- Campo, estadio, iluminación y 22 jugadores físicos.
- Física Rigidbody del balón; pases, tiros, entradas y comportamiento de portero.
- IA posicional, cámara dinámica de retransmisión, marcador y detección de goles.
- Controles táctiles Android y controles de teclado.
- Equipos, jugadores, ciudades y uniformes ficticios.
- Carrera determinista con ligas, copa, fichajes, scouting, contratos y partidas guardadas.
- El autor advierte que IA y física continúan en mejora y que Android requiere pruebas de juego y perfilado.

## Hallazgos de licencia

El README fuente declara Apache-2.0 y advierte que marcas, nombres, semejanzas y recursos de terceros tienen derechos independientes. En el commit auditado no se encontró un archivo `LICENSE` en la raíz. La declaración del README no sustituye la verificación del texto de licencia ni de las licencias de paquetes y recursos.

Decisión conservadora: incorporar inicialmente el proyecto como submódulo Git fijado a un commit, sin copiar, renombrar ni alterar sus archivos. Antes de redistribuir el código integrado o hacer una publicación comercial, verificar el texto de licencia con el autor y revisar los terceros.

## Integración aplicada

- Submódulo: `vendor/Soccer-27`.
- URL: https://github.com/Wilberforcedev/Soccer-26-game.git
- Commit: `f741b5d5ae3d531db367e1105023a7b39a1ffe3d`.
- El repositorio original no se modifica.
- No se reemplaza ni elimina ningún archivo de la fuente.
- El README de Fútbol Nova se actualiza para explicar cómo inicializar y abrir el submódulo.

## Pendiente de validar

1. Inicializar el submódulo en un clon limpio.
2. Confirmar que todos los paquetes Unity se resuelven.
3. Revisar escenas, scripts, assets y licencias de terceros.
4. Configurar un runner Unity con licencia almacenada en GitHub Secrets.
5. Ejecutar build Android ARM64 y pruebas automáticas.
6. Instalar y probar el APK en un dispositivo real.
7. Adaptar identidad, UI, nombre de paquete y branding a NOVA.
8. Mejorar y medir rendimiento, IA, física, animaciones y consumo de memoria.

No declarar APK, partido completo o publicación listos hasta completar las pruebas.
