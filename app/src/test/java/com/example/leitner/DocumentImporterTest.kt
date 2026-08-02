package com.example.leitner

import com.example.leitner.data.importer.parseFlashcardText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun parsesEasyTestBulletEntriesAndKeepsExample() {
        val text = """
            Easy test 全民英檢中級單字
            1-1
             implement 實行；實行/用具
            The government implemented new policies last year.
             separation 分開;分隔線
            They met again after a separation of two years.
             triumph 勝利;成功
            He didn't win a complete triumph.
        """.trimIndent()

        val cards = parseFlashcardText(text)

        assertEquals(3, cards.size)
        assertEquals("implement", cards[0].front)
        assertTrue(cards[0].back.contains("實行"))
        assertTrue(cards[0].back.contains("例句：The government"))
        assertEquals("separation", cards[1].front)
    }
}
