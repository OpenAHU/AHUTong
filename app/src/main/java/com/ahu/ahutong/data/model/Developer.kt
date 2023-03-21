package com.ahu.ahutong.data.model

data class Developer(val name: String, val desc: String, val qq: String) {
    fun getAvatarUrl(): String {
        return "http://q1.qlogo.cn/g?b=qq&nk=$qq&s=640"
    }

    fun getURL(): String {
        return "mqqapi://card/show_pslcard?&uin=$qq"
    }
}
