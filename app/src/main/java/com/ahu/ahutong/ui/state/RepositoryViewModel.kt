package com.ahu.ahutong.ui.state

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.repository.DownloadedFile
import com.ahu.ahutong.data.repository.GitHubContentItem
import com.ahu.ahutong.data.repository.RepositoryDirectorySummary
import com.ahu.ahutong.data.repository.RepositoryMarkdownDocument
import com.ahu.ahutong.data.repository.RepositoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RepositoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoaded: Boolean = false,
    val items: List<GitHubContentItem> = emptyList(),
    val currentPath: String = "",
    val error: String? = null,
    val isShowingCachedContents: Boolean = false,
    val cacheUpdatedAt: Long? = null,
    val directorySummaries: Map<String, RepositoryDirectorySummary> = emptyMap()
)

data class RepositorySharedUiState(
    val downloadedPaths: Set<String> = emptySet(),
    val downloadProgress: Map<String, Float> = emptyMap(),
    val downloadingPath: String? = null,
    val isCacheWarming: Boolean = false,
    val cacheWarmUpCount: Int = 0
)

data class RepositoryScrollPosition(
    val index: Int = 0,
    val offset: Int = 0
)

data class RepositoryMarkdownUiState(
    val isLoading: Boolean = false,
    val document: RepositoryMarkdownDocument? = null,
    val error: String? = null
)

class RepositoryViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()
    private var loadRequestId = 0
    private val pathRequestIds = mutableMapOf<String, Int>()
    private val scrollPositions = mutableMapOf<String, RepositoryScrollPosition>()
    private var hasStartedWarmUp = false

    private val _directoryStates = MutableStateFlow<Map<String, RepositoryUiState>>(emptyMap())
    val directoryStates: StateFlow<Map<String, RepositoryUiState>> = _directoryStates.asStateFlow()

    private val _sharedState = MutableStateFlow(
        RepositorySharedUiState(downloadedPaths = refreshDownloadedSet())
    )
    val sharedState: StateFlow<RepositorySharedUiState> = _sharedState.asStateFlow()

    private val _markdownState = MutableStateFlow(RepositoryMarkdownUiState())
    val markdownState: StateFlow<RepositoryMarkdownUiState> = _markdownState.asStateFlow()

    fun getInitialDirectoryState(path: String): RepositoryUiState {
        return _directoryStates.value[path] ?: cachedDirectoryState(path) ?: RepositoryUiState(
            currentPath = path,
            isLoading = true
        )
    }

    fun getDirectoryState(path: String): RepositoryUiState {
        return _directoryStates.value[path] ?: cachedDirectoryState(path) ?: RepositoryUiState(
            currentPath = path,
            isLoading = true
        )
    }

    fun getSharedState(): RepositorySharedUiState = _sharedState.value

    fun ensureLoaded(path: String) {
        val state = _directoryStates.value[path]
        if (state == null || !state.isLoaded && state.error == null) {
            loadContents(path)
        }
    }

    fun loadContents(path: String = "", forceRefresh: Boolean = false) {
        val requestId = ++loadRequestId
        pathRequestIds[path] = requestId
        val cached = if (forceRefresh) null else RepositoryManager.getCachedContents(path)
        val startState = _directoryStates.value[path] ?: cachedDirectoryState(path)

        if (cached != null) {
            setDirectoryState(path, directoryStateFromCache(path, cached.items, cached.updateTime))
            return
        }

        setDirectoryState(
            path,
            (startState ?: RepositoryUiState(currentPath = path)).copy(
                isLoading = startState?.items.isNullOrEmpty(),
                isRefreshing = !startState?.items.isNullOrEmpty(),
                isLoaded = startState?.isLoaded == true,
                error = null
            )
        )

        viewModelScope.launch {
            try {
                val items = RepositoryManager.getContents(path, forceRefresh = forceRefresh)
                if (pathRequestIds[path] != requestId) return@launch
                val sortedItems = sortDisplayItems(path, items)
                setDirectoryState(
                    path,
                    RepositoryUiState(
                        isLoading = false,
                        isRefreshing = false,
                        isLoaded = true,
                        items = sortedItems,
                        currentPath = path,
                        isShowingCachedContents = false,
                        cacheUpdatedAt = System.currentTimeMillis(),
                        directorySummaries = RepositoryManager.getDirectorySummaries(sortedItems)
                    )
                )
                _sharedState.value = _sharedState.value.copy(
                    downloadedPaths = refreshDownloadedSet()
                )
            } catch (e: Exception) {
                if (pathRequestIds[path] != requestId) return@launch
                val fallback = RepositoryManager.getCachedContents(path)
                if (fallback != null) {
                    setDirectoryState(
                        path,
                        directoryStateFromCache(path, fallback.items, fallback.updateTime).copy(
                            error = null
                        )
                    )
                } else {
                    setDirectoryState(
                        path,
                        (_directoryStates.value[path] ?: RepositoryUiState(currentPath = path)).copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = "加载失败: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    fun warmUpAllContentCaches(forceRefresh: Boolean = false) {
        if (hasStartedWarmUp && !forceRefresh) return
        hasStartedWarmUp = true
        _sharedState.value = _sharedState.value.copy(
            isCacheWarming = true,
            cacheWarmUpCount = 0
        )
        viewModelScope.launch {
            runCatching {
                RepositoryManager.warmUpAllContentCaches(
                    forceRefresh = forceRefresh,
                    onProgress = { fetchedCount ->
                        _sharedState.value = _sharedState.value.copy(
                            isCacheWarming = true,
                            cacheWarmUpCount = fetchedCount
                        )
                    }
                )
            }.onSuccess { updateTime ->
                val states = _directoryStates.value.toMutableMap()
                states.keys.toList().forEach { path ->
                    RepositoryManager.getCachedContents(path)?.let { cached ->
                        states[path] = directoryStateFromCache(path, cached.items, updateTime)
                    }
                }
                _directoryStates.value = states
                _sharedState.value = _sharedState.value.copy(
                    isCacheWarming = false,
                    cacheWarmUpCount = 0
                )
            }.onFailure {
                _sharedState.value = _sharedState.value.copy(
                    isCacheWarming = false,
                    cacheWarmUpCount = 0
                )
            }
        }
    }

    fun refreshDirectory(path: String) {
        loadContents(path, forceRefresh = true)
    }

    fun saveScrollPosition(path: String, index: Int, offset: Int) {
        scrollPositions[path] = RepositoryScrollPosition(index, offset)
    }

    fun getScrollPosition(path: String): RepositoryScrollPosition {
        return scrollPositions[path] ?: RepositoryScrollPosition()
    }

    fun downloadFile(item: GitHubContentItem) {
        viewModelScope.launch {
            val path = item.path
            _sharedState.value = _sharedState.value.copy(
                downloadingPath = path,
                downloadProgress = _sharedState.value.downloadProgress + (path to 0f)
            )
            try {
                val file = RepositoryManager.downloadFile(path, context) { progress ->
                    _sharedState.value = _sharedState.value.copy(
                        downloadProgress = _sharedState.value.downloadProgress + (path to progress)
                    )
                }
                if (file != null) {
                    val downloads = refreshDownloadedSet()
                    _sharedState.value = _sharedState.value.copy(
                        downloadingPath = null,
                        downloadedPaths = downloads,
                        downloadProgress = _sharedState.value.downloadProgress - path
                    )
                    Toast.makeText(context, "下载完成: ${item.name}", Toast.LENGTH_SHORT).show()
                } else {
                    _sharedState.value = _sharedState.value.copy(
                        downloadingPath = null,
                        downloadProgress = _sharedState.value.downloadProgress - path
                    )
                    setPathError(item.parentPath(), buildDownloadErrorMessage(item.name))
                }
            } catch (e: Exception) {
                _sharedState.value = _sharedState.value.copy(
                    downloadingPath = null,
                    downloadProgress = _sharedState.value.downloadProgress - path
                )
                setPathError(item.parentPath(), buildDownloadErrorMessage(e.message))
            }
        }
    }

    fun openFile(item: GitHubContentItem) {
        if (isMarkdownFile(item.name)) {
            loadMarkdown(item.path)
            return
        }
        val file = RepositoryManager.getDownloadedFile(item.path, context)
        if (file != null) {
            openDownloadedFile(file)
        } else {
            Toast.makeText(context, "文件不存在，请先下载", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(path: String) {
        RepositoryManager.deleteFile(path, context)
        val downloads = refreshDownloadedSet()
        _sharedState.value = _sharedState.value.copy(downloadedPaths = downloads)
    }

    fun getRawUrl(path: String): String = RepositoryManager.getRawUrl(path)

    fun getGitHubUrl(path: String): String = RepositoryManager.getGitHubUrl(path)

    fun shouldShowUnsupportedDirectoryMessage(path: String): Boolean {
        return RepositoryManager.shouldShowUnsupportedDirectoryMessage(path)
    }

    fun loadMarkdown(path: String) {
        _markdownState.value = RepositoryMarkdownUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                RepositoryManager.getMarkdownDocument(path)
            }.onSuccess { document ->
                _markdownState.value = RepositoryMarkdownUiState(document = document)
            }.onFailure { e ->
                _markdownState.value = RepositoryMarkdownUiState(
                    error = "Markdown 加载失败: ${e.message}"
                )
            }
        }
    }

    fun clearMarkdown() {
        _markdownState.value = RepositoryMarkdownUiState()
    }

    private fun loadDownloadedMarkdown(file: DownloadedFile) {
        _markdownState.value = RepositoryMarkdownUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val uri = file.uri?.let { Uri.parse(it) }
                val content = if (uri != null) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText()
                    } ?: throw IllegalStateException("无法读取本地 Markdown")
                } else {
                    val localFile = File(file.localPath)
                    if (!localFile.exists()) {
                        deleteFile(file.path)
                        throw IllegalStateException("文件已被删除")
                    }
                    localFile.readText()
                }
                RepositoryMarkdownDocument(
                    title = file.name,
                    path = file.path,
                    content = content
                )
            }.onSuccess { document ->
                _markdownState.value = RepositoryMarkdownUiState(document = document)
            }.onFailure { e ->
                _markdownState.value = RepositoryMarkdownUiState(
                    error = "Markdown 加载失败: ${e.message}"
                )
            }
        }
    }

    fun getDownloadedFiles(): List<DownloadedFile> {
        return RepositoryManager.getDownloadedFiles(context)
    }

    fun getLocalFile(path: String): File? {
        return RepositoryManager.getLocalFile(path, context)
    }

    fun openDownloadedFile(file: DownloadedFile) {
        if (isMarkdownFile(file.name)) {
            loadDownloadedMarkdown(file)
            return
        }

        val uri = file.uri?.let { Uri.parse(it) }
        if (uri != null) {
            openUriWithSystemViewer(uri, file.name)
            return
        }

        val localFile = File(file.localPath)
        if (localFile.exists()) {
            openWithSystemViewer(localFile)
        } else {
            deleteFile(file.path)
            Toast.makeText(context, "文件已被删除", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWithSystemViewer(file: File) {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show()
                return
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = getMimeType(file.name)
            if (!startFileViewer(uri, file.name, mimeType) &&
                (mimeType == "*/*" || !startFileViewer(uri, file.name, "*/*"))
            ) {
                Toast.makeText(context, "没有找到可打开此文件的软件", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("RepoViewer", "打开失败: ${e.message}", e)
            Toast.makeText(context, "打开失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openUriWithSystemViewer(uri: Uri, fileName: String) {
        try {
            val mimeType = getMimeType(fileName)
            if (!startFileViewer(uri, fileName, mimeType) &&
                (mimeType == "*/*" || !startFileViewer(uri, fileName, "*/*"))
            ) {
                Toast.makeText(context, "没有找到可打开此文件的软件", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("RepoViewer", "打开失败: ${e.message}", e)
            Toast.makeText(context, "打开失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startFileViewer(uri: Uri, fileName: String, mimeType: String): Boolean {
        val clipData = ClipData.newUri(context.contentResolver, fileName, uri)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addCategory(Intent.CATEGORY_DEFAULT)
            this.clipData = clipData
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(viewIntent, "选择打开方式").apply {
            this.clipData = clipData
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return try {
            context.startActivity(chooserIntent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    fun clearError(path: String) {
        _directoryStates.value[path]?.let { state ->
            setDirectoryState(path, state.copy(error = null))
        }
    }

    private fun cachedDirectoryState(path: String): RepositoryUiState? {
        val cached = RepositoryManager.getCachedContents(path) ?: return null
        return directoryStateFromCache(path, cached.items, cached.updateTime)
    }

    private fun directoryStateFromCache(
        path: String,
        items: List<GitHubContentItem>,
        updateTime: Long
    ): RepositoryUiState {
        val sortedItems = sortDisplayItems(path, items)
        return RepositoryUiState(
            isLoading = false,
            isRefreshing = false,
            isLoaded = true,
            items = sortedItems,
            currentPath = path,
            isShowingCachedContents = true,
            cacheUpdatedAt = updateTime.takeIf { it > 0L },
            directorySummaries = RepositoryManager.getDirectorySummaries(sortedItems)
        )
    }

    private fun setDirectoryState(path: String, state: RepositoryUiState) {
        _directoryStates.value = _directoryStates.value + (path to state.copy(currentPath = path))
    }

    private fun setPathError(path: String, message: String) {
        setDirectoryState(
            path,
            (_directoryStates.value[path] ?: cachedDirectoryState(path) ?: RepositoryUiState(
                currentPath = path
            )).copy(error = message)
        )
    }

    private fun GitHubContentItem.parentPath(): String = path.substringBeforeLast('/', "")

    private fun refreshDownloadedSet(): Set<String> {
        val files = RepositoryManager.getDownloadedFiles(context)
        return files.map { it.path }.toSet()
    }

    private fun buildDownloadErrorMessage(detail: String?): String {
        val suffix = "，可前往学习资料设置更换下载源后重试"
        val normalizedDetail = detail?.takeIf { it.isNotBlank() } ?: "未知原因"
        return "下载失败: $normalizedDetail$suffix"
    }

    private fun sortDisplayItems(path: String, items: List<GitHubContentItem>): List<GitHubContentItem> {
        if (path.isBlank()) {
            return items.sortedWith(
                compareBy<GitHubContentItem> { if (it.type == "dir") 0 else 1 }
                    .thenBy { RepositoryManager.getRepositoryOrder(it.path) }
                    .thenBy { it.name.lowercase() }
            )
        }
        val dirs = items.filter { it.type == "dir" }.sortedBy { it.name.lowercase() }
        val files = items.filter { it.type == "file" }.sortedBy { it.name.lowercase() }
        return dirs + files
    }

    companion object {
        fun isMarkdownFile(name: String): Boolean {
            return name.lowercase().endsWith(".md")
        }

        fun getMimeType(fileName: String): String {
            val lower = fileName.lowercase()
            return when {
                lower.endsWith(".pdf") -> "application/pdf"
                lower.endsWith(".doc") -> "application/msword"
                lower.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                lower.endsWith(".ppt") -> "application/vnd.ms-powerpoint"
                lower.endsWith(".pptx") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                lower.endsWith(".xls") -> "application/vnd.ms-excel"
                lower.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                lower.endsWith(".txt") -> "text/plain"
                lower.endsWith(".md") -> "text/plain"
                lower.endsWith(".png") -> "image/png"
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
                lower.endsWith(".gif") -> "image/gif"
                lower.endsWith(".webp") -> "image/webp"
                lower.endsWith(".bmp") -> "image/bmp"
                lower.endsWith(".svg") -> "image/svg+xml"
                lower.endsWith(".zip") -> "application/zip"
                lower.endsWith(".rar") -> "application/x-rar-compressed"
                lower.endsWith(".7z") -> "application/x-7z-compressed"
                lower.endsWith(".apk") -> "application/vnd.android.package-archive"
                else -> "*/*"
            }
        }

        fun getFileTypeIcon(name: String): String {
            val lower = name.lowercase()
            return when {
                lower.endsWith(".pdf") -> "PDF"
                lower.endsWith(".doc") || lower.endsWith(".docx") -> "DOC"
                lower.endsWith(".ppt") || lower.endsWith(".pptx") -> "PPT"
                lower.endsWith(".xls") || lower.endsWith(".xlsx") -> "XLS"
                lower.endsWith(".txt") -> "TXT"
                lower.endsWith(".md") -> "MD"
                lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp") -> "IMG"
                lower.endsWith(".svg") -> "SVG"
                lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z") -> "ZIP"
                lower.endsWith(".apk") -> "APK"
                else -> "?"
            }
        }
    }
}
