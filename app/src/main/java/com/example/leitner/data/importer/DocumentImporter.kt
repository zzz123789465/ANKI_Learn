package com.example.leitner.data.importer

import android.content.Context
import android.net.Uri
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
        val path = uri.toString().lowercase()
        val text = when {
            mimeType == "application/pdf" || path.endsWith(".pdf") -> readPdf(uri)
            mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" || path.endsWith(".docx") -> readDocx(uri)
            else -> throw UnsupportedDocumentTypeException()
        }
        return parseFlashcardText(text)
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
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.name == "word/document.xml") return@use zip.readBytes()
                }
                null
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
                    val words = (child as? org.w3c.dom.Element)?.getElementsByTagNameNS(
                        "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t"
                    )
                    if (words != null) for (wordIndex in 0 until words.length) append(words.item(wordIndex).textContent)
                }
                appendLine()
            }
        }
    }
}

fun parseFlashcardText(text: String): List<CardDraft> {
    val lines = text.lineSequence()
        .map { it.replace('\u00a0', ' ').trim() }
        .map { it.replace(Regex("^[•·●▪‣-]\\s*"), "") }
        .filter { it.isNotBlank() }
        .toList()
    val separator = Regex("\\s*(?:\\t+|[|｜]|[:：=])\\s*")
    val cards = mutableListOf<CardDraft>()
    val unpaired = mutableListOf<String>()
    lines.forEach { line ->
        val parts = line.split(separator, limit = 2)
        if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) cards += CardDraft(parts[0].trim(), parts[1].trim())
        else unpaired += line
    }
    unpaired.chunked(2).forEach { pair -> if (pair.size == 2) cards += CardDraft(pair[0], pair[1]) }
    return cards.distinctBy { it.front.lowercase() to it.back.lowercase() }
}
