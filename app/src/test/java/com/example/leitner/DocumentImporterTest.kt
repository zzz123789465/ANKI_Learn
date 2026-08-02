package com.example.leitner

import com.example.leitner.data.importer.parseFlashcardText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentImporterTest {
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
        assertEquals("a leap of faith", cards[1].front)
        assertEquals("absolutely", cards[3].front)
    }

    @Test
    fun parsesNumberedEntriesAfterOcr() {
        val cards = parseFlashcardText("""
            1. abandon v 放棄；遺棄
            2. abide v 忍受；容忍
            3. ability n 能力；才能
            4. able a 能；可以
        """.trimIndent())
        assertEquals(4, cards.size)
        assertEquals("abandon", cards[0].front)
        assertEquals("放棄；遺棄", cards[0].back)
        assertEquals("ability", cards[2].front)
    }
}
