# Portabilidad de física del balón y reglas de línea

Fecha: 2026-11-22.

## Fuentes originales revisadas

- `Projects/Games/futbol-nova/scripts/ball_flight_model.gd`, SHA `a3e7c59cf2f0fb4579716238a81f0ed111018208`.
- `Projects/Games/futbol-nova/scripts/gameplay/match_rules.gd`, SHA `9da6a4f113fb81b0a5ec9ad3f80a780e16ff01ac`.

## Portado a Java

- Gravedad de referencia 9,81 m/s²; arrastre aerodinámico y fuerza Magnus.
- Límite de fuerza Magnus, masa segura, límite de spin y disipación del giro.
- Integración de posición y velocidad con paso limitado a 0,05 s.
- Evaluación de fuera de juego, participación activa y exenciones de saques.
- Clasificación de balón en juego, gol, saque de banda, córner y saque de puerta.
- El render usa el integrador y valida que el balón cruce completamente la línea y que quepa bajo el larguero/entre postes.

## Pruebas

Gravedad, desplazamiento, spin, fuerza aerodinámica nula, fuera de juego, exenciones, goles, balón alto, saques y cruce completo de línea.

## Pendiente

Los reinicios clasificados todavía no se ejecutan: las salidas no goleadoras conservan un rebote provisional. Faltan trayectoria táctil para curva/loft, colisiones con postes y larguero, árbitro en vivo y conexión del fuera de juego con la acción real de pase.
