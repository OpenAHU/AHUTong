package com.ahu.ahutong.data.crawler

import android.util.Log
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.model.jwxt.GradeResponse
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.RequestBody
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.crawler.utils.GpaRankHtmlParser
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.sdk.LocalServiceClient
import com.ahu.ahutong.sdk.RustSDK
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.buffer
import okio.source
import retrofit2.Response
import com.ahu.ahutong.data.crawler.model.adwnh.Balance
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundPublishRequest
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundResponse
import com.ahu.ahutong.data.model.GpaRankInfo
import java.io.File

class SdkDataSource : BaseDataSource {

    val TAG = this::class.java.simpleName
    private val crawlerFallback = CrawlerDataSource()

    /**
     * 获取 HTTP 客户端，如果不可用则返回 null（fallback 到 JNI）
     */
    private fun getHttpClient(): LocalServiceClient? = LocalServiceClient.getInstance()

    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return crawlerFallback.getSchedule(schoolYear, schoolTerm)
    }

    override suspend fun getSchedule(): AHUResponse<List<Course>> {
        // 优先使用 HTTP 客户端
        val httpClient = getHttpClient()
        if (httpClient != null) {
            Log.d("LocalServiceClient", "[getSchedule] Using HTTP client")
            val result = httpClient.getSchedule()
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) return AHUResponse<List<Course>>().apply {
                    code = 0
                    this.data = data
                    msg = "Success"
                }
            }

            Log.w("LocalServiceClient", "[getSchedule] HTTP failed, fallback to JNI: ${result.exceptionOrNull()?.message}")
        }

        // Fallback: 直接 JNI 调用
        Log.d("LocalServiceClient", "[getSchedule] Fallback to JNI")
        val result = RustSDK.getScheduleSafe()
        if (result.isSuccess) {
            val data = result.getOrNull()
            if (data != null) {
                return AHUResponse<List<Course>>().apply {
                    code = 0
                    this.data = data
                    msg = "Success"
                }
            } else {
                Log.w("LocalServiceClient", "[getSchedule] JNI returned null, fallback to Android crawler")
            }
        } else {
            Log.w("LocalServiceClient", "[getSchedule] JNI failed, fallback to Android crawler: ${result.exceptionOrNull()?.message}")
        }

        return crawlerFallback.getSchedule()
    }

    override suspend fun getNextSchedule(): AHUResponse<List<Course>> {
        return CrawlerDataSource().getNextSchedule()
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        // 优先使用 HTTP 客户端
        val httpClient = getHttpClient()
        if (httpClient != null) {
            Log.d("LocalServiceClient", "[getGrade] Using HTTP client")
            val result = httpClient.getGrade()
            if (result.isSuccess) {
                val data = result.getOrThrow()
                return convertGradeResponse(data)
            }

            Log.w("LocalServiceClient", "[getGrade] HTTP failed, fallback to JNI: ${result.exceptionOrNull()?.message}")
            val jniResult = RustSDK.getGradeSafe()
            if (jniResult.isSuccess) {
                return convertGradeResponse(jniResult.getOrThrow())
            }

            Log.w("LocalServiceClient", "[getGrade] JNI failed, fallback to Android crawler: ${jniResult.exceptionOrNull()?.message}")
            return crawlerFallback.getGrade()
        }

        // Fallback: 直接 JNI 调用
        Log.d("LocalServiceClient", "[getGrade] Fallback to JNI")
        val result = RustSDK.getGradeSafe()
        if (result.isSuccess) {
            return convertGradeResponse(result.getOrThrow())
        }

        return try {
            crawlerFallback.getGrade()
        } catch (e: Exception) {
            val response = AHUResponse<Grade>()
            response.code = -1
            val msg = result.exceptionOrNull()?.message
                ?: "获取成绩失败"
            response.msg = if (msg.contains("error decoding response body", ignoreCase = true)) {
                "教务系统返回异常（可能登录失效），请重新登录后重试"
            } else {
                msg
            }
            response
        }
    }


    override suspend fun getGpaRankFromHtml(): AHUResponse<GpaRankInfo> {
        val response = AHUResponse<GpaRankInfo>()
        try {
            val htmlResponse = JwxtApi.API.getGrade()
            if(!htmlResponse.isSuccessful||htmlResponse.body() == null){
                response.code = -1
                response.msg = "获取成绩页面失败"
                return response
            }

            val html = htmlResponse.body()!!.string()
            val pattern = Regex(
                "var gpaSemesterModel\\s*=\\s*(\\{.*?\\});",
                RegexOption.DOT_MATCHES_ALL
            )

            val match = pattern.find(html)
                ?: throw Exception("未找到 gpaSemesterModel 变量")

            val jsObject = match.groupValues[1]

            val json = convertJsToJson(jsObject)

            val gpaRankInfo = Gson().fromJson(json, GpaRankInfo::class.java)

            response.code = 0
            response.msg = "success"
            response.data = gpaRankInfo
            return response

        }catch (e: Exception){
            e.printStackTrace()
            response.code = -1
            response.msg = "解析失败：${e.message}"
            return response
        }
    }

    override suspend fun getAllCampus(): AHUResponse<AllCampus> {
        val response = AHUResponse<AllCampus>()
        try {
            // 直接请求 JSON 接口
            val campusList = AdwmhApi.API.getAllcampus()

            // 封装返回
            response.code = 0
            response.msg = "success"
            response.data = campusList
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            response.code = -1
            response.msg = "解析校区列表失败：${e.message}"
            return response
        }
    }

    override suspend fun getAllLostFoundType(): AHUResponse<AllLostFoundType> {
        val response = AHUResponse<AllLostFoundType>()
        try {
            // 直接请求 JSON 接口
            val typeList = AdwmhApi.API.getAlllostfoundtype()
            // 封装返回
            response.code = 0
            response.msg = "success"
            response.data = typeList
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            response.code = -1
            response.msg = "解析失败：${e.message}"
            return response
        }
    }
    override suspend fun getLostFoundList(
        pageNo: Int,
        pageSize: Int,
        state: Int
    ): AHUResponse<LostFoundResponse> {
        val response = AHUResponse<LostFoundResponse>()
        try {
            // 直接请求 JSON 接口
            val List = AdwmhApi.API.getLostFoundList(
                pageNo,
                pageSize,
                state
            )
            // 封装返回
            response.code = 0
            response.msg = "success"
            response.data = List
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            response.code = -1
            response.msg = "解析失败：${e.message}"
            return response
        }
    }
    override suspend fun publishLostFound(
        request: LostFoundPublishRequest
    ): AHUResponse<Any> {
        return AdwmhApi.API.publishLostFound(request)
    }
    override suspend fun deleteLostFound(
        id: String
    ): AHUResponse<Any> {
        return AdwmhApi.API.deleteLostFound(id)
    }

    private fun convertJsToJson(js: String): String {
        return js
            .replace(Regex("'"), "\"")                // 单引号 → 双引号
    }


    /**
     * 转换 GradeResponse 为 Grade
     */
    private fun convertGradeResponse(data: GradeResponse): AHUResponse<Grade> {
        val map = hashMapOf<String, Grade.TermGradeListBean>()

        data.semesterId2studentGrades?.values?.forEach { gradeList ->
            val newGradeList = mutableListOf<Grade.TermGradeListBean.GradeListBean>()
            var termName: String? = null

            gradeList.forEach { it ->

                termName = termName ?: it.semesterName
                val grade = Grade.TermGradeListBean.GradeListBean()
                grade.course = it.courseName ?: ""
                grade.credit = (it.credits ?: 0.0).toString()
                grade.grade = it.gaGrade ?: ""
                grade.gradePoint = (it.gp ?: 0.0).toString()
                grade.courseNature = it.courseType ?: ""
                grade.courseNum = it.courseCode ?: ""
                grade.gradeDetail = it.gradeDetail ?: ""
                grade.semesterId = it.semesterId ?: 0
                newGradeList.add(grade)
            }

            termName?.let {

                val names = termName.split("-")
                if (names.size < 3) { //2023-2024-1
                    return@forEach
                }

                val termGradeList = Grade.TermGradeListBean()
                termGradeList.gradeList = newGradeList
                termGradeList.term = names[2]
                termGradeList.schoolYear = "${names[0]}-${names[1]}"
                termGradeList.termGradePoint = newGradeList.sumOf { it ->
                    it.grade?.toDoubleOrNull() ?: 0.0
                }.toString()
                termGradeList.termTotalCredit = newGradeList.sumOf { it ->
                    it.credit?.toDoubleOrNull() ?: 0.0
                }.toString()
                val totalGradePointWeighted = newGradeList.sumOf {
                    (it.gradePoint?.toDoubleOrNull() ?: 0.0) * (it.credit?.toDoubleOrNull() ?: 0.0)
                }

                termGradeList.termGradePointAverage = if (termGradeList.termTotalCredit.toDouble() > 0) {
                    "%.2f".format(totalGradePointWeighted / termGradeList.termTotalCredit.toDouble())
                } else {
                    "0.0"
                }

                map[it] = termGradeList
            }

        }

        val response = AHUResponse<Grade>()
        val termGradeList =  map.values.toList()
        val grade = Grade()

        grade.totalCredit = termGradeList.sumOf {
            it.termTotalCredit?.toDoubleOrNull() ?: 0.0
        }.toString()

        grade.totalGradePoint =termGradeList.sumOf {
            val avg = it.termGradePointAverage?.toDoubleOrNull() ?: 0.0
            val credit = it.termTotalCredit?.toDoubleOrNull() ?: 0.0
            avg * credit
        }.toString()

        val weightedGradePointSum = termGradeList.sumOf {
            val avg = it.termGradePointAverage?.toDoubleOrNull() ?: 0.0
            val credit = it.termTotalCredit?.toDoubleOrNull() ?: 0.0
            avg * credit
        }


        grade.totalGradePointAverage =if (grade.totalCredit.toDouble() > 0) {
            "%.2f".format(weightedGradePointSum / grade.totalCredit.toDouble())
        } else {
            "0.0"
        }

        grade.termGradeList = termGradeList


        response.data = grade
        response.code = 0


        return response
    }

    override suspend fun getCardMoney(): AHUResponse<Card> {
        getHttpClient()?.let { httpClient ->
            Log.d("LocalServiceClient", "[getCardMoney] Using HTTP client")
            val result = httpClient.getBalance()
            if (result.isSuccess) {
                return AHUResponse<Card>().apply {
                    code = 0
                    data = result.getOrThrow()
                    msg = "Success"
                }
            }
            Log.w("LocalServiceClient", "[getCardMoney] HTTP failed, fallback to JNI: ${result.exceptionOrNull()?.message}")
        }

        val jniResult = RustSDK.getBalanceSafe()
        if (jniResult.isSuccess) {
            return AHUResponse<Card>().apply {
                code = 0
                data = jniResult.getOrThrow()
                msg = "Success"
            }
        }

        Log.w("LocalServiceClient", "[getCardMoney] JNI failed, fallback to Android crawler: ${jniResult.exceptionOrNull()?.message}")
        return crawlerFallback.getCardMoney()
    }

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> {
        return AHUResponse<List<BathRoom>>()
    }

    override suspend fun getExamInfo(studentID: String, studentName: String): AHUResponse<List<Exam>> {
        val response = AHUResponse<List<Exam>>()
        try {
            val httpClient = getHttpClient()
            val result = if (httpClient != null) {
                Log.d("LocalServiceClient", "[getExamInfo] Using HTTP client")
                httpClient.getExamInfo()
            } else {
                Log.d("LocalServiceClient", "[getExamInfo] Fallback to JNI")
                RustSDK.getExamInfoSafe()
            }
            if (result.isSuccess) {
                response.code = 0
                response.data = result.getOrNull()
            } else {
                Log.w("LocalServiceClient", "[getExamInfo] Rust failed, fallback to Android crawler: ${result.exceptionOrNull()?.message}")
                return crawlerFallback.getExamInfo(studentID, studentName)
            }
        } catch (e: Exception) {
            Log.w("LocalServiceClient", "[getExamInfo] Rust threw, fallback to Android crawler", e)
            return crawlerFallback.getExamInfo(studentID, studentName)
        }
        return response
    }

    override suspend fun getBathroomTelInfo(
        bathroom: String,
        tel: String
    ): AHUResponse<BathroomTelInfo> {

        val response = AHUResponse<BathroomTelInfo>()

        var feeitemid: String? = null

        when (bathroom) {
            "竹园/龙河" -> {
                feeitemid = "409"
            }

            "桔园/蕙园" -> {
                feeitemid = "430"
            }

            else -> {
                response.code = -1
                response.msg = "目前没有这个浴室啊"
                response.data = null
                return response
            }
        }


        val formBody = FormBody.Builder()
            .add("feeitemid", feeitemid)
            .add("type", "IEC")
            .add("level", "1")
            .add("telPhone", tel)
            .build()


        val res = YcardApi.API.getFeeItemThirdData(formBody)

        if (res.isSuccessful) {
            val responseBody = res.body()
            val responseJson = responseBody?.string()

            val bathroomInfo = Gson().fromJson(responseJson, BathroomTelInfo::class.java)

            bathroomInfo?.let {
                response.code = 0
                response.data = it
                response.msg = "success"
                return response
            }
            response.code = -1
            response.msg = "数据返回错误"

        } else {
            response.code = -1
            response.msg = "请求接口失败"
        }

        return response
    }

    override suspend fun getCardInfo(): AHUResponse<CardInfo> {

        val response = AHUResponse<CardInfo>()

        response.data = YcardApi.API.loadCardRecharge()
        response.code = 0

        return response
    }

    override suspend fun getOrderThirdData(request : RequestBody): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.getOrderThirdData(request.toFormBody())
        response.code = 0;
        return response
    }

    override suspend fun pay(request: RequestBody): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.pay(request.toFormBody())
        response.code = 0;
        return response
    }

    override suspend fun getSchoolCalendar(): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()

        if (RustSDK.isNativeLoaded()) {
            try {
                val context = AHUApplication.getApp()
                val dir = File(context.filesDir, "images")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "xiaoli-rust-download.jpg")
                if (file.exists()) file.delete()

                val ok = RustSDK.downloadSchoolCalendar(file.absolutePath)
                if (ok && file.exists() && file.length() > 0L) {
                    response.code = 0
                    response.msg = "success"
                    response.data = Response.success(
                        file.inputStream().source().buffer()
                            .asResponseBody("image/jpeg".toMediaType(), file.length())
                    )
                    return response
                }
                Log.w(TAG, "downloadSchoolCalendar returned false or empty file, fallback to Android API")
            } catch (e: Throwable) {
                Log.w(TAG, "downloadSchoolCalendar native failed, fallback to Android API", e)
            }
        }

        return crawlerFallback.getSchoolCalendar()
    }


    suspend fun getStudentId(): String {
        val lastURL = JwxtApi.API.getGrade().raw().request.url.toString()
        val data = lastURL.split("/")
        return data.last()
    }
}
