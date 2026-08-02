package com.example.leitner.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    indices = [Index("boxNumber"), Index("nextReviewEpochDay")]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val front: String,
    val back: String,
    val boxNumber: Int,
    val nextReviewEpochDay: Long,
    val lastReviewedAtMillis: Long? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
)
