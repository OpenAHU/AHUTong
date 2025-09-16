package com.ahu.ahutong.data.crawler.model.ycard

import okhttp3.FormBody



open class Request{
    protected val params: MutableMap<String, Any> = linkedMapOf()

    fun addParam(key: String, value: Any): Request {
        params[key] = value
        return this
    }

    fun addParams(newParams: Map<String, Any>): Request {
        params.putAll(newParams)
        return this
    }

    fun toFormBody(): FormBody {
        val builder = FormBody.Builder()
        params.forEach { (key, value) ->
            builder.add(key, value.toString())
        }
        return builder.build()
    }

    fun toMap(): Map<String, Any> = params.toMap()

    fun getParam(key: String): Any? = params[key]
}