package com.ahu.ahutong.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.ahu.ahutong.AHUApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object RepositoryManager {
    private const val REPO_OWNER = "Kaltsit-cell"
    private const val REPO_NAME = "AHU-CS-Repository"
    private const val REPO_BRANCH = "master"
    private const val RAW_BASE = "https://raw.githubusercontent.com/$REPO_OWNER/$REPO_NAME/$REPO_BRANCH"
    private const val CDN_BASE = "https://cdn.jsdelivr.net/gh/$REPO_OWNER/$REPO_NAME@$REPO_BRANCH"
    private const val CONTENT_CACHE_PREFIX = "content_cache_"
    private const val CONTENT_CACHE_TIME_PREFIX = "content_cache_time_"

    private val gson = Gson()
    private val kv: MMKV by lazy {
        MMKV.initialize(AHUApplication.getApp())
        MMKV.mmkvWithID("repository_downloads")
    }

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .build()

    // === GitHub API ===

    suspend fun getContents(path: String = ""): List<GitHubContentItem> {
        return withContext(Dispatchers.IO) {
            GitHubApi.instance.getContents(REPO_OWNER, REPO_NAME, encodePath(path), REPO_BRANCH).also {
                saveContentCache(path, it)
            }
        }
    }

    fun getCachedContents(path: String = ""): CachedRepositoryContents? {
        val key = contentCacheKey(path)
        val json = kv.decodeString("$CONTENT_CACHE_PREFIX$key", null) ?: return null
        return try {
            val type = object : TypeToken<List<GitHubContentItem>>() {}.type
            val items: List<GitHubContentItem> = gson.fromJson(json, type)
            CachedRepositoryContents(
                items = items,
                updateTime = kv.decodeLong("$CONTENT_CACHE_TIME_PREFIX$key", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getRawUrl(path: String): String = "$RAW_BASE/${encodePath(path)}"

    /**
     * 获取所有可用的下载 URL（按优先级排列：CDN 优先）
     */
    private fun getDownloadUrls(path: String): List<String> = listOf(
        "$CDN_BASE/${encodePath(path)}",
        "$RAW_BASE/${encodePath(path)}"
    )

    // === 下载管理 ===

    private fun getDownloadDir(context: Context): File {
        val root = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(root, "repository")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * 获取所有已下载文件列表
     */
    fun getDownloadedFiles(context: Context): List<DownloadedFile> {
        val files = getDownloadedPaths(context)
        return files.mapNotNull { (path, localName) ->
            val file = File(getDownloadDir(context), localName)
            if (file.exists()) {
                DownloadedFile(
                    name = File(path).name,
                    path = path,
                    localPath = file.absolutePath,
                    size = file.length(),
                    downloadTime = file.lastModified()
                )
            } else {
                // 文件已被删除，清理记录
                removeDownloadRecord(path)
                null
            }
        }
    }

    /**
     * 检查文件是否已下载
     */
    fun isDownloaded(path: String, context: Context): Boolean {
        val localName = getDownloadedLocalName(path) ?: return false
        return File(getDownloadDir(context), localName).exists()
    }

    /**
     * 下载文件
     */
    suspend fun downloadFile(
        path: String,
        context: Context,
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        val localName = getLocalFileName(path)
        val outFile = File(getDownloadDir(context), localName)
        val urls = getDownloadUrls(path)

        for ((index, url) in urls.withIndex()) {
            try {
                val request = Request.Builder().url(url).build()
                val response = downloadClient.newCall(request).execute()

                if (!response.isSuccessful) continue

                val body = response.body ?: continue
                val total = body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var completed: Long = 0
                        var read = input.read(buffer)
                        while (read >= 0) {
                            output.write(buffer, 0, read)
                            completed += read
                            if (total > 0) {
                                onProgress(completed.toFloat() / total.toFloat())
                            }
                            read = input.read(buffer)
                        }
                        output.flush()
                    }
                }

                saveDownloadRecord(path, localName)
                return@withContext outFile
            } catch (e: Exception) {
                // 当前 URL 失败，尝试下一个
                if (index == urls.lastIndex) {
                    outFile.delete()
                    return@withContext null
                }
            }
        }
        outFile.delete()
        null
    }

    /**
     * 获取文件的本地 File 对象
     */
    fun getLocalFile(path: String, context: Context): File? {
        val localName = getDownloadedLocalName(path) ?: return null
        val file = File(getDownloadDir(context), localName)
        return if (file.exists()) file else null
    }

    /**
     * 删除已下载的文件
     */
    fun deleteFile(path: String, context: Context): Boolean {
        val file = getLocalFile(path, context) ?: return false
        val deleted = file.delete()
        if (deleted) {
            removeDownloadRecord(path)
        }
        return deleted
    }

    // === 内部方法 ===

    private fun getDownloadedPaths(context: Context): List<Pair<String, String>> {
        val json = kv.decodeString("downloaded_files", "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<List<String>>>() {}.type
            val list: List<List<String>> = gson.fromJson(json, type)
            list.mapNotNull { item ->
                if (item.size >= 2) Pair(item[0], item[1]) else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getDownloadedLocalName(path: String): String? {
        val files = getDownloadedPaths(AHUApplication.getApp())
        return files.firstOrNull { it.first == path }?.second
    }

    private fun saveDownloadRecord(path: String, localName: String) {
        val files = getDownloadedPaths(AHUApplication.getApp()).toMutableList()
        // 移除旧记录
        files.removeAll { it.first == path }
        files.add(Pair(path, localName))
        val json = gson.toJson(files.map { listOf(it.first, it.second) })
        kv.encode("downloaded_files", json)
    }

    private fun removeDownloadRecord(path: String) {
        val files = getDownloadedPaths(AHUApplication.getApp()).toMutableList()
        files.removeAll { it.first == path }
        val json = gson.toJson(files.map { listOf(it.first, it.second) })
        kv.encode("downloaded_files", json)
    }

    private fun saveContentCache(path: String, items: List<GitHubContentItem>) {
        val key = contentCacheKey(path)
        kv.encode("$CONTENT_CACHE_PREFIX$key", gson.toJson(items))
        kv.encode("$CONTENT_CACHE_TIME_PREFIX$key", System.currentTimeMillis())
    }

    private fun getLocalFileName(path: String): String {
        val sourceName = File(path).name.ifEmpty { "repository_file" }
        val dotIndex = sourceName.lastIndexOf('.')
        val nameWithoutExtension = if (dotIndex > 0) sourceName.substring(0, dotIndex) else sourceName
        val extension = if (dotIndex > 0) sourceName.substring(dotIndex) else ""
        val pathHash = Integer.toHexString(path.hashCode())
        return "${nameWithoutExtension}_$pathHash$extension"
    }

    private fun encodePath(path: String): String {
        if (path.isEmpty()) return ""
        return path.split('/').joinToString("/") { Uri.encode(it) }
    }

    private fun contentCacheKey(path: String): String {
        return Base64.encodeToString(
            path.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    }
}
