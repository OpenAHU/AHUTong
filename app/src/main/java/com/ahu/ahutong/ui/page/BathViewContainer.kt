package com.ahu.ahutong.ui.page

import android.widget.TextView
import com.ahu.ahutong.R
import com.simon.library.ViewContainer

//type是用来判断是哪个时间段的
class BathViewContainer(val type:Int) : ViewContainer() {
    lateinit var times:TextView;
    lateinit var dateTimes:TextView
    lateinit var north:TextView
    lateinit var south:TextView
    override fun resId(): Int {
       return R.layout.fragment_bathroom
    }

    override fun onCreateView() {
        times=findViewById(R.id.times)
        dateTimes=findViewById(R.id.dateTimes)
        north=findViewById(R.id.north)
        south=findViewById(R.id.south)
    }

    override fun onDestroy() {
    }
}