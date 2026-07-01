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

    val repositories = listOf(
        RepoConfig("cs", "计算机科学与技术学院", "Kaltsit-cell", "AHU-CS-Repository", branch = "master"),
        RepoConfig("ai", "人工智能学院", "DylanAo", "AHU-AI-Repository", branch = "main"),
        RepoConfig("ic", "集成电路学院", "Tonyseth", "AHU-IC-Design-personal-Repository", branch = "main"),
        RepoConfig("ee", "电子信息工程学院", "HarryWeasley3", "AHU-EE-Repository", branch = "main"),
        RepoConfig("internet", "互联网学院", "Zeraora-807", "AHU-Internet-Exams-Archive", branch = "main"),
        RepoConfig("sbi", "石溪学院", "UponNoise", "AHU_SBI_DMT", branch = "main")
    )

    private val repoById = repositories.associateBy { it.id }

    fun getRepo(repoId: String): RepoConfig = repoById[repoId] ?: repositories.first()
    fun getRepoUrl(repoId: String): String = getRepo(repoId).githubUrl

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

    // === Prefix helpers (scoped per repo) ===

    private fun contentCachePrefix(repoId: String) = "content_cache_$repoId/"
    private fun contentCacheTimePrefix(repoId: String) = "content_cache_time_$repoId/"
    private fun contentCacheKey(repoId: String, path: String) = Base64.encodeToString(
        path.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE
    )

    // === GitHub API ===

    suspend fun getContents(repoId: String, path: String = ""): List<GitHubContentItem> {
        val repo = getRepo(repoId)
        return withContext(Dispatchers.IO) {
            GitHubApi.instance.getContents(repo.owner, repo.repo, encodePath(path), repo.branch).also {
                saveContentCache(repoId, path, it)
            }
        }
    }

    fun getCachedContents(repoId: String, path: String = ""): CachedRepositoryContents? {
        val key = contentCacheKey(repoId, path)
        val json = kv.decodeString("${contentCachePrefix(repoId)}$key", null) ?: return null
        return try {
            val type = object : TypeToken<List<GitHubContentItem>>() {}.type
            val items: List<GitHubContentItem> = gson.fromJson(json, type)
            CachedRepositoryContents(
                items = items,
                updateTime = kv.decodeLong("${contentCacheTimePrefix(repoId)}$key", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getRawUrl(repoId: String, path: String): String {
        val repo = getRepo(repoId)
        return "${repo.rawBase}/${encodePath(path)}"
    }

    private fun getDownloadUrls(repoId: String, path: String): List<String> {
        val repo = getRepo(repoId)
        return listOf(
            "${repo.cdnBase}/${encodePath(path)}",
            "${repo.rawBase}/${encodePath(path)}"
        )
    }

    // === 下载管理 ===

    private fun getDownloadDir(context: Context): File {
        val root = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(root, "repository")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

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
                removeDownloadRecord(path)
                null
            }
        }
    }

    fun isDownloaded(path: String, context: Context): Boolean {
        val localName = getDownloadedLocalName(path) ?: return false
        return File(getDownloadDir(context), localName).exists()
    }

    suspend fun downloadFile(
        repoId: String,
        path: String,
        context: Context,
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        val localName = getLocalFileName(path)
        val outFile = File(getDownloadDir(context), localName)
        val urls = getDownloadUrls(repoId, path)

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
                            if (total > 0) onProgress(completed.toFloat() / total.toFloat())
                            read = input.read(buffer)
                        }
                        output.flush()
                    }
                }
                saveDownloadRecord(path, localName)
                return@withContext outFile
            } catch (e: Exception) {
                if (index == urls.lastIndex) {
                    outFile.delete()
                    return@withContext null
                }
            }
        }
        outFile.delete()
        null
    }

    fun getLocalFile(path: String, context: Context): File? {
        val localName = getDownloadedLocalName(path) ?: return null
        val file = File(getDownloadDir(context), localName)
        return if (file.exists()) file else null
    }

    fun deleteFile(path: String, context: Context): Boolean {
        val file = getLocalFile(path, context) ?: return false
        val deleted = file.delete()
        if (deleted) removeDownloadRecord(path)
        return deleted
    }

    // === 内部方法 ===

    private fun getDownloadedPaths(context: Context): List<Pair<String, String>> {
        val json = kv.decodeString("downloaded_files", "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<List<String>>>() {}.type
            val list: List<List<String>> = gson.fromJson(json, type)
            list.mapNotNull { item -> if (item.size >= 2) Pair(item[0], item[1]) else null }
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
        files.removeAll { it.first == path }
        files.add(Pair(path, localName))
        kv.encode("downloaded_files", gson.toJson(files.map { listOf(it.first, it.second) }))
    }

    private fun removeDownloadRecord(path: String) {
        val files = getDownloadedPaths(AHUApplication.getApp()).toMutableList()
        files.removeAll { it.first == path }
        kv.encode("downloaded_files", gson.toJson(files.map { listOf(it.first, it.second) }))
    }

    private fun saveContentCache(repoId: String, path: String, items: List<GitHubContentItem>) {
        val key = contentCacheKey(repoId, path)
        kv.encode("${contentCachePrefix(repoId)}$key", gson.toJson(items))
        kv.encode("${contentCacheTimePrefix(repoId)}$key", System.currentTimeMillis())
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
}
