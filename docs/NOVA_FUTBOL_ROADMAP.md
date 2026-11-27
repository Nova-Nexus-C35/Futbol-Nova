# Hoja de ruta — Fútbol Nova

## Fase 0 — Base y trazabilidad
- [x] Identificar el repositorio maestro de la cuenta Primary.
- [x] Revisar la estructura inicial del destino.
- [x] Revisar documentación y commit actual de Soccer 27.
- [x] Fijar la fuente mediante submódulo Git para evitar copiar a ciegas.
- [ ] Verificar el texto de licencia y licencias de terceros.
- [ ] Probar inicialización del submódulo en un clon limpio.

## Fase 1 — Importación y compilación Android
- [ ] Importar `vendor/Soccer-27/unity/FC2026Unity` en Unity 6000.3.24f1.
- [ ] Resolver paquetes y errores de importación.
- [ ] Configurar Android ARM64, orientación horizontal y paquete NOVA.
- [ ] Configurar Unity CI con secretos; nunca guardar credenciales en Git.
- [ ] Generar APK debug desde GitHub Actions.
- [ ] Instalar y probar el APK en Android.

## Fase 2 — Identidad NOVA
- [ ] Cambiar nombre, icono, splash, menús, colores, tipografía y HUD.
- [ ] Eliminar marcas de Soccer 27/FC 2026 de la interfaz final.
- [ ] Definir identificador de paquete exclusivo de Fútbol Nova.
- [ ] Crear catálogo de clubes, competiciones, uniformes y jugadores ficticios o autorizados.

## Fase 3 — Calidad del partido
- [ ] Validar partido 11 contra 11 y duración/estados de partido.
- [ ] Medir y mejorar controles táctiles y multitáctiles.
- [ ] Ajustar física del balón, pases, tiros, centros y colisiones.
- [ ] Mejorar IA ofensiva, defensiva, porteros y posicionamiento.
- [ ] Revisar animaciones, transiciones, cámara y repeticiones.
- [ ] Perfilado de FPS, memoria, batería y temperatura en varios dispositivos.

## Fase 4 — Modos y contenido
- [ ] Amistosos locales offline.
- [ ] Torneos y ligas.
- [ ] Carrera, fichajes y progresión.
- [ ] Personalización de clubes y uniformes.
- [ ] Guardado local robusto y migraciones de datos.
- [ ] Multijugador online, después de estabilizar el juego local.

## Criterios de aceptación

Una fase no se considera terminada solo por añadir código. Debe incluir compilación, pruebas automatizadas cuando sean posibles, verificación en Android real y evidencia del resultado.
