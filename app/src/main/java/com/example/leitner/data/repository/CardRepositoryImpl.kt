package com.example.leitner.data.repository

import androidx.room.withTransaction
import com.example.leitner.data.local.CardDao
import com.example.leitner.data.local.CardEntity
import com.example.leitner.data.local.LeitnerDatabase
import com.example.leitner.data.mapper.toDomain
import com.example.leitner.domain.model.BoxSummary
import com.example.leitner.domain.model.Flashcard
import com.example.leitner.domain.model.LeitnerBox
import com.example.leitner.domain.repository.CardRepository
import com.example.leitner.domain.repository.ReviewAnswer
import com.example.leitner.domain.repository.ReviewResult
import com.example.leitner.util.DateProvider
import com.example.leitner.util.ReviewScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val database: LeitnerDatabase,
    private val dao: CardDao,
    private val dateProvider: DateProvider
) : CardRepository {
    override fun observeAllCards(): Flow<List<Flashcard>> = dao.observeAllCards().map { it.map(CardEntity::toDomain) }

    override fun observeBoxSummaries(): Flow<List<BoxSummary>> {
        return dao.observeBoxSummaries(dateProvider.today().toEpochDay()).map { rows ->
            val byBox = rows.associateBy { it.boxNumber }
            LeitnerBox.entries.map { box ->
                val row = byBox[box.number]
                BoxSummary(box, row?.totalCount ?: 0, row?.dueCount ?: 0)
            }
        }
    }

    override suspend fun getDueCards(boxNumber: Int?): List<Flashcard> {
        val today = dateProvider.today().toEpochDay()
        val cards = if (boxNumber == null) dao.getDue(today) else dao.getDueInBox(boxNumber, today)
        return cards.map(CardEntity::toDomain)
    }

    override suspend fun addCard(front: String, back: String, boxNumber: Int): Long {
        require(front.isNotBlank() && back.isNotBlank())
        val now = dateProvider.currentTimeMillis()
        return dao.insert(CardEntity(
            front = front.trim(), back = back.trim(), boxNumber = LeitnerBox.fromNumber(boxNumber).number,
            nextReviewEpochDay = dateProvider.today().toEpochDay(), createdAtMillis = now, updatedAtMillis = now
        ))
    }

    override suspend fun updateCard(id: Long, front: String, back: String, boxNumber: Int) {
        require(front.isNotBlank() && back.isNotBlank())
        database.withTransaction {
            val current = dao.getById(id) ?: error("Card not found: $id")
            dao.update(current.copy(front = front.trim(), back = back.trim(), boxNumber = LeitnerBox.fromNumber(boxNumber).number, updatedAtMillis = dateProvider.currentTimeMillis()))
        }
    }

    override suspend fun deleteCard(id: Long) = dao.deleteById(id)

    override suspend fun reviewCard(cardId: Long, answer: ReviewAnswer): ReviewResult = database.withTransaction {
        val current = dao.getById(cardId) ?: error("Card not found: $cardId")
        val previous = LeitnerBox.fromNumber(current.boxNumber)
        val nextBox = if (answer == ReviewAnswer.CORRECT) previous.promote() else LeitnerBox.BOX_1
        val nextDate = ReviewScheduler.nextReviewDate(nextBox, dateProvider.today())
        val now = dateProvider.currentTimeMillis()
        dao.update(current.copy(
            boxNumber = nextBox.number, nextReviewEpochDay = nextDate.toEpochDay(),
            lastReviewedAtMillis = now, updatedAtMillis = now,
            correctCount = current.correctCount + if (answer == ReviewAnswer.CORRECT) 1 else 0,
            incorrectCount = current.incorrectCount + if (answer == ReviewAnswer.INCORRECT) 1 else 0
        ))
        ReviewResult(cardId, nextBox.number, nextDate, answer)
    }
}
