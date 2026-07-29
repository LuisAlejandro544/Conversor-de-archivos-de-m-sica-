package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AudioDatabase
import com.example.data.AudioRepository
import com.example.data.ConvertedAudioEntity
import com.example.engine.AudioConverterEngine
import com.example.engine.AudioPlayerManager
import com.example.model.AudioFormat
import com.example.model.AudioPreset
import com.example.model.ConversionConfig
import com.example.model.ConversionProgressState
import com.example.model.SourceAudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AudioDatabase.getDatabase(application)
    private val repository = AudioRepository(db.audioDao())
    val converterEngine = AudioConverterEngine(application)
    val playerManager = AudioPlayerManager(application)

    private val _sampleTracks = MutableStateFlow<List<SourceAudioTrack>>(emptyList())
    val sampleTracks: StateFlow<List<SourceAudioTrack>> = _sampleTracks.asStateFlow()

    private val _selectedTrack = MutableStateFlow<SourceAudioTrack?>(null)
    val selectedTrack: StateFlow<SourceAudioTrack?> = _selectedTrack.asStateFlow()

    private val _conversionConfig = MutableStateFlow(ConversionConfig())
    val conversionConfig: StateFlow<ConversionConfig> = _conversionConfig.asStateFlow()

    private val _conversionState = MutableStateFlow<ConversionProgressState>(ConversionProgressState.Idle)
    val conversionState: StateFlow<ConversionProgressState> = _conversionState.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterFormat = MutableStateFlow<String?>(null)
    val filterFormat: StateFlow<String?> = _filterFormat.asStateFlow()

    val historyAudios: StateFlow<List<ConvertedAudioEntity>> = combine(
        repository.allAudios,
        _searchQuery,
        _filterFormat
    ) { list, query, format ->
        list.filter { item ->
            val matchesQuery = query.isBlank() || item.fileName.contains(query, ignoreCase = true)
            val matchesFormat = format == null || item.targetFormat.equals(format, ignoreCase = true)
            matchesQuery && matchesFormat
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playerState = playerManager.playerState

    val defaultPresets = listOf(
        AudioPreset(
            id = "p_high_mp3",
            title = "Alta Fidelidad Música",
            subtitle = "MP3 320 kbps - Excelente para reproducción de canciones",
            targetFormat = AudioFormat.MP3,
            bitrateKbps = 320,
            sampleRateHz = 44100,
            channels = 2,
            category = "Música",
            isRecommended = true
        ),
        AudioPreset(
            id = "p_voice_podcast",
            title = "Voz & Podcast Optimizado",
            subtitle = "AAC 96 kbps Mono - Tamaño de archivo reducido",
            targetFormat = AudioFormat.AAC,
            bitrateKbps = 96,
            sampleRateHz = 32000,
            channels = 1,
            category = "Voz"
        ),
        AudioPreset(
            id = "p_lossless_wav",
            title = "Lossless Estudio WAV",
            subtitle = "PCM 16-bit 48 kHz - Calidad de masterización sin pérdidas",
            targetFormat = AudioFormat.WAV,
            bitrateKbps = 1536,
            sampleRateHz = 48000,
            channels = 2,
            category = "Estudio",
            isRecommended = true
        ),
        AudioPreset(
            id = "p_web_ogg",
            title = "Redes & Web OGG",
            subtitle = "OGG Vorbis 128 kbps - Para reproducción en web y juegos",
            targetFormat = AudioFormat.OGG,
            bitrateKbps = 128,
            sampleRateHz = 44100,
            channels = 2,
            category = "Compartir"
        ),
        AudioPreset(
            id = "p_apple_m4a",
            title = "Apple AAC M4A",
            subtitle = "M4A 256 kbps - Optimizado para iPhone, Mac e iTunes",
            targetFormat = AudioFormat.M4A,
            bitrateKbps = 256,
            sampleRateHz = 44100,
            channels = 2,
            category = "Apple"
        ),
        AudioPreset(
            id = "p_flac_hi_res",
            title = "Audiófilo FLAC",
            subtitle = "Compresión sin pérdidas FLAC 48 kHz",
            targetFormat = AudioFormat.FLAC,
            bitrateKbps = 1536,
            sampleRateHz = 48000,
            channels = 2,
            category = "Estudio"
        )
    )

    init {
        loadSampleTracks()
    }

    private fun loadSampleTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            val samples = converterEngine.prepareSampleTracks()
            _sampleTracks.value = samples
            if (_selectedTrack.value == null && samples.isNotEmpty()) {
                _selectedTrack.value = samples.first()
                updateTrimAndClampConfigForTrack(samples.first())
            }
        }
    }

    fun selectTrack(track: SourceAudioTrack) {
        _selectedTrack.value = track
        updateTrimAndClampConfigForTrack(track)
    }

    private fun updateTrimAndClampConfigForTrack(track: SourceAudioTrack) {
        val currentConfig = _conversionConfig.value
        val maxBitrate = if (track.bitrateKbps in 1..320) track.bitrateKbps else 320
        val maxSampleRate = if (track.sampleRateHz > 0) track.sampleRateHz else 48000

        val clampedBitrate = currentConfig.bitrateKbps.coerceAtMost(maxBitrate).coerceAtLeast(64)
        val clampedSampleRate = currentConfig.sampleRateHz.coerceAtMost(maxSampleRate).coerceAtLeast(22050)

        _conversionConfig.value = currentConfig.copy(
            bitrateKbps = clampedBitrate,
            sampleRateHz = clampedSampleRate,
            trimStartSec = 0.0,
            trimEndSec = track.durationSeconds
        )
    }

    fun inspectCustomUri(uri: Uri) {
        viewModelScope.launch {
            val track = converterEngine.inspectAudioUri(uri)
            _selectedTrack.value = track
            updateTrimAndClampConfigForTrack(track)
        }
    }

    fun setTargetFormat(format: AudioFormat) {
        val track = _selectedTrack.value
        val maxBitrate = if (track != null && track.bitrateKbps in 1..320) track.bitrateKbps else 320
        _conversionConfig.value = _conversionConfig.value.copy(
            targetFormat = format,
            bitrateKbps = if (!format.supportsBitrate) 1536 else _conversionConfig.value.bitrateKbps.coerceAtMost(maxBitrate)
        )
    }

    fun setBitrate(bitrateKbps: Int) {
        val track = _selectedTrack.value
        val maxBitrate = if (track != null && track.bitrateKbps in 1..320) track.bitrateKbps else 320
        _conversionConfig.value = _conversionConfig.value.copy(bitrateKbps = bitrateKbps.coerceAtMost(maxBitrate))
    }

    fun setSampleRate(sampleRateHz: Int) {
        val track = _selectedTrack.value
        val maxSampleRate = if (track != null && track.sampleRateHz > 0) track.sampleRateHz else 48000
        _conversionConfig.value = _conversionConfig.value.copy(sampleRateHz = sampleRateHz.coerceAtMost(maxSampleRate))
    }

    fun setChannels(channels: Int) {
        _conversionConfig.value = _conversionConfig.value.copy(channels = channels)
    }

    fun setVolumeGain(gainDb: Float) {
        _conversionConfig.value = _conversionConfig.value.copy(volumeGainDb = gainDb)
    }

    fun setTrimRange(startSec: Double, endSec: Double, isEnabled: Boolean) {
        _conversionConfig.value = _conversionConfig.value.copy(
            trimStartSec = startSec,
            trimEndSec = endSec,
            isTrimEnabled = isEnabled
        )
    }

    fun applyPreset(preset: AudioPreset) {
        val track = _selectedTrack.value
        val maxBitrate = if (track != null && track.bitrateKbps in 1..320) track.bitrateKbps else 320
        val maxSampleRate = if (track != null && track.sampleRateHz > 0) track.sampleRateHz else 48000

        _conversionConfig.value = _conversionConfig.value.copy(
            targetFormat = preset.targetFormat,
            bitrateKbps = preset.bitrateKbps.coerceAtMost(maxBitrate),
            sampleRateHz = preset.sampleRateHz.coerceAtMost(maxSampleRate),
            channels = preset.channels
        )
        _activeTab.value = 0 // Navigate to converter
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterFormat(format: String?) {
        _filterFormat.value = format
    }

    fun startConversion() {
        val track = _selectedTrack.value ?: return
        val config = _conversionConfig.value

        viewModelScope.launch(Dispatchers.IO) {
            _conversionState.value = ConversionProgressState.Converting(
                progressPercent = 5,
                currentStep = "Iniciando motor de conversión...",
                processedBytes = 0,
                totalBytes = track.sizeBytes,
                estimatedSecRemaining = 3
            )

            try {
                val outputFile = converterEngine.convertAudio(
                    sourceTrack = track,
                    config = config,
                    onProgress = { percent, step, processed, total ->
                        _conversionState.value = ConversionProgressState.Converting(
                            progressPercent = percent,
                            currentStep = step,
                            processedBytes = processed,
                            totalBytes = total,
                            estimatedSecRemaining = ((100 - percent) / 30).coerceAtLeast(1)
                        )
                    }
                )

                val newSize = outputFile.length()
                val effectiveDuration = if (config.isTrimEnabled && config.trimEndSec > config.trimStartSec) {
                    config.trimEndSec - config.trimStartSec
                } else {
                    track.durationSeconds
                }

                val entity = ConvertedAudioEntity(
                    fileName = outputFile.name,
                    originalFormat = track.format,
                    targetFormat = config.targetFormat.displayName,
                    outputFilePath = outputFile.absolutePath,
                    fileSizeBytes = newSize,
                    originalSizeBytes = track.sizeBytes,
                    durationSeconds = effectiveDuration,
                    bitrateKbps = config.bitrateKbps,
                    sampleRateHz = config.sampleRateHz,
                    channels = config.channels,
                    presetUsed = "Preset " + config.targetFormat.displayName
                )

                val id = repository.insert(entity).toInt()

                _conversionState.value = ConversionProgressState.Success(
                    entityId = id,
                    fileName = outputFile.name,
                    targetFormat = config.targetFormat.displayName,
                    outputFilePath = outputFile.absolutePath,
                    originalSize = track.sizeBytes,
                    newSize = newSize,
                    durationSec = effectiveDuration
                )

            } catch (e: Exception) {
                _conversionState.value = ConversionProgressState.Error(
                    message = e.localizedMessage ?: "Error inesperado durante la conversión de audio."
                )
            }
        }
    }

    fun dismissConversionState() {
        _conversionState.value = ConversionProgressState.Idle
    }

    fun toggleFavorite(audio: ConvertedAudioEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(audio.id, audio.isFavorite)
        }
    }

    fun deleteConvertedAudio(audio: ConvertedAudioEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(audio.outputFilePath)
                if (file.exists()) file.delete()
            } catch (_: Exception) { }
            repository.delete(audio)
        }
    }

    fun playAudio(filePath: String, name: String) {
        playerManager.playFile(filePath, name)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Int) {
        playerManager.seekTo(positionMs)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.stop()
    }
}
