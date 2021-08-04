package com.ahu.ahutong.data.model

data class GridItem(val imgres: Int, val text: String){
//    fun getRes(): Int{
//        return res
//    }
    fun getURL(): String{
        return "mqqapi://card/show_pslcard?&uin=$text"
    }
}
