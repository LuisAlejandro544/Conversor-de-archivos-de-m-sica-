# 🤖 Rules for AI Agents Working on AudioLabs

Este archivo establece las reglas de comportamiento y restricciones para agentes IA que interactúen con el repositorio de **AudioLabs**.

---

## 🚫 Restricciones Críticas
1. **NO Eliminar Archivos de Configuración Core**: No eliminar ni alterar destructivamente `build.gradle.kts`, `settings.gradle.kts`, `AndroidManifest.xml` ni `metadata.json`.
2. **NO Modificar `applicationId` Existente**: La configuración en `app/build.gradle.kts` debe mantener la identidad configurada (`com.aistudio.audiolabs.xzwvks`).
3. **NO Usar `local.properties`**: Las claves o configuraciones de entorno deben manejarse vía variables de entorno o `BuildConfig`.
4. **Respetar el Estilo Visual Studio**: No sustituir los colores sobrios por temas fluorescentes ni componentes inconsistentes. Mantener el uso de Material 3 (`MaterialTheme.colorScheme`).

---

## 📋 Protocolo de Cambios en Código
1. **Lectura Previa**: Usar siempre `view_file` para inspeccionar el contenido real de un archivo antes de modificarlo.
2. **Uso de Herramientas Dedicadas**: Preferir `view_file` y `list_dir` sobre comandos `cat` o `ls` en terminal.
3. **Verificación de Compilación**: Ejecutar `compile_applet` tras realizar un conjunto de cambios para validar que no haya errores de sintaxis o importación.
