package com.ahu.ahutong.ui.widget.schedule.bean

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.ahu.ahutong.R
import org.json.JSONObject
import kotlin.random.Random

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午4:37
 * @Email: 468766131@qq.com
 */

class SimpleTheme(config: String) : ScheduleTheme.Theme(config) {
    val colors: List<String>

    init {
        val jsonObject = JSONObject(config)
        val type = jsonObject.optString("type")
        if (type != SimpleTheme::class.java.name) {
            throw IllegalArgumentException("主题不是Simple主题")
        }
        val color1 = jsonObject.optString("color1")
        val color2 = jsonObject.optString("color2")
        val color3 = jsonObject.optString("color3")
        val color4 = jsonObject.optString("color4")
        val color5 = jsonObject.optString("color5")
        colors = listOf<String>(color1, color2, color3, color4, color5)
    }


    override fun setWeekdayListHeader(linearLayout: LinearLayout) {
        return
    }

    override fun setContentBackground(linearLayout: LinearLayout) {
        return
    }

    override fun setItem(item: View, isThisWeek: Boolean) {
        if (isThisWeek) {
            item.background = getRandomBackground()
        } else {
            item.background = getGrayBackground()
        }
    }

    override fun setToday(view: TextView) {
        view.setBackgroundResource(R.drawable.schedule_weekday_background)
        val gradientDrawable = view.background as GradientDrawable
        gradientDrawable.colors =
            intArrayOf(Color.parseColor(colors[0]), Color.parseColor(colors[0]))
        view.setTextColor(Color.WHITE)
    }

    override fun toConfig(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("type", SimpleTheme::class.java.name)
        for (i in 1..5) {
            jsonObject.put("color$i", colors[i - 1])
        }
        return jsonObject
    }


    private fun getRandomBackground(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        //圆角矩形
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadius = 15f
        //随机颜色
        val index = Random(System.currentTimeMillis()).nextInt(colors.size)
        gradientDrawable.colors =
            intArrayOf(Color.parseColor(colors[index]), Color.parseColor(colors[index]))
        return gradientDrawable
    }

    private fun getGrayBackground(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        //圆角矩形
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadius = 15f
        //灰色颜色
        gradientDrawable.colors = intArrayOf(Color.GRAY, Color.GRAY)
        return gradientDrawable
    }
}