package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "converted_audios")
data class ConvertedAudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val originalFormat: String,
    val targetFormat: String,
    val outputFilePath: String,
    val fileSizeBytes: Long,
    val originalSizeBytes: Long,
    val durationSeconds: Double,
    val bitrateKbps: Int,
    val sampleRateHz: Int,
    val channels: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val presetUsed: String = "Personalizado"
)
