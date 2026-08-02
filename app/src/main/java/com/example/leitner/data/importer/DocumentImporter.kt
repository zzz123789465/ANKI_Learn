package com.example.leitner.data.importer

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.example.leitner.domain.repository.CardDraft
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

class UnsupportedDocumentTypeException : IllegalArgumentException("目前支援 PDF 與 Word .docx 檔案")

class DocumentImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun import(uri: Uri, mimeType: String?): List<CardDraft> {
        val fileName = displayName(uri).orEmpty().lowercase()
        val path = uri.toString().lowercase()
        val normalizedMime = mimeType?.lowercase().orEmpty()
        val text = when {
            normalizedMime == "application/pdf" || normalizedMime.contains("pdf") || fileName.endsWith(".pdf") || path.endsWith(".pdf") -> readPdf(uri)
            normalizedMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" || normalizedMime.contains("wordprocessingml") || fileName.endsWith(".docx") || path.endsWith(".docx") -> readDocx(uri)
            else -> throw UnsupportedDocumentTypeException()
        }
        return parseFlashcardText(text)
    }

    private fun displayName(uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun readPdf(uri: Uri): String {
        PDFBoxResourceLoader.init(context)
        val stream = context.contentResolver.openInputStream(uri) ?: error("無法開啟 PDF")
        return stream.use { input -> PDDocument.load(input).use { PDFTextStripper().getText(it) } }
    }

    private fun readDocx(uri: Uri): String {
        val stream = context.contentResolver.openInputStream(uri) ?: error("無法開啟 Word 檔案")
        val documentXml = stream.use { input ->
            ZipInputStream(input).use { zip ->
                var result: ByteArray? = null
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.name == "word/document.xml") {
                        result = zip.readBytes()
                        break
                    }
                }
                result
            }
        } ?: error("不是有效的 .docx 檔案")

        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(documentXml))
        val paragraphs = document.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "p")
        return buildString {
            for (index in 0 until paragraphs.length) {
                val paragraph = paragraphs.item(index)
                val textNodes = paragraph.childNodes
                for (childIndex in 0 until textNodes.length) {
                    val child = textNodes.item(childIndex)
                    val words = (child as? org.w3c.dom.Element)?.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t")
                    if (words != null) for (wordIndex in 0 until words.length) append(words.item(wordIndex).textContent)
                }
                appendLine()
            }
        }
    }
}

private val easyTestVocabularyLine = Regex("^[●•·▪‣\\uF06C\\uF0AC]\\s*([A-Za-z][A-Za-z'-]*)\\s+(.+)$")
private val sectionHeader = Regex("^\\d+-\\d+$")
private val pageNumber = Regex("^\\d+$")
private val columnBoundary = Regex("(?<=[\\u3400-\\u9fff])\\s+(?=[A-Za-z])")
private val chineseCharacter = Regex("[\\u3400-\\u9fff]")

private fun parseEasyTestVocabulary(lines: List<String>): List<CardDraft> {
    val cards = mutableListOf<CardDraft>()
    var index = 0
    while (index < lines.size) {
        val match = easyTestVocabularyLine.matchEntire(lines[index])
        if (match == null) {
            index++
            continue
        }
        val front = match.groupValues[1]
        val meaning = match.groupValues[2].trim()
        val next = lines.getOrNull(index + 1)
        val hasExample = next != null && next.contains(' ') && !easyTestVocabularyLine.matches(next) && !sectionHeader.matches(next) && !pageNumber.matches(next)
        val back = if (hasExample) "$meaning\n\n例句：$next" else meaning
        cards += CardDraft(front, back)
        index += if (hasExample) 2 else 1
    }
    return cards.distinctBy { it.front.lowercase() to it.back.lowercase() }
}

/** Splits lines containing two or more English/Chinese vocabulary pairs. */
private fun parseBilingualColumnLine(line: String): List<CardDraft> {
    if (line.contains("單字一覽表") || line.matches(Regex("^[A-Z]$"))) return emptyList()
    return line.split(columnBoundary).mapNotNull { chunk ->
        val chineseStart = chineseCharacter.find(chunk)?.range?.first ?: return@mapNotNull null
        val front = chunk.substring(0, chineseStart).trim()
        val back = chunk.substring(chineseStart).trim()
        if (front.firstOrNull()?.isLetter() == true && front.any { it.isLetter() } && back.isNotBlank()) CardDraft(front, back) else null
    }
}

fun parseFlashcardText(text: String): List<CardDraft> {
    val lines = text.lineSequence().map { it.replace('\u00a0', ' ').trim() }.filter { it.isNotBlank() }.toList()
    val easyTestCards = parseEasyTestVocabulary(lines)
    if (easyTestCards.size >= 3) return easyTestCards

    val columnCards = lines.flatMap(::parseBilingualColumnLine).distinctBy { it.front.lowercase() to it.back.lowercase() }
    if (columnCards.size >= 3) return columnCards

    val separator = Regex("\\s*(?:\\t+|[|｜]|[:：=])\\s*")
    val cards = mutableListOf<CardDraft>()
    val unpaired = mutableListOf<String>()
    lines.forEach { line ->
        val cleaned = line.replace(Regex("^[•·●▪‣-]\\s*"), "")
        val parts = cleaned.split(separator, limit = 2)
        if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) cards += CardDraft(parts[0].trim(), parts[1].trim())
        else if (!sectionHeader.matches(cleaned) && !pageNumber.matches(cleaned)) unpaired += cleaned
    }
    unpaired.chunked(2).forEach { pair -> if (pair.size == 2) cards += CardDraft(pair[0], pair[1]) }
    return cards.distinctBy { it.front.lowercase() to it.back.lowercase() }
}
