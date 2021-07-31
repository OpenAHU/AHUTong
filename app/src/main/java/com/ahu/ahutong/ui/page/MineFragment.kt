package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentMineBinding
import com.ahu.ahutong.ui.page.state.MineViewModel
import com.google.android.material.appbar.AppBarLayout

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
class MineFragment private constructor(): BaseFragment<FragmentMineBinding>() {

    private lateinit var mState: MineViewModel


    override fun initViewModel() {
        mState = getFragmentScopeViewModel(MineViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mine, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    inner class ClickProxy{
        //about us
        fun aboutUs(view: View){

        }
        //login or logout
        fun login(view: View){

        }
        //jump to developer fragment
        fun developer(view: View){

        }
        //jump to setting fragment
        fun setting(view: View){

        }


    }

    companion object{
        val INSTANCE = MineFragment()

    }
}