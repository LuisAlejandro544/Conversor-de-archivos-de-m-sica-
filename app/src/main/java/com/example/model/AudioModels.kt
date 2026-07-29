package com.example.model

import android.net.Uri

enum class AudioFormat(
    val extension: String,
    val displayName: String,
    val mimeType: String,
    val description: String,
    val supportsBitrate: Boolean = true
) {
    MP3("mp3", "MP3", "audio/mpeg", "Estándar universal, excelente compatibilidad y compresión equilibrada."),
    WAV("wav", "WAV", "audio/wav", "Sin pérdida PCM (Uncompressed), máxima calidad de estudio profesional.", supportsBitrate = false),
    AAC("aac", "AAC", "audio/aac", "Compresión avanzada, superior a MP3 a igual o menor tasa de bits."),
    M4A("m4a", "M4A", "audio/mp4", "Formato optimizado para dispositivos Apple, podcasts y streaming."),
    OGG("ogg", "OGG", "audio/ogg", "Código abierto Vorbis, ideal para juegos, web y reproductores libres."),
    FLAC("flac", "FLAC", "audio/flac", "Compresión sin pérdida de alta fidelidad para audiófilos y archivos.", supportsBitrate = false),
    OPUS("opus", "OPUS", "audio/opus", "Códec de ultra baja latencia optimizado para llamadas y voz."),
    AIFF("aiff", "AIFF", "audio/aiff", "Sin pérdida de estándar broadcast profesional y producción de audio.", supportsBitrate = false)
}

data class SourceAudioTrack(
    val id: String,
    val name: String,
    val uri: Uri?,
    val localFilePath: String?,
    val format: String,
    val sizeBytes: Long,
    val durationSeconds: Double,
    val sampleRateHz: Int = 44100,
    val channels: Int = 2,
    val bitrateKbps: Int = 320,
    val isSample: Boolean = false
)

data class AudioPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetFormat: AudioFormat,
    val bitrateKbps: Int,
    val sampleRateHz: Int,
    val channels: Int, // 1 = Mono, 2 = Stereo
    val category: String, // "Música", "Voz", "Estudio", "Sistema"
    val isRecommended: Boolean = false
)

data class ConversionConfig(
    val targetFormat: AudioFormat = AudioFormat.MP3,
    val bitrateKbps: Int = 192,
    val sampleRateHz: Int = 44100,
    val channels: Int = 2, // 1 = Mono, 2 = Stereo
    val volumeGainDb: Float = 0f, // -6dB to +6dB
    val trimStartSec: Double = 0.0,
    val trimEndSec: Double = 0.0, // 0.0 means full duration
    val isTrimEnabled: Boolean = false
)

sealed interface ConversionProgressState {
    data object Idle : ConversionProgressState
    data class Converting(
        val progressPercent: Int,
        val currentStep: String,
        val processedBytes: Long,
        val totalBytes: Long,
        val estimatedSecRemaining: Int
    ) : ConversionProgressState
    data class Success(
        val entityId: Int,
        val fileName: String,
        val targetFormat: String,
        val outputFilePath: String,
        val originalSize: Long,
        val newSize: Long,
        val durationSec: Double
    ) : ConversionProgressState
    data class Error(val message: String) : ConversionProgressState
}
