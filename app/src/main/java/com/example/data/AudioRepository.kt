package com.example.data

import kotlinx.coroutines.flow.Flow

class AudioRepository(private val audioDao: AudioDao) {

    val allAudios: Flow<List<ConvertedAudioEntity>> = audioDao.getAllConvertedAudios()
    val favoriteAudios: Flow<List<ConvertedAudioEntity>> = audioDao.getFavoriteAudios()

    suspend fun getAudioById(id: Int): ConvertedAudioEntity? = audioDao.getAudioById(id)

    suspend fun insert(audio: ConvertedAudioEntity): Long = audioDao.insertConvertedAudio(audio)

    suspend fun toggleFavorite(id: Int, currentFavorite: Boolean) {
        audioDao.updateFavorite(id, !currentFavorite)
    }

    suspend fun delete(audio: ConvertedAudioEntity) = audioDao.deleteAudio(audio)

    suspend fun deleteById(id: Int) = audioDao.deleteAudioById(id)

    suspend fun clearHistory() = audioDao.clearAllHistory()
}
