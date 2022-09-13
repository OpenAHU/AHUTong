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
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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
                findNavController().navigate(R.id.change_theme_fragment)
                return@setOnPreferenceClickListener true
            }
        }
        findPreference<Preference>("diy_theme")?.apply {
            setOnPreferenceClickListener {
                // 跳转到自定义主题
                Toast.makeText(requireContext(), "功能正在开发,敬请期待.", Toast.LENGTH_SHORT).show()
                return@setOnPreferenceClickListener true
            }
        }
        findPreference<SwitchPreference>("show_all")?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                if (newValue !is Boolean) {
                    return@setOnPreferenceChangeListener false
                }
                AHUCache.saveIsShowAllCourse(newValue)
                Toast.makeText(requireContext(), "设置成功！", Toast.LENGTH_SHORT).show()
                return@setOnPreferenceChangeListener true
            }
        }
        findPreference<Preference>("license")?.apply {
            setOnPreferenceClickListener {
                //跳转到开源协议
                findNavController().navigate(R.id.license_fragment)
                return@setOnPreferenceClickListener true
            }
        }

        findPreference<Preference>("clear")?.apply {
            setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireActivity()).apply {
                    setTitle("提示")
                    setMessage("您是否确定要清除应用数据，其中包含您的登录状态、课表等, 且删除后无法恢复。")
                    setPositiveButton("确定") { _, _ ->
                        //清除所有数据
                        activityState.logout()
                        AHUCache.clearAll()
                        Toast.makeText(requireContext(), "已清除所有数据", Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("取消", null)
                }.show()
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