# Hito de IA: conducción en carriles seguros

Fecha: 2026-11-23  
Repositorio: `Nova-Nexus-C35/Futbol-Nova`  
Rama: `rebuild/libgdx-android-2026-11-22`

## Objetivo

Evitar que un jugador de IA que decide conservar el balón se quede inmóvil o avance directamente hacia un defensor. La conducción debe tener un objetivo espacial adelantado y reevaluar el espacio disponible.

## Implementación

- `MatchGameplay.chooseAiCarryTarget(...)` evalúa tres destinos: centro y dos diagonales de 4.5 m.
- Cada destino se proyecta 5.5 m hacia la portería rival, respetando los límites del campo.
- La puntuación prioriza la distancia al rival más cercano y penaliza desplazamientos laterales innecesarios.
- `FutbolNovaGame.updateGameplay(...)` actualiza el objetivo del portador IA mientras el temporizador de decisión está activo; al terminar la ventana, la IA vuelve a evaluar el pase.
- La dirección de avance se deriva del sentido de ataque vigente y funciona tras el cambio de campo del descanso.

## Pruebas añadidas

- La IA elige una diagonal cuando el carril central está ocupado por rivales.
- El objetivo queda dentro del ancho reglamentario del terreno.
- Tras invertir el sentido de ataque, el objetivo avanza hacia la portería correcta.

## Limitaciones conocidas

- No es todavía un regate técnico: no incluye fintas, amagos, aceleraciones variables, protección con el cuerpo ni animaciones de contacto.
- La evaluación usa distancia geométrica simple; no calcula velocidad relativa, ángulo de intercepción ni riesgo probabilístico de pérdida.
- Requiere validación en partidos completos y calibración de parámetros.

## Investigación relacionada

Se consultaron League-Soccer, soccer-js y FootballEngine como referencias de decisiones pass/carry/shoot y separación entre lógica táctica y render. Sus stacks/licencias no justifican una dependencia directa; la implementación del carril es propia. Detalles en `Docs/OPEN_SOURCE_RESEARCH.md`.
