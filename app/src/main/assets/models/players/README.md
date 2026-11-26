# Futbolista GLB activo

El juego carga opcionalmente dos futbolistas humanoides riggeados, uno por equipo:

- `app/src/main/assets/models/players/home.glb` — uniforme azul del equipo local.
- `app/src/main/assets/models/players/away.glb` — uniforme rojo del equipo visitante.

Los modelos se generan desde `Assets/Players/characters/home.json` y `away.json` con el workflow `.github/workflows/generate-football-players.yml`, usando Ara 3D Gen CLI fijado a `0.18.1`.

## Requisitos

- GLB glTF 2.0, eje vertical Y.
- Malla skinned con nodo `skin` y pesos de vértices válidos.
- Esqueleto `game_lite` con 25 huesos humanoides. Ara añade `WeaponR` y, por defecto, `jaw` + los dos ojos como joints auxiliares; el GLB actual declara 29 joints totales (25 de cuerpo + 4 auxiliares). CI exige 25–29 joints y el renderer configura el presupuesto con `maxBones` real. Si el export supera 29, revisar los joints auxiliares y el límite del shader antes de aceptarlo.
- Clips esperados: locomoción (`idle`, `walk`, `run`) y fútbol (`soccer_shoot`, `soccer_pass`, pases laterales, recepción, control de pecho, cabezazo, tackle, entradas, portero, saque de banda y celebración).
- Escala y origen revisados para altura aproximada de 1.8 unidades, pies en Y=0 y orientación de origen +Z (Ara); el renderer aplica el giro de 180° necesario para las coordenadas de juego.
- Texturas incluidas dentro del GLB o rutas relativas empaquetadas. No depender de red.

## Fallback y seguridad

Si cualquiera de los GLB de local o visitante no existe o no se puede cargar, el juego conserva automáticamente `PlayerVisual` procedural. No se debe eliminar el fallback hasta validar en dispositivo la carga, las animaciones, la memoria, los FPS y la pérdida/restauración del contexto OpenGL.

## Fixture de integración

`app/src/test/resources/gltf/fixtures/quaternius-player.glb` es un fixture CC0 de Quaternius procedente de `kendrekaran/striker-3d`. Contiene una malla riggeada, 31 articulaciones y clips como Man_Idle, Man_Run y Man_Walk. Se usa únicamente para verificar el formato y el contrato de licencia en pruebas; **no es el arte final realista** y no se copia a los assets de producción.

Licencia y procedencia: `app/src/test/resources/gltf/fixtures/QUATERNIUS-LICENSE.txt`.


## Licencias y procedencia de producción

Ara 3D Gen: https://github.com/olatunde85/3d-gen (código MIT). Los modelos base MakeHuman son CC0 1.0. Las animaciones deportivas forman parte de los datos de movimiento CMU Graphics Lab: se permite su uso incluso en productos comerciales, pero los datos no se pueden revender directamente ni siquiera convertidos. Mantener los avisos/licencias incluidos en el export y revisar el ZIP antes de publicar APK o assets por separado.

El pipeline genera los GLB en GitHub Actions y los guarda en esta rama. No se debe declarar terminado el realismo hasta comprobar los modelos exportados, el build Android y el rendimiento en el teléfono objetivo.


## Perfil móvil

Las especificaciones de Ara fijan `rig: game_lite`, un esqueleto de 25 huesos sin dedos, para limitar el coste de skinning en Android. Ara exporta con +Y arriba y el personaje mirando a +Z; `GltfPlayerSquad` compensa la orientación de base con 180° al aplicar el yaw del partido. El renderer calcula el máximo de huesos entre ambos modelos para configurar el shader.


## Validación del esqueleto real — 2026-11-26

- Ara documenta `game_lite` como rig humanoide de 25 huesos, y explica que cada rig añade el socket `WeaponR` y por defecto `jaw` + ambos ojos. El GLB exportado declara 29 joints totales, coherente con 25 + 4 auxiliares.
- Fuentes: https://ara3dgen.com/docs/rigging/ y https://ara3dgen.com/docs/library/.
- CI valida 25–29 joints, un nodo que enlaza mesh+skin y canales de animación no vacíos para cada acción requerida. `GltfPlayerSquad` configura `SceneManager` usando el `maxBones` real del GLB.
