package com.ahu.ahutong.data.reptile

import android.util.Log
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.data.model.Room
import com.ahu.ahutong.data.reptile.utils.DES
import com.ahu.ahutong.data.reptile.utils.timeMap
import com.ahu.ahutong.data.reptile.utils.weekdayMap
import com.google.gson.JsonParser
import com.sink.library.log.SinkLog
import kotlinx.coroutines.*
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.math.RoundingMode
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.DecimalFormat
import java.util.regex.Pattern

@Suppress("BlockingMethodInNonBlockingContext")
object Reptile {
    /**
     * 登录
     * @param reptileUser User
     * @return Result<Boolean>
     */
    suspend fun login(reptileUser: ReptileUser): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            var response = Jsoup.newSession()
                .url(Constants.URL_LOGIN_BASE.format(Constants.URL_LOGIN_HOME))
                .timeout(ReptileManager.getInstance().timeout)
                .execute()
            val jsessionID = response.cookie("JSESSIONID")
            //保存Cookie 『JesessionID』『Language』
            ReptileManager.getInstance().cookieStore.putAll(response.cookies())
            //计算登录参数
            val body = response.parse().body()
            val lt = body.select("#lt").attr("value")
            val execution = body.select("input[name=execution]").attr("value")
            val _eventId = body.select("input[name=_eventId]").attr("value")
            val rsa = DES().strEnc(reptileUser.username + reptileUser.password + lt, "1", "2", "3")
            val loginData = mapOf(
                "lt" to lt,
                "ul" to reptileUser.username.length.toString(),
                "pl" to reptileUser.password.length.toString(),
                "rsa" to rsa,
                "execution" to execution,
                "_eventId" to _eventId
            )
            //开始登录
            response = Jsoup.newSession()
                .url(Constants.URL_LOGIN_1.format(jsessionID, Constants.URL_LOGIN_HOME))
                .cookies(ReptileManager.getInstance().cookieStore.cookies)
                .timeout(ReptileManager.getInstance().timeout)
                .method(Connection.Method.POST)
                .followRedirects(false) // 禁止重定向
                .data(loginData)
                .execute()
            //保存登录的Cookie 『CASTGC』『Language』、『CASPRIVACY』= ""
            ReptileManager.getInstance().cookieStore.putAll(response.cookies())

