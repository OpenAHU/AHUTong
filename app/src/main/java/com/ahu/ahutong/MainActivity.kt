package com.ahu.ahutong

import android.os.Bundle

import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ui.page.state.MainViewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun initViewModel() {
        mState = getActivityScopeViewModel(MainViewModel::class.java);
    }

    override fun getDataBindingConfig(): DataBindingConfig {
       return DataBindingConfig(R.layout.activity_main, BR.state, mState)
    }
}