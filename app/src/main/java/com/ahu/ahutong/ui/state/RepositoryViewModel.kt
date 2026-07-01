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
import com.ahu.ahutong.data.repository.RepoConfig
import com.ahu.ahutong.data.repository.RepositoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RepositoryUiState(
    val selectedRepoId: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val pendingPath: String? = null,
    val items: List<GitHubContentItem> = emptyList(),
    val currentPath: String = "",
    val pathStack: List<String> = emptyList(),
    val error: String? = null,
    val isShowingCachedContents: Boolean = false,
    val cacheUpdatedAt: Long? = null,
    val downloadedPaths: Set<String> = emptySet(),
    val downloadProgress: Map<String, Float> = emptyMap(),
    val downloadingPaths: Set<String> = emptySet(),
)

class RepositoryViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()
    private var loadRequestId = 0

    private val _uiState = MutableStateFlow(RepositoryUiState())
    val uiState: StateFlow<RepositoryUiState> = _uiState.asStateFlow()

    fun selectRepo(repoId: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedRepoId = repoId,
            items = emptyList(),
            currentPath = "",
            pathStack = emptyList(),
            pendingPath = null,
            error = null
        )
        loadContents()
    }

    fun resetToRepoSelector() {
        loadRequestId++
        _uiState.value = RepositoryUiState()
    }

    fun loadContents(
        path: String = "",
        forceRefresh: Boolean = false,
        targetPathStack: List<String>? = null
    ) {
        val repoId = _uiState.value.selectedRepoId ?: return
        val requestId = ++loadRequestId
        val startState = _uiState.value
        val cached = if (forceRefresh) null else RepositoryManager.getCachedContents(repoId, path)
        val isSameDisplayedPath = startState.currentPath == path
        val hasDisplayedItems = startState.items.isNotEmpty()
        val nextPathStack = targetPathStack ?: startState.pathStack

        if (cached != null) {
            _uiState.value = startState.copy(
                isLoading = false,
                isRefreshing = true,
                pendingPath = null,
                items = sortDisplayItems(cached.items),
                currentPath = path,
                pathStack = nextPathStack,
                error = null,
                isShowingCachedContents = true,
                cacheUpdatedAt = cached.updateTime.takeIf { it > 0L },
                downloadedPaths = refreshDownloadedSet()
            )
        } else {
            _uiState.value = startState.copy(
                isLoading = !hasDisplayedItems,
                isRefreshing = hasDisplayedItems,
                pendingPath = path.takeIf { !isSameDisplayedPath },
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val items = RepositoryManager.getContents(repoId, path)
                val downloads = refreshDownloadedSet()
                if (requestId != loadRequestId) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    pendingPath = null,
                    items = sortDisplayItems(items),
                    currentPath = path,
                    pathStack = nextPathStack,
                    downloadedPaths = downloads,
                    isShowingCachedContents = false,
                    cacheUpdatedAt = System.currentTimeMillis(),
                    error = null
                )
            } catch (e: Exception) {
                if (requestId != loadRequestId) return@launch
                val currentState = _uiState.value
                val canKeepDisplayedItems = currentState.items.isNotEmpty()
                if (canKeepDisplayedItems) {
                    val failedNavigation = !isSameDisplayedPath && cached == null
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        pendingPath = null,
                        isShowingCachedContents = if (failedNavigation) currentState.isShowingCachedContents else true,
                        cacheUpdatedAt = cached?.updateTime?.takeIf { it > 0L } ?: currentState.cacheUpdatedAt,
                        error = if (failedNavigation) "无法进入 ${path.substringAfterLast('/')}，仍停留在当前目录" else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        pendingPath = null,
                        error = "加载失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshCurrentDirectory() {
        loadContents(_uiState.value.currentPath, forceRefresh = true)
    }

    fun enterDirectory(item: GitHubContentItem) {
        if (item.type != "dir") return
        val state = _uiState.value
        if (state.pendingPath != null) return
        val newPath = item.path
        if (newPath == state.currentPath) return
        loadContents(newPath, targetPathStack = state.pathStack + state.currentPath)
    }

    fun goBack(): Boolean {
        val state = _uiState.value
        if (state.pendingPath != null) {
            loadRequestId++
            _uiState.value = state.copy(
                isLoading = false, isRefreshing = false, pendingPath = null, error = null
            )
            return true
        }
        val stack = state.pathStack
        if (stack.isEmpty()) return false
        val parentPath = stack.last()
        loadContents(parentPath, targetPathStack = stack.dropLast(1))
        return true
    }

    fun downloadFile(item: GitHubContentItem) {
        val repoId = _uiState.value.selectedRepoId ?: return
        viewModelScope.launch {
            val path = item.path
            _uiState.value = _uiState.value.copy(
                downloadingPaths = _uiState.value.downloadingPaths + path,
                downloadProgress = _uiState.value.downloadProgress + (path to 0f)
            )
            try {
                val file = RepositoryManager.downloadFile(repoId, path, context) { progress ->
                    _uiState.value = _uiState.value.copy(
                        downloadProgress = _uiState.value.downloadProgress + (path to progress)
                    )
                }
                if (file != null) {
                    val downloads = refreshDownloadedSet()
                    _uiState.value = _uiState.value.copy(
                        downloadingPaths = _uiState.value.downloadingPaths - path,
                        downloadedPaths = downloads,
                        downloadProgress = _uiState.value.downloadProgress - path
                    )
                    Toast.makeText(context, "下载完成: ${item.name}", Toast.LENGTH_SHORT).show()
                } else {
                    _uiState.value = _uiState.value.copy(
                        downloadingPaths = _uiState.value.downloadingPaths - path,
                        downloadProgress = _uiState.value.downloadProgress - path,
                        error = "下载失败: ${item.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadingPaths = _uiState.value.downloadingPaths - path,
                    downloadProgress = _uiState.value.downloadProgress - path,
                    error = "下载失败: ${e.message}"
                )
            }
        }
    }

    fun openFile(item: GitHubContentItem) {
        val file = RepositoryManager.getLocalFile(item.path, context)
        if (file != null) openWithSystemViewer(file)
        else Toast.makeText(context, "文件不存在，请先下载", Toast.LENGTH_SHORT).show()
    }

    fun deleteFile(path: String) {
        RepositoryManager.deleteFile(path, context)
        _uiState.value = _uiState.value.copy(downloadedPaths = refreshDownloadedSet())
    }

    fun getDownloadedFiles(): List<DownloadedFile> = RepositoryManager.getDownloadedFiles(context)

    fun getLocalFile(path: String): File? = RepositoryManager.getLocalFile(path, context)

    fun openDownloadedFile(file: DownloadedFile) {
        val localFile = File(file.localPath)
        if (localFile.exists()) openWithSystemViewer(localFile)
        else {
            deleteFile(file.path)
            Toast.makeText(context, "文件已被删除", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWithSystemViewer(file: File) {
        try {
            if (!file.exists()) { Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show(); return }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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
        return try { context.startActivity(chooserIntent); true } catch (e: ActivityNotFoundException) { false }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    private fun refreshDownloadedSet(): Set<String> {
        return RepositoryManager.getDownloadedFiles(context).map { it.path }.toSet()
    }

    private fun sortDisplayItems(items: List<GitHubContentItem>): List<GitHubContentItem> {
        val dirs = items.filter { it.type == "dir" }.sortedBy { it.name.lowercase() }
        val files = items.filter { it.type == "file" }.sortedBy { it.name.lowercase() }
        return dirs + files
    }

    companion object {
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
