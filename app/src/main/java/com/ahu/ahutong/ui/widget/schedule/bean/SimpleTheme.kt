package com.ahu.ahutong.ui.widget.schedule.bean

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.ahu.ahutong.R
import kotlin.random.Random

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午4:37
 * @Email: 468766131@qq.com
 */

class SimpleTheme(private val colors: List<String>): ScheduleTheme.Theme(){


    override fun setWeekdayListHeader(linearLayout: LinearLayout) {
        return
    }

    override fun setContentBackground(linearLayout: LinearLayout) {
        return
    }

    override fun setItem(item: View, isThisWeek: Boolean) {
        if (isThisWeek){
            item.background = getRandomBackground()
        }else{
            item.background = getGrayBackground()
        }
    }

    override fun setToday(view: TextView) {
        view.setBackgroundResource(R.drawable.schedule_weekday_background)
        val gradientDrawable = view.background as GradientDrawable
        gradientDrawable.colors = intArrayOf(Color.parseColor(colors[0]), Color.parseColor(colors[0]))
        view.setTextColor(Color.WHITE)
    }

    private fun getRandomBackground(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        //圆角矩形
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadius = 15f
        //随机颜色
        val index = Random(System.currentTimeMillis()).nextInt(colors.size)
        gradientDrawable.colors = intArrayOf(Color.parseColor(colors[index]), Color.parseColor(colors[index]))
        return gradientDrawable
    }

    private fun getGrayBackground(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        //圆角矩形
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadius = 5f
        //灰色颜色
        gradientDrawable.colors = intArrayOf(Color.GRAY, Color.GRAY)
        return gradientDrawable
    }
}