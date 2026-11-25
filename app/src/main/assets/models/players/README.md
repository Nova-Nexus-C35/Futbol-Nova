# Futbolista GLB activo

El juego puede cargar opcionalmente un jugador humanoide riggeado desde:

`app/src/main/assets/models/players/active.glb`

## Requisitos

- GLB glTF 2.0, eje vertical Y.
- Malla skinned con nodo `skin` y pesos de vértices válidos.
- Esqueleto con un máximo inicial de 24 huesos por draw para el perfil GLES2 más conservador; si el modelo supera el límite, dividir/optimizar las mallas antes de elevar el presupuesto.
- Clips con nombres reconocibles: `Idle` o `Stand`, `Walk` o `Jog`, `Run` o `Sprint`.
- Escala y origen revisados para altura aproximada de 1.8 unidades, pies en Y=0 y orientación frontal hacia -Z.
- Texturas incluidas dentro del GLB o rutas relativas empaquetadas. No depender de red.

## Fallback y seguridad

Si `active.glb` no existe o no se puede cargar, el juego conserva automáticamente `PlayerVisual` procedural. No se debe eliminar el fallback hasta validar en dispositivo la carga, las animaciones, la memoria, los FPS y la pérdida/restauración del contexto OpenGL.

## Fixture de integración

`app/src/test/resources/gltf/fixtures/quaternius-player.glb` es un fixture CC0 de Quaternius procedente de `kendrekaran/striker-3d`. Contiene una malla riggeada, 31 articulaciones y clips como Man_Idle, Man_Run y Man_Walk. Se usa únicamente para verificar el formato y el contrato de licencia en pruebas; **no es el arte final realista** y no se copia automáticamente a `active.glb`.

Licencia y procedencia: `app/src/test/resources/gltf/fixtures/QUATERNIUS-LICENSE.txt`.
