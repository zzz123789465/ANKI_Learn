package com.example.leitner.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.leitner.domain.settings.StudySettingsRepository
import com.example.leitner.util.DateProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.studySettingsDataStore by preferencesDataStore(name = "study_settings")

class StudySettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dateProvider: DateProvider
) : StudySettingsRepository {
    private object Keys {
        val dailyLimit = intPreferencesKey("daily_limit")
        val completedDate = stringPreferencesKey("completed_date")
        val completedCount = intPreferencesKey("completed_count")
    }

    override val dailyLimit: Flow<Int> = context.studySettingsDataStore.data.map { it[Keys.dailyLimit] ?: DEFAULT_DAILY_LIMIT }

    override suspend fun getDailyLimit(): Int = dailyLimit.first()

    override suspend fun getCompletedToday(): Int {
        val preferences = context.studySettingsDataStore.data.first()
        return if (preferences[Keys.completedDate] == todayKey()) preferences[Keys.completedCount] ?: 0 else 0
    }

    override suspend fun setDailyLimit(limit: Int) {
        context.studySettingsDataStore.edit { it[Keys.dailyLimit] = limit.coerceIn(MIN_DAILY_LIMIT, MAX_DAILY_LIMIT) }
    }

    override suspend fun recordCompletedCard() {
        context.studySettingsDataStore.edit { preferences ->
            val today = todayKey()
            val current = if (preferences[Keys.completedDate] == today) preferences[Keys.completedCount] ?: 0 else 0
            preferences[Keys.completedDate] = today
            preferences[Keys.completedCount] = current + 1
        }
    }

    private fun todayKey(): String = dateProvider.today().toString()

    companion object {
        const val DEFAULT_DAILY_LIMIT = 20
        const val MIN_DAILY_LIMIT = 1
        const val MAX_DAILY_LIMIT = 500
    }
}
