package com.ahu.ahutong.ui.screen.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.repository.GitHubContentItem
import com.ahu.ahutong.data.repository.RepositoryDirectorySummary
import com.ahu.ahutong.data.repository.RepositoryManager
import com.ahu.ahutong.ui.state.RepositoryMarkdownUiState
import com.ahu.ahutong.ui.state.RepositoryViewModel
import com.kyant.monet.n1
import com.kyant.monet.withNight
import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.collect

@Composable
fun Repository(
    navController: NavHostController,
    path: String
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val viewModel: RepositoryViewModel = viewModel(viewModelStoreOwner = activity)
    val directoryStates by viewModel.directoryStates.collectAsState()
    val sharedState by viewModel.sharedState.collectAsState()
    val markdownState by viewModel.markdownState.collectAsState()
    val state = directoryStates[path] ?: viewModel.getDirectoryState(path)
    val initialScrollPosition = viewModel.getScrollPosition(path)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialScrollPosition.index,
        initialFirstVisibleItemScrollOffset = initialScrollPosition.offset
    )
    val shouldShowInitialProgress = path.isBlank() && state.items.isEmpty() && sharedState.isCacheWarming

    LaunchedEffect(path) {
        viewModel.ensureLoaded(path)
        viewModel.warmUpAllContentCaches()
    }

    LaunchedEffect(listState, path) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.saveScrollPosition(path, index, offset)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(96.n1 withNight 10.n1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
            Text(
                text = "学习资料",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            RepositoryRefreshButton(
                loading = state.isLoading || state.isRefreshing || sharedState.isCacheWarming,
                onRefresh = { viewModel.refreshDirectory(path) }
            )
            IconButton(onClick = { navController.navigate("repository_downloads") }) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "已下载",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { navController.navigate("repository_settings") }) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "学习资料设置"
                )
            }
        }

        RepositoryBreadcrumb(
            currentPath = path,
            onPathClick = { targetPath ->
                popToRepositoryPath(
                    navController = navController,
                    currentPath = path,
                    targetPath = targetPath
                )
            }
        )

        state.error?.let { error ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FF5252))
                    .clickable { viewModel.clearError(path) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = error,
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "关闭",
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (shouldShowInitialProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background((96.n1 withNight 10.n1).copy(alpha = 0.96f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Text(
                            text = "已获取 ${sharedState.cacheWarmUpCount} 个文件",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "正在整理学习资料目录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(state.items, key = { it.path }) { item ->
                        RepositoryItemRow(
                            item = item,
                            isDownloaded = item.path in sharedState.downloadedPaths,
                            isDownloading = item.path == sharedState.downloadingPath,
                            progress = sharedState.downloadProgress[item.path] ?: 0f,
                            directorySummary = state.directorySummaries[item.path],
                            onItemClick = {
                                if (item.type == "dir") {
                                    navController.navigateRepository(item.path)
                                }
                            },
                            onDownload = { viewModel.downloadFile(item) },
                            onOpen = { viewModel.openFile(item) }
                        )
                    }

                    if (state.items.isEmpty() && !state.isLoading) {
                        item {
                            RepositoryEmptyDirectoryMessage(
                                showGithubLink = viewModel.shouldShowUnsupportedDirectoryMessage(path),
                                onGithubClick = { openExternalUrl(viewModel.getGitHubUrl(path), activity) }
                            )
                        }
                    } else if (state.items.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    if (!state.isLoading && state.items.isNotEmpty()) {
                        item {
                            RepositoryFooter()
                        }
                    }
                }
            }
        }
    }

    RepositoryMarkdownReader(
        markdownState = markdownState,
        onDismiss = { viewModel.clearMarkdown() }
    )
}

