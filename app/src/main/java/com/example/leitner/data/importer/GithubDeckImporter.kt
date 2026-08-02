package com.example.leitner.data.importer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.Html
import com.example.leitner.domain.repository.CardDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.ZipInputStream
import javax.inject.Inject

data class GithubDeckAsset(
    val repository: String,
    val title: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val pageUrl: String
)

class GithubDeckImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun search(query: String): List<GithubDeckAsset> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode("$query anki", "UTF-8")
        val repositories = getJson("https://api.github.com/search/repositories?q=$encoded&per_page=10")
            .getJSONArray("items")
        buildList {
            for (index in 0 until repositories.length()) {
                val repository = repositories.getJSONObject(index)
                val fullName = repository.getString("full_name")
                val releases = getJsonArray("https://api.github.com/repos/$fullName/releases?per_page=5")
                for (releaseIndex in 0 until (releases?.length() ?: 0)) {
                    val release = releases!!.getJSONObject(releaseIndex)
                    val assets = release.optJSONArray("assets") ?: continue
                    for (assetIndex in 0 until assets.length()) {
                        val asset = assets.getJSONObject(assetIndex)
                        val name = asset.optString("name")
                        if (name.endsWith(".apkg", ignoreCase = true)) {
                            add(
                                GithubDeckAsset(
                                    repository = fullName,
                                    title = "$name (${release.optString("name", "Release")})",
                                    sizeBytes = asset.optLong("size"),
                                    downloadUrl = asset.getString("browser_download_url"),
                                    pageUrl = release.optString("html_url", repository.getString("html_url"))
                                )
                            )
                        }
                    }
                }
            }
            distinctBy { it.downloadUrl }
        }
    }

    suspend fun import(asset: GithubDeckAsset): List<CardDraft> = withContext(Dispatchers.IO) {
        val downloaded = File.createTempFile("anki_deck_", ".apkg", context.cacheDir)
        try {
            download(asset.downloadUrl, downloaded)
            parseApkg(downloaded)
        } finally {
            downloaded.delete()
        }
    }

    private fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "ANKI-Learn-Android")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("GitHub 回應 ${connection.responseCode}，可能已達公開 API 限制")
            }
            return JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun getJsonArray(url: String): org.json.JSONArray {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "ANKI-Learn-Android")
        }
        try {
            if (connection.responseCode !in 200..299) throw IllegalStateException("GitHub 回應 ${connection.responseCode}，可能已達公開 API 限制")
            return org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun download(url: String, destination: File) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "ANKI-Learn-Android")
        }
        try {
            if (connection.responseCode !in 200..299) throw IllegalStateException("卡組下載失敗：${connection.responseCode}")
            val maxBytes = 100L * 1024 * 1024
            if (connection.contentLengthLong > maxBytes) throw IllegalStateException("卡組超過 100 MB，為避免耗盡裝置空間已停止")
            var total = 0L
            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > maxBytes) throw IllegalStateException("卡組超過 100 MB，已停止下載")
                        output.write(buffer, 0, count)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseApkg(file: File): List<CardDraft> {
        val collections = mutableListOf<File>()
        ZipInputStream(file.inputStream().buffered()).use { zip ->
            var entries = 0
            var entry = zip.nextEntry
            while (entry != null) {
                if (++entries > 10_000) throw IllegalStateException("卡組檔案內容異常")
                val name = entry.name.replace('\\', '/')
                if (name.startsWith("/") || name.split('/').contains("..")) throw IllegalStateException("卡組包含不安全的檔案路徑")
                if (!entry.isDirectory && (name == "collection.anki21" || name == "collection.anki2")) {
                    val extracted = File.createTempFile("anki_collection_", ".db", context.cacheDir)
                    extracted.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val count = zip.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > 500L * 1024 * 1024) throw IllegalStateException("卡組資料庫過大")
                            output.write(buffer, 0, count)
                        }
                    }
                    collections += extracted
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val databaseFile = collections.firstOrNull { it.length() > 0 } ?: throw IllegalStateException("找不到有效的 Anki collection 資料庫")
        return try {
            SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READONLY).use { database ->
                database.rawQuery("SELECT flds FROM notes", null).use { cursor ->
                    buildList {
                        while (cursor.moveToNext() && size < 20_000) {
                            val fields = cursor.getString(0).split('\u001f')
                            val front = clean(fields.getOrNull(0).orEmpty())
                            val back = clean(fields.getOrNull(1).orEmpty())
                            if (front.isNotBlank() && back.isNotBlank()) add(CardDraft(front, back))
                        }
                    }.distinctBy { it.front to it.back }
                }
            }
        } finally {
            collections.forEach { it.delete() }
        }
    }

    private fun clean(value: String): String = Html.fromHtml(
        value.replace(Regex("\\[sound:[^]]+]"), ""),
        Html.FROM_HTML_MODE_LEGACY
    ).toString().replace(Regex("\\s+"), " ").trim()

}
