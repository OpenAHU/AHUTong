package com.ahu.ahutong.data.crawler

import android.util.Log
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.model.jwxt.GradeResponse
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.Request
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.sdk.LocalServiceClient
import com.ahu.ahutong.sdk.RustSDK
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.ResponseBody
import retrofit2.Response
import com.ahu.ahutong.data.crawler.model.adwnh.Balance

class CrawlerDataSource : BaseDataSource {

    val TAG = this::class.java.simpleName
    
    /**
     * 获取 HTTP 客户端，如果不可用则返回 null（fallback 到 JNI）
     */
    private fun getHttpClient(): LocalServiceClient? = LocalServiceClient.getInstance()

    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return AHUResponse<List<Course>>()
    }

    override suspend fun getSchedule(): AHUResponse<List<Course>> {
        val response = AHUResponse<List<Course>>()
        
        // 优先使用 HTTP 客户端
        val httpClient = getHttpClient()
        if (httpClient != null) {
            Log.d("LocalServiceClient", "[getSchedule] Using HTTP client")
            val result = httpClient.getSchedule()
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    response.code = 0
                    response.data = data
                    response.msg = "Success"
                } else {
                    response.code = -1
                    response.msg = "课表数据为空，可能登录已过期"
                }
            } else {
                response.code = -1
                response.msg = result.exceptionOrNull()?.message ?: "获取课表失败"
            }
            return response
        }
        
        // Fallback: 直接 JNI 调用
        Log.d("LocalServiceClient", "[getSchedule] Fallback to JNI")
        val result = RustSDK.getScheduleSafe()
        if (result.isSuccess) {
            val data = result.getOrNull()
            if (data != null) {
                response.code = 0
                response.data = data
                response.msg = "Success"
            } else {
                response.code = -1
                response.msg = "课表数据为空，可能登录已过期"
            }
        } else {
            response.code = -1
            response.msg = result.exceptionOrNull()?.message ?: "获取课表失败"
        }
        return response
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
            
            val response = AHUResponse<Grade>()
            response.code = -1
            val msg = result.exceptionOrNull()?.message
                ?: jniResult.exceptionOrNull()?.message
                ?: "获取成绩失败"
            response.msg = if (msg.contains("error decoding response body", ignoreCase = true)) {
                "教务系统返回异常（可能登录失效），请重新登录后重试"
            } else {
                msg
            }
            return response
        }
        
        // Fallback: 直接 JNI 调用
        Log.d("LocalServiceClient", "[getGrade] Fallback to JNI")
        val result = RustSDK.getGradeSafe()
        if (result.isFailure) {
            val response = AHUResponse<Grade>()
            response.code = -1
            val msg = result.exceptionOrNull()?.message ?: "获取成绩失败"
            response.msg = if (msg.contains("error decoding response body", ignoreCase = true)) {
                "教务系统返回异常（可能登录失效），请重新登录后重试"
            } else {
                msg
            }
            return response
        }

        return convertGradeResponse(result.getOrThrow())
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
        val result = AHUResponse<Card>()
        try {
            val cardInfo = YcardApi.API.loadCardRecharge()
            if (cardInfo.success) {
                val balanceFen = cardInfo.data.card.firstOrNull()?.accinfo?.firstOrNull()?.balance ?: 0
                val card = Card()
                card.balance = balanceFen / 100.0
                result.data = card
                result.code = 0
            } else {
                result.code = -1
                result.msg = "一卡通接口返回失败"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result.code = -1
            result.msg = "获取一卡通余额失败: ${e.message}"
        }
        return result
    }

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> {
        return AHUResponse<List<BathRoom>>()
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

    override suspend fun getOrderThirdData(request : Request): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.getOrderThirdData(request.toFormBody())
        response.code = 0;
        return response
    }

    override suspend fun pay(request: Request): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.pay(request.toFormBody())
        response.code = 0;
        return response
    }


    suspend fun getStudentId(): String {
        val lastURL = JwxtApi.API.getGrade().raw().request.url.toString()
        val data = lastURL.split("/")
        return data.last()
    }
}
