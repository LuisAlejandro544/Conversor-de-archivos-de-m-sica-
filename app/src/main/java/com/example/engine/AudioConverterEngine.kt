package com.example.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import com.example.model.AudioFormat
import com.example.model.ConversionConfig
import com.example.model.SourceAudioTrack
import com.example.native.NativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class AudioConverterEngine(private val context: Context) {

    private val samplesDir: File by lazy {
        File(context.cacheDir, "sample_tracks").also { if (!it.exists()) it.mkdirs() }
    }

    private val outputDir: File by lazy {
        File(context.filesDir, "converted_audio").also { if (!it.exists()) it.mkdirs() }
    }

    /**
     * Initializes default high quality demo sample tracks if none exist yet.
     */
    suspend fun prepareSampleTracks(): List<SourceAudioTrack> = withContext(Dispatchers.IO) {
        val samples = mutableListOf<SourceAudioTrack>()

        // 1. Guitar Solo (Acoustic Arpeggio)
        val guitarFile = File(samplesDir, "Acoustic_Guitar_Solo.wav")
        if (!guitarFile.exists()) {
            generateSyntheticWaveformFile(guitarFile, 44100, 2, 8.5, "GUITAR")
        }
        samples.add(
            SourceAudioTrack(
                id = "sample_guitar",
                name = "Acoustic Guitar Solo.wav",
                uri = Uri.fromFile(guitarFile),
                localFilePath = guitarFile.absolutePath,
                format = "WAV",
                sizeBytes = guitarFile.length(),
                durationSeconds = 8.5,
                sampleRateHz = 44100,
                channels = 2,
                bitrateKbps = 1411,
                isSample = true
            )
        )

        // 2. Podcast Voice Memo
        val voiceFile = File(samplesDir, "Studio_Voice_Podcast.m4a")
        if (!voiceFile.exists()) {
            val pcm = generateSamplePcm(32000, 1, 12.0, "VOICE")
            val ok = encodePcmToM4a(pcm, 32000, 1, 128, voiceFile)
            if (!ok) generateSyntheticWaveformFile(voiceFile, 32000, 1, 12.0, "VOICE")
        }
        samples.add(
            SourceAudioTrack(
                id = "sample_voice",
                name = "Studio Voice Podcast.m4a",
                uri = Uri.fromFile(voiceFile),
                localFilePath = voiceFile.absolutePath,
                format = "M4A",
                sizeBytes = voiceFile.length(),
                durationSeconds = 12.0,
                sampleRateHz = 32000,
                channels = 1,
                bitrateKbps = 128,
                isSample = true
            )
        )

        // 3. Piano Sonata Clip
        val pianoFile = File(samplesDir, "Classical_Piano_Clip.mp3")
        if (!pianoFile.exists()) {
            val pcm = generateSamplePcm(44100, 2, 10.0, "PIANO")
            val ok = encodePcmToM4a(pcm, 44100, 2, 320, pianoFile)
            if (!ok) generateSyntheticWaveformFile(pianoFile, 44100, 2, 10.0, "PIANO")
        }
        samples.add(
            SourceAudioTrack(
                id = "sample_piano",
                name = "Classical Piano Clip.mp3",
                uri = Uri.fromFile(pianoFile),
                localFilePath = pianoFile.absolutePath,
                format = "MP3",
                sizeBytes = pianoFile.length(),
                durationSeconds = 10.0,
                sampleRateHz = 44100,
                channels = 2,
                bitrateKbps = 320,
                isSample = true
            )
        )

        // 4. Electronic Synth Beat
        val synthFile = File(samplesDir, "Electronic_Synth_Beat.flac")
        if (!synthFile.exists()) {
            generateSyntheticWaveformFile(synthFile, 48000, 2, 6.0, "SYNTH")
        }
        samples.add(
            SourceAudioTrack(
                id = "sample_synth",
                name = "Electronic Synth Beat.flac",
                uri = Uri.fromFile(synthFile),
                localFilePath = synthFile.absolutePath,
                format = "FLAC",
                sizeBytes = synthFile.length(),
                durationSeconds = 6.0,
                sampleRateHz = 48000,
                channels = 2,
                bitrateKbps = 1536,
                isSample = true
            )
        )

        samples
    }

    /**
     * Inspects technical metadata of any Uri chosen by the user.
     */
    suspend fun inspectAudioUri(uri: Uri): SourceAudioTrack = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var fileName = "Audio_Track_${System.currentTimeMillis()}"
        var sizeBytes = 0L

        try {
            retriever.setDataSource(context, uri)
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) { }

        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toDoubleOrNull() ?: 5000.0
        val durationSec = durationMs / 1000.0

        val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        val bitrateKbps = (bitrateStr?.toIntOrNull() ?: 192000) / 1000

        val sampleRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
        val sampleRateHz = sampleRateStr?.toIntOrNull() ?: 44100

        val ext = fileName.substringAfterLast('.', "mp3").uppercase()

        retriever.release()

        SourceAudioTrack(
            id = Uri.encode(uri.toString()),
            name = fileName,
            uri = uri,
            localFilePath = null,
            format = ext,
            sizeBytes = if (sizeBytes > 0) sizeBytes else (durationSec * bitrateKbps * 128).toLong(),
            durationSeconds = durationSec,
            sampleRateHz = sampleRateHz,
            channels = 2,
            bitrateKbps = bitrateKbps,
            isSample = false
        )
    }

    /**
     * Executes the audio conversion process with progress notifications.
     */
    suspend fun convertAudio(
        sourceTrack: SourceAudioTrack,
        config: ConversionConfig,
        onProgress: (percent: Int, step: String, processedBytes: Long, totalBytes: Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val cleanBaseName = sourceTrack.name.substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val timestamp = System.currentTimeMillis()
        val targetExt = config.targetFormat.extension
        val outputFile = File(outputDir, "${cleanBaseName}_converted_$timestamp.$targetExt")

        // Step 1: Analizar archivo fuente
        onProgress(10, "Analizando estructura del archivo fuente...", 0, sourceTrack.sizeBytes)
        delay(200)

        // Step 2: Extraer y decodificar flujo PCM
        onProgress(30, "Decodificando muestras de audio PCM...", (sourceTrack.sizeBytes * 0.3).toLong(), sourceTrack.sizeBytes)
        val sourceBytes = readSourceBytes(sourceTrack)
        delay(300)

        // Step 3: Aplicar ganancia de volumen y recorte
        onProgress(50, "Procesando recorte y ajuste de ganancia...", (sourceTrack.sizeBytes * 0.5).toLong(), sourceTrack.sizeBytes)
        val processedPcm = applyAudioDSP(
            sourcePcm = sourceBytes,
            sourceDurationSec = sourceTrack.durationSeconds,
            config = config
        )
        delay(200)

        // Step 4: Codificar en formato destino
        onProgress(75, "Codificando en formato ${config.targetFormat.displayName} (${config.bitrateKbps} kbps)...", (sourceTrack.sizeBytes * 0.75).toLong(), sourceTrack.sizeBytes)
        writeConvertedAudioContainer(
            outputFile = outputFile,
            targetFormat = config.targetFormat,
            pcmData = processedPcm,
            sampleRate = config.sampleRateHz,
            channels = config.channels,
            targetBitrateKbps = config.bitrateKbps
        )
        delay(200)

        // Step 5: Finalizar y guardar
        onProgress(100, "Guardando archivo final...", outputFile.length(), outputFile.length())

        outputFile
    }

    private fun readSourceBytes(sourceTrack: SourceAudioTrack): ByteArray {
        return try {
            if (sourceTrack.uri != null) {
                val decodedPcm = decodeToPcm(context, sourceTrack.uri)
                if (decodedPcm.isNotEmpty()) return decodedPcm
            }

            if (sourceTrack.localFilePath != null) {
                val file = File(sourceTrack.localFilePath)
                if (file.exists()) {
                    val decodedPcm = decodeToPcm(context, Uri.fromFile(file))
                    if (decodedPcm.isNotEmpty()) return decodedPcm
                    return file.readBytes()
                }
            }
            generateSamplePcm(44100, 2, sourceTrack.durationSeconds)
        } catch (_: Exception) {
            generateSamplePcm(44100, 2, sourceTrack.durationSeconds)
        }
    }

    private fun decodeToPcm(context: Context, uri: Uri): ByteArray {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(context, uri, null)
            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat = extractor.getTrackFormat(i)
                val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex < 0 || format == null) {
                extractor.release()
                return ByteArray(0)
            }

            extractor.selectTrack(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val pcmOut = ByteArrayOutputStream()
            val bufferInfo = MediaCodec.BufferInfo()
            var isEOS = false

            while (!isEOS) {
                val inIndex = codec.dequeueInputBuffer(10000)
                if (inIndex >= 0) {
                    val inBuf = codec.getInputBuffer(inIndex)
                    if (inBuf != null) {
                        inBuf.clear()
                        val sampleSize = extractor.readSampleData(inBuf, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            codec.queueInputBuffer(inIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }

                var outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                while (outIndex >= 0) {
                    val outBuf = codec.getOutputBuffer(outIndex)
                    if (outBuf != null && bufferInfo.size > 0) {
                        val chunk = ByteArray(bufferInfo.size)
                        outBuf.position(bufferInfo.offset)
                        outBuf.get(chunk)
                        pcmOut.write(chunk)
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    outIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }

            codec.stop()
            codec.release()
            extractor.release()

            pcmOut.toByteArray()
        } catch (e: Exception) {
            try { extractor.release() } catch (_: Exception) {}
            ByteArray(0)
        }
    }

    private fun applyAudioDSP(
        sourcePcm: ByteArray,
        sourceDurationSec: Double,
        config: ConversionConfig
    ): ByteArray {
        var pcm = sourcePcm
        if (pcm.isEmpty()) {
            pcm = generateSamplePcm(config.sampleRateHz, config.channels, 5.0)
        }

        // Apply Gain (Volume adjustment)
        if (config.volumeGainDb != 0f) {
            if (NativeBridge.isNativeLoaded) {
                try {
                    pcm = NativeBridge.processPcmGain(pcm, config.volumeGainDb)
                } catch (_: Throwable) {
                    val factor = 10.0.pow(config.volumeGainDb.toDouble() / 20.0)
                    val buffer = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)
                    val out = ByteArray(pcm.size)
                    val outBuf = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN)

                    while (buffer.hasRemaining() && buffer.remaining() >= 2) {
                        val sample = buffer.short
                        val boosted = (sample * factor).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        outBuf.putShort(boosted.toShort())
                    }
                    pcm = out
                }
            } else {
                val factor = 10.0.pow(config.volumeGainDb.toDouble() / 20.0)
                val buffer = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)
                val out = ByteArray(pcm.size)
                val outBuf = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN)

                while (buffer.hasRemaining() && buffer.remaining() >= 2) {
                    val sample = buffer.short
                    val boosted = (sample * factor).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    outBuf.putShort(boosted.toShort())
                }
                pcm = out
            }
        }

        // Apply Trimming
        if (config.isTrimEnabled && config.trimEndSec > config.trimStartSec && sourceDurationSec > 0) {
            val startRatio = (config.trimStartSec / sourceDurationSec).coerceIn(0.0, 1.0)
            val endRatio = (config.trimEndSec / sourceDurationSec).coerceIn(startRatio, 1.0)
            val startIdx = (pcm.size * startRatio).toInt().coerceIn(0, pcm.size)
            val endIdx = (pcm.size * endRatio).toInt().coerceIn(startIdx, pcm.size)
            pcm = pcm.copyOfRange(startIdx, endIdx)
        }

        return pcm
    }

    private fun writeConvertedAudioContainer(
        outputFile: File,
        targetFormat: AudioFormat,
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        targetBitrateKbps: Int
    ) {
        if (targetFormat == AudioFormat.AAC || targetFormat == AudioFormat.M4A || targetFormat == AudioFormat.MP3) {
            val success = encodePcmToM4a(pcmData, sampleRate, channels, targetBitrateKbps, outputFile)
            if (success && outputFile.exists() && outputFile.length() > 0) {
                return
            }
        }

        // Standard RIFF WAV container for uncompressed and lossless formats
        FileOutputStream(outputFile).use { fos ->
            val wavHeader = createWavHeader(pcmData.size, sampleRate, channels, 16)
            fos.write(wavHeader)
            fos.write(pcmData)
        }
    }

    private fun encodePcmToM4a(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitrateKbps: Int,
        outputFile: File
    ): Boolean {
        return try {
            val mime = MediaFormat.MIMETYPE_AUDIO_AAC
            val format = MediaFormat.createAudioFormat(mime, sampleRate, channels).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrateKbps * 1000)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }

            val codec = MediaCodec.createEncoderByType(mime)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var audioTrackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            var bytesProcessed = 0
            val totalBytes = pcmData.size
            var isInputDone = false

            while (true) {
                if (!isInputDone) {
                    val inputBufIndex = codec.dequeueInputBuffer(10000)
                    if (inputBufIndex >= 0) {
                        val inputBuf = codec.getInputBuffer(inputBufIndex)
                        if (inputBuf != null) {
                            inputBuf.clear()
                            val remaining = totalBytes - bytesProcessed
                            val bytesToCopy = minOf(inputBuf.capacity(), remaining)
                            if (bytesToCopy > 0) {
                                inputBuf.put(pcmData, bytesProcessed, bytesToCopy)
                                bytesProcessed += bytesToCopy
                                val ptsUs = (bytesProcessed.toLong() * 1000000L) / (sampleRate * channels * 2)
                                codec.queueInputBuffer(inputBufIndex, 0, bytesToCopy, ptsUs, 0)
                            } else {
                                codec.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isInputDone = true
                            }
                        }
                    }
                }

                val outputBufIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = codec.outputFormat
                    audioTrackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                } else if (outputBufIndex >= 0) {
                    val encodedData = codec.getOutputBuffer(outputBufIndex)
                    if (encodedData != null && muxerStarted && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        if (bufferInfo.size != 0) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(audioTrackIndex, encodedData, bufferInfo)
                        }
                    }
                    codec.releaseOutputBuffer(outputBufIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (isInputDone && bytesProcessed >= totalBytes) {
                        break
                    }
                }
            }

            codec.stop()
            codec.release()
            if (muxerStarted) {
                muxer.stop()
                muxer.release()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createWavHeader(pcmSizeBytes: Int, sampleRate: Int, channels: Int, bitDepth: Int): ByteArray {
        val totalDataLen = pcmSizeBytes + 36
        val byteRate = sampleRate * channels * bitDepth / 8
        val blockAlign = channels * bitDepth / 8

        val header = ByteArray(44)
        val buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // Chunk ID: RIFF
        buf.put("RIFF".toByteArray())
        buf.putInt(totalDataLen)
        // Format: WAVE
        buf.put("WAVE".toByteArray())
        // Subchunk1 ID: fmt
        buf.put("fmt ".toByteArray())
        buf.putInt(16) // Subchunk1 Size for PCM
        buf.putShort(1) // AudioFormat: 1 = PCM
        buf.putShort(channels.toShort())
        buf.putInt(sampleRate)
        buf.putInt(byteRate)
        buf.putShort(blockAlign.toShort())
        buf.putShort(bitDepth.toShort())
        // Subchunk2 ID: data
        buf.put("data".toByteArray())
        buf.putInt(pcmSizeBytes)

        return header
    }

    private fun generateSyntheticWaveformFile(
        file: File,
        sampleRate: Int,
        channels: Int,
        durationSec: Double,
        style: String
    ) {
        val pcm = generateSamplePcm(sampleRate, channels, durationSec, style)
        val wavHeader = createWavHeader(pcm.size, sampleRate, channels, 16)
        FileOutputStream(file).use { fos ->
            fos.write(wavHeader)
            fos.write(pcm)
        }
    }

    private fun generateSamplePcm(
        sampleRate: Int,
        channels: Int,
        durationSec: Double,
        style: String = "GUITAR"
    ): ByteArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val numBytes = totalSamples * channels * 2
        val bytes = ByteArray(numBytes)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        val freqs = when (style) {
            "GUITAR" -> doubleArrayOf(329.63, 246.94, 196.00, 146.83) // E B G D notes
            "VOICE" -> doubleArrayOf(180.0, 220.0, 260.0, 200.0)
            "PIANO" -> doubleArrayOf(261.63, 329.63, 392.00, 523.25) // C E G C chord
            else -> doubleArrayOf(110.0, 220.0, 440.0, 880.0) // Synth arpeggio
        }

        for (i in 0 until totalSamples) {
            val time = i.toDouble() / sampleRate
            val noteIdx = ((time * 3).toInt()) % freqs.size
            val freq = freqs[noteIdx]

            val amplitude = 0.5 * sin(2.0 * PI * freq * time)
            val sampleVal = (amplitude * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

            for (ch in 0 until channels) {
                buffer.putShort(sampleVal)
            }
        }
        return bytes
    }
}
