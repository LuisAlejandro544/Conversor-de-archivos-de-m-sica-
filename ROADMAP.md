# 🗺️ AudioLabs Roadmap & Plan de Desarrollo

Este documento detalla el estado actual del desarrollo de **AudioLabs**, las funcionalidades implementadas y la hoja de ruta de futuras fases.

---

## ✅ Fase 1: Fundamentos y UI Core (Completado)
- [x] **Arquitectura Base**: Implementación del patrón MVVM con `AudioViewModel`, `AudioRepository` y `StateFlow`.
- [x] **Interfaz de Usuario Jetpack Compose**:
  - `StudioHeaderBar`: Encabezado con contador de audios convertidos y banner publicitario/promocional de estudio.
  - `ConverterScreen`: Formato destino, bitrates, sample rates, canales, ajuste dB de volumen y recortador con slider.
  - `PresetsScreen`: Configuración de 1 toque para Música, Podcast, Lossless y Mensajes.
  - `FilesHistoryScreen`: Lista de audios convertidos con búsqueda, filtro de formato, marcación de favoritos y compartir vía `FileProvider`.
  - `AudioInspectorScreen`: Vista técnica de metadatos y gráfico interactivo de espectro de frecuencia.
  - `AudioPlayerBottomBar`: Barra flotante inferior de reproducción de audio con seekbar e indicador min:seg.
  - `ConversionProgressDialog`: Diálogo con animación de ondas (*audio waveform*) y desglose de ahorro de espacio.
- [x] **Persistencia de Datos**: Integración de **Room Database** (`ConvertedAudioDao`, `ConvertedAudioEntity`).
- [x] **Motor de Conversión PCM**:
  - Simulación y procesamiento de decodificación/recodificación mediante `MediaMetadataRetriever` y generación de cabeceras de onda (*WAV WAVHEADER*) y archivos locales reales en `cacheDir`.

---

## ⚡ Fase 2: Integración Nativa NDK, C/C++, Rust & Media3 (Completado)
- [x] **Framework de Transcodificación AndroidX Media3**:
  - `media3-transformer`: Pipeline para conversión de códecs hardware/software (AAC, MP3, AMR, Opus).
  - `media3-exoplayer` & `media3-common`: Motor de reproducción de audio y gestión de fuentes multimedia.
- [x] **Estructura C / C++ NDK**:
  - `CMakeLists.txt` configurado para C11 y C++17, enlazado con librerías nativas de audio `OpenSLES` y `log`.
  - Módulos `native_c` y `native_cpp` compilados con Ninja y CMake en Gradle.
- [x] **Estructura Rust JNI & Crates Audio**:
  - Módulo `native_rust` en `Cargo.toml` con `jni` (0.21), `android_logger` (0.13), `log` (0.4).
  - Integración de `symphonia` (0.5) para decodificación universal de contenedores/códecs, `hound` (3.5) para manipulación PCM/WAV y `rubato` (0.14) para procesamiento resampler/DSP.
- [x] **JNI Bridge & Motor C++ Nativo (AudioTrack + C++)**:
  - `NativeBridge.kt` para la carga e interop JNI de la librería nativa `native_cpp`.
  - Implementación en C++ (`native_cpp.cpp`) de funciones JNI de procesamiento de ganancia PCM, resampleo e interpolación lineal, y síntesis de buffers de audio para integración con `AudioTrack` y `AudioConverterEngine`.
- [x] **Protección de Calidad de Origen**: Inspección automática de parámetros del origen (`bitrateKbps` y `sampleRateHz`) y desactivación de opciones superiores para evitar sobremuestreo e inflado artificial del archivo.
- [x] **Pipeline de Integración Continua (CI/CD)**: Flujo de GitHub Actions (`android_build.yml`) con generación de `debug.keystore` al vuelo, caché de Gradle/Rust y compilación automatizada del APK de debug.

---

## 🚀 Fase 3: Próximos Milenarios (En Planificación)

### 3.1 Procesamiento DSP Nativo Real (C++ & FFmpeg NDK)
- [ ] Integración de binarios NDK FFmpeg o libmp3lame / libopus nativos para codificación multiformato directo por hardware.
- [ ] Implementación de filtros de ecualización paramétrica de 5 bandas en C++.

### 3.2 Lote de Conversión (*Batch Processing*)
- [ ] Selección múltiple de archivos de audio desde la galería/almacenamiento.
- [ ] Cola de conversión en segundo plano utilizando `WorkManager` con notificación persistente.

### 3.3 Herramientas Avanzadas de Edición de Audio
- [ ] Eliminación de ruido de fondo (*Noise Gate* / *De-noiser*) usando filtros DSP en Rust.
- [ ] Fundido de entrada y salida (*Fade-In / Fade-Out*).
- [ ] Unificación / Fusión de múltiples pistas de audio (*Audio Joiner*).

### 3.4 Sincronización en la Nube & Integración
- [ ] Exportación directa a Google Drive / Dropbox.
- [ ] Copia de respaldo automática de la base de datos de historial.