@Composable
internal fun RepositoryMarkdownReader(
    markdownState: RepositoryMarkdownUiState,
    onDismiss: () -> Unit
) {
    if (!markdownState.isLoading && markdownState.document == null && markdownState.error == null) {
        return
    }

    val context = LocalContext.current
    val markwon = remember(context) { Markwon.create(context) }
    val markdownTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(96.n1 withNight 16.n1)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = markdownState.document?.title ?: "Markdown",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "关闭",
                    color = Color(0xFF42A5F5),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            when {
                markdownState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }
                markdownState.error != null -> {
                    Text(
                        text = markdownState.error,
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                markdownState.document != null -> {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        factory = { viewContext ->
                            ScrollView(viewContext).apply {
                                addView(
                                    TextView(viewContext).apply {
                                        movementMethod = LinkMovementMethod.getInstance()
                                        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f)
                                        setPadding(0, 0, 0, 0)
                                    }
                                )
                            }
                        },
                        update = { scrollView ->
                            val textView = scrollView.getChildAt(0) as TextView
                            textView.setTextColor(markdownTextColor)
                            markwon.setMarkdown(textView, markdownState.document.content)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepositoryRefreshButton(
    loading: Boolean,
    onRefresh: () -> Unit
) {
    IconButton(
        onClick = onRefresh,
        enabled = !loading,
        modifier = Modifier.size(40.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "刷新"
            )
        }
    }
}

@Composable
private fun RepositoryBreadcrumb(
    currentPath: String,
    onPathClick: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val segments = currentPath.split('/').filter { it.isNotEmpty() }
    val scrollState = rememberScrollState()
    val chipColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color(0xFFF5F6FB)
    }
    val selectedChipColor = if (isDark) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        Color(0xFFFFF1C8)
    }
    val selectedTextColor = if (isDark) {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFFDA9A00)
    }
    val paths = buildList {
        add("学习资料" to "")
        if (segments.isNotEmpty()) {
            val repositoryId = segments.first()
            val repositoryTitle = RepositoryManager.getRepositoryTitle(repositoryId)
            add(repositoryTitle to repositoryId)

            val actualRelativeSegments = segments.drop(1)
            val visibleRelativeSegments = if (actualRelativeSegments.firstOrNull() == repositoryTitle) {
                actualRelativeSegments.drop(1)
            } else {
                actualRelativeSegments
            }

            visibleRelativeSegments.indices.forEach { index ->
                val visibleCount = index + 1
                val actualCount = if (actualRelativeSegments.firstOrNull() == repositoryTitle) {
                    visibleCount + 1
                } else {
                    visibleCount
                }
                val actualPath = buildString {
                    append(repositoryId)
                    actualRelativeSegments.take(actualCount).forEach { segment ->
                        append('/')
                        append(segment)
                    }
                }
                add(visibleRelativeSegments[index] to actualPath)
            }
        }
    }

    LaunchedEffect(currentPath, scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        paths.forEachIndexed { index, (label, path) ->
            val selected = path == currentPath
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) selectedChipColor else chipColor)
                    .clickable(enabled = !selected) { onPathClick(path) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (selected) {
                    selectedTextColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (index < paths.lastIndex) {
                Text(
                    text = "›",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun RepositoryItemRow(
    item: GitHubContentItem,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    directorySummary: RepositoryDirectorySummary?,
    onItemClick: () -> Unit,
    onDownload: () -> Unit,
    onOpen: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isDir = item.type == "dir"
    val isMarkdown = RepositoryViewModel.isMarkdownFile(item.name)
    val rowEnabled = isDir || isDownloaded || isMarkdown
    val fileAccentColor = when (RepositoryViewModel.getFileTypeIcon(item.name)) {
        "PDF" -> Color(0xFFE53935)
        "DOC" -> Color(0xFF1565C0)
        "PPT" -> Color(0xFFE65100)
        "XLS" -> Color(0xFF2E7D32)
        else -> Color(0xFF757575)
    }
    val fileBadgeColor = if (isDark) {
        fileAccentColor.copy(alpha = 0.88f)
    } else {
        fileAccentColor.copy(alpha = 0.14f)
    }
    val fileBadgeTextColor = if (isDark) Color.White else fileAccentColor
    val secondaryTextColor = if (isDark) {
        Color.White.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = rowEnabled) {
                if (isDir) {
                    onItemClick()
                } else {
                    onOpen()
                }
            }
            .padding(horizontal = 6.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDir) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(42.dp)
            )
        } else {
            val typeLabel = RepositoryViewModel.getFileTypeIcon(item.name)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fileBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = fileBadgeTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!isDir && isDownloaded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.TaskAlt,
                        contentDescription = "已下载",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = if (isDir) {
                    directorySummary?.let {
                        "${it.directoryCount} 个目录 | ${it.fileCount} 个文件"
                    } ?: "目录"
                } else {
                    formatSize(item.size)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .width(44.dp)
                .padding(start = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDir -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
                isDownloading -> {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                }
                isDownloaded -> {
                    IconButton(onClick = onOpen, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "打开",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                else -> {
                    IconButton(onClick = onDownload, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "下载",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoryEmptyDirectoryMessage(
    showGithubLink: Boolean,
    onGithubClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showGithubLink) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "文件夹内包含的文件不是文档格式，如需查看，请前往",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "GitHub",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF42A5F5),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onGithubClick() }
                )
            }
        } else {
            Text(
                "此目录为空",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun openExternalUrl(url: String, context: Context) {
    if (url.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun popToRepositoryPath(
    navController: NavHostController,
    currentPath: String,
    targetPath: String
) {
    if (targetPath == currentPath) return
    val currentSegments = currentPath.split('/').filter { it.isNotEmpty() }
    val targetSegments = targetPath.split('/').filter { it.isNotEmpty() }
    val isAncestor = targetSegments.size <= currentSegments.size &&
        currentSegments.take(targetSegments.size) == targetSegments

    if (isAncestor) {
        repeat(currentSegments.size - targetSegments.size) {
            navController.popBackStack()
        }
        return
    }

    navController.navigateRepository(targetPath)
}

internal fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "${bytes / (1024 * 1024 * 1024)}GB"
    }
}

@Composable
private fun RepositoryFooter() {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val textColor = if (isDark) {
        Color.White.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val linkColor = Color(0xFF42A5F5)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            "发现新资料？向",
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
        Text(
            "GitHub 仓库",
            style = MaterialTheme.typography.bodySmall,
            color = linkColor,
            modifier = Modifier.clickable {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/Kaltsit-cell/AHU-CS-Repository")
                ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                context.startActivity(intent)
            }
        )
        Text(
            "提 PR 或向开发者",
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
        Text(
            "联系",
            style = MaterialTheme.typography.bodySmall,
            color = linkColor,
            modifier = Modifier.clickable {
                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:1793838025@qq.com")
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        )
    }
}
