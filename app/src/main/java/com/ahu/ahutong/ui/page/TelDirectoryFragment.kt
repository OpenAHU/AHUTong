package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentBathroomBinding
import com.ahu.ahutong.databinding.FragmentTeldirectoryBinding
import com.ahu.ahutong.ui.page.state.BathRoomViewModel
import com.ahu.ahutong.ui.page.state.TelDirectoryViewModel

class TelDirectoryFragment : BaseFragment<FragmentTeldirectoryBinding>() {
    private lateinit var mState: TelDirectoryViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(TelDirectoryViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_teldirectory, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}