package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentMineBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.MineViewModel


/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
class MineFragment : BaseFragment<FragmentMineBinding>() {

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
        dataBinding.refreshLayout.setOnRefreshListener  { dataBinding.refreshLayout.isRefreshing = false; }
    }


    inner class ClickProxy {
        //about us
        fun aboutUs(view: View) {
            nav().navigate(R.id.about_fragment)
        }

        fun feedback(view: View) {
            try {
                val intent = Intent()
                intent.data =
                    Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DL3WKrBqXGuSoqrpbm4zVqHWN-WyB-Y29")
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "请安装QQ后重试！", Toast.LENGTH_SHORT).show()
            }
        }

        fun recommend() {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "我正在使用安大通App，快来一起用吧，https://www.coolapk.com/apk/com.ahu.ahutong"
            )
            intent.type = "text/plain"
            startActivity(intent)
        }

        //login or logout
        fun login(view: View) {
            if (activityState.isLogin.value == true) {
                buildDialog("提示",
                    "是否退出登录，点击确定您的登录状态将被删除！",
                    "确定", { _, _ ->
                        activityState.logout()
                    },
                "取消"
                ).show()

            } else {
                nav().navigate(R.id.action_home_fragment_to_login_fragment)
            }
        }

        //jump to developer fragment
        fun developer(view: View) {
            nav().navigate(R.id.action_home_fragment_to_developer_fragment)
        }

        //jump to setting fragment
        fun setting(view: View) {
            nav().navigate(R.id.setting_fragment)
        }


    }

    companion object {
        val INSTANCE = MineFragment()

    }
}