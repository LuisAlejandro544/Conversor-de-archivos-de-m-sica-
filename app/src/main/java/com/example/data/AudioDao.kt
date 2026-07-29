package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Query("SELECT * FROM converted_audios ORDER BY timestamp DESC")
    fun getAllConvertedAudios(): Flow<List<ConvertedAudioEntity>>

    @Query("SELECT * FROM converted_audios WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteAudios(): Flow<List<ConvertedAudioEntity>>

    @Query("SELECT * FROM converted_audios WHERE id = :id LIMIT 1")
    suspend fun getAudioById(id: Int): ConvertedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConvertedAudio(audio: ConvertedAudioEntity): Long

    @Update
    suspend fun updateAudio(audio: ConvertedAudioEntity)

    @Query("UPDATE converted_audios SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteAudio(audio: ConvertedAudioEntity)

    @Query("DELETE FROM converted_audios WHERE id = :id")
    suspend fun deleteAudioById(id: Int)

    @Query("DELETE FROM converted_audios")
    suspend fun clearAllHistory()
}
