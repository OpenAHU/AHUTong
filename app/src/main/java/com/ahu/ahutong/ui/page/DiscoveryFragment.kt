package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.News
import com.ahu.ahutong.data.model.Sector
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.databinding.FragmentDiscoveryBinding
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel


/**
 * @Author SinkDev
 * @Date 2021/7/27-19:42
 * @Email 468766131@qq.com
 */
class DiscoveryFragment private constructor() : BaseFragment<FragmentDiscoveryBinding>() {

    private lateinit var mState: DiscoveryViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(DiscoveryViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_discovery, BR.state, mState)
    }

    override fun observeData() {
        mState.bannerData.observe(this, {
            val adapter= dataBinding.discoveryRec.adapter as DiscoveryAdapter
            adapter.setBanners(it)
        })
        mState.newData.observe(this, Observer<MutableList<News>> {
            val adapter= dataBinding.discoveryRec.adapter as DiscoveryAdapter
            adapter.setNews(it)
        })
        val a =News()
        a.releaseTime="2021.8.5"
        a.author="Simon"
        a.abstractX="文章内容，文章内容"
        a.title="文章标题"
        a.department="department"
        a.detailUrl="http://39.106.7.220/img/img.php"
        mState.newData.value= arrayListOf(a)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.discoveryRec.layoutManager = LinearLayoutManager(context)
        val bean = DiscoveryAdapter.DiscoveryBean(mutableListOf(),mState.tools,mState.sectors,mutableListOf(
            News(), News(), News()
        ))
        dataBinding.discoveryRec.adapter = DiscoveryAdapter(bean)
    }

   inner class ToolClickProxy {
        fun onClick(view: View, tool: Tool) {
            nav().navigate(tool.action)
        }
    }
    class SectorClickProxy{
        fun onClick(view: View,sector: Sector){
            //判断类型，然后网络请求，再通过livedata进行post操作
        }
    }
    class NewsClickProxy{
        fun onClick(view: View,news: News){

        }
    }

    companion object {
        val INSTANCE = DiscoveryFragment()
    }

}