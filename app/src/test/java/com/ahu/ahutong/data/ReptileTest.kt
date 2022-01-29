package com.ahu.ahutong.data

import com.ahu.ahutong.data.reptile.Reptile
import com.ahu.ahutong.data.reptile.ReptileManager
import com.ahu.ahutong.data.reptile.ReptileUser
import com.ahu.ahutong.data.reptile.store.DefaultCookieStore
import com.google.gson.Gson
import com.sink.library.log.SinkLogConfig
import com.sink.library.log.SinkLogManager
import com.sink.library.log.SinkLogManager.Companion.init
import com.sink.library.log.parser.SinkJsonParser
import com.sink.library.log.printer.SinkLogConsolePrinter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReptileTest {
    val user = ReptileUser("Y02014373", "Sink468766131.")

    @Before
    fun init() {
        ReptileManager.getInstance()
            .setCookieStore(DefaultCookieStore())
            .setCurrentUser(user.username, user.password)
        //SinkLog
        init(object : SinkLogConfig() {
            override fun getGlobalTag(): String {
                return "AHUTong"
            }

            override fun enableTest(): Boolean {
                return true
            }

            override fun getJsonParser(): SinkJsonParser {
                return object : SinkJsonParser {
                    override fun toJson(obj: Any): String {
                        return Gson().toJson(obj)
                    }
                }
            }
        }, SinkLogConsolePrinter())
    }

    @Test
    fun testLogin() {
        runBlocking {
            Reptile.login(user)
                .onSuccess {
                    assert(it)
                }
                .onFailure {
                    assert(false) {
                        it
                    }
                }
        }
    }

    @Test
    fun testGetCardMoney() {
        runBlocking {
            val cardMoney = Reptile.getCardMoney()
            assert(cardMoney.isSuccessful) {
                cardMoney.msg
            }
            println(cardMoney)
        }
    }
}