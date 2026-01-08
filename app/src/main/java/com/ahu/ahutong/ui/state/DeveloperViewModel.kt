package com.ahu.ahutong.ui.state

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.core.net.toUri

/**
 *
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
class DeveloperViewModel : ViewModel() {
    interface CardInfo {
        val name: String
        val desc: String
        val qq: String
        val img: String
        val onclick: (Context) -> Unit
    }

    class Developer(override val name: String, override val desc: String, override val qq: String) :
        CardInfo {

        override val img: String
            get() = "https://q1.qlogo.cn/g?b=qq&nk=$qq&s=640"
        override val onclick: (Context) -> Unit
            get() = { context ->
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "mqqapi://card/show_pslcard?&uin=$qq".toUri()
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, "请安装 QQ 或 Tim", Toast.LENGTH_SHORT).show()
                }
            }
    }

    class Partner(override val name: String, override val desc: String) : CardInfo {
        override val qq: String = ""
        override val img: String = ""
        override val onclick: (Context) -> Unit
            get() = { context ->
                Toast.makeText(context, "请联系任意一位小伙伴", Toast.LENGTH_SHORT).show()
            }
    }

    val developers by lazy {
        listOf(
            Developer("高玉灿（20级）", "架构规划、页面设计、爬虫", "468766131"),
            Developer("谭哲昊（21级）", "架构规划、小组件", "330771794"),
            Developer("王学雷（22级）", "页面设计、交互设计、新技术探索", "257314409"),
            Developer("徐健灿（22级）", "爬虫、交互设计", "3148336396"),
            Developer("王    钰（22级）", "爬虫、功能巡检", "605606366"),
        )
    }
    val partners by lazy {
        listOf(
            Partner(
                "Hello~",
                "We are waiting for you!\nGet connection with us now!\nClick me for more info!",
            ),
        )
    }
}
