package com.ahu.ahutong.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.repository.DownloadedFile
import com.ahu.ahutong.data.repository.RepositoryManager
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.RepositoryViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun RepositoryDownloads(
    navController: NavHostController
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val activity = context as androidx.activity.ComponentActivity
    val viewModel: RepositoryViewModel = viewModel(viewModelStoreOwner = activity)
    val markdownState by viewModel.markdownState.collectAsState()
    var files by remember { mutableStateOf(viewModel.getDownloadedFiles()) }
    var deleteConfirmPath by remember { mutableStateOf<String?>(null) }
    var batchDeleteTargets by remember { mutableStateOf<List<String>?>(null) }
    var isManaging by remember { mutableStateOf(false) }
    var selectedPaths by remember { mutableStateOf(setOf<String>()) }
    val secondaryTextColor = if (isDark) {
        Color.White.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    fun refreshFiles() {
        files = viewModel.getDownloadedFiles()
    }

    LaunchedEffect(Unit) { refreshFiles() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(96.n1 withNight 10.n1)
    ) {
        // 顶栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
            Text(
                text = if (isManaging) "已选择 ${selectedPaths.size} 项" else "已下载文件",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (files.isNotEmpty()) {
                TextButton(onClick = {
                    isManaging = !isManaging
                    if (!isManaging) selectedPaths = emptySet()
                }) {
                    Text(if (isManaging) "完成" else "管理")
                }
            }
        }

        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无下载文件", style = MaterialTheme.typography.bodyLarge,
                        color = secondaryTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("浏览学习资料时可下载文件", style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(files, key = { it.path }) { file ->
                    val isSelected = file.path in selectedPaths
                    DownloadedFileRow(
                        file = file,
                        isManaging = isManaging,
                        isSelected = isSelected,
                        onClick = {
                            if (isManaging) {
                                selectedPaths = if (isSelected) selectedPaths - file.path
                                else selectedPaths + file.path
                            } else {
                                viewModel.openDownloadedFile(file)
                            }
                        },
                        onDelete = { deleteConfirmPath = file.path }
                    )
                }
            }

            // 管理模式底部栏
            if (isManaging && files.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(SmoothRoundedCornerShape(16.dp))
                        .background(100.n1 withNight 30.n1)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        selectedPaths = if (selectedPaths.size == files.size) emptySet()
                        else files.map { it.path }.toSet()
                    }) {
                        Text(if (selectedPaths.size == files.size) "取消全选" else "全选")
                    }
                    TextButton(
                        onClick = {
                            if (selectedPaths.isNotEmpty()) {
                                batchDeleteTargets = selectedPaths.toList()
                            }
                        },
                        enabled = selectedPaths.isNotEmpty()
                    ) {
                        Text(
                            "删除选中 (${selectedPaths.size})",
                            color = if (selectedPaths.isNotEmpty()) Color(0xFFFF5252)
                                    else secondaryTextColor
                        )
                    }
                }
            }
        }
    }

    RepositoryMarkdownReader(
        markdownState = markdownState,
        onDismiss = { viewModel.clearMarkdown() }
    )

    // 单个删除确认
    deleteConfirmPath?.let { path ->
        ConfirmDialog(
            title = "确认删除",
            message = "确定要删除此文件吗？",
            onCancel = { deleteConfirmPath = null },
            onConfirm = {
                viewModel.deleteFile(path)
                refreshFiles()
                selectedPaths = selectedPaths - path
                deleteConfirmPath = null
            }
        )
    }

    // 批量删除确认
    batchDeleteTargets?.let { targets ->
        ConfirmDialog(
            title = "批量删除",
            message = "确定要删除选中的 ${targets.size} 个文件吗？",
            onCancel = { batchDeleteTargets = null },
            onConfirm = {
                targets.forEach { viewModel.deleteFile(it) }
                refreshFiles()
                selectedPaths = emptySet()
                batchDeleteTargets = null
            }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .clip(SmoothRoundedCornerShape(24.dp))
                .background(96.n1 withNight 10.n1)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "取消",
                    modifier = Modifier.clickable { onCancel() }.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "删除",
                    modifier = Modifier.clickable { onConfirm() }.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
private fun DownloadedFileRow(
    file: DownloadedFile,
    isManaging: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val typeLabel = RepositoryViewModel.getFileTypeIcon(file.name)
    val fileAccentColor = when (typeLabel) {
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
    val uncheckedCheckboxColor = if (isDark) {
        Color.White.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 管理模式：复选框
        if (isManaging) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                contentDescription = if (isSelected) "已选择" else "未选择",
                tint = if (isSelected) 90.a1 withNight 90.a1 else uncheckedCheckboxColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // 类型标签
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

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatSize(file.size)} · ${RepositoryManager.formatDisplayPath(file.path)}",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isManaging) {
            IconButton(onClick = { onClick() }, modifier = Modifier.padding(start = 4.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "打开",
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.padding(start = 4.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
