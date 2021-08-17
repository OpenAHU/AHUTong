package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar


/**
 * @Author: SinkDev
 * @Date: 2021/8/15-下午4:13
 * @Email: 468766131@qq.com
 */
class SettingFragment : PreferenceFragmentCompat() {
    val activityState: MainViewModel by lazy {
        ViewModelProvider(requireActivity())
            .get(MainViewModel::class.java)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefernce_setting)
        findPreference<Preference>("change_theme")?.apply {
            setOnPreferenceClickListener {
                // 跳转到主题更换界面
                return@setOnPreferenceClickListener true
            }
        }
        findPreference<Preference>("diy_theme")?.apply {
            setOnPreferenceClickListener {
                // 跳转到自定义主题
                return@setOnPreferenceClickListener true
            }
        }
        findPreference<ListPreference>("data_source")?.apply {
            entries = arrayOf("智慧安大（本地爬虫版）", "智慧安大（后端版）", "教务系统")
            entryValues = arrayOf(
                User.UserType.AHU_LOCAL.type,
                User.UserType.AHU_Wisdom.type, User.UserType.AHU_Teach.type
            )
            key = "data_source"
            dialogTitle = "请选择数据源"
            //dialogMessage = "智慧安大到教务系统需重新登录, 反之亦然。"
            negativeButtonText = "关闭"
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue !is String) {
                    return@setOnPreferenceChangeListener false
                }
                //没进行登录
                if (activityState.isLogin.value != true) {
                    val bundle = Bundle().apply {
                        putString("type", newValue.toString())
                    }
                    findNavController().navigate(R.id.login_fragment, bundle)
                    Toast.makeText(requireContext(), "请先进行登录!", Toast.LENGTH_SHORT).show()
                    return@setOnPreferenceChangeListener false
                }
                val oldValue = AHUCache.getLoginType().type
                if (oldValue == newValue) {
                    return@setOnPreferenceChangeListener false
                }
                if (oldValue == User.UserType.AHU_Wisdom.type &&
                    newValue == User.UserType.AHU_LOCAL.type
                ) {
                    AHUCache.saveLoginType(User.UserType.AHU_LOCAL)
                } else if (oldValue == User.UserType.AHU_LOCAL.type &&
                    newValue == User.UserType.AHU_Wisdom.type
                ) {
                    AHUCache.saveLoginType(User.UserType.AHU_Wisdom)
                } else {
                    //退出登录
                    activityState.logout()
                    //进入登录界面
                    val bundle = Bundle().apply {
                        putString("type", newValue.toString())
                    }
                    findNavController().navigate(R.id.login_fragment, bundle)
                }
                return@setOnPreferenceChangeListener false
            }
        }
        findPreference<SwitchPreference>("show_all")?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                if (newValue !is Boolean) {
                    return@setOnPreferenceChangeListener false
                }
                AHUCache.saveIsShowAllCourse(newValue)
                return@setOnPreferenceChangeListener true
            }
        }
        findPreference<Preference>("license")?.apply {
            setOnPreferenceClickListener {
                //跳转到开源协议
                return@setOnPreferenceClickListener true
            }
        }
        findPreference<Preference>("clear")?.apply {
            setOnPreferenceClickListener {
                //清除所有数据
                activityState.logout()
                AHUCache.clearAll()
                return@setOnPreferenceClickListener true
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        return LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(-1, -1)
            orientation = LinearLayout.VERTICAL
            addView(AppBarLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT)
                fitsSystemWindows = true
                addView(MaterialToolbar(requireContext()).apply {
                    title = "设置"
                    layoutParams =
                        LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT)
                    navigationIcon =
                        AppCompatResources.getDrawable(getContext(), R.drawable.icon_back)
                    setNavigationOnClickListener {
                        findNavController().popBackStack()
                    }
                })
            })
            addView(view)
        }
    }
}