package com.ahu.ahutong.data.crawler.model.ycard

import okhttp3.FormBody



open class RequestBody{
    protected val params: MutableMap<String, Any> = linkedMapOf()

    fun addParam(key: String, value: Any): RequestBody {
        params[key] = value
        return this
    }

    fun addParams(newParams: Map<String, Any>): RequestBody {
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