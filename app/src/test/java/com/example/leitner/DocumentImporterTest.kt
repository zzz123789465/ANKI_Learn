package com.example.leitner

import com.example.leitner.data.importer.parseFlashcardText
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentImporterTest {
    @Test
    fun parsesSeparatedWordAndMeaningLines() {
        val cards = parseFlashcardText("apple\t蘋果\nbook:書")
        assertEquals(2, cards.size)
        assertEquals("apple", cards[0].front)
        assertEquals("蘋果", cards[0].back)
        assertEquals("book", cards[1].front)
    }

    @Test
    fun parsesAdjacentWordAndMeaningLines() {
        val cards = parseFlashcardText("apple\n蘋果\nbook\n書")
        assertEquals(2, cards.size)
        assertEquals("book", cards[1].front)
        assertEquals("書", cards[1].back)
    }
}
