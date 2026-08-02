package com.example.leitner.domain.model

import java.time.LocalDate

data class Flashcard(
    val id: Long,
    val front: String,
    val back: String,
    val box: LeitnerBox,
    val nextReviewDate: LocalDate,
    val correctCount: Int,
    val incorrectCount: Int
)

data class BoxSummary(
    val box: LeitnerBox,
    val totalCount: Int,
    val dueCount: Int
)
