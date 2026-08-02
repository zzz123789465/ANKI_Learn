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
    }

    @Test
    fun parsesEasyTestBulletEntriesAndKeepsExample() {
        val cards = parseFlashcardText("""
             implement 實行；實行/用具
            The government implemented new policies last year.
             separation 分開;分隔線
            They met again after a separation of two years.
             triumph 勝利;成功
            He didn't win a complete triumph.
        """.trimIndent())
        assertEquals(3, cards.size)
        assertEquals("implement", cards[0].front)
        assertTrue(cards[0].back.contains("例句：The government"))
    }

    @Test
    fun splitsTwoVocabularyPairsOnOneLine() {
        val cards = parseFlashcardText("a game of cat and mouse 貓捉老鼠遊戲 a leap of faith 放手一搏\naboard 船上 absolutely 絕對地")
        assertEquals(4, cards.size)
        assertEquals("a game of cat and mouse", cards[0].front)
        assertEquals("貓捉老鼠遊戲", cards[0].back)
        assertEquals("a leap of faith", cards[1].front)
        assertEquals("absolutely", cards[3].front)
    }
}
