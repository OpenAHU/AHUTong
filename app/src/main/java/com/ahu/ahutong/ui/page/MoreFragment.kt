package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentBathroomBinding
import com.ahu.ahutong.databinding.FragmentMoreBinding
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import com.ahu.ahutong.ui.adapter.MoreAdapter
import com.ahu.ahutong.ui.adapter.MoreBean
import com.ahu.ahutong.ui.page.state.BathRoomViewModel
import com.ahu.ahutong.ui.page.state.MoreViewModel

class MoreFragment  : BaseFragment<FragmentMoreBinding>() {
    private lateinit var mState: MoreViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(MoreViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_more, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataBinding.recyclerMore.layoutManager = LinearLayoutManager(context)
        val bean = MoreBean(
             mState.study, mState.life
        )
        dataBinding.recyclerMore.adapter = MoreAdapter(bean)
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}