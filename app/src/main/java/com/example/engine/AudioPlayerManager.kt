package com.example.engine

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val currentFilePath: String? = null,
    val currentTitle: String? = null,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val isLoaded: Boolean = false
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    fun playFile(filePath: String, title: String) {
        stop()

        try {
            val mp = MediaPlayer()

            if (filePath.startsWith("content://")) {
                val uri = Uri.parse(filePath)
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    mp.setDataSource(pfd.fileDescriptor)
                } ?: mp.setDataSource(context, uri)
            } else {
                val cleanPath = if (filePath.startsWith("file://")) filePath.substring(7) else filePath
                val file = File(cleanPath)
                if (file.exists()) {
                    java.io.FileInputStream(file).use { fis ->
                        mp.setDataSource(fis.fd)
                    }
                } else {
                    mp.setDataSource(filePath)
                }
            }

            mp.prepare()
            mp.start()

            mediaPlayer = mp

            _playerState.value = AudioPlayerState(
                isPlaying = true,
                currentFilePath = filePath,
                currentTitle = title,
                currentPositionMs = 0,
                durationMs = mp.duration,
                isLoaded = true
            )

            mp.setOnCompletionListener {
                _playerState.value = _playerState.value.copy(isPlaying = false, currentPositionMs = 0)
                stopProgressTracking()
            }

            startProgressTracking()
        } catch (e: Exception) {
            e.printStackTrace()
            _playerState.value = AudioPlayerState(
                isPlaying = false,
                currentFilePath = filePath,
                currentTitle = title,
                isLoaded = false
            )
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
            stopProgressTracking()
        } else {
            mp.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
            startProgressTracking()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
    }

    fun stop() {
        stopProgressTracking()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _playerState.value = AudioPlayerState()
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (_playerState.value.isPlaying) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _playerState.value = _playerState.value.copy(
                            currentPositionMs = mp.currentPosition,
                            durationMs = mp.duration
                        )
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }
}
