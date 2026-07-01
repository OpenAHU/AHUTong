package com.ahu.ahutong.data.repository

import com.google.gson.annotations.SerializedName

/**
 * GitHub 仓库内容项（目录或文件）
 */
data class GitHubContentItem(
    val name: String,
    val path: String,
    val type: String,        // "file" or "dir"
    val size: Long = 0,
    @SerializedName("download_url")
    val downloadUrl: String? = null,
    @SerializedName("html_url")
    val htmlUrl: String? = null
)

/**
 * 本地下载记录
 */
data class DownloadedFile(
    val name: String,
    val path: String,         // GitHub 路径
    val localPath: String,    // 本地文件路径
    val size: Long = 0,
    val downloadTime: Long = System.currentTimeMillis()
)

/**
 * 本地缓存的目录内容
 */
/**
 * 学院仓库配置
 */
data class RepoConfig(
    val id: String,
    val name: String,           // "计算机科学与技术学院"
    val owner: String,
    val repo: String,
    val branch: String = "master"
) {
    val rawBase get() = "https://raw.githubusercontent.com/$owner/$repo/$branch"
    val cdnBase get() = "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch"
    val githubUrl get() = "https://github.com/$owner/$repo"
}

data class CachedRepositoryContents(
    val items: List<GitHubContentItem>,
    val updateTime: Long
)
