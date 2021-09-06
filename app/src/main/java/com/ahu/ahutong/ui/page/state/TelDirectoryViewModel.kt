package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Developer
import com.ahu.ahutong.data.model.Tel
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleTheme

class TelDirectoryViewModel : ViewModel() {
    val theme by lazy { AHUCache.getScheduleTheme() ?: DefaultDataUtils.getDefaultTheme() }
    companion object{
        val TelBook by lazy {
            mapOf(
                "常用" to listOf<Tel>(
                    Tel("网络服务中心", "0551-63861118"),
                    Tel("一卡通办公室", "0551-63861077"),
                    Tel("报警电话", "0551-63861110"),
                    Tel("校医院值班电话", "0551-63861120"),
                    Tel("物业办公室", "0551-63861044"),
                    Tel("物业修缮服务室", "0551-63861114"),
                    Tel("考试考务中心", "0551-63861053"),
                    Tel("学生资助管理中心", "0551-63861008"),
                    Tel("大学生勤俭中心", "0551-63861181"),
                    Tel("图书馆", "0551-63861109")
                ),
                "教务处" to listOf<Tel>(
                    Tel("综合办公室", "0551-63861055"),
                    Tel("考试考务中心", "0551-63861053"),
                    Tel("教学质量科", "0551-63861235"),
                    Tel("教学运行中心", "0551-63861203"),
                    Tel("学籍管理科", "0551-63861202")
                ),
                "团委" to listOf<Tel>(
                    Tel("团委办公室", "0551-63861121"),
                    Tel("创服中心", "0551-63861550"),
                    Tel("社团联合员会", "0551-63861182"),
                    Tel("大学生勤俭中心", "0551-63861181"),
                    Tel("团学宣传中心", "0551-63861662")
                ),
                "学生处" to listOf(
                    Tel("学生思想教育科", "0551-63861054"),
                    Tel("学生管理科", "0551-63861900"),
                    Tel("学生资助管理中心", "0551-63861008"),
                    Tel("就业指导中心", "0551-63861355")
                ),
                "财务处" to listOf(
                    Tel("办公室","0551-63861569"),
                    Tel("收费管理科", "0551-63861561")
                ),
                "保卫处" to listOf(
                    Tel("办公室", "0551-63861224"),
                    Tel("户籍室", "0551-63861184"),
                    Tel("报警电话", "0551-63861110")
                ),
                "宿舍" to listOf(
                    Tel("桃园", "0551-63861034"),
                    Tel("李园", "0551-63861037"),
                    Tel("桔园", "0551-63861036"),
                    Tel("枣园", "0551-63861218"),
                    Tel("榴园", "0551-63861217"),
                    Tel("杏园", "0551-63861219"),
                    Tel("松园", "0551-63861160"),
                    Tel("竹园", "0551-63861115"),
                    Tel("梅园", "0551-63861113"),
                    Tel("桂园", "0551-63861097"),
                    Tel("枫园", "0551-63861096")
                ),
                "物业" to listOf(
                    Tel("办公室", "0551-63861044"),
                    Tel("修缮服务室", "0551-63861114")
                ),
                "校医院" to listOf(
                    Tel("24小时值班电话", "0551-63861120"),
                    Tel("校医疗保障办公室", "0551-65108781")
                )

            )
        }
    }

}
