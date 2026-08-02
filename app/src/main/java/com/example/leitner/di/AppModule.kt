package com.example.leitner.di

import android.content.Context
import androidx.room.Room
import com.example.leitner.data.local.CardDao
import com.example.leitner.data.local.LeitnerDatabase
import com.example.leitner.data.repository.CardRepositoryImpl
import com.example.leitner.data.settings.StudySettingsRepositoryImpl
import com.example.leitner.domain.repository.CardRepository
import com.example.leitner.domain.settings.StudySettingsRepository
import com.example.leitner.util.DateProvider
import com.example.leitner.util.SystemDateProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LeitnerDatabase =
        Room.databaseBuilder(context, LeitnerDatabase::class.java, "leitner.db").build()

    @Provides fun provideCardDao(database: LeitnerDatabase): CardDao = database.cardDao()
    @Provides @Singleton fun provideDateProvider(): DateProvider = SystemDateProvider()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindRepository(impl: CardRepositoryImpl): CardRepository
    @Binds @Singleton abstract fun bindStudySettings(impl: StudySettingsRepositoryImpl): StudySettingsRepository
}
