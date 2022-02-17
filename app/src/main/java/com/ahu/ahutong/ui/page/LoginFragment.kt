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
                activityState.isLogin.value = true
                Toast.makeText(requireContext(), "登录成功，欢迎您：${it.name}", Toast.LENGTH_SHORT).show()
                nav().popBackStack()
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
        activityState.localReptileLoginStatus.observe(this) {
            when (it) {
                SinkWebViewClient.STATUS_LOGIN_SUCCESS -> {
                    dataBinding.btLogin.isClickable = true  // 恢复按钮
                    Toast.makeText(
                        requireContext(),
                        "登录成功，欢迎您：${AHUCache.getCurrentUser()?.name ?: return@observe}",
                        Toast.LENGTH_SHORT
                    ).show()
                    nav().popBackStack()
                }
                SinkWebViewClient.STATUS_LOGIN_FAILURE -> {
                    dataBinding.btLogin.isClickable = true  // 恢复按钮
                }
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
        dataBinding.edPassword.setOnFocusChangeListener { _, focus ->
            if (focus) {
                dataBinding.emoji.close()
            } else {
                dataBinding.emoji.open()
            }
        }
        arguments?.let {
            val type = it.getString("type")
            mState.loginType = when (type) {
                "1" -> User.UserType.AHU_Teach
                "2" -> User.UserType.AHU_Wisdom
                else -> User.UserType.AHU_LOCAL
            }
        }
        dataBinding.rgLogin.setOnCheckedChangeListener { _, id ->
            mState.loginType = when (id) {
                R.id.rd_wisdom -> User.UserType.AHU_Wisdom
                R.id.rd_teach -> User.UserType.AHU_Teach
                else -> User.UserType.AHU_LOCAL
            }
        }

    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun login(view: View) {
            val username = dataBinding.edUserId.text.toString()
            val password = dataBinding.edPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "请不要输入空气哦！", Toast.LENGTH_SHORT).show()
                return
            }

            LoginViewModel.type[dataBinding.rgLogin.checkedRadioButtonId]?.let {
                dataBinding.btLogin.isClickable = false // 禁用按钮防止重复操作
                mState.loginType = it
                if (it != User.UserType.AHU_LOCAL) {
                    mState.loginWithServer(username, password)
                } else {
                    Toast.makeText(requireContext(), "本地爬虫登录较为缓慢，请耐心等待！", Toast.LENGTH_SHORT).show()
                    AHUCache.saveCurrentUser(User(username))
                    AHUCache.saveCurrentPassword(password)
                    //切换数据源
                    AHUCache.saveLoginType(it)
                    AHURepository.dataSource = ReptileDataSource(ReptileUser(username, password))
                    // 触发登录逻辑
                    activityState.isLogin.value = true
                }
            }

        }
    }
}