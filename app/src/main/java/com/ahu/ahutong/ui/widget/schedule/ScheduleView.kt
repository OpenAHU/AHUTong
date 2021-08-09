package com.ahu.ahutong.ui.widget.schedule

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ext.dp
import com.ahu.ahutong.ui.widget.schedule.bean.CourseDate
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleCourse
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleTheme
import com.sink.library.log.SinkLog
import java.util.*

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午2:01
 * @Email: 468766131@qq.com
 */
class ScheduleView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val headerTimeMsg by lazy {
        hashMapOf(
            1 to "8:20",
            2 to "9:15",
            3 to "10:20",
            4 to "11:15",
            5 to "14:00",
            6 to "14:55",
            7 to "15:50",
            8 to "16:45",
            9 to "19:00",
            10 to "19:55",
            11 to "20:50"
        )
    }
    private val headerWeekdayMsg by lazy {
        hashMapOf(1 to "周一", 2 to "周二", 3 to "周三", 4 to "周四", 5 to "周五", 6 to "周六", 7 to "周日")
    }

    //表头-星期几
    private val weekdayList: LinearLayout

    //表头-第几节课
    private val timeList: LinearLayout

    //下半部分
    private val bottomLayout: LinearLayout

    //小兔子
    private val settingImg: ImageView

    //表主体
    private val contentLinearLayout: LinearLayout

    //课表数据
    private var coursesData: MutableMap<Int, MutableList<ScheduleCourse>> = mutableMapOf()

    //第几周
    private var week = 1

    //周几
    private var weekday = 1

    //开学时间
    private lateinit var startTime: Date

    //主题
    private var theme: ScheduleTheme = DefaultDataUtils.getDefaultTheme()

    //是否显示非本周课
    private var isShowAllCourses = false
    private var tableHeaderWidth = 35.dp
    private var tableHeaderHeight = 50.dp

    //每个格子的宽高
    private var courseWidth: Float = 0.0f
    private var courseHeight: Float = 0.0f

    private var mCourseListener: (View, ScheduleCourse, CourseDate) -> Unit
    private var mEmptyCourseListener: (View, CourseDate) -> Unit

    init {
        //加载布局
        View.inflate(context, R.layout.layout_schedule, this)
        //初始化控件
        weekdayList = findViewById(R.id.schedule_weekday_list)
        timeList = findViewById(R.id.schedule_time_list)
        settingImg = findViewById(R.id.schedule_setting)
        contentLinearLayout = findViewById(R.id.schedule_course_content)
        bottomLayout = findViewById(R.id.schedule_bottom)
        mCourseListener = { _, _, _ -> }
        mEmptyCourseListener = { _, _ -> }
    }

    /**
     *  创建一堆构造器重载
     */
    constructor(context: Context?) : this(context, null, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )


    fun loadSchedule() {
        post {
            courseHeight = (height - tableHeaderHeight) / 11
            courseWidth = (width - tableHeaderWidth) / 7
            initTableHeader()
            initTableBody()
        }

    }

    private fun initTableBody() {
        if (contentLinearLayout.childCount != 0){
            contentLinearLayout.removeAllViews()
        }

        if (coursesData.isNullOrEmpty()) {
            return
        }
        for (i in 1..7) {
            //创建纵向课程容器
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = VERTICAL
            //填充课程
            coursesData[i]?.forEach {
                val course = it.getCourse(week, isShowAllCourses)
                if (course == null) {
                    addEmptyCourse(linearLayout, CourseDate(i, it.startTime))
                } else {
                    addCourseMessage(
                        course, linearLayout, CourseDate(i, it.startTime),
                        course.startWeek < week && course.endWeek > week
                    )
                }
            }
            contentLinearLayout.addView(linearLayout, courseWidth.toInt(), -1)
        }
    }

    /**
     * 添加负责占位的空白课
     */
    private fun addEmptyCourse(linearLayout: LinearLayout, date: CourseDate) {
        val view = View(context)
        view.setOnClickListener { it ->
            mEmptyCourseListener(view, date)
        }
        //添加进父布局
        val lparams = LayoutParams(courseWidth.toInt(), courseHeight.toInt())
        linearLayout.addView(view, lparams)
    }

    /**
     * 添加课程信息
     */
    private fun addCourseMessage(
        course: Course,
        linearLayout: LinearLayout,
        date: CourseDate,
        isThisWeek: Boolean
    ) {
        //解析课程item
        val courseView = inflate(context, R.layout.item_schdule_course, null)
        val tvName = courseView.findViewById<TextView>(R.id.course_name)
        val tvLocation = courseView.findViewById<TextView>(R.id.course_location)
        //设置显示信息
        if (isThisWeek) {
            tvName.text = course.name
        } else {
            tvName.text = resources.getString(R.string.schedule_not_this_week, course.name)
        }
        tvLocation.text = course.location
        //设置背景
        theme.theme.setItem(courseView, isThisWeek)
        //设置点击事件
        courseView.setOnClickListener { view ->
            coursesData[date.weekDay]?.get(date.startTime)?.let {
                mCourseListener(view, it, date)
            }
        }
        //添加进父布局
        val lparams =
            LayoutParams(courseWidth.toInt() - 4, courseHeight.toInt() * course.length - 8)
        lparams.setMargins(2, 4, 2, 4)
        linearLayout.addView(courseView, lparams)

    }

    private fun initTableHeader() {
        initTableTimeHeader()
        initTableWeekdayHeader()
    }

    /**
     * 初始化WeekdayList表头
     */
    private fun initTableWeekdayHeader() {
        if (weekdayList.childCount != 0){
            weekdayList.removeAllViews()
        }
        //周一到周末
        //日期计算
        val calendar = Calendar.getInstance()
        calendar.time = startTime // 设置时间为开学时间
        calendar.add(Calendar.DATE, 7 * (week - 1)) //以开学时间为基准，计算要显示的周的时间
        //为周几，日期添加数据
        for (i in 1..7) {
            //获取item对象
            val headerItem =
                LayoutInflater.from(context).inflate(R.layout.item_schedule_header, this, false)
            //初始化控件
            val tvTop = headerItem.findViewById<TextView>(R.id.tv_top)
            val tvBottom = headerItem.findViewById<TextView>(R.id.tv_bottom)
            //设置值
            tvTop.text = headerWeekdayMsg[i]
            if (i == weekday) {
                theme.theme.setToday(tvTop)
            }
            tvBottom.text = String.format(
                "%d/%d",
                calendar[Calendar.MONTH] + 1,
                calendar[Calendar.DAY_OF_MONTH]
            )
            //日期加1
            calendar.add(Calendar.DATE, 1)
            //添加view
            weekdayList.addView(headerItem, courseWidth.toInt(), -1)
        }
        //设置主题
        theme.theme.setWeekdayListHeader(weekdayList)
    }

    /**
     * 初始化Time表头
     */
    private fun initTableTimeHeader() {
        if (timeList.childCount != 0){
            timeList.removeAllViews()
        }
        for (i in 1..11) {
            val headerItem =
                LayoutInflater.from(context).inflate(R.layout.item_schedule_header, this, false)
            //初始化控件
            val tvTop = headerItem.findViewById<TextView>(R.id.tv_top)
            val tvBottom = headerItem.findViewById<TextView>(R.id.tv_bottom)
            //设置值
            tvTop.text = "$i"
            tvBottom.text = headerTimeMsg[i]
            timeList.addView(headerItem, -1, courseHeight.toInt())
        }

    }

    /**
     * 设置当前Date
     * @param week Int
     * @param weekday Int
     * @return ScheduleView
     */
    fun date(week: Int, weekday: Int): ScheduleView {
        this.week = week
        this.weekday =
            if (weekday == Calendar.SUNDAY) 7 else weekday - 1
        return this
    }

    /**
     * 设置课表数据
     * @param data List<Course>
     * @return ScheduleView
     */
    fun data(data: List<Course>): ScheduleView {
        //清空数据
        coursesData = mutableMapOf()
        //转换数据
        for (i in 1..7) {
            val list = mutableListOf<ScheduleCourse>()
            var j = 0
            while (j < 11) {
                val scheduleCourse = ScheduleCourse(j + 1)
                data.forEach {
                    //这一天的这一节开始的
                    if (it.weekday == i && it.startTime == scheduleCourse.startTime) {
                        scheduleCourse.addCourse(it)
                    }
                }
                list.add(scheduleCourse)
                //修改步长
                j += scheduleCourse.length
            }
            coursesData[i] = list
        }
        return this
    }


    /**
     * 设置主题
     * @param theme ScheduleTheme
     * @return ScheduleView
     */
    fun theme(theme: ScheduleTheme): ScheduleView {
        this.theme = theme
        return this
    }


    /**
     * 是否显示非本周课
     * @param isShowAllCourses Boolean
     * @return ScheduleView
     */
    fun showAllCourse(isShowAllCourses: Boolean): ScheduleView {
        SinkLog.i("set show all courses is $isShowAllCourses")
        this.isShowAllCourses = isShowAllCourses
        return this
    }

    /**
     * 开学时间
     * @param startTime Date
     * @return ScheduleView
     */
    fun startTime(startTime: Date): ScheduleView {
        val calendar = Calendar.getInstance()
        calendar.time = startTime // 设置时间为开学时间
        if (calendar[Calendar.DAY_OF_WEEK] != Calendar.MONDAY) {
            throw IllegalArgumentException("startTime must be Monday")
        }
        this.startTime = startTime
        return this
    }


    /**
     * 设置空课监听器
     * @param listener Function2<[@kotlin.ParameterName] View, [@kotlin.ParameterName] CourseDate, Unit>
     */
    fun setEmptyCourseListener(listener: (view: View, location: CourseDate) -> Unit) {
        this.mEmptyCourseListener = listener
    }


    /**
     * 设置有课监听器
     * @param listener Function3<[@kotlin.ParameterName] View, [@kotlin.ParameterName] ScheduleCourse, [@kotlin.ParameterName] CourseDate, Unit>
     */
    fun setCourseListener(listener: (view: View, scheduleCourse: ScheduleCourse, location: CourseDate) -> Unit) {
        this.mCourseListener = listener
    }

}