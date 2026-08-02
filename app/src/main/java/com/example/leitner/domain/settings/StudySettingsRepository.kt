package com.example.leitner.domain.settings

import kotlinx.coroutines.flow.Flow

interface StudySettingsRepository {
    val dailyLimit: Flow<Int>
    suspend fun getDailyLimit(): Int
    suspend fun getCompletedToday(): Int
    suspend fun setDailyLimit(limit: Int)
    suspend fun recordCompletedCard()
}
