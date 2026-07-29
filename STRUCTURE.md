# 📂 Estructura del Proyecto AudioLabs

Visión general de la organización de directorios, paquetes y módulos del proyecto **AudioLabs**.

```
app/
├── build.gradle.kts                   # Configuración del módulo Android Gradle & NDK CMake
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml        # Manifiesto (Permisos de lectura/escritura/almacenamiento)
│   │   ├── cpp/                       # Módulos NDK en C y C++
│   │   │   ├── CMakeLists.txt         # Script de compilación CMake
│   │   │   ├── native_c.h             # Encabezado del módulo C
│   │   │   ├── native_c.c             # Implementación C
│   │   │   ├── native_cpp.h           # Encabezado del módulo C++
│   │   │   └── native_cpp.cpp         # Implementación C++
│   │   ├── rust/                      # Módulo Crate de Rust
│   │   │   ├── Cargo.toml             # Manifiesto de dependencias Rust (jni, symphonia, hound, rubato)
│   │   │   └── src/
│   │   │       └── lib.rs             # Módulo principal Rust JNI
│   │   └── java/com/example/          # Código fuente Kotlin
│   │       ├── MainActivity.kt        # Actividad principal con Scaffold y navegación por pestañas
│   │       ├── data/                  # Capa de datos y persistencia
│   │       │   ├── AppDatabase.kt     # Base de datos Room
│   │       │   ├── ConvertedAudioDao.kt # DAO para consultas SQLite
│   │       │   └── ConvertedAudioEntity.kt # Entidad de audio procesado
│   │       ├── engine/                # Motores de Audio y Reproductores
│   │       │   ├── AudioConverterEngine.kt # Lógica de conversión de audio (WAV, PCM, simulado)
│   │       │   └── AudioPlayerManager.kt  # Administrador de reproducción MediaPlayer
│   │       ├── model/                 # Modelos de dominio y enums
│   │       │   ├── AudioFormat.kt     # Enums de formatos (MP3, WAV, AAC, etc.)
│   │       │   ├── AudioPreset.kt     # Ajustes preestablecidos
│   │       │   ├── ConversionConfig.kt # Data class con bitrate, samplerate, gain, trim
│   │       │   ├── ConversionProgressState.kt # Sealed class para UI State del proceso
│   │       │   └── SourceAudioTrack.kt # Pista seleccionada para procesar
│   │       ├── native/                # Capa de puente con C/C++/Rust
│   │       │   └── NativeBridge.kt    # Carga de librerías .so nativas
│   │       ├── repository/            # Repositorio de datos
│   │       │   └── AudioRepository.kt # Coordinación entre Room DAO y el Motor de Conversión
│   │       └── ui/                    # Interfaz de Usuario (Compose)
│   │           ├── AudioViewModel.kt  # ViewModel central de estado UI
│   │           ├── components/        # Componentes UI reutilizables
│   │           │   ├── AudioPlayerBottomBar.kt # Reproductor inferior flotante
│   │           │   ├── ConversionProgressDialog.kt # Diálogo con animación de espectro
│   │           │   ├── FormatTag.kt            # Chip de formato estilizado
│   │           │   ├── StudioHeaderBar.kt      # Barra superior de marca
│   │           │   └── UIFormatters.kt         # Formateadores de bytes y tiempo (min:seg)
│   │           ├── screens/           # Pantallas principales
│   │           │   ├── AudioInspectorScreen.kt # Inspector de frecuencias y metadatos
│   │           │   ├── ConverterScreen.kt      # Pantalla principal del conversor
│   │           │   ├── FilesHistoryScreen.kt   # Historial y lista de archivos
│   │           │   └── PresetsScreen.kt        # Presets preconfigurados
│   │           └── theme/             # Sistema de temas Material Design 3
│   │               ├── Color.kt       # Paleta de colores Studio
│   │               └── Theme.kt       # Definición del tema Compose
```

---

## 🏛️ Descripción de Módulos y Capas

1. **`ui/` (Presentation Layer)**:
   - Pantallas compuestas con Jetpack Compose en un flujo estricto Unidireccional de Datos (*UDF*).
   - Uso de `AudioViewModel` con `StateFlow` y `collectAsStateWithLifecycle()`.

2. **`engine/` (Processing Layer)**:
   - `AudioConverterEngine`: Encargado de tomar un archivo de entrada (local o demo asset), inspeccionar duración/canales/frecuencias y generar el archivo convertido procesado.
   - `AudioPlayerManager`: Control del ciclo de vida del `android.media.MediaPlayer`.

3. **`data/` & `repository/` (Persistence Layer)**:
   - Persistencia local mediante **Room SQLite**. Permite realizar búsquedas por nombre, filtrado por formato y marcar favoritos sin bloquear el hilo principal.

4. **`cpp/` & `rust/` & `native/` (Native Layer)**:
   - Integración interoperable para ejecutar rutinas de alto rendimiento en C, C++ y Rust vía JNI.
