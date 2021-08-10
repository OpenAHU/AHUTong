package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentHomeBinding
import com.ahu.ahutong.ui.page.state.HomeViewModel
import com.sink.library.log.SinkLog
import java.lang.RuntimeException

/**
 * @Author SinkDev
 * @Date 2021/7/27-18:59
 * @Email 468766131@qq.com
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var mState: HomeViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(HomeViewModel::class.java)

    }

    override fun getDataBindingConfig(): DataBindingConfig {
       return DataBindingConfig(R.layout.fragment_home, BR.state, mState)
           .addBindingParam(BR.proxy, ActionProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //pager2
        dataBinding.homeViewPager2.apply {
            isUserInputEnabled = false //禁止滑动
            adapter = object: FragmentStateAdapter(requireActivity()) {

                override fun getItemCount(): Int {
                    return 3
                }
                override fun createFragment(position: Int): Fragment {
                    return when(position) {
                        0 -> ScheduleFragment.INSTANCE
                        1 -> DiscoveryFragment.INSTANCE
                        2 -> MineFragment.INSTANCE
                        else -> throw RuntimeException("不存在该position")
                    }
                }

            }


        }
    }


    inner class ActionProxy{
        var selectAction: (MenuItem)->Boolean = fun(item: MenuItem): Boolean {
            val pos = when(item.itemId) {
                R.id.schedule_fragment -> 0
                R.id.discovery_fragment -> 1
                R.id.mine_fragment -> 2
                else -> -1
            }
            if (pos == -1){
                SinkLog.e("选中的ItemId不存在！")
                return false
            }
            if (dataBinding.homeViewPager2.currentItem == pos){
                SinkLog.i("已在当前界面！")
                return false
            }
            dataBinding.homeViewPager2.setCurrentItem(pos, false)
            return true
        }
    }
}