# Reconciliación maestra de configuración — Fútbol Nova

**Fecha de corte:** 2026-11-27  
**Estado:** inventario y reconciliación documental; no equivale a completar la migración de todos los sistemas.  
**Runtime ejecutable vigente:** repositorio `Nova-Nexus-C35/Futbol-Nova`, rama `work/futbol-nova-completion-audit-2026-11-27`.  
**Workspace central:** `Cumba35/Nova-Workspace`, carpeta `proyectos/juegos/futbol-nova/`.

## 1. Decisión de autoridad y conservación

Se encontraron tres ubicaciones con funciones distintas:

| Fuente | Función comprobada | Tratamiento |
|---|---|---|
| [Nova-Nexus-C35/Futbol-Nova](https://github.com/Nova-Nexus-C35/Futbol-Nova/tree/work/futbol-nova-completion-audit-2026-11-27) | Reconstrucción Android nativa más reciente, con Java/C++/OpenGL ES, datos y CI | Fuente del runtime activo en esta etapa |
| [Cumba35/Nova-Workspace](https://github.com/Cumba35/Nova-Workspace/tree/main/proyectos/juegos/futbol-nova) | Proyecto maestro histórico y amplio: diseño, datos, módulos, escenas, scripts, pruebas y referencias | Fuente de requisitos y material para portar; conservar sin borrar |
| [Cumba35/Futbol-Nova](https://github.com/Cumba35/Futbol-Nova) | Distribución independiente histórica del proyecto Godot | Archivo histórico; no sobrescribir ni confundir con el runtime nativo |

El Workspace es el repositorio central del ecosistema NOVA, pero su carpeta de Fútbol Nova conserva una implementación histórica Godot. La aplicación ejecutable más reciente está en el repositorio independiente nativo. La sincronización entre ambos repositorios es manual y revisada; no asumir que un cambio en uno aparece en el otro.

**No se fusionan motores ni se copian scripts GDScript al runtime nativo.** Se portan contratos, reglas, datos y comportamiento por etapas, con pruebas equivalentes.

## 2. Estado de GitHub verificado

- Repositorio nativo: `Nova-Nexus-C35/Futbol-Nova`.
- `main`: `130d784e18589ad40cf260a1459c0b1582c63d47`.
- Rama de trabajo: `work/futbol-nova-completion-audit-2026-11-27`, commit `e27ef4ad242ec33a9e183cde4330fef15af228ab`.
- Pull Request #5: abierto, destino `main`; no se fusionó durante esta reconciliación.
- Comparación de rama: 33 commits por delante de `main`, 0 por detrás.
- GitHub Actions #57, ejecución `46082171838`: `completed / success` para el commit `e27ef4a`. Esto acredita la CI de esa ejecución; no acredita instalación ni prueba física en un teléfono.
- Workspace central: `Cumba35/Nova-Workspace`, `main` en `59ac0c6ef965f3a0802449ce107b8326931caad8` al consultar. El último commit visible actualizó el índice de referencias 441–460, no el motor del juego.

## 3. Configuración técnica vigente del runtime

| Parámetro | Valor |
|---|---|
| Aplicación Android | `com.nova.futbolnova` |
| Versión | `0.2.0-native`, `versionCode 2` |
| Lenguaje de aplicación | Java 17 |
| Simulación / motor | C++17 |
| Gráficos | OpenGL ES 3.0 |
| Android SDK | compileSdk 35 |
| minSdk / targetSdk | 28 / 28 |
| NDK | 26.3.11579264 |
| CMake | 3.22.1 |
| Android Gradle Plugin | 8.6.1 |
| Gradle de CI documentado | 8.7 |
| ABI | `arm64-v8a` |
| Orientación | Horizontal; pantalla completa |
| Distribución | Offline-first; amistoso local 11v11 como modo base |

Archivos de implementación:
- `native-android/app/src/main/java/com/nova/futbolnova/MainActivity.java`
- `native-android/app/src/main/java/com/nova/futbolnova/CanonicalDataRepository.java`
- `native-android/app/src/main/cpp/native_engine.cpp`
- `native-android/app/build.gradle`
- `data/native_runtime_manifest.json`
- `.github/workflows/futbol-nova-native-android.yml`
- `tools/validate_native_data.py`

El APK no depende de Godot, Unity, Unreal ni de un motor comercial. Los JSON de `data/` se empaquetan como assets offline. El manifiesto declara fuentes canónicas, requisitos de elegibilidad y módulos. Los paquetes opcionales previstos son paquetes de datos JSON/ZIP verificados; no se deben activar módulos no instalados ni descargar/ejecutar código arbitrario como si fuera contenido.

## 4. Datos deportivos consolidados

### Base cubana
- 52 clubes y 1.144 futbolistas ficticios.
- Primera: 16 equipos / 352 jugadores.
- Segunda: 16 equipos / 352 jugadores.
- Tercera: 20 equipos / 440 jugadores.
- 22 jugadores por plantilla, IDs únicos y 35 atributos enteros de 0 a 99.

### Catálogo global
- 10 ligas base.
- 725 clubes catalogados.
- 15.950 perfiles ficticios generados en el catálogo de ligas base.
- Selector/catálogo de 100 ligas.
- 211 asociaciones nacionales.
- 16 torneos de selecciones catalogados.
- `national_teams_manifest.json`: cero paquetes de selecciones publicados; catálogo no equivale a selección jugable.

### Clubes originales
El catálogo canónico actual es `data/original_clubs_2026.json`; las plantillas de exhibición canónicas están en `data/players/nova_original_club_rosters_2026.json`.

| Club | ID | Plantilla | Estado |
|---|---|---:|---|
| Nova C35 | `FN-NOVA-C35` | 22 | Exhibición jugable |
| FC Río de Argenti | `FN-NOVA-RDA` | 22 | Exhibición jugable |
| Nova Génesis | `FN-NOVA-GEN` | 0/22 | Solo catálogo |
| Sangre Galáctica | `FN-NOVA-SAG` | 0/22 | Solo catálogo |

Los dos paquetes de exhibición contienen 44 registros: Anier Cumbá, Máximo Padilla, Raymond Valdez y 41 perfiles ficticios generados. Los estadios originales tienen capacidad de diseño indicada de 105.000; son especificaciones del videojuego, no recintos reales ni prueba de propiedad.

### Perfiles personalizados
- Anier Cumbá — `NOVA-PLAYER-0035`: GRL 92, POT 97, MCO/MC, dorsal 35, capitán; club activo Albor Madrid (`FN-ESP-REAL-MADRID`); Nova C35 queda como afiliación histórica/exhibición. Preferencia nacional CUB. La especialización Velocista no debe sumarse dos veces al GRL.
- Máximo Padilla — `NOVA-PLAYER-0036`: GRL 94, POT 97, MCD/MC, dorsal 30; club activo Litoral Barcelona (`FN-ESP-BARCELONA`); Nova C35 queda como afiliación histórica/exhibición. Preferencia nacional CUB. Los atributos individuales no están todos confirmados uno por uno.
- Raymond Valdez — `NOVA-PLAYER-0037`: GRL 91, POT 96, DC/MCO, dorsal 5; FC Río de Argenti (`FN-NOVA-RDA`); nacionalidad `UNK`, sin inventar selección preferida. Los atributos generados siguen pendientes de revisión del propietario.

La pertenencia de Anier y Máximo a la plantilla del paquete de exhibición C35 no cambia su club activo de temporada. Las preferencias nacionales no equivalen a convocatorias. Las estadísticas empiezan en cero; medias y porcentajes permanecen `null` sin partidos/denominador verificado.

## 5. Diferencias de datos entre Workspace y runtime nativo

Se compararon las rutas de archivos del proyecto en Workspace y del repositorio nativo. Resultado: 336 archivos en la carpeta del Workspace y 340 archivos en el árbol nativo revisado; 25 archivos existen solo en Workspace y 29 solo en el nativo.

### Cinco archivos de datos que permanecen solo en Workspace

- `data/fc_rio_de_argenti_club.json`
- `data/nova_c35_club.json`
- `data/nova_genesis_club.json`
- `data/sangre_galactica_club.json`
- `data/original_club_players_2026.json`

Los cuatro JSON de club individuales son fichas históricas cuyos datos principales ya están consolidados en `data/original_clubs_2026.json`. Se conservan en Workspace; no se duplican en el runtime.

**Conflicto de plantillas resuelto por precedencia y trazabilidad:** el archivo histórico `original_club_players_2026.json` contiene 42 registros de jugadores, mientras sus dos equipos referencian 44 IDs (Anier y Máximo se mantienen en el catálogo personalizado separado). Sus nombres e IDs de perfiles generados difieren de la fuente actual. El runtime usa `nova_original_club_rosters_2026.json`, actualizada el 27-11, con 44 registros, 41 perfiles ficticios y los tres perfiles personalizados. No mezclar ni importar los registros antiguos por nombre; cualquier recuperación debe hacerse mediante revisión individual de ID, atributos y procedencia.

### Veinte archivos adicionales solo en Workspace

**Workflow antiguo**
- `.github/workflows/futbol-nova-android-apk.yml` — workflow de exportación Godot; no reactivar en la línea nativa.

**Recursos**
- `assets/players/maximo_padilla_reference.jpg` — referencia de Máximo; conservar para modelado.
- `assets/references/ultimate-team-main-menu-reference.jpg` — referencia histórica; Ultimate Team está excluido del producto y no se debe reintroducir.

**Documentación de portabilidad**
- `docs/CLUB_ASSET_MANIFEST_2026-11-20.md`
- `docs/EXTERNAL_REFERENCE_AUDIT_2026-11-21.md`
- `docs/FC_RIO_DE_ARGENTI_IDENTITY.md`
- `docs/FRIENDLY_ORIGINAL_CLUBS_DEFAULTS_2026-11-20.md`
- `docs/FRIENDLY_PLAYABLE_MVP_ROADMAP_2026-11-21.md`
- `docs/FRIENDLY_RUNTIME_RESEARCH_AND_PLAN_2026-11-20.md`
- `docs/GOALKEEPER_PHYSICS_REFERENCE_AUDIT_2026-11-20.md`
- `docs/NOVA_GENESIS_IDENTITY.md`
- `docs/ORIGINAL_CLUBS_COMPLETION_DRAFT_2026-11-20.md`
- `docs/ORIGINAL_CLUBS_FRIENDLY_IMPLEMENTATION_2026-11-20.md`
- `docs/ORIGINAL_CLUB_ROSTERS_AND_RAYMOND_2026-11-20.md`

**Código GDScript/escenas/pruebas que sirven como especificación, no como runtime nativo**
- `scenes/players/raymond_valdez.tscn`
- `scripts/data/original_club_importer.gd`
- `scripts/players/raymond_valdez.gd`
- `scripts/visuals/footballer_visual_builder.gd`
- `tests/test_footballer_visuals.gd`
- `tests/test_original_club_importer.gd`
- `tests/test_original_club_rosters.gd`

Los archivos se mantienen en Workspace como fuente histórica. La falta de copia en el runtime no significa que se deban borrar: se deben traducir a contratos Java/C++ y pruebas nativas cuando corresponda.

## 6. Activos gráficos encontrados en Dropbox

Se revisaron `/Apps/Futbol Nova/Assets/` y las búsquedas de recursos de jugadores.

| Recurso | Ubicación / tamaño | Resultado |
|---|---|---|
| Nova C35, primera equipación | `/Apps/Futbol Nova/Assets/Clubs/Nova C35/Kits/nova_c35_home_kit_reference.jpg`, 430.494 bytes | Encontrado |
| RDA, primera equipación | `/Apps/Futbol Nova/Assets/Clubs/RDA/Kits/rda_home_kit_reference.jpg`, 608.275 bytes | Encontrado; no sobrescribir |
| Nova Génesis, propuesta en carpeta | `.../Nova Génesis/Kits/Proposals/nova_genesis_home_kit_proposal_01.png`, 3.546.136 bytes | Encontrado como propuesta 01 |
| Nova Génesis, referencia fotográfica | carpeta `References` | No apareció en el listado ni en búsqueda por nombre |
| Nova Génesis, propuesta v2/03 | mencionada en documentos como propuesta aprobada | El archivo 03 no apareció en Dropbox; la carpeta contiene proposal_01 |
| Anier Cumbá | `/Nova/Workspace/Recursos gráficos/Anier Cumbá.png`, 2.366.897 bytes | Encontrado |
| Raymond Valdez | `/Nova/Workspace/Recursos gráficos/Ficha de Jugador - Raymond Valdez.png`, 2.610.408 bytes | Encontrado |
| Máximo Padilla | `assets/players/maximo_padilla_reference.jpg` en Workspace | Encontrado en GitHub Workspace; no se localizó con la búsqueda Dropbox realizada |

No se copiaron binarios en esta etapa. Antes de incorporarlos al runtime se debe verificar visualmente cada imagen, su versión aprobada, hash, licencia/procedencia y destino. En particular, no sustituir la referencia local de RDA ni declarar disponible la propuesta GEN v2 mientras no se localice el binario exacto.

## 7. Diseño del juego y módulos

El documento maestro `docs/GAME_DESIGN.md` y los documentos del Workspace/Dropbox mantienen requisitos de:
- Menú de cuatro pantallas: Inicio, Online, Modos y Configuración.
- Amistoso local como única experiencia jugable base.
- Funcionamiento offline.
- Control de equipo o de un jugador, selección dinámica y controles táctiles.
- Cámaras, selector de equipos, equipaciones y balón por partido.
- Reglas arbitrales, tiempo añadido, lesiones, sustituciones, estadísticas y guardados.
- Manager, Leyenda, ligas, copas, torneos y Online como módulos separados.
- Identidad NOVA original; Ultimate Team excluido.
- Solo recursos originales o con derechos compatibles.

La documentación describe el alcance deseado, no demuestra que esas funciones estén implementadas. El manifiesto de módulos exige paquete real, HTTPS, SHA-256, derechos revisados y pruebas de compatibilidad/instalación antes de publicar o activar.

## 8. Diferencias y bloqueos que quedan abiertos

1. **Amistoso predeterminado:** el manifiesto conserva Albor Madrid (`FN-ESP-REAL-MADRID`) vs Litoral Barcelona (`FN-ESP-BARCELONA`). Se mantiene como preferencia, pero no se debe habilitar hasta disponer de 22 jugadores válidos por equipo. El lector nativo actual carga los dos rosters originales y las tres divisiones cubanas; no carga aún los equipos españoles. La interfaz debe advertirlo y no sustituirlos silenciosamente por equipos aleatorios.
2. **Plantillas españolas:** los dos clubes están marcados `draft_review_required`; completar/verificar plantillas y enlazar perfiles por ID estable antes de seleccionarlos.
3. **Atributos de portero:** confirmar el contrato de las seis estadísticas (`dive`, `hand`, `kick`, `ref`, `gkspd`, `gkpos`) entre JSON, Java y C++.
4. **Activos GEN:** localizar foto original y propuesta v2 aprobada; no asumir que proposal_01 equivale a proposal_03.
5. **Raymond:** revisar referencia facial y sus 35 atributos provisionales antes del modelo final.
6. **Prueba física:** CI #57 fue satisfactoria; falta instalar y jugar en un teléfono Android real.
7. **Legacy:** portar, no copiar sin revisión, las reglas de juego, importer, constructor visual y tests que permanecen en Workspace.

## 9. Orden recomendado para corregir detalles uno por uno

| Orden | Trabajo | Criterio de cierre |
|---:|---|---|
| 1 | Auditar los rosters españoles y el partido predeterminado | 22 IDs únicos por club; selección real y prueba automatizada |
| 2 | Contrato de atributos y porteros | 35 atributos validados y seis atributos GK interpretados igual en Java/C++ |
| 3 | Integración de perfiles personalizados | Aplicación por ID; clubes activos e históricos no se confunden |
| 4 | Selector de equipos y jugadores | Solo ofrece plantillas válidas; no activa catálogos incompletos |
| 5 | Referencias de equipaciones y caras | Binarios correctos, verificados y enlazados |
| 6 | Portar reglas de partido del proyecto histórico | Estados y reinicios cubiertos por pruebas nativas |
| 7 | Modelos, animaciones e IA | Actores articulados y comportamiento comprobado |
| 8 | Guardados, estadísticas y módulos | Persistencia versionada y paquetes validados |
| 9 | Instalación y partido completo en Android | Prueba manual documentada en dispositivo real |

## 10. Reglas de mantenimiento

- No borrar ni sobrescribir el proyecto histórico de Workspace.
- No fusionar ramas divergentes ni cambiar de motor por similitud de nombre.
- Los datos deportivos y manifiestos viven en `data/`; Java, C++ y JNI en `native-android/`; pruebas/validadores en `tools/`; decisiones en `docs/`.
- Antes de editar un archivo existente, leerlo y conservar su SHA.
- Cada cambio se guarda en la rama y repositorio correspondientes con commit descriptivo.
- Después de escribir, releer el archivo desde GitHub y verificar el contenido.
- Ejecutar validadores/CI cuando cambien código o datos.
- No marcar una función como implementada por tener solo una ficha, pantalla o JSON.
- No declarar APK instalada/funcional sin prueba en teléfono.

## Fuentes revisadas

- [Runtime nativo y rama de trabajo](https://github.com/Nova-Nexus-C35/Futbol-Nova/tree/work/futbol-nova-completion-audit-2026-11-27)
- [Pull Request #5](https://github.com/Nova-Nexus-C35/Futbol-Nova/pull/5)
- [Ejecución CI #57](https://github.com/Nova-Nexus-C35/Futbol-Nova/actions/runs/46082171838)
- [Workspace — proyecto histórico](https://github.com/Cumba35/Nova-Workspace/tree/main/proyectos/juegos/futbol-nova)
- [Nota de repositorio Workspace](https://github.com/Cumba35/Nova-Workspace/blob/main/documentacion/notas-repositorios/futbol-nova.md)
- Dropbox Paper: ficha de recuperación 2026-11-26; documento maestro de diseño; arquitectura/configuración/licencias 2026-11-23; manifiesto de activos 2026-11-20.


## Addendum — decisión de amistoso predeterminado (2026-11-28)

La decisión anterior que conservaba Albor Madrid vs Litoral Barcelona queda sustituida para el amistoso predeterminado del runtime nativo. La configuración vigente es **Nova C35** (`FN-NOVA-C35`) como local y **FC Río de Argenti** (`FN-NOVA-RDA`) como visitante. Los dos equipos disponen de 22 perfiles de exhibición verificados en la fuente canónica. Las plantillas españolas y la asignación de club activo de Anier/Máximo no cambian; este ajuste afecta únicamente a la selección inicial del amistoso.
