package com.ahu.ahutong.ui.widget.schedule.bean

import org.json.JSONObject

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午5:08
 * @Email: 468766131@qq.com
 */
object DefaultDataUtils {
    val simpleThemes by lazy {
        listOf(
            createSimpleTheme(
                "绿茵萌动",
                listOf("#8ac58b", "#7ca271", "#abcb88", "#a4b895", "#9daf85")
            ),
            createSimpleTheme(
                "夏天的书店【是吾乡】",
                listOf("#8C615A", "#EA6D56", "#F2946B", "#FFC095", "#D7DAB7")
            ),
            createSimpleTheme(
                "邂逅春天",
                listOf("#86E3CE", "#D0E6A5", "#FFDD94", "#FA897B", "#CCABD8")
            ),
            createSimpleTheme(
                "樱花呢喃",
                listOf("#f2adc2", "#f39ab0", "#f7b1cd", "#f0c3e0", "#f0d0e7")
            ),
            createSimpleTheme(
                "阳光与你",
                listOf("#C8E2EF", "#FDAD58", "#FEC97C", "#F6D985", "#F9E2B3")
            ),
            createSimpleTheme(
                "海的传说",
                listOf("#8EC7CD", "#265359", "#468086", "#72ADAD", "#A3D2CE")
            ),
            createSimpleTheme(
                "小薰姑娘",
                listOf("#C289C5", "#A380B5", "#B99DCE", "#CCADDC", "#E7C7EB")
            ),
            createSimpleTheme("午后咖啡", listOf("#613E3B", "#CA7159", "#A98175", "#CBC0AA", "#F3D18E"))

        )
    }

    // fun getDefaultTheme() = ScheduleTheme("默认主题",SimpleTheme(listOf("#8ac58b", "#7ca271", "#abcb88", "#a4b895", "#9daf85")))
    fun getDefaultTheme(): ScheduleTheme {
        return simpleThemes[0]
    }

    private fun createSimpleTheme(name: String, colors: List<String>): ScheduleTheme {
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("type", SimpleTheme::class.java.name)
        for (i in 1..5) {
            jsonObject.put("color$i", colors[i - 1])
        }
        return ScheduleTheme(jsonObject.toString())
    }
}
