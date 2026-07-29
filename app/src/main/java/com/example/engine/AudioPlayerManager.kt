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
            val file = File(filePath)
            val uri = if (file.exists()) Uri.fromFile(file) else Uri.parse(filePath)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                start()
            }

            _playerState.value = AudioPlayerState(
                isPlaying = true,
                currentFilePath = filePath,
                currentTitle = title,
                currentPositionMs = 0,
                durationMs = mediaPlayer?.duration ?: 0,
                isLoaded = true
            )

            mediaPlayer?.setOnCompletionListener {
                _playerState.value = _playerState.value.copy(isPlaying = false, currentPositionMs = 0)
                stopProgressTracking()
            }

            startProgressTracking()
        } catch (e: Exception) {
            _playerState.value = AudioPlayerState(isLoaded = false)
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
