package com.ahu.ahutong.sdk

import androidx.annotation.Keep

@Keep
interface ProgressCallback {
    /**
     * @param downloaded 已下载字节数
     * @param total 总字节数；如果未知（chunked/无 Content-Length）则为 -1
     */
    fun onProgress(downloaded: Long, total: Long)
}