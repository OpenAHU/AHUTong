package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Tel
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils

class TelDirectoryViewModel : ViewModel() {
    val theme by lazy { AHUCache.getScheduleTheme() ?: DefaultDataUtils.getDefaultTheme() }

    companion object {
        /**
         * 来自 2022 版学生手册，
         * 座机号码区号均为 0551，
         * 无蜀山校区
         */
        val TelBook by lazy {
            mapOf(
                "师生综合服务大厅" to listOf(
                    Tel("咨询台", "63861400"),
                    Tel("财务处", "63861322", "65107303"),
                    Tel("研究生院", "63861455", "65107332"),
                    Tel("教务处", "63861477", "65107135"),
                    Tel("学生处（学生发展中心）", "63861686", "65107232"),
                    Tel("国际合作与交流处", "63861848"),
                    Tel("学校办公室", "63861659", "65107020"),
                    Tel("网络信息中心", "63861855", "65107282"),
                    Tel("保卫处", "63861918"),
                    Tel("人事处", "63861922"),
                    Tel("国有资产管理与实验室管理处", "63861949", "65107310"),
                    Tel("人文社会科学处", "63861958"),
                    Tel("科学技术处", "63861983"),
                    Tel("校团委", null, "65107537")
                ),
                "学生处" to listOf(
                    Tel("学生思想教育科", "63861054"),
                    Tel("学生管理科", "63861008"),
                    Tel("学生资助管理中心", "63861900"),
                    Tel("毕业生就业指导中心", "63861355"),
                    Tel("心理健康教育与咨询中心", "63861018", "65106972")
                ),
                "校团委" to listOf(
                    Tel("团委办公室", "63861121")
                ),
                "校医院" to listOf(
                    Tel("24 小时值班电话", "63861120"),
                    Tel("校医疗保障委员会办公室", "63861715")
                ),
                "磬苑后勤" to listOf(
                    Tel("学校物业监管办公室", "63861358", null),
                    Tel("校园报修电话", "63861114", null),
                    Tel("北区 24 小时报修电话", "62950090", null),
                    Tel("南区 24 小时报修电话", "63861333", null),
                    Tel("桃园", "63861034", null),
                    Tel("李园", "63861037", null),
                    Tel("桔园", "63861036", null),
                    Tel("枣园", "63861218", null),
                    Tel("榴园", "63861217", null),
                    Tel("杏园", "63861219", null),
                    Tel("蕙8", "63861654", null),
                    Tel("蕙9", "63861617", null),
                    Tel("蕙10", "63861697", null),
                    Tel("松园", "63861160", null),
                    Tel("竹园", "63861115", null),
                    Tel("梅园", "63861113", null),
                    Tel("桂园", "63861097", null),
                    Tel("枫园", "63861096", null),
                    Tel("槐园", "63861081", null),
                    Tel("留学生公寓", "63861096", null)
                ),
                "龙河后勤" to listOf(
                    Tel("学校物业监管办公室", null, "63861358"),
                    Tel("宿舍管理办公室", null, "65107014"),
                    Tel("校园报修电话", null, "63861114"),
                    Tel("206楼", null, "65107064"),
                    Tel("207-208楼", null, "65108150"),
                    Tel("209楼", null, "65107940"),
                    Tel("301楼", null, "65108004"),
                    Tel("304-306楼", null, "65108004")
                ),
                "报警电话" to listOf(
                    Tel("报警电话", "63861110", "65108229")
                )
            )
        }
    }
}
