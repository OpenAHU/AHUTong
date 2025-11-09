package com.ahu.ahutong.extension

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * Created by tanzhehao on 2025/11/7
 * @author tanzhehao@bytedance.com
 */

fun Int.alignToEven(): Int {
    return this + (this and 1)
}

inline fun <reified T> interfaceDelegate(): T {
    val javaClass = T::class.java
    return Proxy.newProxyInstance(javaClass.classLoader, arrayOf(javaClass),
        noOpHandler
    ) as T
}

@PublishedApi
internal val noOpHandler = InvocationHandler { _, _, _ -> }