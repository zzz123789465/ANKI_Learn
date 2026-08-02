package com.example.leitner

import com.example.leitner.domain.model.LeitnerBox
import org.junit.Assert.assertEquals
import org.junit.Test

class LeitnerBoxTest {
    @Test fun boxOnePromotesToBoxTwo() = assertEquals(LeitnerBox.BOX_2, LeitnerBox.BOX_1.promote())
    @Test fun boxFourStaysAtBoxFour() = assertEquals(LeitnerBox.BOX_4, LeitnerBox.BOX_4.promote())
}
