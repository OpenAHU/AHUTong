package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.ReptileDataSource
import com.ahu.ahutong.data.reptile.ReptileUser
import com.ahu.ahutong.data.reptile.login.SinkWebViewClient
import com.ahu.ahutong.databinding.FragmentLoginBinding
import com.ahu.ahutong.ui.page.state.LoginViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:56
 * @Email: 468766131@qq.com
 */
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private lateinit var mState: LoginViewModel
    private lateinit var activityState: MainViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(LoginViewModel::class.java)
        activityState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun observeData() {
        super.observeData()
        mState.serverLoginResult.observe(this) {
            dataBinding.btLogin.isClickable = true  // 恢复按钮
            it.onSuccess {
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
        dataBinding.edWisdomPassword.setOnFocusChangeListener { _, focus ->
            if (focus) {
                dataBinding.emoji.close()
            } else {
                dataBinding.emoji.open()
            }
        }

        dataBinding.edTeachPassword.setOnFocusChangeListener { _, focus ->
            if (focus) {
                dataBinding.emoji.close()
            } else {
                dataBinding.emoji.open()
            }
        }

    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun login(view: View) {
            val username = dataBinding.edUserId.text.toString()
            val teachPassword = dataBinding.edTeachPassword.text.toString()
            val wisdomPassword = dataBinding.edWisdomPassword.text.toString()
            if (username.isEmpty() || teachPassword.isEmpty() || wisdomPassword.isEmpty()) {
                Toast.makeText(requireContext(), "请不要输入空气哦！", Toast.LENGTH_SHORT).show()
                return
            }
            mState.loginWithServer(username, teachPassword, wisdomPassword)

        }
    }
}