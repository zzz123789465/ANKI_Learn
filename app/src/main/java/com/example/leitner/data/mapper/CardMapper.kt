package com.example.leitner.data.mapper

import com.example.leitner.data.local.CardEntity
import com.example.leitner.domain.model.Flashcard
import com.example.leitner.domain.model.LeitnerBox
import java.time.LocalDate

fun CardEntity.toDomain() = Flashcard(
    id = id,
    front = front,
    back = back,
    box = LeitnerBox.fromNumber(boxNumber),
    nextReviewDate = LocalDate.ofEpochDay(nextReviewEpochDay),
    correctCount = correctCount,
    incorrectCount = incorrectCount
)
