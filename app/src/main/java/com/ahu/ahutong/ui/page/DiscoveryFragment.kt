package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.databinding.FragmentDiscoveryBinding
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel
import java.util.*


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.discoveryRec.layoutManager=LinearLayoutManager(context)
        dataBinding.discoveryRec.adapter= DiscoveryAdapter()
        mState.getBannerData()?.observe(viewLifecycleOwner, { t ->
                val adapter:DiscoveryAdapter= dataBinding.discoveryRec.adapter as DiscoveryAdapter;
                adapter.setBanners(t)
            })

        val banners = ArrayList<Banner>()
        banners.add(Banner())
        banners.add(Banner())
        mState.getBannerData()!!.value = banners
        val ad= dataBinding.discoveryRec.adapter as DiscoveryAdapter
        ad.addGridItem(mState.gridItems)
    }

    companion object {
        val INSTANCE = DiscoveryFragment()
    }
}