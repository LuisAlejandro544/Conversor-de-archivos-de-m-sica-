# 🎧 AudioLabs - Conversor de Audio Profesional para Android

**AudioLabs** es una aplicación móvil nativa para Android construida con **Kotlin**, **Jetpack Compose** y **Material Design 3**, diseñada para la conversión, procesamiento y gestión de archivos de audio localmente, con una interfaz de estudio sobria y profesional.

---

## 📸 Características Principales

- 🔄 **Conversión Multiformato**: Convierte archivos de audio entre formatos **MP3, WAV, AAC, M4A, OGG, FLAC, OPUS y AIFF**.
- 🎚️ **Ajuste Fino de Calidad (DSP)**:
  - Tasa de bits personalizable (*Bitrate*: 64 kbps hasta 320 kbps).
  - Frecuencia de muestreo (*Sample Rate*: 22.05 kHz, 32 kHz, 44.1 kHz, 48 kHz).
  - Configuración de canales (*Estéreo* / *Mono*).
  - Ajuste de ganancia de volumen (-6 dB a +6 dB).
- 🛡️ **Detección y Protección Inteligente de Calidad**: Analiza automáticamente los parámetros técnicos del audio de origen (bitrate y frecuencia de muestreo) y limita las opciones superiores para evitar el re-muestreo o inflar artificialmente el tamaño del archivo.
- ⚙️ **Integración CI/CD con GitHub Actions**:
  - `android_build.yml`: Compila automáticamente el APK de Debug con caché de dependencias y generación dinámica de `debug.keystore`.
  - `process_zip.yml`: Auto-extractor automático. Detecta archivos `.zip` subidos a la carpeta `/Zip/`, extrae y reemplaza los archivos modificados en el repositorio, elimina el `.zip` y realiza el commit automático.
- ✂️ **Recorte de Audio (*Audio Trimmer*)**: Selector visual de rango con slider de precisión para cortar fragmentos de inicio y fin.
- 🎛️ **Presets Profesionales**: Configuración con un solo toque optimizada para Música (Alta Fidelidad), Podcast/Voz, Estudio Lossless (WAV/FLAC) y Web.
- 📁 **Gestor e Historial Local**: Base de datos **Room** para persistencia local de audios convertidos, filtrado por formato, búsqueda, marcación de favoritos y compartir.
- 🎵 **Reproductor Integrado**: Reproducción fluida en segundo plano con control de barra de progreso (*seeking*) y tiempo en tiempo real.
- 📊 **Inspector Técnico de Audio**: Análisis de frecuencias, tamaño original vs final, ratio de compresión y visualizador de espectro acústico.
- ⚡ **Motor de Decodificación & DSP Nativo C++ (NDK + AudioTrack)**: Motor nativo C++17 de alto rendimiento integrado vía JNI (`native_cpp`) para procesamiento de ganancia DSP, resampleo e integración con `AudioTrack`.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología / Librería |
| :--- | :--- |
| **Lenguaje Principal** | Kotlin 2.x |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Arquitectura** | MVVM + Clean Architecture / Unidirectional Data Flow |
| **Persistencia Local** | Room Database (SQLite) + Flow |
| **Transcodificación & Media** | **AndroidX Media3 Transformer** (`media3-transformer`, `media3-exoplayer`, `media3-common`) |
| **Motor Rust (Audio DSP)** | `native_rust` (`symphonia` 0.5 para decodificación universal, `hound` 3.5 para WAV, `rubato` 0.14 para resampleo) |
| **Motor Nativo C / C++** | C11 (`native_c`) y C++17 (`native_cpp` vía CMake, vinculado a NDK `OpenSLES` y `log`) |
| **Imágenes & Assets** | Coil Compose |

---

## 🚀 Requisitos de Compilación e Instalación

### Requisitos Previos
- Android Studio Ladybug (o superior) / Android SDK 36 (minSdk 24).
- JDK 11 o JDK 17.
- Gradle (Gestionado automáticamente por el wrapper del entorno).

### Pasos para Ejecutar
1. Clonar o abrir el repositorio en Android Studio.
2. Abrir la terminal del proyecto y ejecutar la verificación de compilación:
   ```bash
   gradle assembleDebug
   ```
3. Instalar en el dispositivo o emulador conectado.

---

## 🎨 Paleta de Diseño Studio

El diseño de AudioLabs utiliza un estilo de **estudio de grabación profesional y sobrio**, evitando intencionadamente colores neón, cyberpunk o hiper-futuristas:
- **Slate 900 / 800 (`#0F172A` / `#1E293B`)**: Fondos oscuros de panel y tarjetas de control.
- **Studio Cobalt (`#1E56A0` / `#3B82F6`)**: Color primario de acento profesional.
- **Audio Green (`#059669`)**: Indicador de éxito, espacio ahorrado y archivos guardados.
- **Audio Amber (`#D97706`)**: Presets destacados y estado de favoritos.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.
