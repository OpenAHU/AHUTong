package com.ahu.ahutong.ui.page

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import coil.load
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.databinding.FragmentDiscoveryBinding
import com.ahu.ahutong.ui.adapter.BannerAdapter
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:42
 * @Email 468766131@qq.com
 */
class DiscoveryFragment private constructor(): BaseFragment<FragmentDiscoveryBinding>() {
    private lateinit var mState: DiscoveryViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(DiscoveryViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
       return DataBindingConfig(R.layout.fragment_discovery, BR.state, mState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var bann:Banner= Banner()
        bann.imgPath="http://39.106.7.220/img/img.php"
        var list: Array<Banner> = arrayOf(bann,bann,bann)
        val bannerAdapter: BannerAdapter = object : BannerAdapter(list) {
            override fun onLoadImg(view: ImageView, imgPath: String) {
                view.load(imgPath)
            }
        }
        dataBinding.discoveryBanner.adapter=bannerAdapter;
        dataBinding.discoveryBanner.setBackgroundColor(Color.BLACK)

       // bannerAdapter.createBannerAdapter(list)
    }
    companion object{
        val INSTANCE = DiscoveryFragment()
    }
}