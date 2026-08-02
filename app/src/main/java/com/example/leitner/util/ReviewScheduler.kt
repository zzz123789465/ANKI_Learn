package com.example.leitner.util

import com.example.leitner.domain.model.LeitnerBox
import java.time.LocalDate

object ReviewScheduler {
    fun nextReviewDate(box: LeitnerBox, reviewedOn: LocalDate): LocalDate = when (box) {
        LeitnerBox.BOX_1 -> reviewedOn.plusDays(1)
        LeitnerBox.BOX_2 -> reviewedOn.plusDays(3)
        LeitnerBox.BOX_3 -> reviewedOn.plusWeeks(1)
        LeitnerBox.BOX_4 -> reviewedOn.plusMonths(1)
    }
}
