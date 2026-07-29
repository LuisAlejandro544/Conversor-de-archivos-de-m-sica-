# Carpeta Zip

Coloca cualquier archivo `.zip` en esta carpeta y haz commit/push a GitHub.

El Action `.github/workflows/process_zip.yml` detectará el archivo `.zip`, extraerá y sobrescribirá los archivos actualizados en el proyecto, eliminará el archivo `.zip` y realizará un commit automático con los cambios aplicados.
