package com.ahu.ahutong.ui.state

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.model.AppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午1:10
 * @Email: 468766131@qq.com
 */
class AboutViewModel : ViewModel() {
    val versionName: String by lazy {
        val packageInfo = Utils.getApp().packageManager.getPackageInfo(
            Utils.getApp().packageName,
            0
        )
        packageInfo.versionName
    }

    val latestVersions: MutableLiveData<Result<AHUResponse<AppVersion>>> = MutableLiveData()

    /**
     * App 更新
     * @return Job
     */
    fun getAppLatestVersion() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            latestVersions.value = try {
                Result.success(AHUService.API.getLatestVersion())
            } catch (e: Exception) {
                Result.failure(Throwable("网络连接异常，获取最新版本失败！"))
            }
        }
    }

    // TODO: fix crash
    fun checkForUpdates(context: ComponentActivity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                latestVersions.value = try {
                    val localVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    // 主动检查，要提示
                    latestVersions.value?.onSuccess {
                        if (!it.isSuccessful) {
                            Toast.makeText(context, "检查更新失败：${it.msg}", Toast.LENGTH_SHORT).show()
                            return@onSuccess
                        }
                        if (it.data.version != localVersion) {
                            // TODO: use Compose
                            /*MaterialAlertDialogBuilder(context).apply {
                                setTitle("更新")
                                setMessage("发现新版本！\n新版特性：\n ${it.data.message}")
                                setPositiveButton("前往下载") { _, _ ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(it.data.url)
                                        }
                                    )
                                }
                                setNegativeButton("取消", null)
                            }.show()*/
                            Toast.makeText(context, "当前已是最新版本！", Toast.LENGTH_SHORT).show()
                            return@onSuccess
                        }
                    }?.onFailure {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                    Result.success(AHUService.API.getLatestVersion())
                } catch (e: Exception) {
                    Result.failure(Throwable("网络连接异常，获取最新版本失败！"))
                }
            }
        }
    }
}
