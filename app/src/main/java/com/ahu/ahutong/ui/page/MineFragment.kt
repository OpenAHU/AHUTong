package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentMineBinding
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.MineViewModel

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
class MineFragment: BaseFragment<FragmentMineBinding>() {

    private lateinit var mState: MineViewModel
    private lateinit var activityState: MainViewModel


    override fun initViewModel() {
        mState = getFragmentScopeViewModel(MineViewModel::class.java)
        activityState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_mine, BR.state, mState)
            .addBindingParam(BR.activityState, activityState)
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
            if (activityState.isLogin.value == true){
                //注销 (TODO)
            }else {
                nav().navigate(R.id.action_home_fragment_to_login_fragment)
            }
        }
        //jump to developer fragment
        fun developer(view: View){
            nav().navigate(R.id.action_home_fragment_to_developer_fragment)
        }
        //jump to setting fragment
        fun setting(view: View){
            nav().navigate(R.id.setting_fragment)
        }


    }

    companion object{
        val INSTANCE = MineFragment()

    }
}