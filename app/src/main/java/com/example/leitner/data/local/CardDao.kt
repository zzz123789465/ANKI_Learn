package com.example.leitner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM flashcards ORDER BY boxNumber, nextReviewEpochDay, createdAtMillis DESC")
    fun observeAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CardEntity?

    @Query("SELECT * FROM flashcards WHERE nextReviewEpochDay <= :today ORDER BY nextReviewEpochDay, boxNumber")
    suspend fun getDue(today: Long): List<CardEntity>

    @Query("SELECT * FROM flashcards WHERE boxNumber = :boxNumber AND nextReviewEpochDay <= :today ORDER BY nextReviewEpochDay")
    suspend fun getDueInBox(boxNumber: Int, today: Long): List<CardEntity>

    @Query("""
        SELECT boxNumber, COUNT(*) AS totalCount,
        SUM(CASE WHEN nextReviewEpochDay <= :today THEN 1 ELSE 0 END) AS dueCount
        FROM flashcards GROUP BY boxNumber ORDER BY boxNumber
    """)
    fun observeBoxSummaries(today: Long): Flow<List<BoxSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Long)
}
