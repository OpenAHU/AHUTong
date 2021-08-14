package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentLoginBinding
import com.ahu.ahutong.ui.page.state.LoginViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:56
 * @Email: 468766131@qq.com
 */
class LoginFragment: BaseFragment<FragmentLoginBinding>() {
    private lateinit var mState: LoginViewModel
    private lateinit var actvivtyState: MainViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(LoginViewModel::class.java)
        actvivtyState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun observeData() {
        super.observeData()
        mState.loginResult.observe(this){
            it.onSuccess {
                actvivtyState.isLogin.value = true
                Toast.makeText(requireContext(), "登录成功，欢迎您：${it.name}", Toast.LENGTH_SHORT).show()
                nav().popBackStack()
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
       return DataBindingConfig(R.layout.fragment_login, BR.state, mState)
           .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置动画
        dataBinding.edPassword.setOnFocusChangeListener{_, focus ->
            if (focus){
                dataBinding.emoji.close()
            }else {
                dataBinding.emoji.open()
            }
        }

    }


    inner class ClickProxy{
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun login(view: View){
            val username = dataBinding.edUserId.text.toString()
            val password = dataBinding.edPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()){
                Toast.makeText(requireContext(), "请不要输入空气哦！", Toast.LENGTH_SHORT).show()
                return
            }
            LoginViewModel.type[dataBinding.rgLogin.checkedRadioButtonId]?.let {
                mState.loginType = it
                mState.login(username, password)
            }

        }
    }
}