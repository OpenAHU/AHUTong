package com.ahu.ahutong.ui.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.repository.GitHubContentItem
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.RepositoryViewModel
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Repository(
    navController: NavHostController
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val viewModel: RepositoryViewModel = viewModel(viewModelStoreOwner = activity)
    val state by viewModel.uiState.collectAsState()

    BackHandler(enabled = state.pathStack.isNotEmpty() || state.pendingPath != null) {
        viewModel.goBack()
    }

    LaunchedEffect(Unit) {
        if (state.items.isEmpty() && !state.isLoading) {
            viewModel.loadContents()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(96.n1 withNight 10.n1)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (!viewModel.goBack()) {
                    navController.popBackStack()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
            Text(
                text = if (state.currentPath.isEmpty()) "学习资料" else state.currentPath.substringAfterLast('/'),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (state.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = { viewModel.refreshCurrentDirectory() }) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "刷新"
                    )
                }
            }
            TextButton(onClick = { navController.navigate("repository_downloads") }) {
                Text("已下载")
            }
        }

        // 路径面包屑
        if (state.currentPath.isNotEmpty()) {
            Text(
                text = state.currentPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        RepositorySyncStatus(
            pendingPath = state.pendingPath,
            isRefreshing = state.isRefreshing && state.items.isNotEmpty(),
            isShowingCachedContents = state.isShowingCachedContents,
            cacheUpdatedAt = state.cacheUpdatedAt
        )

        // 错误提示
        state.error?.let { error ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FF5252))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = error,
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("关闭", color = Color(0xFFFF5252))
                }
            }
        }

        // 内容
        if (state.isLoading && state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.items, key = { it.path }) { item ->
                    RepositoryItemRow(
                        item = item,
                        isNavigationEnabled = state.pendingPath == null,
                        isDownloaded = item.path in state.downloadedPaths,
                        isDownloading = item.path == state.downloadingPath,
                        progress = state.downloadProgress[item.path] ?: 0f,
                        onItemClick = {
                            if (item.type == "dir") {
                                viewModel.enterDirectory(item)
                            }
                        },
                        onDownload = { viewModel.downloadFile(item) },
                        onOpen = { viewModel.openFile(item) }
                        )
                }

                if (state.items.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "此目录为空",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 底部提示
                if (!state.isLoading) {
                    item {
                        RepositoryFooter()
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositorySyncStatus(
    pendingPath: String?,
    isRefreshing: Boolean,
    isShowingCachedContents: Boolean,
    cacheUpdatedAt: Long?
) {
    val text = when {
        pendingPath != null -> "正在打开 ${pendingPath.substringAfterLast('/')}"
        isRefreshing -> "正在同步 GitHub 最新目录"
        isShowingCachedContents -> {
            val cacheTime = formatCacheAge(cacheUpdatedAt)
            if (cacheTime.isEmpty()) {
                "GitHub 连接失败，当前显示上次缓存"
            } else {
                "GitHub 连接失败，当前显示 $cacheTime 的缓存"
            }
        }
        else -> return
    }
    val backgroundColor = if (isRefreshing) Color(0x2233A1FD) else Color(0x33FFB300)
    val textColor = if (isRefreshing) Color(0xFF1976D2) else Color(0xFF8A5A00)

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = textColor,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun RepositoryItemRow(
    item: GitHubContentItem,
    isNavigationEnabled: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onItemClick: () -> Unit,
    onDownload: () -> Unit,
    onOpen: () -> Unit
) {
    val isDir = item.type == "dir"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(SmoothRoundedCornerShape(16.dp))
            .background(100.n1 withNight 30.n1)
            .clickable(enabled = isDir && isNavigationEnabled) { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            if (isDir) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(28.dp)
                )
            } else {
                val typeLabel = RepositoryViewModel.getFileTypeIcon(item.name)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (typeLabel) {
                                "PDF" -> Color(0xFFE53935)
                                "DOC" -> Color(0xFF1565C0)
                                "PPT" -> Color(0xFFE65100)
                                "XLS" -> Color(0xFF2E7D32)
                                else -> Color(0xFF757575)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 名称
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isDir && item.size > 0) {
                    Text(
                        text = formatSize(item.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 操作按钮
            if (!isDir) {
                if (isDownloading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else if (isDownloaded) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.TaskAlt,
                                contentDescription = "已下载",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "已下载",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        TextButton(
                            onClick = onOpen,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("打开", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.CloudDownload,
                            contentDescription = "下载",
                            tint = Color(0xFF42A5F5),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 下载进度条
        if (isDownloading && progress > 0f && progress < 1f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )
        }
    }
}

internal fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "${bytes / (1024 * 1024 * 1024)}GB"
    }
}

private fun formatCacheAge(cacheUpdatedAt: Long?): String {
    if (cacheUpdatedAt == null || cacheUpdatedAt <= 0L) return ""
    val diffMillis = (System.currentTimeMillis() - cacheUpdatedAt).coerceAtLeast(0L)
    val minutes = diffMillis / (60 * 1000)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        else -> "${days}天前"
    }
}

@Composable
private fun RepositoryFooter() {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
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
