# Fútbol Nova

**Fútbol Nova** es un videojuego de fútbol 3D para Android, inspirado en la experiencia de simulación de FIFA / EA SPORTS FC y PES / eFootball, con identidad, clubes y contenido propios de NOVA.

## Base técnica adoptada

El motor de partidos 3D se incorpora como submódulo Git, fijado a una versión concreta del proyecto público **Soccer 27**:

- Repositorio original: https://github.com/Wilberforcedev/Soccer-26-game
- Carpeta Unity activa: `unity/FC2026Unity`
- Versión Unity indicada por el autor: **Unity 6.3 LTS — 6000.3.24f1**
- Commit fijado: `f741b5d5ae3d531db367e1105023a7b39a1ffe3d`
- Ruta local del submódulo: `vendor/Soccer-27`

El proyecto fuente documenta partidos 3D con 22 jugadores, física Rigidbody del balón, pases, tiros, entradas, portero, IA posicional, cámara de retransmisión, controles táctiles Android y sistemas de carrera. La IA y la física siguen en desarrollo; esta integración **no significa que Fútbol Nova ya esté compilado, probado o listo para publicar**.

## Abrir el proyecto

Clonar incluyendo el submódulo:

```bash
git clone --recurse-submodules https://github.com/Nova-Nexus-C35/Futbol-Nova.git
```

Si el repositorio ya está clonado:

```bash
git submodule update --init --recursive
```

Abrir en Unity Hub la carpeta:

```text
vendor/Soccer-27/unity/FC2026Unity
```

Escena inicial indicada por el proyecto fuente:

```text
Assets/Scenes/Bootstrap.unity
```

Se requiere Unity 6000.3.24f1 con Android Build Support, Android SDK & NDK Tools y OpenJDK. La compilación debe ejecutarse en un runner de GitHub Actions con licencia Unity configurada como secreto; no se deben guardar licencias, claves o contraseñas en el repositorio.

## Dirección de producto NOVA

La base se adaptará gradualmente para:

1. Reemplazar marca, iconos, menús y presentación por la identidad NOVA.
2. Mantener partidos completos 11 contra 11 y cámara de retransmisión.
3. Mejorar respuesta del balón, regates, pases, tiros, porteros y animaciones.
4. Añadir amistosos, torneos, ligas y modo carrera.
5. Incorporar equipos, jugadores y uniformes propios o con licencias verificadas.
6. Optimizar rendimiento, memoria, controles y compatibilidad en Android ARM64.
7. Añadir multijugador solo después de estabilizar el partido local.

## Estructura

- `vendor/Soccer-27`: submódulo externo fijado a un commit; no modificar directamente el repositorio original.
- `docs/BASE_INTEGRATION_AUDIT.md`: auditoría de procedencia, licencia y límites de la integración.
- `docs/NOVA_FUTBOL_ROADMAP.md`: plan de adaptación y validación.

## Política de terceros y publicación

Se conserva la atribución al autor original. El README del proyecto fuente declara Apache-2.0, pero en la revisión del commit fijado no se encontró un archivo `LICENSE` en la raíz. Por eso, antes de redistribuir copias del código o publicar una versión comercial, hay que obtener y verificar el texto de licencia y revisar las licencias de paquetes y recursos de terceros.

No se incorporan marcas, escudos, uniformes, nombres ni semejanzas de clubes, ligas o futbolistas reales sin autorización. La base utiliza contenido ficticio según la documentación del autor.

## Estado

**Integración inicial:** submódulo fijado y documentación de adaptación.  
**Pendiente:** validar importación de Unity, revisar todos los paquetes/recursos, configurar CI con Unity License, compilar APK ARM64, probar en dispositivo y aplicar la identidad NOVA dentro del proyecto Unity.
