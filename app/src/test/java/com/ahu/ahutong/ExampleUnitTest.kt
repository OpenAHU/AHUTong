package com.ahu.ahutong

import com.ahu.ahutong.data.model.User
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val ahuTeach = User.UserType.AHU_Teach
        println(ahuTeach.toString())
    }
}