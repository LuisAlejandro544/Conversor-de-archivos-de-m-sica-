# 🤖 AI Agent Context & Codebase Guidelines for AudioLabs

Bienvenido al proyecto **AudioLabs**. Este documento sirve como guía contextual rápida para cualquier agente de Inteligencia Artificial o asistente de desarrollo que trabaje en esta base de código.

---

## 🎯 Propósito del Proyecto
AudioLabs es una aplicación móvil nativa Android para la **conversión y procesamiento técnico de audio**. Mantiene un estándar visual sobrio y profesional (estilo estudio de sonido/grabación) en Jetpack Compose, con capacidades de interoperabilidad nativa (C, C++, Rust).

---

## 📐 Reglas Arquitectónicas Obligatorias

1. **Estado Unidireccional (*Unidirectional Data Flow*)**:
   - Todo el estado UI reside en `AudioViewModel`.
   - La interfaz consumirá los estados mediante `StateFlow` y `collectAsStateWithLifecycle()`.
   - Las pantallas no deben contener lógica de negocio; invocan funciones explícitas expuestas por el ViewModel.

2. **Interoperabilidad Nativa & Librerías de Media**:
   - **Media3 Transformer**: Utilizado en Kotlin (`androidx.media3:media3-transformer`) para operaciones de recodificación hardware y composición multimedia.
   - **C/C++ NDK**: Archivos C (`src/main/cpp/native_c.*`) y C++ (`src/main/cpp/native_cpp.*`) vinculados a `OpenSLES` y `log` en `CMakeLists.txt`.
   - **Rust Crate**: Cargo configura `symphonia` (decodificación universal de audio), `hound` (manipulación WAV PCM) y `rubato` (resampling de frecuencia de muestreo). `NativeBridge.kt` carga las librerías dinámicas.

3. **Pruebas y Verificación de Compilación**:
   - Ejecuta siempre `compile_applet` tras modificar dependencias o archivos Kotlin para asegurar que la app compila sin errores.

4. **Identidad Visual & M3**:
   - Mantener el esquema de colores Studio (`StudioCobalt`, `StudioDarkCanvas`, `AudioGreen`, `AudioAmber`).
   - Todos los componentes interactivos deben mantener un área táctil mínima de 48dp y atributos `testTag` para identificabilidad.

---

## 🔑 Glosario de Clases y Archivos Clave

- `MainActivity.kt`: Punto de entrada, contenedor `Scaffold` con barra de navegación inferior y reproductor emergente.
- `AudioViewModel.kt`: Maneja el flujo de conversión, presets, búsqueda, reproductor, clamping automático de parámetros de calidad (limitados por la fuente) y estado del diálogo.
- `NativeBridge.kt`: Capa de puente JNI para interoperabilidad nativa C++ (`native_cpp`), exponiendo funciones de ganancia DSP, resampleo e integración con `AudioTrack`.
- `AudioConverterEngine.kt`: Realiza las transformaciones de audio en segundo plano mediante Coroutines e inspección con `MediaMetadataRetriever` y aceleración C++ nativa para DSP.
- `.github/workflows/android_build.yml`: Pipeline CI/CD para compilación de APK Debug con generación dinámica de `debug.keystore`.
- `ConvertedAudioEntity.kt`: Representación de la tabla `converted_audios` en Room.
- `AudioFormat.kt`: Enum con todos los formatos soportados (MP3, WAV, AAC, M4A, OGG, FLAC, OPUS, AIFF) y sus metadatos.
