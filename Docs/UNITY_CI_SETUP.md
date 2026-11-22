# Configurar Unity CI y generar el APK Android

Estado: pendiente de credenciales de licencia Unity. No pegar licencias, contraseñas ni seriales en conversaciones o archivos del repositorio.

## 1. Qué confirmó la primera ejecución

GitHub Actions sí inició el workflow. El job terminó en el paso `Validate Unity license secrets` porque no encontró las credenciales de Unity. Unity Editor, las pruebas y la compilación Android no llegaron a ejecutarse. El job de APK quedó omitido porque depende de las pruebas.

## 2. Secretos necesarios

En el repositorio `Nova-Nexus-C35/Futbol-Nova`, abrir:

`Settings → Secrets and variables → Actions → New repository secret`

Para Unity Personal:
- `UNITY_LICENSE`: contenido del archivo de licencia `.ulf`.
- `UNITY_EMAIL`: correo de la cuenta Unity.
- `UNITY_PASSWORD`: contraseña de la cuenta Unity.

Para Unity Professional:
- `UNITY_SERIAL`: serial de licencia.
- `UNITY_EMAIL`: correo de la cuenta Unity.
- `UNITY_PASSWORD`: contraseña de la cuenta Unity.

No guardar esos valores en commits ni enviarlos por el chat. El workflow solo los inyecta como variables de entorno en los pasos de Unity.

## 3. Obtener un archivo de solicitud desde GitHub (sin Unity instalado en el teléfono)

1. Abrir la pestaña **Actions** del repositorio.
2. Seleccionar **Request Unity activation file**.
3. Pulsar **Run workflow**.
4. Al terminar, abrir la ejecución y descargar el artefacto `unity-manual-activation-request`.
5. Extraer el archivo `.alf` del ZIP.

El artefacto es solo una solicitud de activación; todavía no es una licencia `.ulf`.

**Importante:** la documentación actual de GameCI advierte que Unity ya no admite la activación manual de licencias Personal en todos los casos. Si el portal de licencias no ofrece Personal o rechaza el archivo, no intentes trucos ni compartas credenciales: será necesario activar Personal mediante Unity Hub en un equipo compatible o utilizar una licencia Professional válida. Este flujo no garantiza que Unity emita una licencia Personal.

Referencia: https://game.ci/docs/github/activation/

## 4. Ejecutar las pruebas

Cuando los secretos estén configurados:
- Un push a la rama `feature/rebuild-unity-android-foundation-2026-11-22` o una actualización del PR #1 dispara el job EditMode automáticamente.
- El resultado esperado es que las seis pruebas EditMode se ejecuten. No se considerarán aprobadas hasta ver el job completado con éxito.

## 5. Generar el APK

Cuando las pruebas pasen:
1. En **Actions**, seleccionar **Unity validation and Android build**.
2. Pulsar **Run workflow** sobre la rama de reconstrucción.
3. Marcar `build_android`.
4. Esperar a que termine el job **Android ARM64 APK**.
5. Descargar el artefacto `futbol-nova-android-apk`.

La salida esperada es `Build/FutbolNova.apk`. El APK solo se considerará entregable después de comprobar que el artefacto existe y que se instala/abre en un dispositivo Android.

## 6. Estado de seguridad y publicación

El PR #1 permanece como borrador y no se ha fusionado. No se debe fusionar hasta que pasen las pruebas y la compilación; la prueba en dispositivo también queda pendiente.
