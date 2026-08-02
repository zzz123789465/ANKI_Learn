package com.example.leitner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CardEntity::class], version = 1, exportSchema = true)
abstract class LeitnerDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
}
