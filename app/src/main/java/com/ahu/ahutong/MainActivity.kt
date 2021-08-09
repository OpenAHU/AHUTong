package com.ahu.ahutong

import android.os.Bundle
import android.os.Debug
import android.util.Log

import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.sink.library.log.SinkLog

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel

    override fun initViewModel() {
        mState = getActivityScopeViewModel(MainViewModel::class.java);
    }

    override fun getDataBindingConfig(): DataBindingConfig {
       return DataBindingConfig(R.layout.activity_main, BR.state, mState)
    }

}