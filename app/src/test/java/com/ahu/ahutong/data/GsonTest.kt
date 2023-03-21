package com.ahu.ahutong.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Test

class GsonTest {
    @Test
    fun gsonTest() {
        val json = "{\n" +
            "    \"code\": 0,\n" +
            "    \"data\": \"SB\",\n" +
            "    \"msg\": null,\n" +
            "    \"success\": true,\n" +
            "    \"successAndHasData\": true\n" +
            "}"
        val resp = Gson().fromJson<AHUResponse<String>>(
            json,
            object : TypeToken<AHUResponse<String>>() {}.type
        )
        println(resp)
    }
}
