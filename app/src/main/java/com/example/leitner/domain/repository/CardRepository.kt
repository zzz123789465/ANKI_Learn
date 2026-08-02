package com.example.leitner.domain.repository

import com.example.leitner.domain.model.BoxSummary
import com.example.leitner.domain.model.Flashcard
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class CardDraft(val front: String, val back: String)

enum class ReviewAnswer { CORRECT, INCORRECT }

data class ReviewResult(
    val cardId: Long,
    val newBoxNumber: Int,
    val nextReviewDate: LocalDate,
    val answer: ReviewAnswer
)

interface CardRepository {
    fun observeAllCards(): Flow<List<Flashcard>>
    fun observeBoxSummaries(): Flow<List<BoxSummary>>
    suspend fun getDueCards(boxNumber: Int? = null): List<Flashcard>
    suspend fun addCard(front: String, back: String, boxNumber: Int = 1): Long
    suspend fun addCards(cards: List<CardDraft>, boxNumber: Int = 1): Int
    suspend fun updateCard(id: Long, front: String, back: String, boxNumber: Int)
    suspend fun deleteCard(id: Long)
    suspend fun reviewCard(cardId: Long, answer: ReviewAnswer): ReviewResult
}
