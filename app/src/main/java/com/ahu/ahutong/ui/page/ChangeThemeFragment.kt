package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.databinding.FragmentChangeThemeBinding
import com.ahu.ahutong.databinding.ItemThemeSimpleBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.ChangeThemeViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleTheme
import com.ahu.ahutong.ui.widget.schedule.bean.SimpleTheme

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午9:56
 * @Email: 468766131@qq.com
 */
class ChangeThemeFragment : BaseFragment<FragmentChangeThemeBinding>() {
    private lateinit var mState: ChangeThemeViewModel
    private lateinit var gState: MainViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ChangeThemeViewModel::class.java)
        gState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_change_theme, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerTheme.layoutManager = LinearLayoutManager(requireContext())
        dataBinding.recyclerTheme.adapter =
            object : BaseAdapter<ScheduleTheme, ItemThemeSimpleBinding>(DefaultDataUtils.simpleThemes) {
                override fun layout(): Int {
                    return R.layout.item_theme_simple
                }

                override fun bindingData(binding: ItemThemeSimpleBinding, data: ScheduleTheme) {
                    binding.name = data.name
                    binding.theme = data.theme as SimpleTheme
                    binding.root.setOnClickListener {
                        AHUCache.saveScheduleTheme(data)
                        Toast.makeText(requireContext(), "主题切换成功.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }
}
