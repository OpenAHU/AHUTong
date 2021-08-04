package com.ahu.ahutong.data.model

import com.ahu.ahutong.R

data class Sector(val title: String ){
    companion object{
        val defaultSectors = listOf(
            Sector("院系风采"),
            Sector("教务通知"),
            Sector("社团动态"),
            Sector("校招实习")
        )

    }
}

