package com.example.leitner

import com.example.leitner.domain.model.LeitnerBox
import com.example.leitner.util.ReviewScheduler
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewSchedulerTest {
    private val date = LocalDate.of(2026, 8, 2)
    @Test fun boxOneIsNextDay() = assertEquals(LocalDate.of(2026, 8, 3), ReviewScheduler.nextReviewDate(LeitnerBox.BOX_1, date))
    @Test fun boxTwoIsThreeDaysLater() = assertEquals(LocalDate.of(2026, 8, 5), ReviewScheduler.nextReviewDate(LeitnerBox.BOX_2, date))
    @Test fun boxFourIsNextMonth() = assertEquals(LocalDate.of(2026, 9, 2), ReviewScheduler.nextReviewDate(LeitnerBox.BOX_4, date))
}
