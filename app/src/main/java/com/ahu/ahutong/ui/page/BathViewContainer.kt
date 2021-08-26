package com.ahu.ahutong.ui.page

import android.annotation.SuppressLint
import android.widget.TextView
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.R
import com.ahu.ahutong.utils.DateUtils
import com.simon.library.ViewContainer

//type是用来判断是哪个时间段的
class BathViewContainer() : ViewContainer() {
    lateinit var times:TextView;
    lateinit var dateTimes:TextView
    lateinit var north:TextView
    lateinit var south:TextView
    override fun resId(): Int {
       return R.layout.fragment_bathroom
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView() {
        //root.layoutParams.width= AHUApplication.width/2
        dateTimes=findViewById(R.id.dateTimes)
        north=findViewById(R.id.north)
        south=findViewById(R.id.south)
        dateTimes.text=DateUtils.getTime()+dateTimes.text
    }

    override fun onDestroy() {
    }
}