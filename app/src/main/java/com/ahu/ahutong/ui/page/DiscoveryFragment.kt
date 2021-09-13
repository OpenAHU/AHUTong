package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.databinding.FragmentDiscoveryBinding
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel


/**
 * @Author SinkDev
 * @Date 2021/7/27-19:42
 * @Email 468766131@qq.com
 */
class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>() {

    private lateinit var mState: DiscoveryViewModel
    private lateinit var activityState: MainViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(DiscoveryViewModel::class.java)
        activityState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_discovery, BR.state, mState)
    }


    override fun observeData() {
        mState.bannerData.observe(this, {
            val adapter = dataBinding.discoveryRec.adapter as DiscoveryAdapter
            adapter.setBanners(it)
        })
        mState.activityBean.observe(this) { result ->
            val adapter = dataBinding.discoveryRec.adapter as DiscoveryAdapter
            adapter.setActivityBean(result)
            dataBinding.refreshLayout.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.discoveryRec.layoutManager = LinearLayoutManager(context)
        val bean = DiscoveryAdapter.DiscoveryBean(
            mutableListOf(),
            mState.tools,
            mutableListOf(),
            DiscoveryAdapter.ActivityBean("0.00", "男生", "女生")
        )
        dataBinding.discoveryRec.adapter = DiscoveryAdapter(bean)
        mState.loadActivityBean()
        val adapter = dataBinding.discoveryRec.adapter as DiscoveryAdapter
        adapter.setCourses(mState.loadCourse())
        dataBinding.refreshLayout.isRefreshing = true
        dataBinding.refreshLayout.setOnRefreshListener {
            mState.loadActivityBean()
        }

    }

    inner class ToolClickProxy {
        fun onClick(view: View, tool: Tool) {
            if (activityState.isLogin.value == false) {
                Toast.makeText(requireContext(), "登录后才能使用小工具。", Toast.LENGTH_SHORT).show()
                return
            }
            nav().navigate(tool.action)
        }
    }

    inner class CourseClickProxy {
        fun onClick(view: View, course: Course) {
            val bundle = Bundle().apply {
                putBoolean("add", false)
                putSerializable("course", course)
            }
            nav().navigate(R.id.course_fragment, bundle)
        }
    }


    companion object {
        val INSTANCE = DiscoveryFragment()
    }

}