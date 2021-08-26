package com.ahu.ahutong.ui.page

import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.R
import com.simon.library.ViewContainer

class BalanceContainer :ViewContainer() {
    override fun resId(): Int {
        return R.layout.fragment_balance;
    }

    override fun onCreateView() {
       // root.layoutParams.width=AHUApplication.width/2
    }

    override fun onDestroy() {

    }
}