            val url = response.header("Location")
                ?: throw IllegalStateException("账号或密码错误")
            //获取tp_up
            response = Jsoup.newSession()
                .url(url)
                .timeout(ReptileManager.getInstance().timeout)
                .method(Connection.Method.GET)
                .followRedirects(false)
                .execute()
            //检查登录是否成功
            response.header("Location")
                ?: throw IllegalStateException("账号或密码错误")
            //保存『tp_up』
            ReptileManager.getInstance().cookieStore.putAll(response.cookies())
            Result.success(true)
        } catch (e: Exception) {
            return@withContext handleError(e)
        }
    }


    /**
     * 教务登录
     * @return Result<Boolean>
     */
    private suspend fun loginTeachSystem(): Result<Boolean> = withContext(Dispatchers.IO) {
        async {
            login(ReptileManager.getInstance().currentUser)
        }.await().onFailure {
            return@withContext Result.failure(it)
        }
        try {
            val response = Jsoup.newSession()
                .timeout(ReptileManager.getInstance().timeout)
                .url("https://jwxt0.ahu.edu.cn/login_cas.aspx")
                .cookie(
                    Constants.COOKIE_LOGIN_TICKET,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_LOGIN_TICKET)
                )
                .cookie(
                    Constants.COOKIE_AHU_JESESSIONID,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_AHU_JESESSIONID)
                )
                .followRedirects(false)
                .execute()
            val loginUrl = response.header("Location")
                ?: throw IllegalStateException("账号或密码错误或访问过于频繁")
            //保存『ASP.NET_SessionId』
            ReptileManager.getInstance().cookieStore.putAll(response.cookies())
            Jsoup.newSession()
                .timeout(ReptileManager.getInstance().timeout)
                .url(loginUrl)
                .method(Connection.Method.GET)
                .cookie(
                    Constants.COOKIE_LOGIN_TICKET,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_LOGIN_TICKET)
                )
                .cookie(
                    Constants.COOKIE_AHU_JESESSIONID,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_AHU_JESESSIONID)
                )
                .cookie(
                    Constants.COOKIE_TEACH_SESSION,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                )
                .execute()
            return@withContext Result.success(true)
        } catch (e: Exception) {
            return@withContext handleError(e)
        }
    }

    /**
     * 获取校园卡余额
     * @return AHUResponse<String>
     */
    suspend fun getCardMoney(): AHUResponse<String> = withContext(Dispatchers.IO) {
        val ahuResponse = AHUResponse<String>()
        async {
            login(ReptileManager.getInstance().currentUser)
        }.await().onFailure {
            ahuResponse.data = ""
            ahuResponse.code = 1
            ahuResponse.msg = it.message
            return@withContext ahuResponse
        }
        try {
            val response = Jsoup.newSession()
                .url(Constants.URL_CARD_MONEY)
                .timeout(ReptileManager.getInstance().timeout)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .header("Content-Type", "application/json;charset=utf-8")
                .cookie(
                    Constants.COOKIE_NAME_AHU,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_NAME_AHU)
                )
                .requestBody("{}")
                .execute()
            if (response.statusCode() != 200) {
                if (response.statusCode() == 503) {
                    throw IllegalStateException("服务器异常，访过于频繁。")
                }
                throw IllegalStateException(response.statusMessage())
            }
            ahuResponse.data =
                JsonParser.parseString(response.body()).asJsonObject.get("KHYE").asString
            ahuResponse.code = 0
            ahuResponse.msg = "OK"
            return@withContext ahuResponse
        } catch (e: Exception) {
            SinkLog.e(e.toString())
            ahuResponse.data = ""
            ahuResponse.code = 1
            ahuResponse.msg = "获取余额失败"
            return@withContext ahuResponse
        }
    }

    /**
     * 获取课表
     * @param schoolYear String
     * @param schoolTerm String
     * @return AHUResponse<List<Course>>
     */
    suspend fun getSchedule(schoolYear: String, schoolTerm: String): AHUResponse<List<Course>> =
        withContext(Dispatchers.IO) {
            val ahuResponse = AHUResponse<List<Course>>()
            async {
                loginTeachSystem()
            }.await().onFailure {
                ahuResponse.data = null
                ahuResponse.code = 1
                ahuResponse.msg = it.message
                return@withContext ahuResponse
            }
            try {
                var body = Jsoup.newSession()
                    .url(Constants.URL_TEACH_SCHEDULE.format(ReptileManager.getInstance().currentUser.username))
                    .timeout(ReptileManager.getInstance().timeout)
                    .referrer(Constants.URL_TEACH_MAIN.format(ReptileManager.getInstance().currentUser.username))
                    .cookie(
                        Constants.COOKIE_TEACH_SESSION,
                        ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                    )
                    .get().body()
                val year = body.select("#ddlXN>option[selected=selected]").attr("value")
                val term = body.select("#ddlXQ>option[selected=selected]").attr("value")
                if (!year.equals(schoolYear) || !term.equals(schoolTerm)) {
                    val __VIEWSTATE = body.select("#__VIEWSTATE").attr("value")
                    val __VIEWSTATEGENERATOR = body.select("#__VIEWSTATEGENERATOR").attr("value")
                    val data = mapOf(
                        "__EVENTTARGET" to "",
                        "__EVENTARGUMENT" to "",
                        "__LASTFOCUS" to "",
                        "__VIEWSTATE" to __VIEWSTATE,
                        "__VIEWSTATEGENERATOR" to __VIEWSTATEGENERATOR,
                        "ddlXN" to schoolYear,
                        "ddlXQ" to schoolTerm
                    )
                    body = Jsoup.newSession()
                        .url(Constants.URL_TEACH_SCHEDULE.format(ReptileManager.getInstance().currentUser.username))
                        .timeout(ReptileManager.getInstance().timeout)
                        .referrer(Constants.URL_TEACH_SCHEDULE.format(ReptileManager.getInstance().currentUser.username))
                        .cookie(
                            Constants.COOKIE_TEACH_SESSION,
                            ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                        )
                        .data(data)
                        .post().body()
                }

                val table = body.select("#DBGrid").select("tr")
                val courses = mutableListOf<Course>()
                for (tr in table) {
                    if (tr.hasClass("datelisthead")) {
                        continue
                    }
                    val tds = tr.select("td")
                    //解析上课时间、weekday、startTime、endTime
                    for ((courseMsg, location) in tds[8].text().split(";")
                        .zip(tds[9].text().split(";"))) {
                        //创建课程
                        val course = Course()
                        course.courseId = tds[1].text()
                        course.name = tds[2].text()
                        course.extra = tds[3].text()
                        course.teacher = tds[5].text()
                        course.location = location
                        course.singleDouble = "0"
                        //正则匹配地址
                        val pattern = Pattern.compile("(.{2})第(.+?)节\\{第(\\d+?)-(\\d+?)周.")
                        val matcher = pattern.matcher(courseMsg)
                        if (!matcher.find()) {
                            //没有信息，跳过
                            continue
                        }
                        course.setWeekday(weekdayMap.get(matcher.group(1) ?: ""))
                        val times =
                            matcher.group(2)?.split(",") ?: throw IllegalStateException("时间获取失败")
                        //三节课的最后一节
                        if (times.size == 1) {
                            val course1 = courses[courses.lastIndex]
                            if (course1.courseId == course.courseId) {
                                course1.setLength((course1.length + 1).toString())
                                courses[courses.lastIndex] = course1
                                continue
                            }
                        }
                        course.setStartTime(times[0])
                        course.setLength(times.size.toString())
                        course.setStartWeek(matcher.group(3))
                        course.setEndWeek(matcher.group(4))
                        //添加进去
                        courses.add(course)
                    }

                }
                ahuResponse.data = courses
                ahuResponse.msg = "OK"
                ahuResponse.code = 0
                return@withContext ahuResponse
            } catch (e: Exception) {
                SinkLog.e(e.toString())
                ahuResponse.data = null
                ahuResponse.code = 1
                ahuResponse.msg = "获取课表失败"
                return@withContext ahuResponse
            }
        }

    /**
     * 获取考试信息
     * @param schoolYear String
     * @param schoolTerm String
     * @return AHUResponse<List<Exam>>
     */
    suspend fun getExam(schoolYear: String, schoolTerm: String): AHUResponse<List<Exam>> =
        withContext(Dispatchers.IO) {
            val ahuResponse = AHUResponse<List<Exam>>()
            async {
                loginTeachSystem()
            }.await().onFailure {
                ahuResponse.data = null
                ahuResponse.code = 1
                ahuResponse.msg = it.message
                return@withContext ahuResponse
            }
            try {
                var body = Jsoup.newSession()
                    .url(Constants.URL_TEACH_EXAM.format(ReptileManager.getInstance().currentUser.username))
                    .timeout(ReptileManager.getInstance().timeout)
                    .referrer(Constants.URL_TEACH_MAIN.format(ReptileManager.getInstance().currentUser.username))
                    .cookie(
                        Constants.COOKIE_TEACH_SESSION,
                        ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                    )
                    .get().body()
                val year = body.select("#xnd>option[selected=selected]").attr("value")
                val term = body.select("#xqd>option[selected=selected]").attr("value")
                //切换学期
                if (!year.equals(schoolYear) || !term.equals(schoolTerm)) {
                    val __VIEWSTATE = body.select("#__VIEWSTATE").attr("value")
                    val __VIEWSTATEGENERATOR = body.select("#__VIEWSTATEGENERATOR").attr("value")
                    val data = mapOf(
                        "__EVENTTARGET" to "",
                        "__EVENTARGUMENT" to "",
                        "__LASTFOCUS" to "",
                        "__VIEWSTATE" to __VIEWSTATE,
                        "__VIEWSTATEGENERATOR" to __VIEWSTATEGENERATOR,
                        "xnd" to schoolYear,
                        "xqd" to schoolTerm
                    )
                    body = Jsoup.newSession()
                        .url(Constants.URL_TEACH_EXAM.format(ReptileManager.getInstance().currentUser.username))
                        .timeout(ReptileManager.getInstance().timeout)
                        .referrer(Constants.URL_TEACH_EXAM.format(ReptileManager.getInstance().currentUser.username))
                        .cookie(
                            Constants.COOKIE_TEACH_SESSION,
                            ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                        )
                        .data(data)
                        .post().body()
                }
                //解析html
                val table = body.select("#DataGrid1").select("tr")
                val exams = mutableListOf<Exam>()
                for (tr in table) {
                    if (tr.hasClass("datelisthead")) {
                        continue
                    }
                    val exam = Exam()
                    val tds = tr.select("td")
                    exam.course = tds[1].text()
                    exam.time = tds[3].text()
                    exam.location = tds[4].text()
                    exam.seatNum = tds[6].text()
                    exams.add(exam)
                }
                ahuResponse.msg = "OK"
                ahuResponse.code = 0
                ahuResponse.data = exams
                return@withContext ahuResponse
            } catch (e: Exception) {
                SinkLog.e(e.toString())
                ahuResponse.data = null
                ahuResponse.code = 1
                ahuResponse.msg = "获取考试信息失败"
                return@withContext ahuResponse
            }
        }


    /**
     * 获取空教室
     * @param campus String
     * @param weekday String
     * @param weekNum String
     * @param time String
     * @return AHUResponse<List<Room>>
     */
    suspend fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): AHUResponse<List<Room>> =
        withContext(Dispatchers.IO) {
            val ahuResponse = AHUResponse<List<Room>>()
            async {
                loginTeachSystem()
            }.await().onFailure {
                ahuResponse.data = null
                ahuResponse.code = 1
                ahuResponse.msg = it.message
                return@withContext ahuResponse
            }
            try {
                var body = Jsoup.newSession()
                    .url(Constants.URL_TEACH_ROOM.format(ReptileManager.getInstance().currentUser.username))
                    .timeout(ReptileManager.getInstance().timeout)
                    .referrer(Constants.URL_TEACH_MAIN.format(ReptileManager.getInstance().currentUser.username))
                    .cookie(
                        Constants.COOKIE_TEACH_SESSION,
                        ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                    )
                    .get().body()
                val kssj = weekday + weekNum
                val sjd = timeMap[time] ?: "'10'|'1','3','5','7','9','0','0','0','0'"

                val __VIEWSTATE = body.select("#__VIEWSTATE").attr("value")
                val __VIEWSTATEGENERATOR = body.select("#__VIEWSTATEGENERATOR").attr("value")
                val data = mapOf(
                    "__VIEWSTATE" to __VIEWSTATE,
                    "__VIEWSTATEGENERATOR" to __VIEWSTATEGENERATOR,
                    "xiaoq" to campus,
                    "jslb" to "",
                    "min_zws" to "0",
                    "max_zws" to "",
                    "kssj" to kssj,
                    "jssj" to kssj,
                    "xqj" to weekday,
                    "ddlDsz" to "单",
                    "sjd" to sjd,
                    "Button2" to "空教室查询"
                )
                body = Jsoup.newSession()
                    .url(Constants.URL_TEACH_ROOM.format(ReptileManager.getInstance().currentUser.username))
                    .timeout(ReptileManager.getInstance().timeout)
                    .referrer(Constants.URL_TEACH_ROOM.format(ReptileManager.getInstance().currentUser.username))
                    .cookie(
                        Constants.COOKIE_TEACH_SESSION,
                        ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                    )
                    .data(data)
                    .post().body()

                //解析html
                val table = body.select("#DataGrid1").select("tr")
                val rooms = mutableListOf<Room>()
                for (tr in table) {
                    if (tr.hasClass("datelisthead")) {
                        continue
                    }
                    val room = Room()
                    val tds = tr.select("td")
                    room.pos = tds[1].text()
                    room.seating = tds[4].text()
                    rooms.add(room)
                }
                ahuResponse.msg = "OK"
                ahuResponse.code = 0
                ahuResponse.data = rooms
                return@withContext ahuResponse
            } catch (e: Exception) {
                SinkLog.e(e.toString())
                ahuResponse.data = null
                ahuResponse.code = 1
                ahuResponse.msg = "获取空教室信息失败"
                return@withContext ahuResponse
            }

        }

    /**
     * 获取成绩
     * @return AHUResponse<Grade>
     */
    suspend fun getGrade(): AHUResponse<Grade> = withContext(Dispatchers.IO) {
        val ahuResponse = AHUResponse<Grade>()
        async {
            loginTeachSystem()
        }.await().onFailure {
            ahuResponse.data = null
            ahuResponse.code = 1
            ahuResponse.msg = it.message
            return@withContext ahuResponse
        }
        try {
            var body = Jsoup.newSession()
                .url(Constants.URL_TEACH_GRADE.format(ReptileManager.getInstance().currentUser.username))
                .timeout(ReptileManager.getInstance().timeout)
                .referrer(Constants.URL_TEACH_MAIN.format(ReptileManager.getInstance().currentUser.username))
                .cookie(
                    Constants.COOKIE_TEACH_SESSION,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                )
                .get().body()
            //拼接请求体
            val __VIEWSTATE = body.select("#__VIEWSTATE").attr("value")
            val __VIEWSTATEGENERATOR = body.select("#__VIEWSTATEGENERATOR").attr("value")
            val data = mapOf(
                "__VIEWSTATEGENERATOR" to __VIEWSTATEGENERATOR,
                "__VIEWSTATE" to __VIEWSTATE,
                "ddlXN" to "",
                "ddlXQ" to "",
                "Button2" to "在校学习成绩查询"
            )
            //请求全部成绩
            body = Jsoup.newSession()
                .url(Constants.URL_TEACH_GRADE.format(ReptileManager.getInstance().currentUser.username))
                .timeout(ReptileManager.getInstance().timeout)
                .referrer(Constants.URL_TEACH_GRADE.format(ReptileManager.getInstance().currentUser.username))
                .cookie(
                    Constants.COOKIE_TEACH_SESSION,
                    ReptileManager.getInstance().cookieStore.get(Constants.COOKIE_TEACH_SESSION)
                )
                .data(data)
                .post().body()
            val grade = Grade()
            grade.totalCredit = body.select("#xftj").text()
            grade.totalGradePoint = body.select("#xfjdzh").text().replace("学分绩点总和：", "")
            grade.totalGradePointAverage = body.select("#pjxfjd").text().replace("平均学分绩点：", "")
            grade.termGradeList = mutableListOf()
            //当前schoolYear、schoolTerm
            var schoolYear = ""
            var schoolTerm = ""
            var termGradeListBean = Grade.TermGradeListBean()
            var termTotalCredit = 0.0
            var termTotalGradePoint = 0.0
            var gradeList = mutableListOf<Grade.TermGradeListBean.GradeListBean>()
            //解析成绩
            val table = body.select("#Datagrid1").select("tr")
            for (tr in table) {
                if (tr.hasClass("datelisthead")) {
                    continue
                }
                val tds = tr.select("td")
                //是否开启新学期
                val year = tds[0].text()
                val term = tds[1].text()
                if (!year.equals(schoolYear) || !term.equals(schoolTerm)) {
                    if (gradeList.isNotEmpty()) {
                        //填充信息
                        setTermGradeContent(
                            termTotalGradePoint,
                            termTotalCredit,
                            termGradeListBean,
                            schoolYear,
                            schoolTerm,
                            gradeList,
                            grade
                        )
                        //0
                        termTotalCredit = 0.0
                        termTotalGradePoint = 0.0
                        gradeList = mutableListOf()
                    }
                    //刷新学年学期
                    schoolYear = year
                    schoolTerm = term
                    termGradeListBean = Grade.TermGradeListBean()
                }
                val gradeListBean = Grade.TermGradeListBean.GradeListBean()
                gradeListBean.courseNum = tds[2].text()
                gradeListBean.course = tds[3].text()
                gradeListBean.courseNature = tds[4].text()
                gradeListBean.credit = tds[6].text()
                gradeListBean.gradePoint = tds[7].text()
                gradeListBean.grade = tds[8].text()
                gradeList.add(gradeListBean)
                //加上credit、point
                termTotalCredit += gradeListBean.credit.toDouble()
                termTotalGradePoint += gradeListBean.gradePoint.toDouble() * gradeListBean.credit.toDouble()
            }
            //填充信息
            setTermGradeContent(
                termTotalGradePoint,
                termTotalCredit,
                termGradeListBean,
                schoolYear,
                schoolTerm,
                gradeList,
                grade
            )
            //设置返回值
            ahuResponse.data = grade
            ahuResponse.msg = "OK"
            ahuResponse.code = 0
            return@withContext ahuResponse
        } catch (e: Exception) {
            ahuResponse.data = null
            ahuResponse.code = 1
            ahuResponse.msg = "获取成绩失败"
            return@withContext ahuResponse
        }
    }

    /**
     * 设置学期成绩信息
     * @param termTotalGradePoint Double
     * @param termTotalCredit Double
     * @param termGradeListBean TermGradeListBean
     * @param schoolYear String
     * @param schoolTerm String
     * @param gradeList MutableList<GradeListBean>
     * @param grade Grade
     */
    private fun setTermGradeContent(
        termTotalGradePoint: Double,
        termTotalCredit: Double,
        termGradeListBean: Grade.TermGradeListBean,
        schoolYear: String,
        schoolTerm: String,
        gradeList: MutableList<Grade.TermGradeListBean.GradeListBean>,
        grade: Grade
    ) {
        //计算平均绩点
        val gradePointAverage = termTotalGradePoint / termTotalCredit
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        //设置信息
        termGradeListBean.termGradePointAverage = df.format(gradePointAverage)
        termGradeListBean.termTotalCredit = df.format(termTotalCredit)
        termGradeListBean.termGradePoint = df.format(termTotalGradePoint)
        termGradeListBean.schoolYear = schoolYear
        termGradeListBean.term = schoolTerm
        termGradeListBean.gradeList = gradeList
        //添加数据
        grade.termGradeList.add(termGradeListBean)
    }

    private fun handleError(e: Exception): Result<Boolean> {
        when (e) {
            is HttpStatusException -> {
                val statusCode = e.statusCode
                if (statusCode >= 500) {
                    return Result.failure(Throwable("服务器异常，请慢点刷新！"))
                } else {
                    return Result.failure(Throwable("请求地址异常，界面找不到！"))
                }
            }
            is SocketTimeoutException -> {
                if (e.message == "timeout") {
                    return Result.failure(Throwable("当前网络不稳定，请求失败"))
                }
            }
            is ConnectException -> {
                return Result.failure(Throwable("当前没有网络连接哦！"))
            }
        }
        e.printStackTrace()
        return Result.failure(e)
    }

}
