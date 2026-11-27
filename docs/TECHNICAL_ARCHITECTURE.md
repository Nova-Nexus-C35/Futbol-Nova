# Fútbol Nova — arquitectura técnica nativa

## Motor y plataformas

Runtime propio para Android:
- Java 17: ciclo de vida, interfaz, navegación, persistencia Android y entrada táctil.
- C++17: estado del partido, física, IA, reglas y simulación en tiempo real.
- OpenGL ES 3.0: mallas, shaders, cámara, materiales, iluminación y renderizado.
- JNI: contrato acotado entre la capa Android y el motor C++.
- Android SDK/NDK, CMake y Gradle: toolchain de plataforma y compilación, no motores de juego.

No se permite incorporar Godot, Unity, Unreal, ni otro motor externo al APK. Los archivos Godot existentes son históricos y se consultan para portar reglas y comportamiento; no se amplían como runtime ni se incluyen en la APK.

## Capas

1. **Presentación Android:** Inicio, Online, Modos, Configuración, selector, HUD, pausa y accesibilidad.
2. **Renderizador nativo:** OpenGL ES 3.0, cámara, geometría, materiales, iluminación y optimización móvil.
3. **Gameplay C++:** futbolistas, balón, colisiones, porteros, goles, saques y reglas.
4. **IA táctica:** roles, formación, marcaje, presión, desmarques, coberturas y transiciones.
5. **Simulación deportiva:** calendario, resultados, ligas, copas, tablas y estadísticas.
6. **Carreras:** Manager, Leyenda, contratos, fichajes, entrenamiento y progresión.
7. **Datos y persistencia:** JSON versionado, IDs estables, validación, guardados y migraciones.

## Datos canónicos

Los JSON de `data/` son la fuente única. `data/native_runtime_manifest.json` declara las rutas y las políticas de activación. No duplicar catálogos en código. Las plantillas requieren 22 jugadores con IDs únicos y no vacíos; los perfiles personalizados se asocian solo por ID. Un club catalogado sin plantilla validada no es seleccionable.

## Motor de partido

- Estado de partido independiente del renderizador.
- Simulación fija y reproducible; renderizado desacoplado del paso físico.
- Movimiento con aceleración, velocidad máxima, giro y frenado.
- Balón con masa, gravedad, fricción, rebote, contactos y detección continua.
- Acciones contextuales: pase, pase al espacio, centro, tiro, regate, entrada, despeje y cabezazo.
- Porteros con área propia, anticipación, posicionamiento, estirada, blocaje y despeje.
- Árbitro y reglas separados: faltas, ventaja, tarjetas, fuera de juego, penaltis y reinicios.
- Eventos de partido como fuente de marcador, estadísticas, comentarios y repeticiones.

## Simulación de temporada

El motor de temporadas no depende de OpenGL ni de la escena 3D. Puede simular encuentros no jugados con un modelo probabilístico basado en atributos, forma, táctica, fatiga y localía. Los partidos jugados producen el mismo contrato de resultado y eventos.

## Rendimiento Android

- ABI inicial: arm64-v8a.
- OpenGL ES 3.0; reducir efectos y resolución dinámica según capacidad del dispositivo.
- IA a frecuencia menor que renderizado cuando sea posible.
- Instanciación, LOD y animaciones optimizadas para 22 jugadores y público.
- Perfilado de CPU/GPU en teléfonos reales antes de elevar calidad gráfica.
- Modo offline como base; no añadir multijugador online hasta estabilizar el partido local.

## Calidad y distribución

GitHub Actions valida JSON, compila Java/C++ con Android SDK/NDK y CMake, verifica que la APK contiene `lib/arm64-v8a/libfutbolnova.so` y publica el artefacto. La compilación no sustituye pruebas de instalación, controles, rendimiento y un amistoso completo en dispositivo real.

## Migración de sistemas históricos

Los scripts y escenas Godot existentes conservan reglas, contratos, diseños y lógica útil. Cada sistema se porta por fases a Java/C++ y recibe pruebas equivalentes antes de activarse. No eliminar fuentes históricas ni datos canónicos hasta completar su reemplazo y validación nativa.
