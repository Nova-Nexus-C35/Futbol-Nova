# Fútbol Nova

Repositorio propietario del videojuego Fútbol Nova.

## Estado técnico observado

Existe un primer prototipo Android 3D en Java/libGDX, con geometría procedural para campo, jugadores y balón, cámara en perspectiva, controles táctiles básicos, física simplificada del balón, marcador y reloj. El commit de prototipo es `196c16e9bd0589de03c49542300bb1e5965b47dd`.

Este prototipo es una primera vertical slice, no un juego completo ni prueba de que todos los modos estén implementados.

## Reconstrucción pendiente

- Inventariar árbol completo, dependencias, recursos, escenas y pantallas.
- Completar bucle de partido, IA rival, equipos, alineaciones, reglas, faltas, saques, árbitro, sustituciones y animaciones.
- Implementar modos definidos en los requisitos del proyecto, progresión, economía y guardado.
- Añadir pruebas unitarias/instrumentadas, rendimiento, compatibilidad Android y pruebas de jugabilidad.
- Verificar workflows, builds reproducibles y artefactos instalables.
- Integrar recursos externos solo con licencia compatible y procedencia registrada.

## Referencias

- [Nova Workspace — índice maestro](https://github.com/Nova-Nexus-C35/Nova-Workspace/blob/main/ECOSYSTEM-TRANSITION-INDEX.md)
- [Nova Nexus — administración](https://github.com/Nova-Nexus-C35/Nova-Nexus)


## Línea de desarrollo vigente

La rama de desarrollo activa y fuente de trabajo más avanzada es [`rebuild/libgdx-android-2026-11-22`](https://github.com/Nova-Nexus-C35/Futbol-Nova/tree/rebuild/libgdx-android-2026-11-22), basada en Java 17 + libGDX 1.14.2 para Android ARM64. Incluye 22 actores, simulación 4-3-3, IA inicial, física aerodinámica del balón, posesión/pases/tiros/tackles básicos, reglas geométricas, datos JSON y pruebas unitarias.

La auditoría funcional más reciente de esa rama está en [`Docs/FUNCTIONALITY_GAP_AUDIT_2026-11-23.md`](https://github.com/Nova-Nexus-C35/Futbol-Nova/blob/rebuild/libgdx-android-2026-11-22/Docs/FUNCTIONALITY_GAP_AUDIT_2026-11-23.md). GitHub Actions run #49 aprobó las pruebas Java y el empaquetado APK ARM64; todavía falta instalar y probar el APK en el Android real del propietario.

La rama Unity `feature/rebuild-unity-android-foundation-2026-11-22` fue descartada expresamente, y su PR #1 está cerrado sin fusionar. No reactivar Unity ni sus requisitos de licencia. La rama principal conserva una versión anterior y no se reemplaza hasta completar revisión e integración controlada de la línea libGDX.
