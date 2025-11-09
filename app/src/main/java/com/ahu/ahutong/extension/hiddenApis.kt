package com.ahu.ahutong.extension

import android.util.Property
import java.lang.reflect.Method

/**
 * Created by tanzhehao on 2025/10/7
 * @author tanzhehao@bytedance.com
 */
internal val methods: Property<Class<*>, Array<Method>> =
    Property.of(Class::class.java, Array<Method>::class.java, "DeclaredMethods")

internal val mirrorSurfaceFun by lazy {
    val scCls = Class.forName("android.view.SurfaceControl")
    methods.get(scCls).first {
        it.name.equals("mirrorSurface")
    }.apply {
        isAccessible = true
    }
}

internal val getSurfaceControlFun by lazy {
    methods.get(Class.forName("android.view.ViewRootImpl"))
        .first { it.name == "getSurfaceControl" }
        .apply { isAccessible = true }
}