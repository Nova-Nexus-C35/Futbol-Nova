# Configurar Unity CI y generar el APK Android

Estado: bloqueado por la activación de Unity. No pegar licencias, contraseñas ni seriales en conversaciones o archivos del repositorio.

## 1. Qué confirmó GitHub Actions

- Ejecución #9 (run ID `45064539126`): falló porque no existía `UNITY_LICENSE`.
- Ejecución #19 (run ID `45064638129`): falló porque también faltaban `UNITY_EMAIL` y `UNITY_PASSWORD`.
- Ejecución #25 (run ID `45064708965`): volvió a fallar en el mismo preflight de credenciales.
- En todas, Unity Editor y las pruebas no llegaron a ejecutarse; el job Android fue omitido.

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

## 3. Obtener la licencia Personal

La guía actual de GameCI indica que se debe activar Unity Personal en Unity Hub y copiar el archivo `.ulf`. La ruta depende del sistema operativo:

- Windows: `C:\ProgramData\Unity\Unity_lic.ulf`
- macOS: `/Library/Application Support/Unity/Unity_lic.ulf`
- Linux: `~/.local/share/unity3d/Unity/Unity_lic.ulf`

La documentación actual ya no recomienda el antiguo workflow de solicitud manual `.alf`; se probó en este repositorio y GitHub devolvió que la acción está obsoleta. Ese workflow fue eliminado para no dejar un procedimiento roto.

Como trabajas desde Android y no tienes PC, la activación Personal no se puede completar únicamente desde este chat ni desde el teléfono: hace falta acceso temporal a un equipo compatible con Unity Hub para generar el `.ulf`, o disponer de una licencia Professional válida. Si utilizas un equipo prestado, inicia sesión solo en Unity Hub, activa la licencia, copia el archivo y cierra sesión al terminar. Nunca compartas la contraseña en el chat.

Referencia actual: https://game.ci/docs/github/activation/

## 4. Ejecutar las pruebas

Cuando los secretos estén configurados:
- Un push a la rama `feature/rebuild-unity-android-foundation-2026-11-22` o una actualización del PR #1 dispara el job EditMode automáticamente.
- Las seis pruebas EditMode solo se considerarán aprobadas cuando el job termine con éxito.

## 5. Generar el APK

Cuando las pruebas pasen:
1. En **Actions**, seleccionar **Unity validation and Android build**.
2. Pulsar **Run workflow** sobre la rama de reconstrucción.
3. Marcar `build_android`.
4. Esperar a que termine el job **Android ARM64 APK**.
5. Descargar el artefacto `futbol-nova-android-apk`.

La salida esperada es `Build/FutbolNova.apk`. El APK solo se considerará entregable después de comprobar que el artefacto existe y que se instala/abre en un dispositivo Android.

## 6. Estado de publicación

El PR #1 permanece como borrador y no se ha fusionado. No se debe fusionar hasta que pasen las pruebas, la compilación y la prueba en dispositivo.
